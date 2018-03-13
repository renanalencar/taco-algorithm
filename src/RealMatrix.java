import java.io.*;
import java.util.Vector;

/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class RealMatrix {

    private String matrix_file_name; // para carregar a instância de arquivo
    private BufferedReader matrix_file; // nome do arquivo

    private int id_register;
    private int id_work_day;
    private int id_point_a;
    private int id_point_b;
    private double lat_a;
    private double lng_a;
    private double lat_b;
    private double lng_b;
    private double real_distance;
    private int real_time_cost;

    //TODO checar se está a fazer o carregamento
    public RealMatrix() throws FileNotFoundException {
        this.matrix_file_name   = "data/costs.txt";
        FileInputStream fstream = new FileInputStream(this.matrix_file_name);
        DataInputStream in      = new DataInputStream(fstream);
        this.matrix_file        = new BufferedReader(new InputStreamReader(in));
    }

    //TODO checar se está a fazer o carregamento
    public void read_next_register() throws IOException {

        String line;
        Vector<Integer> output = new Vector<Integer>();

        	line = this.matrix_file.readLine();
            line = trim(line);

            Vector<String> _line = split(line, "\\s+");
            int lineSize = _line.size();
            this.id_register = Integer.parseInt(_line.elementAt(0));
            if(lineSize == 10 && this.id_register !=-1) {
                
                this.id_work_day = Integer.parseInt(_line.elementAt(1));
                this.id_point_a = Integer.parseInt(_line.elementAt(2));
                this.id_point_b = Integer.parseInt(_line.elementAt(3));
                this.lat_a = Double.parseDouble(_line.elementAt(4));
                this.lng_a = Double.parseDouble(_line.elementAt(5));
                this.lat_b = Double.parseDouble(_line.elementAt(6));
                this.lng_b = Double.parseDouble(_line.elementAt(7));
                this.real_distance = Double.parseDouble(_line.elementAt(8));
                this.real_time_cost = Integer.parseInt(_line.elementAt(9));
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

    private boolean is_number(final String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    //TODO checar se está alterando um variável externa de fato
    public void load_real_distance_matrix(int selected_id_work_day, DoubleMatrix real_distance_matrix) throws IOException {
        this.matrix_file.close();

        FileInputStream fstream = new FileInputStream(this.matrix_file_name);
        DataInputStream in      = new DataInputStream(fstream);
        this.matrix_file        = new BufferedReader(new InputStreamReader(in));

        boolean flag = true;
        while (flag) {  // lendo tudo o arquivo das matrizes
            this.read_next_register();
            if (this.id_register != -1) {
                if (this.id_work_day == selected_id_work_day) {
                    real_distance_matrix.set_value(this.id_point_a, this.id_point_b, this.real_distance);
                }
            } else {
                flag = false;
            }
        }
    }

    //TODO checar se está alterando um variável externa de fato
    public void load_real_time_matrix(int selected_id_work_day, DoubleMatrix real_time_matrix) throws IOException {
        this.matrix_file.close();

        FileInputStream fstream = new FileInputStream(this.matrix_file_name);
        DataInputStream in      = new DataInputStream(fstream);
        this.matrix_file        = new BufferedReader(new InputStreamReader(in));

        boolean flag = true;
        while (flag) {  // lendo tudo o arquivo das matrizes
            this.read_next_register();
            if (id_register != -1){
                if (id_work_day == selected_id_work_day) {
                    real_time_matrix.set_value(this.id_point_a, this.id_point_b, this.real_time_cost);
                }
            } else {
                flag = false;
            }
        }
    }

}
