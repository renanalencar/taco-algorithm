/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class AcoMtspAlgorithm implements ControlExperiment, ControlSTACS {
    // variáveis do ambiente:
    private int depot;
    private int n;                      // número de nós da instância
    private int m;                      // número de caixeiros
    private double time_ini_execution;  // instante de início da execução
    private DoubleMatrix cost_matrix;  // matriz de custos
    private Node plan_nodes_vector[];    // vetor com as coordenadas planas dos nós
    private IntList positions_teams;   // posiçoes atuais das equipes
    private IntList valid_nodes_instance; // índices das matrizes que fazem parte da isntância corrente

    // variávies da aplicação:
    private int type_solution;             // tipo de solução que está senddo gerada, defina a partir de positions_teams
    private DoubleMatrix pheromone_matrix;// matriz de feromônio
    private Colony colony;                // colônia
    private Ant ants[];                     // formigas
    private IntList candidate_list;       // lista de nós candidatos
    private AcsAlgorithm AntColonySystem; // rotinas do algoritmo Ant Colony System
    private TacoAlgorithm TACOAlgorithm;  // rotinas do algoritmo de Vallivaara 2008
    private Random rand;                     // gerador de números pseudo randômicos
    //private Utilities util;                  // funções auxiliares
    private IntList sorted_lists[];            // listas para todos os nós com as distâncias ordenadas
    private LocalSearch local_search;   // busca local aplicada às soluções geradas
    private Intersection intersec;      // busca local proposta que tenta retirar intersecções de soluções simétricas planas
    private MtspNearestNeighbor nn_app; // aplicação para criação da solução do vizinho mais próximo

    private int moving_ant;    // formiga escolhida para se mover
    private int current_node;  // nó atual da formiga escolhida para se mover
    private int next_node;     // nó pra o qual a formiga escolhida se moverá

    private int cycle_counter;     // contador de ciclos
    private int non_improved_counter_cycles;  // contador de ciclos sem melhora da best_so_far_solution da execução
    private int created_sols_counter;  // contador de soluções criadas

    public AcoMtspAlgorithm(long seed_random, MtspInstance instance) {
        this.depot = DEPOT_INDEX;
        this.n = instance.get_n_nodes();
        this.m = instance.get_n_salesmen();
        this.time_ini_execution = (double) System.currentTimeMillis();  // instante do início da execução
        this.cost_matrix = instance.get_cost_matrix();
        this.plan_nodes_vector = instance.get_plan_nodes_vector();
        this.positions_teams = instance.get_positions_teams();
        this.valid_nodes_instance = instance.get_valid_nodes_instance();

        // criando matriz de feromônio:
        this.pheromone_matrix = new DoubleMatrix(n);

        // criando a colônia:
        this.colony = new Colony(n, m, cost_matrix);
        this.ants = colony.ants_colony();
        this.candidate_list = colony.candidate_list_colony();

        // para acessar as rotinas ACS:
        this.AntColonySystem = new AcsAlgorithm(n, m, cost_matrix, pheromone_matrix);

        // para acessar as rotinas TACO:
        this.TACOAlgorithm = new TacoAlgorithm(m, cost_matrix, ants);
        this.rand = new Random (seed_random);
        //util = new Utilities();

        // criando as listas com os nós ordenados pelas distâncias:
        this.sorted_lists = new IntList[n];
        for (int i = 0; i < n; i++) {
            sorted_lists[i] = new IntList();
            sorted_lists[i].create(n);
        }
        this.make_sorted_lists();

        this.local_search = new LocalSearch(time_ini_execution, cost_matrix);
        this.intersec = new Intersection(n, plan_nodes_vector);
    }


    public void update_type_solution() {
        this.type_solution = 1; // finais fechados
        for (int k = 0; k < this.m; k++) {
            if (this.positions_teams.value(k) != this.depot) {
                this.type_solution = 2; // finais abertos
            }
        }
    }

    /**
     * Método que execução do algoritmo ACO para MTSP proposto
     * retorna a melhor solução encontrada
     * @return
     */
    public MtspSolution execute() {
        // 1: finais fechados; 2: finais abertos:
        this.update_type_solution();    // atualizando tipo da solução a ser gerada a partir das posições das formigas

        // inicializando a matriz de feromônio como no ACS:
        this.AntColonySystem.initialize_pheromone_matrix(this.valid_nodes_instance);
        if(PPOE == 1) {
            System.out.print("\r\nInicialization:   ");
            this.AntColonySystem.print_pheromone_matrix();
        } // imprimir a matriz após inicializada

        if(PDMT == 1)
            this.cost_matrix.print_matrix();   // imprimindo a matriz de distâncias

        double current_time = 0.0;      // contador de tempo
        boolean updated_best_exec;         // sinaliza se foi encontrada uma soluçao melhor no último ciclo

        this.cycle_counter = 0;   // contador de ciclos
        this.non_improved_counter_cycles = 0;   // contador de ciclos sem melhora da best_so_far_solution da execução
        MtspSolution best_solution_execution = null; // melhor solução da execução

        // em cada loop são criadas N soluções:
        while ((cycle_counter != MAX_CYCLES) && (this.non_improved_counter_cycles != NO_IMP_CYCLES) && ((current_time <= MAX_TIME_EXEC) || (MAX_TIME_EXEC == -1))) { // os três critérios de parada

            if (PDCR == 1)
                System.out.print("\r\n---------> iteration " + (this.cycle_counter + 1) + "\r\n");

            MtspSolution best_solution_cycle = null; // melhor solução do ciclo

            for (this.created_sols_counter = 0; this.created_sols_counter < N; this.created_sols_counter++) { // em cada loop é criada uma solução

                if (PDCR == 1)
                    System.out.print("\r\n------> creating solution " + (created_sols_counter+1) + "\r\n");

                // constrói uma solução
                MtspSolution created_solution = this.solution_construction(positions_teams);

                if (LS2O == 1) { // 2-opt em todas as soluções criadas
                    boolean updated = this.local_search.two_opt(created_solution);
//                if (updated) cout << "\r\n  updated 2-opt";
                }

                // atualizando melhor solução do ciclo:
                if (this.created_sols_counter == 0) {
                    best_solution_cycle = created_solution;
                } else {
                    double created_cost;
                    double best_cost;
                    if (APP_OBJECTIVE == 1){  // atualizar pela longest_route
                        created_cost = created_solution.get_longest_route();
                        best_cost = best_solution_cycle.get_longest_route();
                    }
                    else{  // atualizar pelo total da solução
                        created_cost = created_solution.get_total_cost();
                        best_cost = best_solution_cycle.get_total_cost();
                    }
                    if (created_cost < best_cost) { // atualizar a melhor do ciclo:
                        //delete best_solution_cycle;
                        best_solution_cycle = created_solution;
                    }
                    else {
                        //delete created_solution;
                    }
                }
            }

            if (LS3O == 1) { // 3-opt nas melhores soluções dos ciclos
                boolean updated = this.local_search.three_opt(best_solution_cycle);
//            if (updated) cout << "\r\n  updated 3-opt";
            }

            if(PASO == 1){  // imprimir todas melhores soluções de cada ciclo
                if (this.cycle_counter == 0) {
                    System.out.print("\r\n---> Improved solutions:\r\niter\tlongest\t\ttotal\t\tsolution\r\n");
                }
                System.out.print("\r\n" + (this.cycle_counter+1) + "\t");
                best_solution_cycle.print();
            }

            if (GPU == 1) {  // atualização global de feromônio ativada
                if (GPUBSF == 0)
                    this.global_pheromone_update(best_solution_cycle); // atualização pela melhor solução do ciclo
            }

            // atualizando melhor solução da execução:
            if (cycle_counter == 0) {
                best_solution_execution = best_solution_cycle;
                updated_best_exec = true;
            } else {
                double cycle_cost;
                double best_cost;

                if (APP_OBJECTIVE == 1) {  // atualizar pela longest_route
                    cycle_cost = best_solution_cycle.get_longest_route();
                    best_cost = best_solution_execution.get_longest_route();
                } else {  // atualizar pelo total da solução
                    cycle_cost = best_solution_cycle.get_total_cost();
                    best_cost = best_solution_execution.get_total_cost();
                }

                if (cycle_cost < best_cost){ // atualizar a melhor da execução
                    //delete best_solution_execution;
                    best_solution_execution = best_solution_cycle;
                    updated_best_exec = true;
                } else {
                    //delete best_solution_cycle;
                    updated_best_exec = false;
                }
            }

            if (updated_best_exec){   // a solução foi atualizada
                this.non_improved_counter_cycles = 0;
                if(PISO == 1){  // imprimir todas as soluções melhoradas
                    if (cycle_counter == 0) {
                        System.out.print("Improvement by ACO Algorithm:\r\n");
                    }
                    System.out.print("  cycle: " + (cycle_counter+1) + "  \t");
                    best_solution_execution.print();
                }
            } else {  // a solução não foi atualizada
                this.non_improved_counter_cycles++;
            }

            if (GPU == 1) {  // atualização global de feromônio ativada
                if (GPUBSF == 1)
                    this.global_pheromone_update(best_solution_execution); // atualização pela best_so_far
            }

            // atualizando contador de iterações e de tempo:
            this.cycle_counter++;
            current_time = System.currentTimeMillis() - this.time_ini_execution;

            if (PPOE == 1) {  // imprimir a matriz de feromônio ao final da execução
                System.out.print("\r\n---> Final state pheromone matrix:   ");
                this.print_pheromone_matrix();
            }
        }
        return best_solution_execution;
    }

    /**
     * Método que construção de uma solução
     * @param positions_teams
     * @return
     */
    public MtspSolution solution_construction(IntList positions_teams) {
        this.colony.empty_visited_list();   // esvaziando a lista de nós visitados

        this.colony.add_visited_list_no_valid_nodes(this.valid_nodes_instance);  // adicionando os nós que não são válidos à visited_list

        this.reinitialize_ants(positions_teams);  // resetando as formigas (positions_teams são adicionados à visited_list)

        while (!this.colony.is_full_visited_list()) {   // enquanto a visited_list não estiver cheia

            this.moving_ant = this.TACOAlgorithm.select_ant();  // selecionando a formiga para se mover como val08 (menor rota parcial)

            this.current_node = this.ants[this.moving_ant].current_node();  // atualizando current_node

            if (PDCR == 1) {  // imprimir detalhes de construção das rotas
                System.out.print("\r\ncurr_ant: " + this.moving_ant + "   curr_node: " + this.current_node);
                System.out.print("     partial route:   ");
                this.ants[this.moving_ant].print_route();
                System.out.print("vist_list: ");
                this.colony.print_visited_list();
                System.out.print("\r\n");
            }

            boolean end_repeat = false;  // flag da repetição pela busca de uma formiga melhor
            int repeating = -1;       // contagem das repetições por uma formiga melhor

            // enquanto não for o melhor movimento
            while(!end_repeat) {
                repeating ++;

                // atualizando a lista de nós candidatos para o nó no qual a formiga escolhida se encontra:
                this.colony.update_candidate_list(this.type_solution, this.moving_ant, this.sorted_lists);

                if (PDCR == 1) {
                    System.out.print("cand_list: ");
                    this.colony.print_candidate_list();
                    System.out.print("\r\n");
                }


                this.next_node = this.AntColonySystem.state_transation_rule(this.current_node, this.candidate_list, this.rand);  // escolha do próximo nó como no ACS

                if (PDCR == 1) {
                    System.out.print("choosed_node: " + this.next_node + "\r\n");
                }

                // se REPCH == 0, não checa nenhuma vez; se REPCH == 1, igual a val08
                if (repeating < CBA_REPET) {
                    // retorna a formiga que resulta no melhor movimento:
                    int better_ant = this.TACOAlgorithm.check_better_ant(this.next_node);
                    if (this.moving_ant == better_ant) {  // é o melhor movimento
                        end_repeat = true;
                    } else {  // atualizando para a melhor formiga
                        this.moving_ant = better_ant;
                        this.current_node = this.ants[this.moving_ant].current_node();  // atualizando current_node
                    }
                } else {
                    end_repeat = true;  // alcançou limite de repetições
                }
            }

            this.colony.movement(this.moving_ant, this.next_node);  // movimenta a formiga escolhida para o nó escolhido
            if (LPU == 1)
                this.AntColonySystem.local_pheromone_update(this.current_node, this.next_node);  // atualização local de feromônio como no ACS

        }

        // finalizando as rotas da formigas:
        this.finalize_routes_ants();

        // imprimir todas as rotas criadas:
        if (PART == 1) {
            System.out.print("\r\n---> criated routes:\r\n");
            for (int k = 0; k < this.m; k++) {
                System.out.print("ant: " + k + "   ");
                this.ants[k].print_route();
            }
        }

        // criar e montar a solução de retorno:
        MtspSolution created_solution = new MtspSolution(n, m);
        if (this.type_solution == 1) { // closed ends
            // organizando todas as rotas para iniciarem e terminarem no depósito:
            for (int k = 0; k < m; k++) {
                this.ants[k].sort_route(this.type_solution);
            }
        }
        // montando a solução com as rotas das formigas:
        double dist;
        for(int k = 0; k < this.m; k++) {
            int n_nodes = this.ants[k].size_route();
            for (int i = 0; i < n_nodes; i++) {
                int current_node_ant = this.ants[k].node_route(i);

                if (this.type_solution == 1) {
                    if ((k==0)&&(i==0)) {  // adicionando o depósito como primeiro nó
                        created_solution.add(this.depot , 0.0);
                    }
                    if ((current_node_ant != this.depot) || (created_solution.last_node() != this.depot)){
                        int node_i = created_solution.last_node();
                        dist = this.cost_matrix.get_value(node_i, current_node_ant);
                        created_solution.add(current_node_ant, dist);
                    }
                }

                if (this.type_solution == 2){
                    if (i==0) {
                        created_solution.add(current_node_ant, 0.0);
                    }
                    else{
                        int node_i = created_solution.last_node();
                        dist = this.cost_matrix.get_value(node_i,current_node_ant);
                        created_solution.add(current_node_ant, dist);
                    }
                }

            }
        }
        // incluindo dados finais:
        double longest_route_current = this.colony.longest_route_curr();
        created_solution.set_longest_route(longest_route_current);
        created_solution.set_iteration(this.cycle_counter+1);
        created_solution.set_time(System.currentTimeMillis() - this.time_ini_execution); // em milisegundos
        created_solution.set_random_seed(this.rand.seed_used());

        return created_solution;
    }

    /**
     * Método que executado no início da construção de cada solução
     * @param positions_teams
     */
    public void reinitialize_ants(IntList positions_teams) {
        if (this.type_solution == 1){ // opções apenas para soluções com rotas fechadas
            if (ANTS_INIT_NODES == 1) this.colony.reset_ants_depot();         // posiciona todas as formigas no depósito
            if (ANTS_INIT_NODES == 2) this.colony.reset_ants_random(this.rand);    // posiciona as formigas em nós inicias aleatórios
            if (ANTS_INIT_NODES == 3) this.colony.reset_ants_by_limit(this.rand, this.created_sols_counter, this.cycle_counter, this.non_improved_counter_cycles);  // nós aleatórios, alterados a cada CHANGE_INI_NODES iterações sem melhora
        }
        if (this.type_solution == 2) this.colony.reset_ants_positions_teams(positions_teams); // posiciona todas as formigas nos nós iniciais das equipes

    }

    /**
     * Método que finaliando as rotas de todas as formigas
     */
    public void finalize_routes_ants() {
        this.check_depot();  // caso alguma formiga não tenha visitado o depósito, ele é incuído no final da rota
        // se a solução for com rotas abertas e as formigas partiram das posições das equipes, a solução está pronta
        if (this.type_solution == 1){ // rotas fechadas: retornar aos nós iniciais
            for (int k = 0; k < m; k++) {
                this.current_node = this.ants[k].current_node();
                int starting_node = this.ants[k].starting_node();
                double cost = this.cost_matrix.get_value(this.current_node,starting_node);
                this.ants[k].move(starting_node, cost);
                if (LPU == 1)
                    this.AntColonySystem.local_pheromone_update(this.current_node, starting_node);
            }
        }
    }

    /**
     * Método que adiciona o depósito em rotas em que ele não foi visitado
     */
    public void check_depot() {
        for (int k = 0; k < this.m; k++) {
            if (!this.ants[k].depot_visited()) {  // o depósito não foi visitado
                this.current_node = this.ants[k].current_node();
                double cost = this.cost_matrix.get_value(this.current_node,depot);
                this.ants[k].move(this.depot, cost);
                if (LPU == 1) this.AntColonySystem.local_pheromone_update(this.current_node, this.depot);
            } else {
                // a solução é com finais abertos mas há formigas cujo nó inicial é o depósito. Então, deve retornar para o depósito.
                if ((this.type_solution == 2) && (this.ants[k].node_route(0) == this.depot)) {
                    this.current_node = this.ants[k].current_node();
                    double cost = this.cost_matrix.get_value(current_node,this.depot);
                    this.ants[k].move(this.depot, cost);
                    if (LPU == 1)
                        this.AntColonySystem.local_pheromone_update(this.current_node, this.depot);
                }
            }
        }
    }

    /**
     * Método que atualização global de feromônio
     * @param solution
     */
    public void global_pheromone_update(MtspSolution solution) {
        this.AntColonySystem.global_pheromone_update(solution);
    }

    /**
     * Método que funcões auxiliares
     */
    public void make_sorted_lists() {
        for (int i = 0; i < this.n; i++){  // nó de partida
            sorted_lists[i].empty();
        double[] col_cost_matrix = this.cost_matrix.get_col_matrix(i);

            Utilities.sort_double(col_cost_matrix, this.sorted_lists[i]);

        }
    }

    public void print_sorted_lists() {
        for (int i = 0; i < this.n; i++) {
            System.out.print(i + ": ");
            this.sorted_lists[i].print();
            System.out.print("\r\n");
        }
    }

    public void print_pheromone_matrix() {
        this.AntColonySystem.print_pheromone_matrix();
    }

}
