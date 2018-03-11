import java.io.IOException;

/**
 * @author renanalencar***
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class StandardExperiment implements ControlExperiment, ControlSTACS {
    private int depot;
    private long seed_random;           // semente dos randomicos do experimento
    private LogExperiment log;

    //TODO Verificar carregamento de arquivo
    public StandardExperiment() throws IOException {
    	log = LogExperiment.getInstance();
        depot = DEPOT_INDEX;
       // this.log.f_log_stand_exper = new BufferedWriter(new FileWriter("outs/log_stand_experiment.txt"));
        //TODO configuração a precisão do float
        //this.log.f_log_stand_exper << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);
//        this.log.f_sols_aco = new BufferedWriter(new FileWriter("outs/plot_best_sols_aco.txt"));
        //TODO configuração a precisão do float
        //this.log.f_sols_aco << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);
    }

    //TODO Verificar carregamento de arquivo
    /**
     * Método que carrega a instância de ControlACO.hpp
     */
    public void run_standard_experiment() throws IOException {
        // cada loop é um experimento independente
        for (int exper_counter = 0; exper_counter < 1; exper_counter++) {
        	System.out.println(++log.i + " aquii");
            //double time_ini_experiment = (double)clock();  // instante do início do experimento
            long time_ini_experiment = System.currentTimeMillis();

            // carregando instância configurada em ControlACO.hpp:
            MtspInstance model_instance = new MtspInstance(N_SALESMEN, depot);
            model_instance.load_euclidean_model_instance();

            // salvando e imprimindo:
            this.save_header_exper();      // salvando os parâmetros do experimento
            if (PISO == 1) {
                System.out.print("\r\n-----------------------------------------------------\r\n");
                System.out.print("Experiment " + (exper_counter + 1) + "\r\n");
                model_instance.print_instance();
            }

            //TODO Configurar impressão de pontos flutuantes
            // formato de impressão de pontos flutuantes:
            //cout << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

            // melhores resultados do experimento:
            double best_longest_exper = 0.0;  // minimizar rota mais longa
            double best_totsol_exper  = 0.0;   // minimizar soma das rotas

            // dados da melhor solução de cada execução:
            double longest_best_sols[] = new double[N_EXECUTIONS];
            double totsol_best_sols[]  = new double[N_EXECUTIONS];
            double cycle_best_sols[]  = new double[N_EXECUTIONS];
            double time_best_sols[]  = new double[N_EXECUTIONS];

            // cada loop é uma execuçao independente:
            for (int exec_counter = 0; exec_counter < N_EXECUTIONS; exec_counter++){  // execuções do algoritmo
                if(PISO == 1) {
                    System.out.print("\r\nExecution " + (exec_counter + 1) + "\r\n");
                }

                //double time_ini_execution = (double)clock();  // instante do início da execução
                long time_ini_execution = System.currentTimeMillis();
                int n = model_instance.get_n_nodes();         // definindo numero de nós da instância
                int m = model_instance.get_n_salesmen();      // definindo número de caixeiros da instância

                // A semente é alterada a cada execução para experimentos padrão:
                //seed_random = (long)time(NULL);  // gerando semente aleatória
                seed_random = System.currentTimeMillis();

                AcoMtspAlgorithm aco_mtsp_app = new AcoMtspAlgorithm(seed_random, model_instance);  // carregando aplicação
                MtspSolution aco_solution = aco_mtsp_app.execute();  // executando a aplicação. A melhor solução é armazenada em best_sol_exe
                //delete aco_mtsp_app;

                // salvando em best_sols_aco_to_plot.txt as melhores soluções de cada execução, de acordo com o objetivo
                Node[] plan_nodes_vector = model_instance.get_plan_nodes_vector();
                aco_solution.save_to_plot(this.log.f_sols_aco, plan_nodes_vector);

                // salvando títulos dos campos da tabela:
                if (exec_counter == 0){
                    this.log.f_log_stand_exper.write("\r\nBest solution of each execution:");
                    this.log.f_log_stand_exper.write("\r\nexec\tlongest\t\ttsolut\t\tcycle\t\ttime(ms)\tseed execution\r\n");
                }

                // copiando custos da melhor solução da execução:
                longest_best_sols[exec_counter] = aco_solution.get_longest_route();
                totsol_best_sols[exec_counter]  = aco_solution.get_total_cost();
                cycle_best_sols[exec_counter]   = aco_solution.get_iteration_sol();
                time_best_sols[exec_counter]    = aco_solution.get_time_sol();

                // salvando as melhores soluções de cada execução:
                double longest = aco_solution.get_longest_route();
                double tsolut  = aco_solution.get_total_cost();
                int cycle      = aco_solution.get_iteration_sol();
                int time       = aco_solution.get_time_sol();

                //TODO checar se este casting é possível
                int seed_r     =  Math.toIntExact(aco_solution.get_seed_rand());

                // atualizando melhores resultados do experimento:
                if ((exec_counter == 0) || (longest < best_longest_exper)) {
                    best_longest_exper = longest;
                }
                if ((exec_counter == 0) || (tsolut < best_totsol_exper)){
                    best_totsol_exper = tsolut;
                }
                this.log.f_log_stand_exper.write((exec_counter+1) + "\t" + longest + "\t\t" + tsolut + "\t\t" + cycle + "\t\t" + time + "\t\t" + seed_r + "\r\n");

                //delete aco_solution;

                double time_execution = (System.currentTimeMillis() - time_ini_execution);
                if(PISO == 1){
                    System.out.print("Total time execution: " + time_execution + " miliseconds\r\n");
                }
            }

            //Utilities u;
            double avglongests = Utilities.average(longest_best_sols, N_EXECUTIONS);
            double avgtsoluts = Utilities.average(totsol_best_sols, N_EXECUTIONS);
            double avgcycles = Utilities.average(cycle_best_sols, N_EXECUTIONS);
            double avgttimes = Utilities.average(time_best_sols, N_EXECUTIONS);
            double sdlongests = Utilities.std_dev(longest_best_sols, N_EXECUTIONS);
            double sdtsoluts = Utilities.std_dev(totsol_best_sols, N_EXECUTIONS);
            double sdcycles = Utilities.std_dev(cycle_best_sols, N_EXECUTIONS);
            double sdttimes = Utilities.std_dev(time_best_sols, N_EXECUTIONS);

            //TODO Configuração precisão do ponto flutuante
            //this.log.f_log_stand_exper << setprecision(2);
            this.log.f_log_stand_exper.write("\r\nAvgs\t" + avglongests + "\t\t" + avgtsoluts + "\t\t" + avgcycles + "\t\t" + avgttimes + "\r\n");
            this.log.f_log_stand_exper.write("SDs\t" + sdlongests + "\t\t" + sdtsoluts + "\t\t" + sdcycles + "\t\t" + sdttimes + "\r\n");


            this.log.f_log_stand_exper.write("\r\nBest cost:\t");
            if (APP_OBJECTIVE == 1) {  // minimizar a longest_route
                this.log.f_log_stand_exper.write(String.valueOf(best_longest_exper));
            } else {
                this.log.f_log_stand_exper.write(String.valueOf(best_totsol_exper));
            }
            this.log.f_log_stand_exper.write("\r\n");

            double time_experiment = ((System.currentTimeMillis() - time_ini_experiment));
            if(PISO == 1) {
                System.out.print("\r\nTotal time experiment: " + (int)time_experiment + " miliseconds\r\n");
            }
            this.log.f_log_stand_exper.write("\r\nTotal time experiment: " + (int)time_experiment + " miliseconds\r\n");

            //delete [] time_best_sols;
            //delete [] cycle_best_sols;
            //delete [] totsol_best_sols;
            //delete [] longest_best_sols;

            //delete model_instance;
        }
    }


    //TODO Verificar a gravação do arquivo
    /**
     * Método que salva o cabeçalho do experimento
     * @throws IOException
     */
    public void save_header_exper() throws IOException {
    	
        this.log.f_log_stand_exper.write("\r\n-----------------------------------------------------\r\n");

        this.log.f_log_stand_exper.write("Experiment objective:\t\t");
        if (APP_OBJECTIVE == 1)
            this.log.f_log_stand_exper.write("minimize longest route (workload balance)\r\n");
        if (APP_OBJECTIVE == 2)
            this.log.f_log_stand_exper.write("minimize sum routes (basic MTSP)\r\n");

        this.log.f_log_stand_exper.write("Type solutions:\t\t\t");
        if (TYPE_MTSP_SOLS == 1)
            this.log.f_log_stand_exper.write("basic MTSP: single depot and closed routes\r\n");
        if (TYPE_MTSP_SOLS == 2)
            this.log.f_log_stand_exper.write("MTSP with multiple starting depots and single end depot (open routes)\r\n");

        this.log.f_log_stand_exper.write("Model instance:\t\t\t");
        switch (MODEL_INSTANCE) {
            case 0:
                this.log.f_log_stand_exper.write("Test grids\r\n");
                break;
            case 1:
                this.log.f_log_stand_exper.write("TSPLIB Eil51 (51 nodes)\r\n");
                break;
            case 2:
                this.log.f_log_stand_exper.write("TSPLIB Eil76 (76 nodes)\r\n");
                break;
            case 3:
                this.log.f_log_stand_exper.write("TSPLIB Eil101 (101 nodes)\r\n");
                break;
            case 4:
                this.log.f_log_stand_exper.write("TSPLIB Pr76 (76 nodes)\r\n");
                break;
            case 5:
                this.log.f_log_stand_exper.write("TSPLIB Pr1002 (1002 nodes)\r\n");
                break;
            case 6:
                this.log.f_log_stand_exper.write("sgb128 (128 nodes)\r\n");
                break;
        }

        this.log.f_log_stand_exper.write("Salesmen number:\t\t" + N_SALESMEN + "\r\n");

        this.log.f_log_stand_exper.write("Depot node:\t\t\t" + depot + "\r\n");

        this.log.f_log_stand_exper.write("Independent executions:\t\t" + N_EXECUTIONS + "\r\n");

        this.log.f_log_stand_exper.write("Stopping criterion:\t\t");
        if (NO_IMP_CYCLES > 0)
            this.log.f_log_stand_exper.write(NO_IMP_CYCLES + " cycles without improved solutions\r\n");
        if (MAX_TIME_EXEC > 0)
            this.log.f_log_stand_exper.write(MAX_TIME_EXEC + " seconds per executin\r\n");
        if (MAX_CYCLES > 0)
            this.log.f_log_stand_exper.write(MAX_CYCLES + " cycles per execution\r\n");

        this.log.f_log_stand_exper.write("\r\nAnt Colony System parameters:\r\n");

        this.log.f_log_stand_exper.write("   N (iterations per cycle):\t" + String.format("%.2f", N) + "\r\n");
        this.log.f_log_stand_exper.write("   q0 (determinism level):\t" + String.format("%.2f", Q0) + "\r\n");
        this.log.f_log_stand_exper.write( "   alfa (pheromone weight):\t" + String.format("%.2f", ALFA) + "\r\n");
        this.log.f_log_stand_exper.write("   beta (visibility weight):\t" + String.format("%.2f", BETA) + "\r\n");
        this.log.f_log_stand_exper.write("   ksi (pheromone persit LPU):\t" + String.format("%.2f", KSI) + "\r\n");
        this.log.f_log_stand_exper.write("   ro (pheromone persit GPU):\t" + String.format("%.2f", RO) + "\r\n");


        this.log.f_log_stand_exper.write("\r\nExperiment parameters:\r\n");

        this.log.f_log_stand_exper.write("   Candidate list length:\t" + CL_LENGTH + "\r\n");

        this.log.f_log_stand_exper.write("   Local Pheromone Update:\t");
        if (LPU == 1)
            this.log.f_log_stand_exper.write("ON\r\n");
        else
            this.log.f_log_stand_exper.write("OFF\r\n");

        this.log.f_log_stand_exper.write("   Global Pheromone Update:\t");
        if (GPU == 1) {
            this.log.f_log_stand_exper.write("ON");
            if (GPUBSF == 1)
                this.log.f_log_stand_exper.write("\t--> best-so-far solution updating");
            else
                this.log.f_log_stand_exper.write("\t--> best solution each cycle updating");
            if (GPUNODEP != 0)
                this.log.f_log_stand_exper.write("\t--> not depositing or evaporanting on edges connected to depot");
            this.log.f_log_stand_exper.write("\r\n");
        } else
            this.log.f_log_stand_exper.write("OFF\r\n");

        this.log.f_log_stand_exper.write("   Type cost matrix: \t\t");
        if (TYPE_EUCLID_MATRIX == 0)
            this.log.f_log_stand_exper.write("double\r\n");
        if (TYPE_EUCLID_MATRIX == 1)
            this.log.f_log_stand_exper.write("integer\r\n");

        this.log.f_log_stand_exper.write("   Start node ants:\t\t");
        if (TYPE_MTSP_SOLS == 2)
            this.log.f_log_stand_exper.write("accordding positions of instance (open routes)\r\n");
        if (TYPE_MTSP_SOLS == 1){
            if (ANTS_INIT_NODES == 1)
                this.log.f_log_stand_exper.write("all starting from single depot\r\n");
            if (ANTS_INIT_NODES == 2)
                this.log.f_log_stand_exper.write("random nodes in all solutions\r\n");
            if (ANTS_INIT_NODES == 3)
                this.log.f_log_stand_exper.write("random nodes each " + CHG_INIT_NODES + " cycles without improvement\r\n");
        }

        this.log.f_log_stand_exper.write("   Check best ant repetitions:\t" + CBA_REPET);
        if (CBA_REPET == 0){  // check better ant repetitions
            this.log.f_log_stand_exper.write("\t--> no checking");
        }
        this.log.f_log_stand_exper.write("\r\n");

        this.log.f_log_stand_exper.write("\r\nLocal Search:\r\n");
        this.log.f_log_stand_exper.write("   2-opt (all created solutions):\t\t");
        if (LS2O == 1)
            this.log.f_log_stand_exper.write("ON\r\n");
        else
            this.log.f_log_stand_exper.write("OFF\r\n");
        this.log.f_log_stand_exper.write("   3-opt (only best cycle solutions):\t\t");
        if (LS3O == 1)
            this.log.f_log_stand_exper.write("ON\r\n");
        else
            this.log.f_log_stand_exper.write("OFF\r\n");

    }
}
