import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class Route {
    private int max_size;
    private IntList route_list;
    private double total_cost;
    private double temporal_cost; // apenas em experimentos com dados reais

    public Route() {

    }

    public Route(int size) {
        this.max_size       = size;
        this.route_list     = new IntList(this.max_size);
        this.total_cost     = 0.0;
        this.temporal_cost  = 0.0;
    }

    public void create(int size) {
        this.max_size       = size;
        this.route_list     = new IntList(this.max_size);
        this.total_cost     = 0.0;
        this.temporal_cost  = 0.0;
    }

    public void reset() {
        this.route_list.empty();
        this.total_cost     = 0.0;
        this.temporal_cost  = 0.0;
    }

    public void add_node(int node, double cost) {
        this.route_list.add(node);
        this.total_cost = this.total_cost + cost;
    }

    /**
     * Este método ordena a rota para começar e terminar em init_node
     * @param init_node Nó inicial
     * @param type_solution Tipo de solução
     */
    public void sort(int init_node, int type_solution) {
        int n_nodes     = this.route_list.n_items();
        int old_route[] = new int[n_nodes];

        for (int i = 0; i < n_nodes; i++) { // copiando a rota para old_route
            old_route[i] = this.route_list.value(i);
        }

        this.route_list.empty(); // resetando a rota
        int i_init_route    = 0; // indica a posição do depósito na rota original
        boolean flag        = false;

        for (int i = 0; i < n_nodes; i++) {
            if (flag) {
                this.route_list.add(old_route[i]);
            } else {
                if (old_route[i] == init_node) { // encontrou o depósito
                    this.route_list.add(old_route[i]); // o depósito é o primeiro da rota
                    i_init_route = i; // grava a posição do depósito
                    flag = true; // inicia gravação dos outros nós
                }
            }
        }

        // copiando os nós do início da rota:
        int ind = 0;
        if (type_solution == 1)
            ind  = 1;
        if (type_solution == 2)
            ind = 0;
        for (int i = ind; i < i_init_route; i++) {
            this.route_list.add(old_route[i]);
        }

        // para rotas que já estiverem ordenadas:
        if (type_solution == 1) {
            if (old_route[0] != init_node)
                this.route_list.add(init_node);
        }

        //delete [] old_route;
    }

    public double cost() {
        return this.total_cost;
    }

    public int last_node() {
        return this.route_list.last_value();
    }

    public int n_nodes() {
        return this.route_list.n_items();
    }

    public int node(int ind) {
        return this.route_list.value(ind);
    }

    public boolean is_empty() {
        if (this.route_list.is_empty())
            return true;
        else
            return false;
    }

    /**
     * Este método é o polimorfismo específico para experimento com dados reais
     * @param node
     * @param cost
     * @param temp_cost
     */
    public void add_node(int node, double cost, double temp_cost) {
        this.route_list.add(node);
        this.total_cost = this.total_cost + cost;
        this.temporal_cost = this.temporal_cost +temp_cost;
    }

    public void add_to_temporal_cost(double time_cost) {
        this.temporal_cost = this.temporal_cost + time_cost;
    }

    double get_temporal_cost() {
        return temporal_cost;
    }

    public void print() {
        for (int i = 0; i <this.route_list.n_items(); i++) {
            System.out.print(i+1 + "\t");
            System.out.print(this.route_list.value(i) + "\r\n");
        }
        System.out.println("---> cost route: " + this.total_cost + "\r\n");
    }

    public void print_short() {
        int size_route = this.route_list.n_items();
        System.out.print("\tpontos visitados: " + size_route);
        System.out.print("\tcusto total: " + this.total_cost);
        System.out.print("\tcusto temporal: " + this.temporal_cost);
        System.out.print("\trota: ");
        this.route_list.print();
        System.out.print("\r\n");
    }

    public void save_short(BufferedWriter file_out) throws IOException {
        int size_route = this.route_list.n_items();
        file_out.write("\tpontos visitados: " + size_route);
        file_out.write("\tcusto total: " + this.total_cost);
        file_out.write("\tcusto temporal: " + this.temporal_cost);
        file_out.write("\t\trota: ");
        this.route_list.save(file_out);
        file_out.write("\r\n");
    }

}
