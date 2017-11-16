import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Classe que representa as ordens de serviço
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class ServiceOrder {
    private int utm_x;
    private int utm_y;
    private int type_service;
    private int time_dispatch;      // momento de despacho do serviço, em minutos a partir das 08:00 do dia
    private int time_execution;     // tempo necessário para execução do serviço
    private int deadline_execution; // instante de término do prazo de execução do seviço
    private int km_ini;
    private int km_end;

    private boolean executed;
    private double total_time_execution_real_solution;
    private double total_time_execution_proposed_solution;

    public ServiceOrder() {

    }

    // é definido um novo tempo de despacho para os serviços quando calculados os custos das rotas reais utilizando os dados considerados.
    public void new_service(int utx, int uty, int type, int tdispatch, int texec, int deadexec, int kmi, int kme) {
        this.utm_x              = utx;
        this.utm_y              = uty;
        this.type_service       = type;
        this.time_dispatch      = tdispatch;
        this.time_execution     = texec;
        this.deadline_execution = deadexec;
        this.km_ini             = kmi;
        this.km_end             = kme;

        this.executed = false;
        this.total_time_execution_real_solution     = 0.0;
        this.total_time_execution_proposed_solution = 0.0;
    }

    public int get_time_execution() {
        return this.time_execution;
    }

    public int get_time_dispatch() {
        return this.time_dispatch;
    }

    public void set_executed_service(double partial_route_temporal_cost) {
        this.executed = true;
        this.total_time_execution_proposed_solution = partial_route_temporal_cost - this.time_dispatch;
    }

    // retorna true se o serviço foi executado
    public boolean get_executed_service() {
        return executed;
    }

    // retorna true se o serviço for uma emregencia
    public boolean is_emergency() {
        if (this.type_service == 0)
            return true;
        else
            return false;
    }

    public void set_total_time_execution_real_solution(double partial_route_cost) {
        this.total_time_execution_real_solution = partial_route_cost - this.time_dispatch;
    }

    public void set_time_despatch(int current_route_cost) {
        this.time_dispatch =  current_route_cost; //new_time
    }

    public void print_total_time_execution(int s) {
        System.out.print("  service: "  + s);
        System.out.print("   \treal time exec: " + this.total_time_execution_real_solution);
        System.out.print("\tproposed time exec: " + this.total_time_execution_proposed_solution);

        double diff_time = this.total_time_execution_real_solution - this.total_time_execution_proposed_solution;
        double improvement_time = (diff_time / this.total_time_execution_real_solution) * 100;
        //cout << setiosflags (ios::fixed) << setprecision(2);
        System.out.print("   \timprovement: " + improvement_time + " %\r\n");
        //cout << setiosflags (ios::fixed) << setprecision(FLOAT_PRECISION);
    }

    public void print_service_order() {
        System.out.print("\ttipo: " + this.type_service + "\t\thorario de despacho: " + this.time_dispatch + "  \ttempo de execução: " + this.time_execution + "\r\n");
    }

    //TODO abrir e escrever em arquivo
    public void save_service_order(BufferedWriter file_out) throws IOException {
        file_out.write("\ttipo: ");
        if (type_service == 0)
            file_out.write("E");
        else
            file_out.write("C");
        file_out.write("\t\thorario de despacho: " + time_dispatch + "  \ttempo de execução da ordem: " + time_execution + " segundos\r\n");
    }
}
