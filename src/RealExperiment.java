import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe para realização do experimento com dados reais
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class RealExperiment implements ControlExperiment, ControlSTACS {
    private int time_ini_experiment; // instante de referẽncia do início do experimento
    private IntList list_id_work_days; // lista com os códigos dos dias de trabalho da base de dados
    private WorkDay current_work_day; // dia de trabalho atual
    private long seed_random; // semente dos randomicos do experimento
    private BufferedWriter f_log_exper; // salvamento em arquivo
    private BufferedWriter f_simul_res; // arquivo com o resumo das simulações
    private BufferedWriter f_time_execs;

    public RealExperiment() throws IOException {
        // criando apenas estruturas, com tamanhos mínimos
        this.list_id_work_days = new IntList(1);
        this.current_work_day  = new WorkDay(-1);

        // abrindo arquivos de resultados
        this.f_log_exper = new BufferedWriter(new FileWriter("outs/log_real_experiment.txt"));
        //TODO Configuração precisão do float
        //f_log_exper << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

        // arquivo com os custos gerados para cada dia de trabalho em cada simulação:
        this.f_simul_res = new BufferedWriter(new FileWriter("outs/simulations_resume.txt"));

        //TODO Configuração precisão do float
        //f_simul_res << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

        this.f_time_execs = new BufferedWriter(new FileWriter("outs/time_execs.txt"));
        //TODO Configuração precisão do float
        //f_time_execs << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);
    }

    public void run_real_experiment() throws IOException {
        this.time_ini_experiment = (int) System.currentTimeMillis();  // instante do início do experimento
        RealData real_data = new RealData();
        int n_work_days = real_data.count_work_days();  // contando número de dias de trabalho na base de dados
        real_data.load_ids_work_days(n_work_days);  // criado a lista com as ids dos dias de trabalho
        //delete real_data;

        for (int current_day = 0; current_day < n_work_days; current_day++) {

            if (INDEX_DAY_TEST != -1){
                current_day = INDEX_DAY_TEST;
            }

            this.f_log_exper.write("\r\n------------------------------\r\nDia de Trabalho: " + this.list_id_work_days.value(current_day) + "\r\n");

            for (int counter_day_simulations = 1; counter_day_simulations <= N_SIMULATIONS_BY_DAY; counter_day_simulations++) {

                double time_ini_execution_day = (double) System.currentTimeMillis();  // instante do início do experimento

                this.f_log_exper.write("\r\n----------\r\nSimulacao: " + counter_day_simulations + "\r\n");

                int current_id_work_day = this.list_id_work_days.value(current_day);
                //delete current_work_day;
                this.current_work_day = new WorkDay(current_id_work_day);

                this.current_work_day.load_data_work_day(counter_day_simulations);

                // a semente é alterada a cada execução de um dia de trabalho
                //seed_random = (long)time(NULL);  // gerando semente aleatória
                this.seed_random = System.currentTimeMillis();
                this.f_log_exper.write( "Semente rândomica utilizada: " + this.seed_random + "\r\n");
                this.save_aco_parameters();

                if (STATIC_SIMULATION == 1) {
                    this.current_work_day.execute_static_simulation(this.seed_random, counter_day_simulations, this.f_log_exper, this.f_simul_res);
                } else {
                    this.current_work_day.execute_simulation(this.seed_random, counter_day_simulations, this.f_log_exper, this.f_simul_res);
                }

                // gravando o tempo total da simulação:
                double time_simulation = System.currentTimeMillis() - time_ini_execution_day;
                this.f_log_exper.write("\r\nTempo total da simulacao: " + (int)time_simulation + " milissegundos\r\n");
                this.f_simul_res.write((int)time_simulation + "\r\n");

                if (counter_day_simulations == 1) {
                    this.f_time_execs.write("\r\n" + current_id_work_day + "\t");
                }
                this.f_time_execs.write((int)time_simulation + "\t");
            }

            if (INDEX_DAY_TEST != -1) {
                current_day = n_work_days;
            }
        }

        double time_experiment = System.currentTimeMillis() - time_ini_experiment;
        this.f_log_exper.write("\r\n------------------------------\r\nTempo total do experimento: " + (int)time_experiment + " milissegundos\r\n");

    }

    public void save_aco_parameters() throws IOException {
        this.f_log_exper.write("Parametros ACO:\r\n");
        this.f_log_exper.write( "   Objetivo MTSP:\t");

        if (APP_OBJECTIVE == 1)
            this.f_log_exper.write("minimizar maior rota (workload balance)\r\n");
        if (APP_OBJECTIVE == 2)
            this.f_log_exper.write("minimizar custo total\r\n");
        this.f_log_exper.write("   Criterio de parada:\t");
        if (NO_IMP_CYCLES > 0)
            this.f_log_exper.write(NO_IMP_CYCLES + " ciclos sem melhora da solucao\r\n");
        if (MAX_TIME_EXEC > 0)
            this.f_log_exper.write(MAX_TIME_EXEC + " segundos por execucao\r\n");
        if (MAX_CYCLES > 0)
            this.f_log_exper.write(MAX_CYCLES + " ciclos por execucao\r\n");
        this.f_log_exper.write("   Tamanho da lista de candidatos:\t" + CL_LENGTH + "\r\n");

        //TODO Configuração precisão do float
        //f_log_exper << setiosflags (ios::fixed) << setprecision(2);
        this.f_log_exper.write("   N (solucoes geradas a cada ciclo):\t" + N + "\r\n");
        this.f_log_exper.write("   q0 (nível de determinismo):\t\t" + Q0 + "\r\n");
        this.f_log_exper.write("   alfa (peso do feromonio):\t\t" + ALFA + "\r\n");
        this.f_log_exper.write("   beta (peso da visibilidade):\t\t" + BETA + "\r\n");
        this.f_log_exper.write("   ksi (persitencia do feromonio nas atualizacoes locais):\t" + KSI + "\r\n");
        this.f_log_exper.write("   ro (persitencia do feromonio nas atualizacoes globais):\t" + RO + "\r\n");

        this.f_log_exper.write("\r\nBusca Local:\r\n");
        this.f_log_exper.write("   2-opt (todas as solucoes criadas):\t\t");

        if (LS2O == 1)
            this.f_log_exper.write( "ON\r\n");
        else
            this.f_log_exper.write("OFF\r\n");
        this.f_log_exper.write("   3-opt (somente nas melhores soluções dos ciclos):\t\t");

        if (LS3O == 1)
            this.f_log_exper.write("ON\r\n");
        else
            this.f_log_exper.write("OFF\r\n");
        //TODO Configuração precisão do float
        //f_log_exper << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

    }
}
