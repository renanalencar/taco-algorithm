/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class AcsAlgorithm implements ControlSTACS, ControlExperiment {
    // variáveis do ambiente:
    private int depot;
    private int n;                      // número de nós da instância
    private int m;                      // número de caixeiros
    private DoubleMatrix cost_matrix;       // ponteiro para a matriz de distâncias
    private DoubleMatrix pheromone_matrix;  // ponteiro para a matriz de feromônio

    // variáveis locais:
    private double tau0;                // qtde inicial de feromônio
    private double Cnn;                 // custo da solução nearest neighbor (total ou longest, de acordo com o objetivo)
    //private Utilities util;            // funções auxiliares
    private double probability[];        // vetor com os numeradores: tau^alfa * eta^beta

    public AcsAlgorithm(int n, int m, DoubleMatrix cost_matrix, DoubleMatrix pheromone_matrix) {
        this.depot = DEPOT_INDEX;
        this.n = n;
        this.m = m;
        this.cost_matrix = cost_matrix;
        this.pheromone_matrix = pheromone_matrix;
        //util = new Utilities();
        this.probability = new double[n - 1];  // vetor com os numeradores: tau^alfa * eta^beta
    }

    /**
     * Método que inicializa a matriz de feromônio
     * @param valid_nodes_instance
     */
    public void initialize_pheromone_matrix(IntList valid_nodes_instance) {
        // criando a solução do vizinho mais próximo:
        MtspNearestNeighbor nn_app = new MtspNearestNeighbor(this.n, this.m); // o depósito é o nó zero do vetor de vértices
        nn_app.create_solution_workload_balance(this.cost_matrix, valid_nodes_instance);

        if (APP_OBJECTIVE == 1){
            this.Cnn = nn_app.larger_route_sol();
        }  // minimizar a longest_route
        else {
            this.Cnn = nn_app.total_sol();
        }  // minimizar a soluçao total
        //delete nn_app;

        this.tau0 = 1/(this.n*this.Cnn);  // dorigo04, pg 71

        for (int i=0; i < this.n; i++){
            for (int j=0; j < this.n; j++){
                this.pheromone_matrix.set_value(i,j,this.tau0);
            }
        }
    }

    /**
     * Método que retorna o nó escolhido, recebe o nó atual da formiga
     * @param current_node
     * @param candidate_list
     * @param rand
     * @return
     */
    public int state_transation_rule(int current_node, IntList candidate_list, Random rand) {
        int choosed_node = -1;  // nó escolhido
        int n_candidates = candidate_list.n_items(); // qtde de nós candidatos

        // definindo a probabilidade para todos os nós candidatos:
        for (int i = 0; i < n_candidates; i++) {  // índice da candidate_list e probability
            int candidate_node = candidate_list.value(i); // nó candidato
            double trail = this.pheromone_matrix.get_value(current_node,candidate_node);
            double cost = this.cost_matrix.get_value(current_node,candidate_node);

            // para valores 0.0 na matriz de custos:
            if (cost == 0.0)
                cost = 0.00000000000000000001;

            double visibility = 1 / cost;
            this.probability[i] = Math.pow(trail, ALFA) * Math.pow(visibility, BETA);

//            if (probability[i] > 9999999999){
//                cout << "\r\n\r\n---->>>> PROBABILIDADE INFINITA\r\n";
//                cout << "vertice atual: " << current_node << "\r\n";
//                cout << "vertice candidato: " << candidate_node << "\r\n";
//                cout << "feromonio: " << trail << "\r\n";
//                cout << "custo: " << visibility << "\r\n";
//                cout << "visibilidade: " << visibility << "\r\n";
//            }

        }

        // pseudo random proportional rule:
        double q = rand.random_number();
        if (q <= Q0) {
            int larger_prob = Utilities.argmax(n_candidates, this.probability);   // nó com maior probabilidade
            choosed_node = candidate_list.value(larger_prob);
        } else {
            int raffled_prob = rand.raffle_double_vector(n_candidates, this.probability); // nó sorteado, de acordo com as probabilidades

            try{

                if ((raffled_prob < 0) || (raffled_prob >= n_candidates)) {  // ERRO NO SORTEIO
                    throw new Exception();
                }
                else {  // sorteio OK, procedimento normal
                    choosed_node = candidate_list.value(raffled_prob);
                }

            }catch (Exception e) {
                System.out.print("\r\n\r\n---->>>> ERRO NO SORTEIO\r\n");
                System.out.print("vertice atual: " + current_node + "\r\n");
                System.out.print("lista de candidatos: ");
                candidate_list.print();
                System.out.print("\r\n");
                System.out.print("probabilidades: \r\n");

                for (int aux = 0; aux < n_candidates; aux++){
                    System.out.print(aux + "\t" + this.probability[aux] + "\r\n");
                }

                System.out.print("\r\nmatriz de custos: ");
                this.cost_matrix.print_matrix();
                System.out.print("\r\n");

                System.out.print("\r\nmatriz de feromonio: ");
                this.pheromone_matrix.print_matrix();
                System.out.print("\r\n");

                //exit(4);
            }

        }
        return choosed_node;
    }

    /**
     * Método que atualização de feromônio realizada logo após a formiga se mover para um novo nó
     * @param i
     * @param j
     */
    public void local_pheromone_update(int i, int j) {
        double current_tau = this.pheromone_matrix.get_value(i,j);
        double new_tau     = ((1-KSI)*current_tau) + (KSI*this.tau0);  // fórmula de atualização local do ACS

        if (!((GPUNODEP == 1) && ((i == this.depot) || (j == this.depot)))) { // verificando se está sendo depositado feromônio nas arestas que conectam ao depósito
            this.pheromone_matrix.set_value(i,j,new_tau);
        }

        if(PPLU == 1) {
            System.out.print("\r\nlocal update:   ");
            this.print_pheromone_matrix();
        }
    }

    /**
     * Método que atualização de feromônio realizada ao final de cada ciclo (N soluções no ACS)
     * @param best_so_far_sol
     */
    public void global_pheromone_update(MtspSolution best_so_far_sol) {
        int n_nodes          = best_so_far_sol.n_nodes();
        double longest_route = best_so_far_sol.get_longest_route();
        double total_sol     = best_so_far_sol.get_total_cost();

        double best_so_far;

        if (APP_OBJECTIVE == 1){  // atualizar pela longest_route
            best_so_far = longest_route;
        }
        else{  // atualizar pelo total da solução
            best_so_far = total_sol;
        }

        double delta_tau = 1 / (this.m * best_so_far);  // fórmula da atualização global do ACS (1)

        for (int i = 1; i < n_nodes; i++) {
            int a = best_so_far_sol.node(i-1);
            int b = best_so_far_sol.node(i);
            double current_tau = this.pheromone_matrix.get_value(a,b);

            double new_tau = ((1-RO)*current_tau) + (RO*delta_tau); // fórmula da atualização global do ACS (2)

            if (!((GPUNODEP == 1) && ((a == this.depot) || (b == this.depot)))){
//-----                cout << "a = " << a << "   b = " << b << "\r\n";
                this.pheromone_matrix.set_value(a,b,new_tau);
            }
        }

        if(PPGU == 1) {
            System.out.print("\r\n---> best_so_far_sol:   ");
            best_so_far_sol.print();

            System.out.print("\r\n---> global update:   ");
            this.print_pheromone_matrix();
        }
    }

    public double cnn() {
        return Cnn;
    }

    public void print_pheromone_matrix() {
        System.out.print("pheromone_matrix (porcentagem de tau0)     tau0: " + String.format("%.10f",tau0) + "\r\n");
        for (int i = 0; i < n; i++) {
            System.out.print("\t" + i);
        }

        for (int i = 0; i < n; i++) {
            System.out.print("\r\n" + i);
            for (int j=0; j < n; j++){
                double current_pher = pheromone_matrix.get_value(i,j);
                if(current_pher == tau0){
                    System.out.print("\t =");
                }
                else{
                    System.out.print("\t" + (current_pher / tau0) * 100 + "%");
                }
            }
        }
        System.out.print("\r\n");
        //cout << setprecision(prec);
    }

}
