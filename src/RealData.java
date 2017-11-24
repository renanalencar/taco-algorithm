import java.io.*;
import java.util.Vector;

/**
 * Classe que o arquivo data.txt possui os dados reais utilizados no experimento
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class RealData implements ControlExperiment {
    private int depot;

    private BufferedReader data_file; // para carregar a instância de arquivo
    private String data_file_name; // nome do arquivo

    private int id_register;
    private int id_work_day;
    private int id_point;
    private int utm_x;
    private int utm_y;
    private int id_team;
    private int type_service;
    private int time_dispatch;
    private int time_execution;
    private int deadline_execution;
    private int km_ini;
    private int km_end;

    //TODO Abrir e salvar arquivo
    public RealData() throws FileNotFoundException {
        this.depot = DEPOT_INDEX;
        this.data_file_name = "data/data.txt";

        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);

        this.data_file = new BufferedReader(new InputStreamReader(in));
        this.id_register = 0;  // o final do arquivo é -1
    }

    // retorna ao início do arquivo
    //TODO Abrir e salvar arquivo
    public void go_init_file() throws IOException {
        this.data_file.close();

        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        this.data_file = new BufferedReader(new InputStreamReader(in));
    }

    // lê o próximo registro (linha) do arquivo
    // os tempos de execução são convertidos para segundos:
    //TODO Abrir e salvar arquivo
    public void read_next_register() throws IOException {
        String line;
        Vector<Integer> output = new Vector<Integer>();

        	line = this.data_file.readLine();
            line = trim(line);
            Vector<String> _line = split(line, "\\s+");

            this.id_register = Integer.parseInt(_line.elementAt(0));
            if(this.id_register != -1){
            this.id_work_day = Integer.parseInt(_line.elementAt(1));
            this.id_point    = Integer.parseInt(_line.elementAt(2));
            this.utm_x       = Integer.parseInt(_line.elementAt(3));
            this.utm_y       = Integer.parseInt(_line.elementAt(4));

            if (this.id_point != this.depot) {  // os registros dos depósitos não possuem os valores abaixo no arquivo
                this.id_team        = Integer.parseInt(_line.elementAt(5));
                this.type_service   = Integer.parseInt(_line.elementAt(6));
                this.time_dispatch  = Integer.parseInt(_line.elementAt(7));
                this.time_execution = Integer.parseInt(_line.elementAt(8)) / 1000000000;  // convertendo tempo de execução para segundos

                this.deadline_execution = Integer.parseInt(_line.elementAt(9));

                this.km_ini = Integer.parseInt(_line.elementAt(10));
                this.km_end = Integer.parseInt(_line.elementAt(11));
            }

        
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

    // retorna o número de dias de serviço no arquivo
    //TODO Abrir e salvar arquivo
    public int count_work_days() throws IOException {
        boolean first_register = true;
        int counter_days = 0;
        int current_day = 0;

        this.data_file.close();
        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        this.data_file = new BufferedReader(new InputStreamReader(in));

        boolean flag = true;
        while (flag) {  // lendo todo o arquivo das matrizes

            this.read_next_register();
           
            if (this.id_register != -1) {
                if (first_register) {
                    first_register = false;
                    current_day = this.id_work_day;
                    counter_days++;
                }
                if (current_day != this.id_work_day) {
                    current_day = this.id_work_day;
                    counter_days++;
                }
            } else {
                flag = false;
            }
        }
        return counter_days;
    }

    // carregas as ids dos dias no vetor
    //TODO Abrir e salvar arquivo
    //public void load_ids_work_days(IntList list_id_work_days, int n_work_days) throws IOException {
    public IntList load_ids_work_days(int n_work_days) throws IOException {
        //delete list_id_work_days;
        IntList list_id_work_days = new IntList(n_work_days);

        boolean first_register = true;
        int counter_days = 0;
        int current_day = 0;

        this.data_file.close();
        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        this.data_file = new BufferedReader(new InputStreamReader(in));

        boolean flag = true;
        while (flag) {  // lendo todo o arquivo das matrizes

            this.read_next_register();

            if (this.id_register != -1){
                if (first_register){
                    first_register = false;
                    current_day = this.id_work_day;
                    list_id_work_days.add(current_day);
                    counter_days++;
                }
                if (current_day != this.id_work_day){
                    current_day = this.id_work_day;
                    list_id_work_days.add(current_day);
                    counter_days++;
                }
            } else {
                flag = false;
            }
        }

        return list_id_work_days;
    }

    // retorna a qtde de serviços de um dia
    //TODO Abrir e salvar arquivo
    public int count_services(int selected_id_work_day) throws IOException {
        int counter_services = 0;

        this.data_file.close();
        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        this.data_file = new BufferedReader(new InputStreamReader(in));

        boolean flag = true;
        while (flag) {  // lendo todo o arquivo das matrizes

            this.read_next_register();

            if (this.id_register != -1){
                if (this.id_work_day == selected_id_work_day) {
                    if (this.id_point != this.depot){
                        counter_services++;
                    }
                }
            } else {
                flag = false;
            }
        }

        return counter_services;
    }

    // retorna a qtde de equipes de um dia
    //TODO Abrir e salvar arquivo
    public int count_teams(int selected_id_work_day) throws IOException {
        int counter_teams = 0;

        data_file.close();
        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        data_file = new BufferedReader(new InputStreamReader(in));

        int current_team = 0;
        boolean flag = true;
        while (flag) {  // lendo todo o arquivo das matrizes

            this.read_next_register();

            if (id_register != -1){
                if (id_work_day == selected_id_work_day){
                    if (id_point != depot){
                        if (counter_teams == 0){
                            counter_teams++;
                            current_team = id_team;
                        }
                        else{
                            if (current_team != id_team){
                                counter_teams++;
                                current_team = id_team;
                            }
                        }
                    }
                }
            }
            else{
                flag = false;
            }
        }
        return counter_teams;
    }

    //TODO Abrir e salvar arquivo
    public ServiceOrder[] load_service_orders(int selected_id_work_day, int n_points) throws IOException {
        this.data_file.close();
        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        this.data_file = new BufferedReader(new InputStreamReader(in));

        ServiceOrder[] day_services = new ServiceOrder[n_points];

        boolean flag = true;
        while (flag) {  // lendo todo o arquivo das matrizes

            this.read_next_register();

            if (this.id_register != -1){
                if (this.id_work_day == selected_id_work_day){
                    if (this.id_point != depot){  // o índice do depósito não é inicializado

                        day_services[this.id_point].new_service(this.utm_x, utm_y, this.type_service, this.time_dispatch, this.time_execution, this.deadline_execution, this.km_ini, this.km_end);

                    }
                }
            }
            else{
                flag = false;
            }
        }

        return day_services;
    }

    //TODO Abrir e salvar arquivo
    public void load_euclidean_graph(int selected_id_work_day, MtspInstance instance) throws IOException {
        this.data_file.close();
        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        this.data_file = new BufferedReader(new InputStreamReader(in));

        boolean flag = true;
        while (flag) {  // lendo todo o arquivo das matrizes

            this.read_next_register();

            if (this.id_register != -1){
                if (this.id_work_day == selected_id_work_day){
                    if (this.id_point != this.depot){

                        instance.add_plan_node(this.utm_x, this.utm_y);

                    }
                }
            }
            else{
                flag = false;
            }
        }
    }

    //TODO Abrir e salvar arquivo
    public int x_depot() throws IOException {
        int coord_x = -1;

        this.data_file.close();
        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        this.data_file = new BufferedReader(new InputStreamReader(in));

        boolean flag = true;
        while (flag) {  // lendo todo o arquivo das matrizes

            this.read_next_register();

            if (this.id_register != -1) {
                if (this.id_point == this.depot) {
                    coord_x = this.utm_x;
                    flag = false;
                }
            } else {
                flag = false;
            }
        }
        return coord_x;
    }

    //TODO Abrir e salvar arquivo
    public int y_depot() throws IOException {
        int coord_y = -1;

        this.data_file.close();
        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        this.data_file = new BufferedReader(new InputStreamReader(in));

        boolean flag = true;
        while (flag) {  // lendo todo o arquivo das matrizes

            this.read_next_register();

            if (this.id_register != -1) {
                if (this.id_point == this.depot) {
                    coord_y = this.utm_y;
                    flag = false;
                }
            } else {
                flag = false;
            }
        }
        return coord_y;
    }

    // é definido um novo tempo de despacho para os serviços quando calculados os custos das rotas reais utilizando os dados considerados.
    //TODO Abrir e salvar arquivo
    public void load_real_solution(int selected_id_work_day, MtspSolution real_solution, MtspInstance instance, ServiceOrder day_services[]) throws IOException {
        DoubleMatrix cost_matrix = instance.get_cost_matrix();
        real_solution.reset(); // a solução real é do tipo de finais fechados

        this.data_file.close();
        //data_file.open(data_file_name, ios::in);
        FileInputStream fstream = new FileInputStream(this.data_file_name);
        DataInputStream in = new DataInputStream(fstream);
        this.data_file = new BufferedReader(new InputStreamReader(in));

        double current_route_cost = 0.0;
        boolean first_point_solution = true;

        boolean flag = true;
        while (flag) {  // lendo todo o arquivo das matrizes
            this.read_next_register();
            if (this.id_register != -1){ // fim do arquivo
                if (this.id_work_day == selected_id_work_day){  // este serviço faz parte da solução real

                    double adding_cost;
                    if (first_point_solution){ // primeiro ponto = depósito
                        first_point_solution = false;
                        adding_cost = 0.0;
                    } else {
                        int last_point_inserted = real_solution.last_node();
                        adding_cost = cost_matrix.get_value(last_point_inserted, this.id_point);
                    }

                    // definindo o horário de despacho do serviço a a partir do momento que uma equipe inicia o deslocamento para ele na solução real
                    if (DESPATCH_REAL_SOL == 1){
                        int old_time = day_services[this.id_point].get_time_dispatch();
                        if (old_time > 0){ // o serviço não faz parte da instância inicial
                            day_services[this.id_point].set_time_despatch((int) current_route_cost); // atualizando o horário de despacho
                        }
                    }

                    // atualizando custo da rota parcial real:
                    current_route_cost = current_route_cost + adding_cost;

                    if (this.id_point == this.depot){
                        current_route_cost = 0.0;
                    } else {
                        day_services[this.id_point].set_total_time_execution_real_solution(current_route_cost);
                    }

                    real_solution.add(this.id_point, adding_cost);  // será usado recalculate_solution para determinar os custos
                }
            } else {
                flag = false;
            }
        }
        real_solution.recalculate_solution(cost_matrix);  // recalculando os custos (redundante)

    }

}
