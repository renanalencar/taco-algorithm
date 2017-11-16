/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class Ant implements ControlExperiment, ControlSTACS {
    private int depot; // nó de início da rota da formiga
    private int start_node; // nó em que a formiga se encontra
    private int curr_node; // a formiga já incluiu o depósito na sua rota?
    private boolean dep_visit; // rota executada pela formiga
    private Route route;

    public Ant() {

    }

    public void create(int n) {
        this.depot = DEPOT_INDEX;
        route = new Route(n+1); // tamanho máximo para m=1 com retorno ao depósito
    }

    public int starting_node() {
        return this.start_node;
    }

    public int current_node() {
        return this.curr_node;
    }

    public boolean depot_visited() {
        return this.dep_visit;
    }

    public double cost_route() {
        return this.route.cost();
    }

    int size_route() {
        return this.route.n_nodes();
    }

    int node_route(int index) {
        return this.route.node(index);
    }

    void reset (int starting_node) {
        this.start_node = starting_node;
        this.curr_node = this.start_node;
        this.dep_visit = false;

        if(this.start_node == this.depot) this.dep_visit = true; // o depósito pode ser o nó de partida
        this.route.reset(); // reiniciando a rota da formiga
        this.route.add_node(this.start_node, 0.0); // adicionando nó inicial
    }

    void move (int next_node, double cost) {
        this.curr_node = next_node;
        if(next_node == this.depot) this.dep_visit = true; // verificando se a formiga está se movendo para o depósito
        this.route.add_node(next_node, cost);
    }

    void sort_route (int type_solution) {
        this.route.sort(this.depot, type_solution);
    }

    void print_route() {
        this.route.print_short();
    }

    void print_ant(int id_ant) {
        System.out.print("ant: " + id_ant);
        System.out.print(" n_star: " + this.start_node);
        System.out.print(" n_cur: " + this.curr_node);
        System.out.println(" d_vis: " + this.dep_visit);
    }

}
