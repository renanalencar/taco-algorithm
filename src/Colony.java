/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class Colony implements ControlExperiment, ControlSTACS {
    private int depot;
    private int n;
    private int m;
    private DoubleMatrix cost_matrix;
    private IntList visited_list;    // lista de nós visitados da colônia
    private IntList candidate_list;  // lista de nós permitidos a uma formiga em um movimento
    private Ant ants[];              // formigas da colônia

    public Colony(int n, int m, DoubleMatrix cost_matrix) {
        this.depot              = DEPOT_INDEX;
        this.n                  = n;
        this.m                  = m;
        this.cost_matrix        = cost_matrix;
        // criando visited e candidate lists:
        this.visited_list       = new IntList(n-1);  // o depósito não é incluído
        this.candidate_list     = new IntList(n-1);  // o nó atual nunca é incluído
        // criando formigas:
        this.ants               = new Ant[m];  // as formigas correspondem aos caixeiros

        for (int k=0; k < m; k++){
            this.ants[k] = new Ant();
            this.ants[k].create(n);
        }
    }

    /**
     * Método que posiciona as formiga em nós inicias aleatórios não repetidos:
     * @param rand
     */
    public void reset_ants_random(Random rand) {
        int raffled;
        int start_node;

        for (int k = 0; k < this.m; k++) {
            boolean flag = true;

            while(flag){
                raffled     = rand.raffle_int(n); // retorna um inteiro aleatório entre 1 e n
                start_node  = raffled - 1;  // os nós são indexados de 0 a n-1

                if (!this.visited_list.on_the_list(start_node)){  // se o nó não estiver na visited_list
                    if (start_node != this.depot)
                        this.visited_list.add(start_node); // adicionando nós iniciais à lista de visitados
                    this.ants[k].reset(start_node);  // reiniciando formiga
                    flag = false;
                }
            }
        }
    }

    /**
     * Método que posiciona todas as formigas no depósito e insere a posição das equipes na visited_list:
     */
    public void reset_ants_depot() {
        int start_node = this.depot;

        for (int k = 0; k < this.m; k++){
            this.ants[k].reset(start_node);  // reiniciando formiga
        }
    }

    public void reset_ants_by_limit(Random rand, int created_sols_counter, int iteration_counter, int non_improved_counter) {
        if (((created_sols_counter == 0) && (iteration_counter == 0)) || ((non_improved_counter != 0) && (non_improved_counter % NO_IMP_CYCLES == 0))) {
            this.reset_ants_random(rand);
        }

        for (int k = 0; k < this.m; k++) {
            int start_node = this.ants[k].starting_node();
            if (!this.visited_list.on_the_list(start_node)) {  // se o nó não estiver na visited_list
                if (start_node != this.depot)
                    this.visited_list.add(start_node); // adicionando nós iniciais à lista de visitados
                this.ants[k].reset(start_node);  // reiniciando formiga
            }
        }
    }

    public void reset_ants_positions_teams(IntList positions_teams) {
        for (int k = 0; k < this.m; k++) {
            int start_node = positions_teams.value(k);
            this.ants[k].reset(start_node);  // reiniciando formiga
            if (start_node != this.depot) {
                if (!this.visited_list.on_the_list(start_node)) {
                    this.visited_list.add(start_node); // adicionando nós iniciais à lista de visitados
                }
            }
        }
    }

    public void add_visited_list_no_valid_nodes(IntList valid_nodes_instance) {
        for (int i = 0; i < n; i++) {
            if (!valid_nodes_instance.on_the_list(i)) {  // se o nó não for válido, inserir na tabu_list
                if (i != this.depot) {
                    if (!this.visited_list.on_the_list(i)) {
                        this.visited_list.add(i);
                    }
                }
            }
        }
    }

    /**
     * Método que esvazia a lista de nós visitados
     */
    public void empty_visited_list() {
        this.visited_list.empty();
    }

    /**
     * Método que verifica se a visited_list está cheia
     * @return
     */
    public boolean is_full_visited_list() {
        if (this.visited_list.is_full())
            return true;
        else
            return false;
    }

    /**
     * Método que atualiza a candidate_list para a formiga em movimento:
     * @param type_solution
     * @param moving_ant
     * @param sorted_lists
     */
    public void update_candidate_list(int type_solution, int moving_ant, IntList sorted_lists[]) {
        this.candidate_list.empty();
        int current_node = this.ants[moving_ant].current_node();
        int aux_node;

        for (int i = 0; i < this.n; i++){

            aux_node = sorted_lists[current_node].value(i);

            if (aux_node == this.depot){
                if (!this.ants[moving_ant].depot_visited()){  // a formiga ainda não visitou o depósito
                    if (type_solution ==  1){  // o depósito pode ser adicionado "no meio" apenas para soluções com rotas fechadas
                        this.candidate_list.add(this.depot);
                    }
                }
            } else {
                if (!this.visited_list.on_the_list(aux_node)){  // aux_node ainda não foi visitado

                    if (aux_node < this.n ){ // erro do dia de trabalho 6 na criação de sorted_lists
                        this.candidate_list.add(aux_node);
                    }

                }
            }
            // limitando a candidate_list pelo parâmetro CL:
            if (this.candidate_list.n_items() == CL_LENGTH) i = n;
        }
    }

    /**
     * Método que movimenta a formiga escolhida para o nó escolhido
     * @param moving_ant
     * @param next_node
     */
    public void movement(int moving_ant, int next_node) {
        int current_node = this.ants[moving_ant].current_node();
        double cost      = this.cost_matrix.get_value(current_node, next_node);
        this.ants[moving_ant].move(next_node, cost);

        if (next_node != this.depot)
            this.visited_list.add(next_node);
    }

    public IntList candidate_list_colony() {
        return this.candidate_list;
    }

    public Ant[] ants_colony() {
        return this.ants;
    }

    /**
     * Método que retorna o custo atual da maior rota entre as formigas
     * @return
     */
    public double longest_route_curr() {
        double l_route = 0.0;

        for (int k = 0; k < this.m; k++){
            if (k == 0){
                l_route = this.ants[k].cost_route();
            }
            else{
                if (this.ants[k].cost_route() > l_route){
                    l_route = this.ants[k].cost_route();
                }
            }
        }

        return l_route;
    }

    /**
     * Método que retorna a soma atual das rotas das formigas
     * @return
     */
    public double sum_route_curr() {
        double t_route = 0.0;

        for (int k = 0; k < this.m; k++){
            t_route = t_route + this.ants[k].cost_route();
        }

        return t_route;
    }

    public void print_visited_list() {
        this.visited_list.print();
    }

    public void print_candidate_list() {
        this.candidate_list.print();
    }

    public void print_sorted_lists(IntList sorted_lists[]) {
        for (int i = 0; i < this.n; i++){
            sorted_lists[i].print();
            System.out.print("\r\n");
        }
    }

    /**
     * Método que imprime em detalhes a lista de nós candidatos:
     * @param moving_ant
     */
    public void print_detail_cands_list(int moving_ant, double pheromone_matrix[][]) {
        if (!this.candidate_list.is_empty()) {
            int current_node = this.ants[moving_ant].current_node();
            System.out.print("\r\nnodes\t");

            for (int i = 0; i < this.candidate_list.n_items(); i++) {
                System.out.print(this.candidate_list.value(i) + "\t");
            }

            System.out.print("\r\ndist\t");
            for (int i = 0; i < this.candidate_list.n_items(); i++) {
                double cost = this.cost_matrix.get_value(current_node, this.candidate_list.value(i));
                System.out.print(cost + "\t");
            }

            System.out.print("\r\npher\t");
            for (int i = 0; i < this.candidate_list.n_items(); i++) {
                System.out.print(pheromone_matrix[current_node][this.candidate_list.value(i)] + "\t");
            }
            System.out.print("\r\n");
        }
        else{
            System.out.print("--> empty candidate list\r\n");
        }
    }
}
