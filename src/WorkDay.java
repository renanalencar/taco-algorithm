import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Classe que representa um dia de trabalho das equipes
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class WorkDay implements ControlExperiment {
    private BufferedWriter f_real_sols;     // soluções reais dos dias para plotagem
    private BufferedWriter f_longests;
    private BufferedWriter f_total_costs;
    
    private LogExperiment logExperiment;

    private long seed_simulation;     // semente randomica da simulção

    private double time_ini_simulation;    // momento de início do experimento deste dia de trabalho
    private int id_work_day;               // código do dia de trabalho
    private int depot;                     // garagem da agência de atendimento
    private int n_points;                  // total de pontos do grafo (serviços + depósito)
    private int n_teams;                   // numero de equipes disponíveis no dia
    private RealData real_data;           // classe para acesso aos arquivos de dados reais
    private RealMatrix real_matrix;       // acesso às matrizes de custos reais
    private ServiceOrder[] day_services;    // todos os serviços do dia (não inclui o depósito)
    private MtspInstance current_instance;     // Instância que será montada e passada como parâmetro para o algoritmo proposto
    private DoubleMatrix real_distance_matrix; // distâncias reais entre os pontos, em kms, a partir de MapQuest
    private DoubleMatrix real_time_matrix;     // tempos reais de deslocamento, em segundos, a partir de MapQuest

    private MtspSolution real_solution;           // soluçao com as rotas fechadas reais executadas pelas equipes
    private MtspSolution complete_final_solution; // a solução para o dia tudo obtida pela simulação
    private MtspSolution current_aco_solution;    // as soluções geradas pelo algoritmo proposto

    private double current_time;             // tempo atual do dia de trabalho
    private int next_emergency;              // índice da proóxima emergência
    private double time_dispatch_next_emerg; // horário de surgimento da próxima emrgência
    private int next_team_free;              // próxima equipe a ficar livre
    private double time_free_next_team;      // instante em que a equipe vai conluir o serviço atual
    private Route[] cur_executed_routes;      // as rotas das equipes em construção

    private DecimalFormat df;

    public WorkDay(int current_id_work_day) throws IOException {
        this.id_work_day          = current_id_work_day;
        this.real_data            = new RealData();
        this.real_matrix          = new RealMatrix();
        
        this.day_services         = new ServiceOrder[1];
        this.real_distance_matrix = new DoubleMatrix(1);
        this.real_time_matrix     = new DoubleMatrix(1);
        this.current_instance     = new MtspInstance(1,0);

        this.real_solution           = new MtspSolution(1,1);
        this.complete_final_solution = new MtspSolution(1,1);
        this.current_aco_solution    = new MtspSolution(1,1);

        // arquivo para gravação das soluções reais para plotagem
       // this.f_real_sols = new BufferedWriter(new FileWriter("outs/plot_real_sols.txt"));
        //TODO Configurar precisão do float
        //f_real_sols << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

        //this.f_longests = new BufferedWriter(new FileWriter("outs/longests.txt"));
        //TODO Configurar precisão do float
        //f_longests << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

       // this.f_total_costs = new BufferedWriter(new FileWriter("outs/total_costs.txt"));
        //TODO Configurar precisão do float
        //f_total_costs << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);
        this.logExperiment = LogExperiment.getInstance();

        this.df = new DecimalFormat();
        this.df.setMaximumFractionDigits(FLOAT_PRECISION);

    }

    // carrega todos os dados do dia de trabalho
    public void load_data_work_day(int counter_day_simulations) throws IOException {
        int n_services = this.real_data.count_services(this.id_work_day);
        this.depot = DEPOT_INDEX;
        this.n_points = n_services + 1; // para incluir o depósito
        this.n_teams = this.real_data.count_teams(this.id_work_day); // definindo número de equipes do dia de trabalho

        // criando instância para preencher grafo euclidiano com coordenadas UTM
        //delete current_instance;
        this.current_instance = new MtspInstance(this.n_teams, this.depot);
        this.current_instance.create_empty_instance(this.n_points, this.n_teams);

        // inserindo o depósito como primeiro nó do grafo
        int depot_x = this.real_data.x_depot();
        int depot_y = this.real_data.y_depot();
        this.current_instance.add_plan_node(depot_x, depot_y);

        // criando o vetor de nós e calculando a matriz de distâncias euclidianas
        this.real_data.load_euclidean_graph(this.id_work_day, this.current_instance);
        this.current_instance.calcule_euclidean_matrix(); // divididas por 1000 (UTM)

        if (PRINT_COSTS_MATRIX == 1) {
            this.current_instance.print_euclidean_matrix();
        }

        // carregando matrizes com custos reais
        //delete real_distance_matrix;
        this.real_distance_matrix = new DoubleMatrix(this.n_points);
        this.real_matrix.load_real_distance_matrix(this.id_work_day, this.real_distance_matrix);

        if (PRINT_COSTS_MATRIX == 1) {
            System.out.print("\r\nReal distance ");
            this.real_distance_matrix.print_matrix();
        }
        //delete real_time_matrix;
        this.real_time_matrix = new DoubleMatrix(n_points);
        this.real_matrix.load_real_time_matrix(id_work_day, real_time_matrix);

        if (PRINT_COSTS_MATRIX == 1) {
            //TODO Configurar precisão do float
            //cout << setiosflags (ios::fixed) << setprecision(0);
            System.out.print("\r\nReal time ");
            real_time_matrix.print_matrix();
            //TODO Configurar precisão do float
            //cout << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);
        }

        // criando estruturas com os dados de todos os serviços do dia
        //delete [] day_services;
        this.day_services = new ServiceOrder[this.n_points]; // para que o índice de day_services corresponda ao índice das matrizes. O índice do deposito é desconsiderado.
        this.day_services = this.real_data.load_service_orders(this.id_work_day, this.n_points);

        // preenchendo a matriz de custos que será usada na criação das soluções
        // utiliza os tempos de execução em day_services, se a matriz for de tempos de desloc + tempos de execução
        this.mount_cost_matrix(); // preenche a matriz de custos da instância, a que será usada na construção das soluções

        if (PRINT_COSTS_MATRIX == 1) {
            System.out.print("\r\nApplied cost ");
            this.current_instance.print_cost_matrix();
        }

        // carregando solução real realizada de acordo com a matriz de custos definida em current_instance
        // já aplica DESPATCH_REAL_SOL: atualiza em day_services o tempo de execução do serviço de acordo como a solução real calculada (time_dispatch - partial_route_cost)
        //delete real_solution;
        this.real_solution = new MtspSolution(this.n_points, this.n_teams);
        this.real_solution = this.real_data.load_real_solution(this.id_work_day, this.n_points, this.n_teams, this.current_instance, this.day_services);

        // salvando a solução real para plotagem (apenas na primeira simulação do dia de trabalho)
        if (counter_day_simulations == 1){
            Node[] plan_coords = this.current_instance.get_plan_nodes_vector();
            //this.f_real_sols.write("\r\nDia de trabalho: " + this.id_work_day + "\r\n");
            this.logExperiment.writeF_REAL_SOLS("\r\nDia de trabalho: " + this.id_work_day + "\r\n");
            this.real_solution.save_to_plot(this.logExperiment.f_real_sols, plan_coords);
        }

        if (PRINT_DAY_SERVICES == 1) {
            System.out.print("\r\n");
            this.print_day_services();
        }
    }

    // monta a matriz de custos da instância, a que será usada na construção das soluções
    public void mount_cost_matrix() {
        switch (TYPE_COST_MATRIX){  // matriz de custos a ser utiliada na aplicação
            case 1: {   // distâncias euclidianas
                for (int i = 0; i < this.n_points; i++){
                    for (int j = 0; j < this.n_points; j++){
                        double cost;
                        cost = this.current_instance.get_value_euclidean_matrix(i,j);
                        this.current_instance.set_value_cost_matrix(i, j, cost);
                    }
                }
                break;
            }
            case 2: {   // distâncias reais
                for (int i = 0; i < this.n_points; i++){
                    for (int j = 0; j < this.n_points; j++){
                        double cost;
                        cost = this.real_distance_matrix.get_value(i, j);
                        this.current_instance.set_value_cost_matrix(i, j, cost);
                    }
                }
                break;
            }
            case 3: {   // tempos reais
                for (int i = 0; i < this.n_points; i++){
                    for (int j = 0; j < this.n_points; j++){
                        double cost;
                        cost = this.real_time_matrix.get_value(i,j);
                        this.current_instance.set_value_cost_matrix(i, j, cost);
                    }
                }
                break;
            }
            case 4: {   // tempos reais de deslocamento + tempo de execução dos serviços
                for (int i = 0; i < this.n_points; i++){
                    for (int j = 0; j < this.n_points; j++){
                        double cost;
                        if (i == j){
                            cost = 0;
                        }
                        else{
                            int time_exec_serv;
                            if (j != this.depot) {
                                time_exec_serv = this.day_services[j].get_time_execution();
                            }
                            else {
                                time_exec_serv = 0;
                            }
                            double time_displacement = this.real_time_matrix.get_value(i, j);
                            cost = time_exec_serv + time_displacement;
                        }
                        this.current_instance.set_value_cost_matrix(i, j, cost);
                    }
                }
                break;
            }
        }
    }

    public void initialize_simulation() {
        // armazena as rotas finais em construção
        this.cur_executed_routes = new Route[this.n_teams];
        for (int k = 0; k < this.n_teams; k++){
            this.cur_executed_routes[k].create(this.n_points+1);  // tamanho máximo para m=1 com retorno ao depósito
        }
    }

    public void initialize_cur_executed_routes(BufferedWriter f_log_exper) throws IOException {
        for (int k=0; k < n_teams; k++){
            this.cur_executed_routes[k].add_node(this.depot, 0.0, 0.0);
        }
        if (SAVE_DAY_STATE_CHANGES == 1) {
            f_log_exper.write("\r\nInicializacao: relógio da simulacao = " + (int)this.current_time + " segundos\t (08:00hs)\r\n");
            this.save_executed_routes(f_log_exper);
        }
    }

    public void execute_simulation(long seed_random, int counter_day_simulations, BufferedWriter f_log_exper, BufferedWriter f_simul_res) throws IOException {
        this.seed_simulation = seed_random;

        this.current_time = 0.0; // relógio da simulação em segundos, definindo 0.0 como 08:00 hs do dia de trabalho atual

        // criando estruturas de dados do experimento
        this.initialize_simulation();

        // adiciona o depósito para todas as equipes em executed_routes
        this.initialize_cur_executed_routes(f_log_exper);

        boolean end_work_day = false;
        while (!end_work_day){  // em cada loop é despachado um serviço

            // atualizando next_emergency e time_dispatch_next_emergency (horário de despacho da próxima emergência)
            // se next_emergency == -1, não existem mais emergências a despachar
            this.update_next_emergency();

            // verifica qual a próxima equipe a ficar livre. Atualiza next_team_free e time_to_free_next_team
            this.update_next_team_free();

            // verificando se surgiu uma emergência até a próxima equipe ficar livre:
            if ((this.time_dispatch_next_emerg <= this.time_free_next_team) && (this.next_emergency != -1)) {
                // despachando a emergência:
                this.emergency_dispatch(this.next_emergency, f_log_exper);

            } else { // não surgiram emergências

                // atualizando os serviços válidos a partir de service_days e current_time
                // (comparando current_time com o tempo de despacho e excluindo os já executados)
                this.update_valid_nodes_instance();
                int n_valid_nodes = this.current_instance.get_n_valid_nodes();

                if (n_valid_nodes == 0){   // não existem serviços a executar em valid_nodes

                    double time_despatch_next_service = this.time_next_service();
                    if (time_despatch_next_service == -1.0){
                        end_work_day = true;   // finalizando os despachos: não existem serviço a executar

                    } else {
                        this.current_time = time_despatch_next_service + 1.0;
                    }
                } else {  // existem serviços a despachar

                    int service_to_despatch = 0; // serviço que será despachado
                    int current_position = this.cur_executed_routes[this.next_team_free].last_node();  // posição atual da equipe (serviço em execução)

                    if (n_valid_nodes < this.n_teams){  // se houver mais equipes que nós válidos, não gera solução ACO

                        // despachar para a próxima equipe livre o serviço com melhor previsão de custo (atendimento + retorno ao depósito)
                        double best_estimated_cost = 0.0;
                        for (int s=0; s < n_valid_nodes; s++){

                            int service_point = this.current_instance.get_valid_node(s);
                            double cost_current_to_service = this.current_instance.get_value_cost_matrix(current_position, service_point);
                            double cost_service_to_depot = this.current_instance.get_value_cost_matrix(service_point, this.depot);
                            double estimated_cost = cost_current_to_service + cost_service_to_depot;

                            if (s == 0){
                                service_to_despatch = service_point;
                                best_estimated_cost = estimated_cost;
                            }
                            else{
                                if (estimated_cost < best_estimated_cost){
                                    service_to_despatch = service_point;
                                    best_estimated_cost = estimated_cost;
                                }
                            }
                        }
                    }

                    else{  // o número de serviços é igual ou maior ao número de equipes: gerar solução ACO

                        if (NEAREST_TEAM == 1){  // despachar o serviço que estiver mais próximo da equipe

                            // despachar para a próxima equipe livre o serviço com melhor previsão de custo (atendimento + retorno ao depósito)
                            double best_cost = 0.0;
                            for (int s = 0; s < n_valid_nodes; s++){

                                int service_point = this.current_instance.get_valid_node(s);
                                double cost_current_to_service = this.current_instance.get_value_cost_matrix(current_position, service_point);

                                if (s == 0){
                                    service_to_despatch = service_point;
                                    best_cost = cost_current_to_service;
                                } else {
                                    if (cost_current_to_service < best_cost){
                                        service_to_despatch = service_point;
                                        best_cost = cost_current_to_service;
                                    }
                                }
                            }
                        } else {  // gerar solução ACO

                            // atualizando a posição das equipes na instância a partir de executed_routes
                            this.update_positions_teams();

                            // gerando uma solução com o algoritmo ACO para a instância atual e salvando em current_best_solution
                            AcoMtspAlgorithm aco_mtsp_app = new AcoMtspAlgorithm(seed_random, this.current_instance);
                            //delete current_aco_solution;
                            this.current_aco_solution = aco_mtsp_app.execute();
                            //delete aco_mtsp_app;
                            this.current_aco_solution.set_random_seed(this.seed_simulation);

                            // salvando a solução ACO gerada
                            if (SAVE_PARTIAL_DAY_SOLS_PLOT == 1){
                                // salvando em aco_partial_sols_day_to_plot.txt a solução final para o dia de trabalho
                                BufferedWriter f_day_aco_sols;
                                f_day_aco_sols = new BufferedWriter(new FileWriter("outs/plot_aco_partial_sols_day.txt"));
                                Node[] plan_nodes_vector = this.current_instance.get_plan_nodes_vector();
                                f_day_aco_sols.write("\r\nDia de trabalho: " + this.id_work_day + "\tSimulação: " + counter_day_simulations + "\r\n");
                                this.current_aco_solution.save_to_plot(f_day_aco_sols, plan_nodes_vector);
                                f_day_aco_sols.close();
                            }

                            // localizando o serviço a ser despachado à proxima equipe livre, de acordo com a solução ACO gerada
                            service_to_despatch = this.current_aco_solution.next_node(current_position);

                            // a solução ACO afirma que o melhor é a equipe livre retornar ao depósito
                            // solução: despachar a solução com melhor custo previsto para a equipe
                            if (service_to_despatch == this.depot){

                                // despachar para a próxima equipe livre o serviço com melhor previsão de custo (atendimento + retorno ao depósito)
                                double best_estimated_cost = 0.0;
                                for (int s = 0; s < n_valid_nodes; s++ ){

                                    int service_point = this.current_instance.get_valid_node(s);
                                    double cost_current_to_service = this.current_instance.get_value_cost_matrix(current_position, service_point);
                                    double cost_service_to_depot = this.current_instance.get_value_cost_matrix(service_point, this.depot);
                                    double estimated_cost = cost_current_to_service + cost_service_to_depot;

                                    if (s == 0){
                                        service_to_despatch = service_point;
                                        best_estimated_cost = estimated_cost;

                                    } else {
                                        if (estimated_cost < best_estimated_cost) {
                                            service_to_despatch = service_point;
                                            best_estimated_cost = estimated_cost;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // atualizando current_time (usado na atualização dos serviços válidos)
                    // passa a ser o horário que a equipe ficou livre + 1 segundo
                    this.current_time = this.time_free_next_team + 1.0;

                    // realizando o despacho do serviço escolhido à equipe escolhida:
                    this.dispatch_service_order(this.next_team_free, service_to_despatch);

                    if (SAVE_DAY_STATE_CHANGES == 1) {
                        f_log_exper.write("\r\nDespacho comercial: relogio da simulacao = " + (int)current_time + " segundos\r\n");
                        f_log_exper.write("Ordem de serviço despachada: " + service_to_despatch + "\t");

                        this.day_services[service_to_despatch].save_service_order(f_log_exper);
                        this.current_instance.save_positions_teams(f_log_exper);

                        f_log_exper.write("Solução ACO criada: ");
                        this.current_aco_solution.save_how_list(f_log_exper);

                        this.save_executed_routes(f_log_exper);
                    }
                }
            }
        }

        // montando a solução de executed_routes em proposed_final_solution
        this.mount_final_solution();

        // finalizando o experimento
        this.finalize_simulation(counter_day_simulations, f_log_exper, f_simul_res);
    }

    public void mount_final_solution() {
        //delete complete_final_solution;
        this.complete_final_solution = new MtspSolution(this.n_points, this.n_teams);
        this.complete_final_solution.add(this.depot, 0.0); // adicionando o depósito no início da solução final
        for (int k = 0; k < this.n_teams; k++){
            int size_route_team = this.cur_executed_routes[k].n_nodes();
            for (int i=1; i < size_route_team; i++){  // desconsiderando o depósito em exected_routes
                int point = this.cur_executed_routes[k].node(i);
                this.complete_final_solution.add(point, 0.0);  // os custos são calculados por recalculate_solution
            }
            this.complete_final_solution.add(depot, 0.0); // adicionando o depósito ao final de cada rota
        }

        // calculando a solução final
        DoubleMatrix cost_matrix = this.current_instance.get_cost_matrix();
        this.complete_final_solution.recalculate_solution(cost_matrix);
        this.complete_final_solution.set_random_seed(seed_simulation);
    }

    public void finalize_simulation(int counter_day_simulations, BufferedWriter f_log_exper, BufferedWriter f_simul_res) throws IOException {

        if (SAVE_DAY_STATE_CHANGES == 1) {
            f_log_exper.write("\r\nResumo da simulacao:");
        }

        f_log_exper.write("\r\nDia de trabalho: " + this.id_work_day + "\tserviços: " + (this.n_points-1) + "\tequipes: " + this.n_teams + "\r\n");
        f_log_exper.write("Solução real:\t\t");
        this.real_solution.save_how_list(f_log_exper);
        f_log_exper.write("Solução proposta:\t");
        this.complete_final_solution.save_how_list(f_log_exper);

        // salvando o resumo da simulação em um arquivo próprio:
        f_simul_res.write(this.id_work_day + "\t");
        this.complete_final_solution.save_longest_cost(f_simul_res);
        this.complete_final_solution.save_total_cost(f_simul_res);

        if (counter_day_simulations == 1){
        	
        	this.logExperiment.f_longests.write("\r\n" + this.id_work_day + "\t");
        	this.logExperiment.f_total_costs.write("\r\n" + this.id_work_day + "\t");
        }
        this.complete_final_solution.save_longest_cost(this.logExperiment.f_longests);
        this.complete_final_solution.save_total_cost(this.logExperiment.f_total_costs);

        // salvando em plot_final_created_sols_day.txt a solução final para o dia de trabalho
        BufferedWriter f_day_aco_final_sols;
        f_day_aco_final_sols = new BufferedWriter(new FileWriter("outs/plot_final_created_sols_day.txt"));
        Node[] plan_nodes_vector = this.current_instance.get_plan_nodes_vector();
        f_day_aco_final_sols.write("\r\nDia de trabalho: " + this.id_work_day + "\tSimulação: " + counter_day_simulations + "\r\n");
        this.complete_final_solution.save_to_plot(f_day_aco_final_sols, plan_nodes_vector);
        f_day_aco_final_sols.close();

        double longest_real = this.real_solution.get_longest_route();
        double longest_proposed = this.complete_final_solution.get_longest_route();
        double diff_longest = longest_real - longest_proposed;
        double improvement_longest = (diff_longest / longest_real) * 100;

        double total_real = this.real_solution.get_total_cost();
        double total_proposed = this.complete_final_solution.get_total_cost();
        double diff_total = total_real - total_proposed;
        double improvement_total = (diff_total / total_real) * 100;

        //TODO Configurar precisão do float
        //f_log_exper << setiosflags (ios::fixed) << setprecision(2);
        f_log_exper.write("Melhoramento:\t\tmaior rota: " + df.format(improvement_longest) + "%\tcusto total: " + df.format(improvement_total) + "%\r\n");
        //TODO Configurar precisão do float
        //f_log_exper << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

        if (PRINT_EMERGENCY_CARE == 1){
            System.out.print("  Emergency care:\r\n");
            for (int s=0; s < this.n_points; s++){
                if (s != this.depot){
                    if (this.day_services[s].is_emergency()){
                        System.out.print("  ");
                        this.day_services[s].print_total_time_execution(s);
                    }
                }
            }
        }
        if (PRINT_DAY_SERVICES == 1) {
            System.out.print("\r\n");
            this.print_day_services();
        }

        //delete [] cur_executed_routes;
    }

    public void execute_static_simulation(long seed_random, int counter_day_simulations, BufferedWriter f_log_exper, BufferedWriter f_simul_res) throws IOException {

        this.seed_simulation = seed_random;

        // todas as ordens de serviço são válidas
        this.update_valid_all_nodes();

        // posicionando todas as equipes no depósito:
        this.position_teams_depot();

        // gerando uma solução com o algoritmo ACO para a instância completa e salvando em complete_final_solution
        AcoMtspAlgorithm aco_mtsp_app = new AcoMtspAlgorithm(seed_random, this.current_instance);
        //delete complete_final_solution;
        this.complete_final_solution = aco_mtsp_app.execute();
        //delete aco_mtsp_app;
        this.complete_final_solution.set_random_seed(this.seed_simulation);

        // finalizando o experimento
        this.finalize_static_simulation(counter_day_simulations, f_log_exper, f_simul_res);
    }

    public void finalize_static_simulation(int counter_day_simulations, BufferedWriter f_log_exper, BufferedWriter f_simul_res) throws IOException {
        if (SAVE_DAY_STATE_CHANGES == 1) {
            f_log_exper.write("\r\nResumo da simulacao:");
        }

        f_log_exper.write("\r\nDia de trabalho: " + this.id_work_day + "\tserviços: " + (this.n_points-1) + "\tequipes: " + this.n_teams + "\r\n");
        f_log_exper.write("Solução real:\t\t");
        this.real_solution.save_how_list(f_log_exper);
        f_log_exper.write("Solução proposta:\t");
        this.complete_final_solution.save_how_list(f_log_exper);

        // salvando o resumo da simulação em um arquivo próprio:
        f_simul_res.write(id_work_day + "\t");
        this.complete_final_solution.save_longest_cost(f_simul_res);
        this.complete_final_solution.save_total_cost(f_simul_res);

        if (counter_day_simulations == 1) {
        	this.logExperiment.f_longests.write("\r\n" + this.id_work_day + "\t");
        	this.logExperiment.f_total_costs.write( "\r\n" + this.id_work_day + "\t");
        }
        this.complete_final_solution.save_longest_cost(this.logExperiment.f_longests);
        this.complete_final_solution.save_total_cost(this.logExperiment.f_total_costs);

        // salvando em plot_final_created_sols_day.txt a solução final para o dia de trabalho
        BufferedWriter f_day_aco_final_sols;
        f_day_aco_final_sols = new BufferedWriter(new FileWriter("outs/plot_final_created_sols_day.txt"));
        Node[] plan_nodes_vector = this.current_instance.get_plan_nodes_vector();
        f_day_aco_final_sols.write("\r\nDia de trabalho: " + this.id_work_day + "\tSimulação: " + counter_day_simulations + "\r\n");
        this.complete_final_solution.save_to_plot(f_day_aco_final_sols, plan_nodes_vector);
        f_day_aco_final_sols.close();

        double longest_real = this.real_solution.get_longest_route();
        double longest_proposed = this.complete_final_solution.get_longest_route();
        double diff_longest = longest_real - longest_proposed;
        double improvement_longest = (diff_longest / longest_real) * 100;

        double total_real = this.real_solution.get_total_cost();
        double total_proposed = this.complete_final_solution.get_total_cost();
        double diff_total = total_real - total_proposed;
        double improvement_total = (diff_total / total_real) * 100;

        //TODO Configurar precisão do float
        //f_log_exper << setiosflags (ios::fixed) << setprecision(2);
        f_log_exper.write("Melhoramento:\t\tmaior rota: " + df.format(improvement_longest) + "%\tcusto total: " + df.format(improvement_total) + "%\r\n");
        //TODO Configurar precisão do float
        //f_log_exper << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);

        if (PRINT_EMERGENCY_CARE == 1) {
            System.out.print("  Emergency care:\r\n");
            for (int s = 0; s < this.n_points; s++) {
                if (s != this.depot) {
                    if (this.day_services[s].is_emergency()) {
                        System.out.print("  ");
                        this.day_services[s].print_total_time_execution(s);
                    }
                }
            }
        }
        if (PRINT_DAY_SERVICES == 1) {
            System.out.print("\r\n");
            this.print_day_services();
        }
    }

    // atualiza next_emergency e time_dispatch_next_emergency
    public void update_next_emergency() {

        this.next_emergency = -1;
        this.time_dispatch_next_emerg = 0.0;

        // os indices de day_services correspondem ao id dos nodes. o índice do depósito é desconsiderado:
        for (int s = 0; s < this.n_points; s++){
            if (s != this.depot){
                if (!this.day_services[s].get_executed_service()){ // o serviço ainda não foi executado
                    double time_dispatch = this.day_services[s].get_time_dispatch();
                    if (this.day_services[s].is_emergency()){ // o serviço a ser despachado é uma emergência
                        if (this.next_emergency == -1){
                            this.next_emergency = s;
                            this.time_dispatch_next_emerg = time_dispatch;

                        } else {
                            if (time_dispatch < this.time_dispatch_next_emerg){
                                this.next_emergency = s;
                                this.time_dispatch_next_emerg = time_dispatch;
                            }
                        }
                    }
                }
            }
        }
    }

    // atualiza next_team_free e time_to_free_next_team
    public void update_next_team_free() {

        double temporal_cost_route;  // custo atual da rota da equipe

        for (int k = 0; k < this.n_teams; k++){

            temporal_cost_route = this.cur_executed_routes[k].get_temporal_cost();

            // verificando o menor custo temporal (a primeira equipe a ficar livre):
            if (k == 0) {
                this.next_team_free = k;
                this.time_free_next_team = temporal_cost_route;

            } else {
                if (temporal_cost_route < this.time_free_next_team){
                    this.next_team_free = k;
                    this.time_free_next_team = temporal_cost_route;
                }
            }
        }
    }

    // realiza o despacho de um serviço emergencial
    public void emergency_dispatch(int emergency_order, BufferedWriter f_log_exper) throws IOException {

        int best_team = 0;  // a melhor equipe para atender a emergência é que estiver mais próxima, de acordo com a matriz de custos aplicada
        double best_cost_to_care = 0.0;  //custo entre a posição atual da equipe e a emergência

        // definindo a equipe mais próxima do local da emergência:
        for (int k = 0; k < this.n_teams; k++) {
            int current_position = this.cur_executed_routes[k].last_node();  // posição atual da equipe (serviço em execução)
            double cost_to_care = this.current_instance.get_value_cost_matrix(current_position, emergency_order); // custo da matriz aplicada

            // verificando a melhor equipe para atendimento:
            if (k == 0) {
                best_team = k;
                best_cost_to_care = cost_to_care;

            } else {
                if (cost_to_care < best_cost_to_care){
                    best_team = k;
                    best_cost_to_care = cost_to_care;
                }
            }
        }

        // atualizando current_time (usado na atualização dos serviços válidos)
        // passa a ser o horário do serviço adicionado de 1 segundo:
        this.current_time = this.day_services[emergency_order].get_time_dispatch() + 1.0;

        // realizando o despacho do serviço à equipe escolhida:
        this.dispatch_service_order(best_team, emergency_order);
        if (SAVE_DAY_STATE_CHANGES == 1) {
            f_log_exper.write("\r\nDespacho emergencial: relogio da simulacao = " + (int)this.current_time + " segundos\r\n");
            f_log_exper.write("Ordem de serviço despachada: " + emergency_order + "\t");
            this.day_services[emergency_order].save_service_order(f_log_exper);
            this.save_executed_routes(f_log_exper);
        }
    }

    // atribui um serviço à rota de uma equipe
    public void dispatch_service_order(int team, int service_order) {

        int current_position = this.cur_executed_routes[team].last_node();  // posição atual da equipe
        double cost = this.current_instance.get_value_cost_matrix(current_position, service_order); // custo da matriz aplicada

        // calculando custo temporal:
        double travel_time   = this.real_time_matrix.get_value(current_position, service_order);
        double exec_time     = this.day_services[service_order].get_time_execution();
        double temporal_cost = travel_time + exec_time;

        // adicionando o serviço à rota da equipe:
        this.cur_executed_routes[team].add_node(service_order, cost, temporal_cost);
        double partial_route_temporal_cost = this.cur_executed_routes[team].get_temporal_cost();
        this.day_services[service_order].set_executed_service(partial_route_temporal_cost);
    }

    public void update_valid_nodes_instance() {

        this.current_instance.reset_valid_nodes_instance();

        // os indices de day_services correspondem ao id dos nodes. o índice do depósito é desconsiderado:
        for (int s = 0; s < this.n_points; s++) {
            if (s != this.depot) {
                if (!this.day_services[s].get_executed_service()){ // o serviço ainda não foi executado
                    if (this.day_services[s].get_time_dispatch() <= this.current_time){ // o tempo de despacho do serviço é menor ou igual ao tempo atual

                        this.current_instance.add_valid_node(s); // adicionando o serviço como um nó válido

                    }
                }
            }
        }
    }

    public void update_valid_all_nodes() {
        this.current_instance.reset_valid_nodes_instance();

        // os indices de day_services correspondem ao id dos nodes. o índice do depósito é desconsiderado:
        for (int s = 0; s < this.n_points; s++){
            if (s != this.depot){
                this.current_instance.add_valid_node(s); // adicionando o serviço como um nó válido
            }
        }
    }

    public void update_positions_teams() {
        this.current_instance.reset_positions_teams();
        for (int k = 0; k < this.n_teams; k++){
            this.current_instance.add_position_team(this.cur_executed_routes[k].last_node());
        }
    }

    public void position_teams_depot() {
        this.current_instance.reset_positions_teams();
        for (int k = 0; k < this.n_teams; k++) {
            this.current_instance.add_position_team(this.depot);
        }
    }

    public double time_next_service() {
        double time_dispatch_next_service = -1.0;  // indica que não há mais serviços a executar em day_services
        int next_service = -1;

        // os indices de day_services correspondem ao id dos nodes. o índice do depósito é desconsiderado:
        for (int s = 0; s < this.n_points; s++){
            if (s != this.depot){
                if (!this.day_services[s].get_executed_service()){ // o serviço ainda não foi executado
                    double time_dispatch = this.day_services[s].get_time_dispatch();
                    if (next_service == -1){
                        next_service = s;
                        time_dispatch_next_service = time_dispatch;

                    } else {
                        if (time_dispatch < time_dispatch_next_service){
                            next_service = s;
                            time_dispatch_next_service = time_dispatch;
                        }
                    }
                }
            }
        }

        return time_dispatch_next_service;
    }

    public void print_executed_routes() {
        System.out.print("Rotas parciais executadas:\r\n");
        for (int k = 0; k < this.n_teams; k++) {
            System.out.print("  Equipe " + k + ": ");
            this.cur_executed_routes[k].print_short();
        }
    }

    public void save_executed_routes(BufferedWriter file_out) throws IOException {
        file_out.write("Rotas parciais executadas:\r\n");
        for (int k = 0; k < this.n_teams; k++){
            file_out.write("  Equipe " + (k+1) + ": ");
            this.cur_executed_routes[k].save_short(file_out);
        }
    }

    public void print_day_services() {
        System.out.print("Day service orders:\r\n");
        for (int i=0; i < this.n_points; i++){
            if (i != this.depot){
                System.out.print("  service " + i + " : ");
                this.day_services[i].print_service_order();
            }
        }
    }
}
