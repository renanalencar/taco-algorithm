import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class IntList {
    private int max_items; // número máximo de valores da lista
    private int index; // aponta o último valor da lista. Se -1, a lista está vazia
    private int values_list[]; // valores da lista

    public IntList() {

    }

    public void create(int size) {
        this.max_items      = size;
        this.index          = -1;
        this.values_list    = new int[this.max_items];
    }

    public IntList(int size) {
        this.max_items      = size;
        this.index          = -1;
        this.values_list    = new int[this.max_items];
    }

    /**
     * Método para esvaziar a lista
     */
    public void empty() {
        this.index = -1;
    }

    /**
     * Método para adicionar um valor no final da lista
     * @param value
     */
    public void add(int value) {
        if (value > this.max_items) {
            // "\r\n---> ADDING LARGER VALUE THAN LIST SIZE <---\r\n"
        }
        if (this.index+1 == this.max_items) {
            System.out.print("\r\n---> OVERFLOW ADDING IN LIST: ");
            print();
            System.out.print("\r\n");
        } else {
            this.index++;
            this.values_list[this.index] = value;
        }
    }

    /**
     * Método que inverte a values_list de index_start a index_end, menos os nós depósito da solução
     * @param index_start
     * @param index_end
     * @param depot_node
     */
    public void reverse_no_depot(int index_start, int index_end, int depot_node) {
        int new_list[]  = new int[this.max_items];
        int aux_ind_end = index_end;

        for (int i = 0; i < this.max_items; i++) {
            if ((i < index_start) || (i > index_end)) { // copiar valores normalmente
                new_list[i] = this.values_list[i];
            } else {// inverter valores
                if (this.values_list[aux_ind_end] == depot_node)
                    aux_ind_end--;

                if (this.values_list[i] == depot_node) {
                    new_list[i] = depot_node;
                } else {
                    new_list[i] = this.values_list[aux_ind_end];
                    aux_ind_end--;
                }
            }
        }

        //delete [] values_list;
        this.values_list = new_list;
    }

    /**
     * Método que inverte a values_list de index_start a index_end
     * @param index_start
     * @param index_end
     */
    public void reverse(int index_start, int index_end) {
        int new_list[]  = new int[this.max_items];
        int aux_ind_end = index_end;

        for (int i = 0; i < this.max_items; i++) {
            if ((i < index_start) || (i > index_end)) { // copiar valores normalmente
                new_list[i] = this.values_list[i];
            } else { // inverter valores
                new_list[i] = this.values_list[aux_ind_end];
                aux_ind_end--;
            }
        }

        //delete [] values_list;
        this.values_list = new_list;
    }

    public void remove(int value) {
        int aux_values[]    = new int[this.max_items];
        int index_aux       = 0;

        for (int i = 0; i <= this.index; i++) {
            if (this.values_list[i] != value) {
                aux_values[index_aux] = this.values_list[i];
                index_aux++;
            }
        }

        this.values_list = aux_values;
        this.index--;
    }

    public void change_values (int index_i, int index_j) {
        int aux_value = this.values_list[index_i];
        this.values_list[index_i] = this.values_list[index_j];
        this.values_list[index_j] = aux_value;
    }

    /**
     * Método que retorna a capacidade da lista
     * @return
     */
    public int size() {
        return this.max_items;
    }

    /**
     * Método que retorna o número de itens na lista
     * @return
     */
    public int n_items() {
        return this.index + 1;
    }

    /**
     * Método que retorna um valor da lista pelo índice
     * @param ind
     * @return
     */
    public int value(int ind) {
        return this.values_list[ind];
    }

    /**
     * Método que retorna o último valor da lista
     * @return
     */
    public int last_value() {
        return this.values_list[this.index];
    }


    /**
     * Método que verifica se o valor está na lista
     * @param value
     * @return
     */
    public boolean on_the_list(int value) {
        boolean ret = false;

        for (int i = 0; i <= this.index; i++) {
            if (value == this.values_list[i]) {
                ret = true;
                i = this.index + 1;
            }
        }
        return ret;
    }

    /**
     * Método que verifica se a lista está cheia?
     * @return
     */
    public boolean is_full() {
        if ((this.index+1) == this.max_items)
            return true;
        else
            return false;
    }

    /**
     * Método que verifica se a lista está vazia?
     * @return
     */
    public boolean is_empty() {
        if (this.index == -1)
            return true;
        else
            return false;
    }

    public int next_value(int value) {
        int next_value = -1;

        for (int i =0; i <= this.index; i++) {
            if (i != this.index) {
                next_value = this.values_list[i+1];
            }
        }
        return next_value;
    }

    public void save(BufferedWriter file_out) throws IOException {
        if (this.index == -1) {
            file_out.write("--> EMPTY LIST");
        } else {
            for (int i = 0; i <= this.index; i++) {
                if (this.values_list[i] < 10) file_out.write(" ");
                file_out.write(this.values_list[i] + " ");
            }
        }
    }

    /**
     * Método que imprime a lista
     */
    public void print() {
        if (this.index == -1) {
            System.out.print("--> EMPTY LIST");
        } else {
            for (int i = 0; i <= this.index; i++) {
                if (this.values_list[i] < 10) System.out.print(" ");
                System.out.print(this.values_list[i] + " ");
            }
        }
    }

    /**
     * Método que imprime a lista adicionando 1 a todos os valores
     */
    public void print_add1() {
        if (this.index == -1) {
            System.out.print("--> EMPTY LIST");
        } else {
            for (int i = 0; i <= this.index; i++) {
                if (this.values_list[i] < 10) System.out.print(" ");
                System.out.print(this.values_list[i]+1 + " ");
            }
        }
    }
}
