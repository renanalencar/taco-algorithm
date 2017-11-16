/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class MtspNearestNeighbor implements ControlSTACS, ControlExperiment {
    private int n;
    private int m;
    private int depot;
    private Route routes[];
    private IntList visited_list;
    private MtspSolution nn_solution;
    private DoubleMatrix cost_matrix;

    public MtspNearestNeighbor(int n_nodes, int m_salesmen) {
        this.n      = n_nodes;
        this.m      = m_salesmen;
        this.depot  = DEPOT_INDEX;

        this.routes = new Route[this.m];
        for (int k = 0; k < this.m; k++) {
            this.routes[k] = new Route();
            this.routes[k].create(n + 1);  // para m=1 com retorno ao depósito
        }

        this.visited_list = new IntList(n);
        this.nn_solution  = new MtspSolution(n, m);
    }

    public void create_solution_workload_balance(DoubleMatrix cost_matrix, IntList valid_nodes_instance) {
        this.cost_matrix = cost_matrix;
        // todos os caixeiros partem do depósito:
        for (int k=0; k<m; k++){
            routes[k].reset();
            routes[k].add_node(depot, 0.0);
        }
        visited_list.empty();
        visited_list.add(depot);

        // adicionando nós inválidos à visited list:
        for (int i=0; i<n; i++) {
            if (!valid_nodes_instance.on_the_list(i)) {  // se o nó não for válido, inserir na tabu_list
                if (i != depot) {
                    if (!visited_list.on_the_list(i)) {
                        visited_list.add(i);
                    }
                }
            }
        }

        // criar as rotas dos caixeiros:
        while (!visited_list.is_full()){
            // selecionar o caixeiro que tem o menor custo parcial:
            int selected_salesman = 0;
            double less_cost = 0.0;
            for (int k = 0; k < m; k++){
                if (k == 0) {
                    selected_salesman = k;
                    less_cost = routes[k].cost();
                }
                else{
                    if (routes[k].cost() < less_cost) {
                        selected_salesman = k;
                        less_cost = routes[k].cost();
                    }
                }
            }
            // localizar o nó mais próximo do nó atual do caixeiro selecionado,
            // que ainda não tenha sido visitado
            int current = routes[selected_salesman].last_node();
            int selected_node = 0;
            double less_dist = 0.0;
            boolean first = true;
            for (int i=0; i<n; i++){
                if (!visited_list.on_the_list(i)){
                    double curr_cost = cost_matrix.get_value(current,i);
                    if(first) {
                        selected_node = i;
                        less_dist = curr_cost;
                        first = false;
                    }
                    else{
                        if (curr_cost < less_dist){
                            selected_node = i;
                            less_dist = curr_cost;
                        }
                    }
                }
            }
            routes[selected_salesman].add_node(selected_node, less_dist);
            visited_list.add(selected_node);
        }
        // criar a solução:
        nn_solution.reset();
        double cost = 0.0;
        for(int k=0; k<m; k++){
            for (int i=0; i < routes[k].n_nodes(); i++){
                if ((k==0)&&(i==0)) {
                    nn_solution.add(depot, 0.0);
                }
                else{
                    int last_node = nn_solution.last_node();
                    int new_node = routes[k].node(i);
                    cost = cost_matrix.get_value(last_node,new_node);
                    nn_solution.add(new_node, cost);
                }
            }
        }
        // adicionando o retorno ao depósito às rotas e à solução:
        for(int k=0; k<m; k++){
            int last_node = routes[k].last_node();
            cost = cost_matrix.get_value(last_node,depot);
            routes[k].add_node(depot, cost);
        }
        int last_node = nn_solution.last_node();
        cost = cost_matrix.get_value(last_node,depot);
        nn_solution.add(depot, cost);

        // salvando a longest_route na solução:
        nn_solution.set_longest_route(this.larger_route_sol());
    }

    public void create_solution_one_by_one(DoubleMatrix cost_matrix, IntList valid_nodes_instance) {
        this.cost_matrix = cost_matrix;
        // todos os caixeiros partem do depósito:
        for (int k=0; k<m; k++){
            routes[k].reset();
            routes[k].add_node(depot, 0.0);
        }
        visited_list.empty();
        visited_list.add(depot);

        // adicionando nós inválidos à visited list:
        for (int i=0; i<n; i++){
            if (!valid_nodes_instance.on_the_list(i)){  // se o nó não for válido, inserir na tabu_list
                if (i != depot){
                    if (!visited_list.on_the_list(i)){
                        visited_list.add(i);
                    }
                }
            }
        }

        int points_by_salesman = valid_nodes_instance.n_items() / m;
        int counter_points = 0;
        int selected_salesman = 0;

        // criar as rotas dos caixeiros:
        while (!visited_list.is_full()){

            if (counter_points > points_by_salesman){
                selected_salesman++;
                if (selected_salesman >= m){
                    selected_salesman--;
                }
                else {
                    counter_points = 0;
                }
            }
            counter_points++;

            // localizar o nó mais próximo do nó atual do caixeiro selecionado,
            // que ainda não tenha sido visitado
            int current = routes[selected_salesman].last_node();
            int selected_node = 0;
            double less_dist = 0.0;
            boolean first = true;
            for (int i=0; i<n; i++){
                if ((!visited_list.on_the_list(i)) && (i!=depot)){
                    double curr_cost = cost_matrix.get_value(current,i);
                    if(first){
                        selected_node = i;
                        less_dist = curr_cost;
                        first = false;
                    }
                    else{
                        if (curr_cost < less_dist){
                            selected_node = i;
                            less_dist = curr_cost;
                        }
                    }
                }
            }
            routes[selected_salesman].add_node(selected_node, less_dist);
            visited_list.add(selected_node);
        }

        // criar a solução a partir das rotas:
        nn_solution.reset();
        double cost = 0.0;
        for(int k=0; k<m; k++){
            for (int i=0; i < routes[k].n_nodes(); i++){
                if ((k==0)&&(i==0)) {
                    nn_solution.add(depot, 0.0);
                }
                else{
                    int last_node = nn_solution.last_node();
                    int new_node = routes[k].node(i);
                    cost = cost_matrix.get_value(last_node,new_node);
                    nn_solution.add(new_node, cost);
                }
            }
        }
        // adicionando o retorno ao depósito às rotas e à solução:
        for(int k=0; k<m; k++){
            int last_node = routes[k].last_node();
            cost = cost_matrix.get_value(last_node,depot);
            routes[k].add_node(depot, cost);
        }
        int last_node = nn_solution.last_node();
        cost = cost_matrix.get_value(last_node,depot);
        nn_solution.add(depot, cost);

        // salvando a longest_route na solução:
        nn_solution.set_longest_route(this.larger_route_sol());
    }

    // retorna a maior rota dentre todos os caixeiros da solução
    public double larger_route_sol() {
        double l_route = 0.0;
        for (int k=0; k<m; k++){
            if (k==0){
                l_route = routes[k].cost();
            }
            else{
                if (routes[k].cost() > l_route){
                    l_route = routes[k].cost();
                }
            }
        }
        return l_route;
    }

    // retorna o total da solução (soma das rotas)
    public double total_sol() {
        double t_sol = 0.0;
        for (int k=0; k<m; k++){
            t_sol = t_sol + routes[k].cost();
        }
        return t_sol;
    }

    public MtspSolution get_nn_solution() {
        return nn_solution;
    }
}
