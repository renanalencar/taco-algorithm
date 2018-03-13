import java.io.*;
import java.util.Vector;

/**
 * Classe que representa o grafo
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class EuclideanGraph implements ControlExperiment, ControlSTACS {
    private Node plan_nodes_vector[];       // vetor de vértices planos (representados por coordenadas)
    private DoubleMatrix euclidean_matrix;  // matriz de distâncias euclidianas

    private int n_nodes;                    // numero de nós do grafo
    private int index_last_nodes_vector;    // aponta o ultimo vértice inserido no grafo (vetor de vértices)
    private BufferedReader file_load;       // para carregar a instância de arquivo
    private String file_name;               // nome do arquivo da instância

    // criando apenas estrutura, com tamanho mínimo

    /**
     * Método que
     */
    public EuclideanGraph() {
        this.index_last_nodes_vector    = -1;  // vetor de nos vazio
        this.n_nodes                    = 1;

        this.plan_nodes_vector          = new Node[this.n_nodes];
        for (int i = 0; i < this.n_nodes; i++) {
            this.plan_nodes_vector[i] = new Node();
        }

        this.euclidean_matrix           = new DoubleMatrix(this.n_nodes);
    }

    public void load_model_graph() throws IOException {
        switch (MODEL_INSTANCE){

            case 0: {  // grids para testes
                int grid_dim = 3;  // dimensão do grid
                this.n_nodes = grid_dim * grid_dim;
                this.index_last_nodes_vector = -1;
                //delete [] plan_nodes_vector;
                this.plan_nodes_vector = new Node[this.n_nodes];
                for (int i = 0; i < this.n_nodes; i++) {
                    this.plan_nodes_vector[i] = new Node();
                }
                //delete euclidean_matrix;  // criando matriz de distâncias
                this.euclidean_matrix = new DoubleMatrix(this.n_nodes);

                for (int i = 0; i < grid_dim; i++) { // criando os nós do grafo
                    for (int j = 0; j < grid_dim; j++) {
                        if (this.index_last_nodes_vector < this.n_nodes) {
                            this.add_vertex(i, j);
                        }
                    }
                }

                this.calcule_euclidean_distances();  // calculando a matriz de distâncias
                break;
            }

            case 1: {  // instância Eil51 da TSPLIB
                if (TEST == -1)
                    this.n_nodes = 51;
                else
                    this.n_nodes = TEST;

                this.file_name = "instances/eil51.txt";
                break;
            }

            case 2: {  // instância Eil76 da TSPLIB
                if (TEST == -1)
                    this.n_nodes = 76;
                else
                    this.n_nodes = TEST;

                this.file_name = "instances/eil76.txt";
                break;
            }

            case 3: {  // instância Eil101 da TSPLIB
                if (TEST == -1)
                    this.n_nodes = 101;
                else
                    this.n_nodes = TEST;

                this.file_name = "instances/eil101.txt";
                break;
            }

            case 4: {  // instância pr76 da TSPLIB
                if (TEST == -1)
                    this.n_nodes = 76;
                else
                    this.n_nodes = TEST;

                this.file_name = "instances/pr76.txt";
                break;
            }

            case 5: {  // instância pr1002 da TSPLIB
                if (TEST == -1)
                    this.n_nodes = 1002;
                else
                    this.n_nodes = TEST;

                this.file_name = "instances/pr1002.txt";
                break;
            }

            case 6: {  // instância sgb128
                if (TEST == -1)
                    this.n_nodes = 17;
                else
                    this.n_nodes = TEST;

                this.file_name = "instances/sgb128.txt";
                break;
            }
        }

        if (MODEL_INSTANCE > 0) { // excluindo grids de testes
            this.index_last_nodes_vector = -1;
            //delete [] plan_nodes_vector;
            this.plan_nodes_vector = new Node[this.n_nodes];
            for (int i = 0; i < this.n_nodes; i++) {
                this.plan_nodes_vector[i] = new Node();
            }
            //delete euclidean_matrix;  // criando matriz de distâncias
            this.euclidean_matrix = new DoubleMatrix(this.n_nodes);

            // carregando instância do arquivo:
            FileInputStream fstream = new FileInputStream(this.file_name);
            DataInputStream in      = new DataInputStream(fstream);
            this.file_load          = new BufferedReader(new InputStreamReader(in));

            String line;
            int vertex = 0;
            double cx  = 0.0;
            double cy  = 0.0;

            //while ((line = br.readLine()) != null)
            for (int i = 0; i < n_nodes; i++) {
                line = file_load.readLine();
                String l = trim(line);

                Vector<String> _line = split(l, " ");

                vertex = Integer.parseInt(_line.elementAt(0));
                cx = Double.parseDouble(_line.elementAt(1));
                cy = Double.parseDouble(_line.elementAt(2));
                this.add_vertex(cx, cy);
            }

            file_load.close();
            this.calcule_euclidean_distances();  // calculando a matriz de distâncias euclidianas
        }
    }

    private String trim(String s) {
        return s.trim();
    }

    private Vector<String> split (final String input, final String regex) {
        String[] words = input.split(regex);
        Vector<String> result = new Vector<>();
        for (String s : words) {
            result.add(s);
        }

        return result;
    }

    public void create_empty_graph(int n_nodes) {
        this.n_nodes = n_nodes;
        this.index_last_nodes_vector = -1;
        //delete [] plan_nodes_vector;
        this.plan_nodes_vector = new Node[n_nodes];
        for (int i = 0; i < this.n_nodes; i++) {
            this.plan_nodes_vector[i] = new Node();
        }
        //delete euclidean_matrix;  // criando matriz de distâncias
        this.euclidean_matrix = new DoubleMatrix(n_nodes);
    }

    public void add_vertex(double x, double y) {
        this.index_last_nodes_vector++;
        this.plan_nodes_vector[this.index_last_nodes_vector].create(x, y);
    }

    /**
     * Método que preenche a matriz de distâncias
     */
    public void calcule_euclidean_distances() {
        double aux1;
        double aux2;
        double dist;

        for (int i = 0; i < this.n_nodes; i++) {
            for (int j = 0; j < this.n_nodes; j++) {
                aux1 = this.plan_nodes_vector[i].x() - this.plan_nodes_vector[j].x();
                aux2 = this.plan_nodes_vector[i].y() - this.plan_nodes_vector[j].y();
                dist = Math.sqrt(Math.pow(aux1,2) + Math.pow(aux2,2));

                if (TYPE_EUCLID_MATRIX == 1) {      // matriz de distâncias inteiras (arredondamento)
                    dist = Math.floor(dist + 0.5); // floor trunca para baixo
                }

                this.euclidean_matrix.set_value(i,j,dist);
            }
        }
    }

    /**
     * Método que preenche a matriz de distâncias
     */
    public void calcule_euclidean_distances_div1000() {
        double aux1;
        double aux2;
        double dist;

        for (int i = 0; i < this.n_nodes; i++){
            for (int j=0; j < this.n_nodes; j++){
                aux1 = this.plan_nodes_vector[i].x() - this.plan_nodes_vector[j].x();
                aux2 = this.plan_nodes_vector[i].y() - this.plan_nodes_vector[j].y();
                dist = Math.sqrt(Math.pow(aux1,2) + Math.pow(aux2,2));
                dist = dist/1000;

                if (TYPE_EUCLID_MATRIX == 1){       // matriz de distâncias inteiras (arredondamento)
                    dist = Math.floor(dist + 0.5);  // floor trunca para baixo
                }

                this.euclidean_matrix.set_value(i,j,dist);
            }
        }
    }

    public int n_nodes_graph() {
        return this.n_nodes;
    }

    public Node[] get_nodes_vector() {
        return this.plan_nodes_vector;
    }

    public DoubleMatrix get_euclidean_matrix() {
        return this.euclidean_matrix;
    }

    public double get_value_euclidean_matrix(int i, int j) {
        return this.euclidean_matrix.get_value(i,j);
    }

    public void print_nodes() {
        for (int i=0; i <= this.index_last_nodes_vector; i++) {
            System.out.print(i+1 + "\t" + this.plan_nodes_vector[i].x() + "\t");
            System.out.print(this.plan_nodes_vector[i].y() + "\r\n");
        }
    }

    public void print_euclidean_matrix() {
        System.out.print("\r\nEuclidean matrix:\r\n");

        for (int i=0; i < this.n_nodes; i++) {
            System.out.print("\t" + i);
        }

        for (int i=0; i < this.n_nodes; i++) {
            System.out.print( "\r\n" + i);

            for (int j=0; j < this.n_nodes; j++) {
                System.out.print("\t" + this.euclidean_matrix.get_value(i,j));
            }
        }

        System.out.print("\r\n");
    }

    public void print_col_dist_matrix(int col) {
        for (int i = 0; i < this.n_nodes; i++) {
            if (i == col) System.out.print("\t" + i);
        }

        for (int i = 0; i < this.n_nodes; i++) {
            System.out.print("\r\n");
            System.out.print(i);

            for (int j = 0; j < this.n_nodes; j++) {
                if (j == col)
                    System.out.print("\t" + this.euclidean_matrix.get_value(i,j));
            }
        }

        System.out.print("\r\n");
    }

    public void print_visibility_matrix() {
        for (int i = 0; i < this.n_nodes; i++) {
            System.out.print("\t" + i);
        }

        for (int i = 0; i < this.n_nodes; i++) {
            System.out.print("\r\n" + i);
            for (int j = 0; j < this.n_nodes; j++) {
                System.out.print("\t" + (1 / this.euclidean_matrix.get_value(i,j)));
            }
        }

        System.out.print("\r\n");
    }
}
