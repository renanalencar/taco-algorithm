import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class MtspSolution implements ControlExperiment {
    private int depot;
    private int size;                  // número de nós da solução
    private IntList nodes_list;       // lista com os nós da solução ordenados
    private double total_cost;         // custo da soma das rotas
    private double longest_route_cost; // custo da maior rota
    private int iteration;             // ciclo em que a solução foi gerada
    private double time_exec;          // tempo de execução, em milissegundos
    private long random_seed;          // semente dos randômicos utilizada para criar a solução

    public MtspSolution(int n, int m) {
        this.depot              = DEPOT_INDEX;
        this.size               = n + (m * 2);  // tamanho máximo para soluções com finais fechados (para o caso de posições atuais iguais)
        this.nodes_list         = new IntList(size);
        this.total_cost         = 0.0;
        this.longest_route_cost = 0.0;
        this.iteration          = -1;
        this.time_exec          = -1;
        this.random_seed        = -1;
    }

    public MtspSolution(int size) {
        this.depot              = DEPOT_INDEX;
        this.size               = size;
        this.nodes_list         = new IntList(size);
        this.total_cost         = 0.0;
        this.longest_route_cost = 0.0;
        this.iteration          = -1;
        this.time_exec          = -1;
        this.random_seed        = -1;
    }

    public void reset() {
        this.nodes_list.empty();
        this.total_cost         = 0.0;
        this.longest_route_cost = 0.0;
        this.iteration          = -1;
        this.time_exec          = -1;
        this.random_seed        = -1;
    }
    public void add(int node, double cost) {
        this.nodes_list.add(node);
        this.total_cost = this.total_cost + cost;
    }

    /**
     * Método que inverte a solução a partir de index_start (inclusive) a index_end (inclusive) e a reacalcula
     * @param index_start
     * @param index_end
     * @param cost_matrix
     */
    //TODO Altera uma variável externa
    public void reverse(int index_start, int index_end,  DoubleMatrix cost_matrix) {
        nodes_list.reverse(index_start, index_end);  // reversão sem considerar nós depósitos
//-----        nodes_list->reverse_no_depot(index_start, index_end);  // reversão mantendo os depositos em suas posições na solução
        // recalculando a solução:
        this.recalculate_solution(cost_matrix);
    }

    /** troca os nós das posições enviadas e recalcula a solução
     * Método que
     * @param index_i
     * @param index_j
     * @param cost_matrix
     */
    //TODO Altera uma variável externa
    public void change_nodes(int index_i, int index_j, DoubleMatrix cost_matrix) {
        // alterando os nós na lista de nós:
        this.nodes_list.change_values(index_i, index_j);

        if ((index_i == this.depot) || (index_j == this.depot)) {  // a troca envolveu o depósito: reorganizar a solução
//-----            cout << "solution a reorg:\t\t\t\t\t"; this->print();
            IntList copy_values = new IntList();
            copy_values.create(size);
            boolean flag = false;
            int index_first_depot = 0;

            for (int i = 0; i < size-1; i++) {
                if ((this.nodes_list.value(i) == this.depot) && (!flag)) {
                    index_first_depot = i;
                    flag = true;
                }
                if (flag)
                    copy_values.add(this.nodes_list.value(i));
            }

            for (int i = 0; i < index_first_depot; i++)
                copy_values.add(this.nodes_list.value(i));
            //----- copy_values->add(depot);

            //delete nodes_list;
            this.nodes_list = copy_values;
        }
        // recalculando a solução:
        this.recalculate_solution(cost_matrix);
    }

    /**
     * Método que atualiza total_cost e longest_route_cost
     * @param cost_matrix
     */
    //TODO Altera uma variável externa
    public void recalculate_solution(DoubleMatrix cost_matrix) {
        this.total_cost = 0.0;
        this.longest_route_cost = 0.0;

        double current_route_cost = 0.0;  // armazena o custo da rota que está sendo calculada

        int size_solution = this.nodes_list.n_items();
        for (int i=1; i < size_solution; i++) {       // começa do segundo nó da lista
            int node_i          = this.nodes_list.value(i);
            int node_j          = this.nodes_list.value(i-1);
            double current_cost = cost_matrix.get_value(node_i, node_j);
            this.total_cost     = this.total_cost + current_cost;

            current_route_cost  = current_route_cost + current_cost;
            if (this.nodes_list.value(i) == this.depot){  // concluiu uma rota
                if ((this.longest_route_cost == 0.0) || (current_route_cost > this.longest_route_cost)) {
                    this.longest_route_cost = current_route_cost;
                }
                current_route_cost = 0.0;  // resetando o custo da rota atual
            }
        }
    }

    /**
     * Método que retorna verdadeiro se a solução for válida
     * @return
     */
    public boolean test_validate() {
        for (int i = 1; i < this.nodes_list.n_items(); i++) {
            if (this.nodes_list.value(i) == this.depot) {
                if (this.nodes_list.value(i-1) == this.depot) {
                    return false;
                }
            }
        }
        return true;
    }

    public int n_nodes() {
        return this.nodes_list.n_items();
    }

    public int node(int ind) {
        return this.nodes_list.value(ind);
    }

    public int last_node() {
        return this.nodes_list.last_value();
    }

    public boolean is_full() {
        if (this.nodes_list.is_full())
            return true;
        else
            return false;
    }

    public boolean is_empty() {
        if (this.nodes_list.is_empty())
            return true;
        else
            return false;
    }

    public void set_longest_route(double cost) {
        this.longest_route_cost = cost;
    }

    public void set_iteration(int c) {
        this.iteration = c;
    }

    public void set_time(double t) {
        this.time_exec = t;
    }

    public void set_random_seed(long s) {
        this.random_seed = s;
    }

    public void set_total_cost(double t_cost) {
        this.total_cost = t_cost; // apenas para cópia de soluções
    }

    public double get_total_cost() {
        return this.total_cost;
    }

    public double get_longest_route() {
        return this.longest_route_cost;
    }

    public int get_iteration_sol() {
        return this.iteration;
    }

    public int get_time_sol() {
        return (int) this.time_exec;
    }

    public long get_seed_rand() {
        return this.random_seed;
    }

    public int next_node(int current_node) {
        return this.nodes_list.next_value(current_node);
    }

    //TODO Abrir e Salvar em arquivo
    public void save_how_list(BufferedWriter file_out) throws IOException {
        file_out.write("maior rota: " + String.format("%."+FLOAT_PRECISION+"f", this.longest_route_cost) + "\tcusto total: " + String.format("%."+FLOAT_PRECISION+"f", this.total_cost) + "  \tsolução: ");
        nodes_list.save(file_out);
        file_out.write("\r\n");
    }

    public void print() {
        System.out.print("maior rota: " + String.format("%."+FLOAT_PRECISION+"f", this.longest_route_cost) + "\tcusto total: " + String.format("%."+FLOAT_PRECISION+"f", this.total_cost) + "  \tsolução:");
//-----        nodes_list->print_add1();
        this.nodes_list.print();
        System.out.print("\r\n");
    }

    //TODO Verificar salvamento
    public void save_to_plot(BufferedWriter file_out, Node nodes[]) throws IOException {
        //TODO Configurar precisão do float
        //file_out << setiosflags (ios::fixed) << setprecision(0);
        if (this.random_seed != -1)
            file_out.write("Semente randômica: " + this.random_seed + "\r\n");
        for (int i = 0; i < this.nodes_list.n_items(); i++) {
            file_out.write(nodes_list.value(i) + "\t");
            file_out.write(nodes[nodes_list.value(i)].x() + "\t");
            file_out.write(String.valueOf(nodes[nodes_list.value(i)].y()));
            file_out.write("\r\n");
        }

    }

    //TODO Verificar salvamento
    public void save_longest_cost(BufferedWriter file_out) throws IOException {
        file_out.write(String.format("%."+FLOAT_PRECISION+"f", this.longest_route_cost) + "\t");
    }

    //TODO Verificar salvamento
    public void save_total_cost(BufferedWriter file_out) throws IOException {
        file_out.write(String.format("%."+FLOAT_PRECISION+"f", this.total_cost) + "\t");
    }
}