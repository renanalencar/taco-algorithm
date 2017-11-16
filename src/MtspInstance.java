import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class MtspInstance implements ControlExperiment {
    private int depot;                        // indice da matriz de custos que representa o depósito (sempre zero, mas poderia ser alterado)
    private int n_salesmen;                   // número de caixeiros da instância
    private DoubleMatrix cost_matrix;        // matriz assimétrica de custos (os índices representam os nós)
    private IntList valid_nodes;             // lista com os índices de nós validos correntes (apenas os existentes nesta lista farão parte das soluções)
    private IntList initial_nodes_routes;    // lista com os índices de nós correntes nos quais as equipes se encontram, nos dados reais
    private EuclideanGraph graph;            // grafo euclidiano da instância (a partir das coordenadas UTM, nos dados reais)

    public MtspInstance(int n_salesmen, int depot) {
        this.n_salesmen           = n_salesmen;
        this.depot                = depot;
        this.graph                = new EuclideanGraph();
        this.valid_nodes          = new IntList(1);
        this.initial_nodes_routes = new IntList(1);
        this.cost_matrix          = new DoubleMatrix(1);
    }

    public int get_n_nodes() {
        return this.graph.n_nodes_graph();
    }

    public int get_n_salesmen() {
        return this.initial_nodes_routes.n_items();
    }

    public IntList get_positions_teams() {
        return this.initial_nodes_routes;
    }

    public IntList get_valid_nodes_instance() {
        return valid_nodes;
    }

    public DoubleMatrix get_cost_matrix() {
        return cost_matrix;
    }

    public Node[] get_plan_nodes_vector() {
        return this.graph.get_nodes_vector();
    }

    public int get_n_valid_nodes() {
        return valid_nodes.n_items();
    }

    public int get_valid_node(int index) {
        return valid_nodes.value(index);
    }

    public void load_euclidean_model_instance() throws IOException {
        // carregando uma instância modelo em um objeto que representa um grafo euclidiano
        this.graph.load_model_graph();
        int n = this.graph.n_nodes_graph();

        // todos os nós estão disponíveis e são sequenciais nas instâncias modelo
        //delete valid_nodes;
        this.valid_nodes = new IntList(n);
        for (int i = 0; i < n; i++ ) {
            this.valid_nodes.add(i);
        }

        // definindo o tipo da solução que será gerada definindo positions_teams
        //delete initial_nodes_routes;
        this.initial_nodes_routes = new IntList(this.n_salesmen);
        for (int k = 0; k < this.n_salesmen; k++) {
            if (TYPE_MTSP_SOLS == 1){
                this.initial_nodes_routes.add(depot); // rotas fechadas, todas partem do depósito
            } else {
                this.initial_nodes_routes.add(k+1);   // rotas abertas com finais fixos
            }
        }

        // preenchendo a matriz de custos com as distância euclidianas
        this.cost_matrix = new DoubleMatrix(n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++ ) {
                double cost = this.graph.get_value_euclidean_matrix(i,j);
                this.cost_matrix.set_value(i,j,cost);
            }
        }
    }

    public void create_empty_instance(int n_nodes, int n_teams) {
        this.graph.create_empty_graph(n_nodes);

        //delete valid_nodes;
        this.valid_nodes = new IntList(n_nodes);

        //delete initial_nodes_routes;
        this.initial_nodes_routes = new IntList(n_teams);

        //delete cost_matrix;
        this.cost_matrix = new DoubleMatrix(n_nodes);
    }

    public void add_plan_node(int coord_x, int coord_y) {
        this.graph.add_vertex(coord_x, coord_y);
    }

    public void calcule_euclidean_matrix() {
        this.graph.calcule_euclidean_distances_div1000();
    }

    public double get_value_euclidean_matrix(int i, int j) {
        return this.graph.get_value_euclidean_matrix(i,j);
    }

    public double get_value_cost_matrix(int i, int j) {
        return this.cost_matrix.get_value(i,j);
    }

    public void set_value_cost_matrix(int i, int j, double cost) {
        this.cost_matrix.set_value(i, j, cost);
    }

    public void reset_valid_nodes_instance() {
        this.valid_nodes.empty();
    }

    public void add_valid_node(int node) {
        this.valid_nodes.add(node);
    }

    public void reset_positions_teams() {
        this.initial_nodes_routes.empty();
    }

    public void add_position_team(int position) {
        this.initial_nodes_routes.add(position);
    }

    public void print_instance() {
        System.out.print("\r\nCurrent instance:\r\n");
        System.out.print("  valid nodes: ");
        this.valid_nodes.print();

        System.out.print("\r\n");
        System.out.print("  positions_teams: ");
        this.initial_nodes_routes.print();
        System.out.print("\r\n");
    }

    public void print_valid_nodes() {
        System.out.print("Valid nodes: ");
        this.valid_nodes.print();
        System.out.print("\r\n");
    }

    public void print_positions_teams() {
        System.out.print("Posições das equipes: ");
        this.initial_nodes_routes.print();
        System.out.print("\r\n");
    }

    public void print_cost_matrix() {
        this.cost_matrix.print_matrix();
    }

    public void print_euclidean_matrix() {
        this.graph.print_euclidean_matrix();
    }

    public void save_positions_teams(BufferedWriter file_out) throws IOException {
        file_out.write("Posições das equipes: : ");
        initial_nodes_routes.save(file_out);
        file_out.write("\r\n");
    }
}
