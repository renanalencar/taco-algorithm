/**
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class LocalSearch implements ControlExperiment, ControlSTACS {
    int depot;
    DoubleMatrix cost_matrix;
    double time_ini_execution;
    int size_sol;
    int index_first;
    int index_last;

    LocalSearch(double time_ini_execution, DoubleMatrix cost_matrix) {
        this.depot = DEPOT_INDEX;
        this.cost_matrix = cost_matrix;
        this.time_ini_execution = time_ini_execution;
    }

    // realiza a busca local 2-opt e atualiza solution com a melhor encontrada
    // realiza a busca local 2-opt e atualiza solution se houver ganho em alguma troca
    // retorna verdadeiro de atualizar a solução
    boolean two_opt(MtspSolution solution) {
        size_sol = solution.n_nodes();  // número de nós da solução
        index_first = 0;                 // primeiro índice da solução
        index_last = size_sol - 2;       // o último nó da solução é o retorno ao nó inicial
//-----        cout << "original solution (2-opt): "; solution->print();
        boolean updated_solution = false;

        for (int index_a = index_first + 1; index_a < index_last; index_a++) {  // a começa do segundo e não vai até o último índice

            for (int index_b = index_a + 1; index_b <= index_last; index_b++) {  // b começa do próximo índice após a e vai até o último índice

                boolean change = true;  // a troca deve ser realizada

                // vizinhos dos nós atuais:
                int index_before_a = index_a - 1;    // index_a nunca é igual a index_first
                int index_after_a = index_a + 1;     // index_a nunca é igual a index_last

                int index_before_b = index_b - 1;                        // index_b nunca é igual a index_first
                int index_after_b;                                       // índice do nó posterior a j
                if (index_b == index_last)
                    index_after_b = index_first;  // index_b pode ser igual a index_last
                else
                    index_after_b = index_b + 1;                        // index_b não é igual a index_last

                // impedindo ponto de corte inválido:
                if ((index_a == index_first + 1) && (index_b == index_last)) {
                    change = false;
//-----                    cout << "--> not changed: invalid cutoff point\r\n";
                }

                // impedindo que dois depositos sejam vizinhos:
                if (change) {
                    if (solution.node(index_a) == depot) {
                        if ((solution.node(index_after_b) == depot) || (solution.node(index_before_b)) == depot) {
                            change = false;
//-----                            cout << "--> not changed: neighbor depots\r\n";
                        }
                    }
                    if (solution.node(index_b) == depot) {
                        if ((solution.node(index_after_a) == depot) || (solution.node(index_before_a) == depot)) {
                            change = false;
//-----                            cout << "--> not changed: neighbor depots\r\n";
                        }
                    }
                }

                if (change) {
                    double gain = this.gain_change_two_nodes(index_a, index_b, solution);

                    if (gain > 0.0) {  // realizando a troca apenas se o ganho total for positivo
                        // copiando a solução para uma temporária:
                        MtspSolution new_sol = new MtspSolution(size_sol);
                        // copiando nós:
                        for (int i = 0; i < size_sol; i++) {
                            new_sol.add(solution.node(i), 0.0);
                        }
                        // copiando outros dados:
                        new_sol.set_total_cost(solution.get_total_cost());
                        new_sol.set_longest_route(solution.get_longest_route());
                        new_sol.set_iteration(solution.get_iteration_sol());
                        new_sol.set_time(solution.get_time_sol());
                        new_sol.set_random_seed(solution.get_seed_rand());
                        //-----                    cout << "solution copied:   "; new_sol->print();
                        //-----                    cout << "changing    index: " << index_a+1 << " -> " << index_b+1;
                        //-----                    cout << "            nodes: " << new_sol->node(index_a) << " -> " << new_sol->node(index_b) << "\r\n";
                        new_sol.change_nodes(index_a, index_b, cost_matrix);  // troca a posição dos nós e recalcula a solução temporária
                        //-----                    cout << "\r\nsolution  changed:"; new_sol->print();
                        double new_best_cost;
                        double current_best_cost;
                        if (APP_OBJECTIVE == 1) {  // minimizar a longest_route
                            new_best_cost = new_sol.get_longest_route();
                            current_best_cost = solution.get_longest_route();
                        } else {  // minimizar o custo total
                            new_best_cost = new_sol.get_total_cost();
                            current_best_cost = solution.get_total_cost();
                        }

                        if (new_best_cost < current_best_cost){    // a nova solução é melhor, de acordo com o objetivo
                            //delete solution;                       // liberando a memória de solution
                            solution = new_sol;                    // solution passa a apontar para a nova solução
                            //TODO checar a tradução do clock para Java
                            solution.set_time(System.currentTimeMillis() - time_ini_execution);  // atualizando o tempo de criação da solução
                            updated_solution = true;

                            if (PRLS == 1) {
                                System.out.print("2-opt:\t\t");
                                solution.print();
                            }
                        } else {
                            //delete new_sol;  // liberando a memória de new_sol
//-----                            cout << "--> not changed: worst objective cost\r\n";
                        }
                    }
                }
            }
        }
        return updated_solution;
    }

    // realiza a busca local 3-opt e atualiza solution com a melhor encontrada
    // realiza a busca local 3-opt e atualiza solution se houver ganho em alguma troca
    // retorna verdadeiro de atualizar a solução
    boolean three_opt(MtspSolution solution) {
        boolean updated_solution = false;
        updated_solution = this.two_opt(solution);  // atualiza solution para o ótimo local 2-opt

        size_sol = solution.n_nodes();  // número de nós da solução
        index_first = 0;                 // primeiro índice da solução
        index_last = size_sol - 2;       // o último nó da solução é o retorno ao nó inicial

        if (PDLS == 1) {
            System.out.print("\r\noriginal solution:\t\t");
            solution.print();
            System.out.print("\r\n");
        }

        for (int index_a = index_first + 1; index_a < index_last - 2; index_a++) {

            for (int index_b = index_a + 1; index_b < index_last - 1; index_b++) {
                int index_c = index_b + 1;

                for (int index_d = index_c + 1; index_d <= index_last; index_d++) {

                    boolean apply = true;  // o conjunto de trocas deve ser realizado

                    // impedindo ponto de corte inválido:
                    if ((index_a == index_first + 1) && (index_d == index_last)) {
                        apply = false;
//-----                        if (PDLS) { cout << "--> not changed: invalid cutoff point\r\n"; }
                    }

                    if (apply) {

                        int best_sol = 0;  // zero corresponde à solução original
                        double new_best_cost;
                        double current_best_cost;

                        // solução 1: a <-> b, c <-> d
                        MtspSolution new_sol_1 = new MtspSolution(size_sol);
                        // copiando nós:
                        for (int i = 0; i < size_sol; i++) {
                            new_sol_1.add(solution.node(i), 0.0);
                        }
                        // copiando outros dados:
                        new_sol_1.set_total_cost(solution.get_total_cost());
                        new_sol_1.set_longest_route(solution.get_longest_route());
                        new_sol_1.set_iteration(solution.get_iteration_sol());
                        new_sol_1.set_time(solution.get_time_sol());
                        new_sol_1.set_random_seed(solution.get_seed_rand());

                        if (PDLS == 1) {
                            System.out.print("\r\n\r\nsolution_1 copied solution:\t");
                            new_sol_1.print();
                        }
                        // trocando a <-> b:
                        if (PDLS == 1) {
                            System.out.print("-> changing    index: " + index_a + " -> " + index_b);
                            System.out.print("   nodes: " + new_sol_1.node(index_a) + " -> " + new_sol_1.node(index_b) + "\r\n");
                        }
                        new_sol_1.change_nodes(index_a, index_b, cost_matrix);  // troca a posição dos nós e recalcula a solução temporária
                        if (PDLS == 1){
                            System.out.print("solution_1  changed:\t\t");
                            new_sol_1.print(); }
                        // trocando c <-> d:
                        if (PDLS == 1) {
                            System.out.print("-> changing    index: " + index_c + " -> " + index_d);
                            System.out.print("   nodes: " + new_sol_1.node(index_c) + " -> " + new_sol_1.node(index_d) + "\r\n");
                        }
                        new_sol_1.change_nodes(index_c, index_d, cost_matrix);  // troca a posição dos nós e recalcula a solução temporária
                        if (PDLS == 1) {
                            System.out.print("solution_1  changed:\t\t");
                            new_sol_1.print(); }


                        if (APP_OBJECTIVE == 1){  // minimizar a longest_route
                            new_best_cost = new_sol_1.get_longest_route();
                            current_best_cost = solution.get_longest_route();
                        } else {  // minimizar o custo total
                            new_best_cost = new_sol_1.get_total_cost();
                            current_best_cost = solution.get_total_cost();
                        }
                        if (new_best_cost < current_best_cost){    // a nova solução é melhor, de acordo com o objetivo
                            if (new_sol_1.test_validate()){  // retorna verdadeiro se a solução for válida
                                current_best_cost = new_best_cost;
                                best_sol = 1;  // código da solução 1
                            }
                        }


                        // solução 2: a <-> c, b <-> d
                        MtspSolution new_sol_2 = new MtspSolution(size_sol);
                        // copiando nós:
                        for (int i = 0; i < size_sol; i++) {
                            new_sol_2.add(solution.node(i), 0.0);
                        }
                        // copiando outros dados:
                        new_sol_2.set_total_cost(solution.get_total_cost());
                        new_sol_2.set_longest_route(solution.get_longest_route());
                        new_sol_2.set_iteration(solution.get_iteration_sol());
                        new_sol_2.set_time(solution.get_time_sol());
                        new_sol_2.set_random_seed(solution.get_seed_rand());

                        if (PDLS == 1) {
                            System.out.print("\r\nsolution_2 copied solution:\t");
                            new_sol_2.print();
                        }
                        // trocando a <-> c:
                        if (PDLS == 1) {
                            System.out.print("-> changing    index: " + index_a + " -> " + index_c);
                            System.out.print("   nodes: " + new_sol_2.node(index_a) + " -> " + new_sol_2.node(index_c) + "\r\n");
                        }
                        new_sol_2.change_nodes(index_a, index_c, cost_matrix);  // troca a posição dos nós e recalcula a solução temporária
                        if (PDLS == 1) {
                            System.out.print("solution_2  changed:\t\t");
                            new_sol_2.print();
                        }
                        // trocando b <-> d:
                        if (PDLS == 1) {
                            System.out.print("-> changing    index: " + index_b + " -> " + index_d);
                            System.out.print("   nodes: " + new_sol_2.node(index_b) + " -> " + new_sol_2.node(index_d) + "\r\n");
                        }
                        new_sol_2.change_nodes(index_b, index_d, cost_matrix);  // troca a posição dos nós e recalcula a solução temporária
                        if (PDLS == 1) {
                            System.out.print("solution_2  changed:\t\t");
                            new_sol_2.print();
                        }

                        if (APP_OBJECTIVE == 1){  // minimizar a longest_route
                            new_best_cost = new_sol_2.get_longest_route();
                        } else {  // minimizar o custo total
                            new_best_cost = new_sol_2.get_total_cost();
                        }
                        if (new_best_cost < current_best_cost) {    // a nova solução é melhor, de acordo com o objetivo
                            if (new_sol_2.test_validate()) {  // retorna verdadeiro se a solução for válida
                                current_best_cost = new_best_cost;
                                best_sol = 2;  // código da solução 2
                            }
                        }


                        // solução 3: a <-> c, b <-> d, a <-> b
                        MtspSolution new_sol_3 = new MtspSolution(size_sol);
                        // copiando nós da solução 2:
                        for (int i=0; i < size_sol; i++) {
                            new_sol_3.add(new_sol_2.node(i), 0.0);
                        }
                        // copiando outros dados:
                        new_sol_3.set_total_cost(new_sol_2.get_total_cost());
                        new_sol_3.set_longest_route(new_sol_2.get_longest_route());
                        new_sol_3.set_iteration(new_sol_2.get_iteration_sol());
                        new_sol_3.set_time(new_sol_2.get_time_sol());
                        new_sol_3.set_random_seed(new_sol_2.get_seed_rand());
                        if (PDLS == 1) {
                            System.out.print("\r\nsolution_3 copied solution_2:   ");
                            new_sol_3.print();
                        }

                        // trocando a <-> b:
                        if (PDLS == 1) {
                            System.out.print("-> changing    index: " + index_a + " -> " + index_b);
                            System.out.print("   nodes: " + new_sol_3.node(index_a) + " -> " + new_sol_3.node(index_b) + "\r\n");
                        }
                        new_sol_3.change_nodes(index_a, index_b, cost_matrix);  // troca a posição dos nós e recalcula a solução temporária
                        if (PDLS == 1){
                            System.out.print("solution_3  changed:\t\t");
                            new_sol_3.print();
                        }

                        if (APP_OBJECTIVE == 1){  // minimizar a longest_route
                            new_best_cost = new_sol_3.get_longest_route();
                        } else {  // minimizar o custo total
                            new_best_cost = new_sol_3.get_total_cost();
                        }
                        if (new_best_cost < current_best_cost) {    // a nova solução é melhor, de acordo com o objetivo
                            if (new_sol_3.test_validate()) {  // retorna verdadeiro se a solução for válida
                                current_best_cost = new_best_cost;
                                best_sol = 3;  // código da solução 3
                            }
                        }


                        // solução 4: a <-> c, b <-> d, c <-> d
                        MtspSolution new_sol_4= new MtspSolution(size_sol);
                        // copiando nós da solução 2:
                        for (int i=0; i < size_sol; i++){
                            new_sol_4.add(new_sol_2.node(i), 0.0);
                        }
                        // copiando outros dados:
                        new_sol_4.set_total_cost(new_sol_2.get_total_cost());
                        new_sol_4.set_longest_route(new_sol_2.get_longest_route());
                        new_sol_4.set_iteration(new_sol_2.get_iteration_sol());
                        new_sol_4.set_time(new_sol_2.get_time_sol());
                        new_sol_4.set_random_seed(new_sol_2.get_seed_rand());

                        if (PDLS == 1) {
                            System.out.print("\r\nsolution_4 copied solution_2:   ");
                            new_sol_4.print();
                        }

                        // trocando c <-> d:
                        if (PDLS == 1) {
                            System.out.print("-> changing    index: " + index_c + " -> " + index_d);
                            System.out.print("   nodes: " + new_sol_4.node(index_c) + " -> " + new_sol_4.node(index_d) + "\r\n");
                        }
                        new_sol_4.change_nodes(index_c, index_d, cost_matrix);  // troca a posição dos nós e recalcula a solução temporária
                        if (PDLS == 1) {
                            System.out.print("solution_4  changed:\t\t");
                            new_sol_4.print();
                        }

                        if (APP_OBJECTIVE == 1){  // minimizar a longest_route
                            new_best_cost = new_sol_4.get_longest_route();
                        } else {  // minimizar o custo total
                            new_best_cost = new_sol_4.get_total_cost();
                        }
                        if (new_best_cost < current_best_cost){    // a nova solução é melhor, de acordo com o objetivo
                            if (new_sol_4.test_validate()){  // retorna verdadeiro se a solução for válida
                                current_best_cost = new_best_cost;
                                best_sol = 4;  // código da solução 4
                            }
                        }

                        switch (best_sol){
                            case 0: {  // não faz nada: a solução atual é a melhor
                                //delete new_sol_4;
                                //delete new_sol_3;
                                //delete new_sol_2;
                                //delete new_sol_1;
                                break;
                            }
                            case 1: {
                                //delete solution;
                                solution = new_sol_1;
                                //TODO checar a tradução do clock para Java
                                solution.set_time(System.currentTimeMillis() - time_ini_execution);  // atualizando o tempo de criação da solução
                                //delete new_sol_4;
                                //delete new_sol_3;
                                //delete new_sol_2;
                                break;
                            }
                            case 2: {
                                //delete solution;
                                solution = new_sol_2;
                                //TODO checar a tradução do clock para Java
                                solution.set_time(System.currentTimeMillis() - time_ini_execution);  // atualizando o tempo de criação da solução
                                //delete new_sol_4;
                                //delete new_sol_3;
                                //delete new_sol_1;
                                break;
                            }
                            case 3: {
                                //delete solution;
                                solution = new_sol_3;
                                //TODO checar a tradução do clock para Java
                                solution.set_time(System.currentTimeMillis() - time_ini_execution);  // atualizando o tempo de criação da solução
                                //delete new_sol_4;
                                //delete new_sol_2;
                                //delete new_sol_1;
                                break;
                            }
                            case 4: {
                                //delete solution;
                                solution = new_sol_4;
                                //TODO checar a tradução do clock para Java
                                solution.set_time(System.currentTimeMillis() - time_ini_execution);  // atualizando o tempo de criação da solução
                                //delete new_sol_3;
                                //delete new_sol_2;
                                //delete new_sol_1;
                                break;
                            }
                        }
                        if (best_sol != 0){ // foi encontrada uma solução melhor
                            updated_solution = true;
                            if (PRLS == 1) {
                                System.out.print("3-opt:\t\t");
                                solution.print();
                            }
                        }
                    }
                }
            }
        }
        return updated_solution;
    }

    double gain_change_two_nodes(int index_a, int index_b, MtspSolution solution) {
        double gain;

        int index_before_a;                                      // índice do nó anterior a i
        if (index_a == index_first)
            index_before_a = index_last;  // a é o primeiro nó da solução
        else
            index_before_a = index_a - 1;                         // a é qualquer outro nó

        int index_after_a = index_a + 1;                           // a não vai até o último índice

        int index_before_b = index_b - 1;                        // j nunca é o primeiro nó
        int index_after_b;                                       // índice do nó posterior a j
        if (index_b == index_last)
            index_after_b = index_first;   // j é o último nó da solução
        else
            index_after_b = index_b + 1;                        // j é qualquer outro nó

        // verificando se a troca gera uma solução com menor custo total:
        double partial_cost_a       = this.partial_cost(index_before_a, index_a, index_after_a, solution);
        double partial_cost_b       = this.partial_cost(index_before_b, index_b, index_after_b, solution);
        double cost_before_change   = partial_cost_a + partial_cost_b;  // custo antes da troca
        double cost_after_change    = 0.0;         // custo caso a troca seja feita:

        cost_after_change = cost_after_change + partial_cost(index_before_a, index_b, index_after_a, solution);
        cost_after_change = cost_after_change + partial_cost(index_before_b, index_a, index_after_b, solution);

        gain = cost_before_change - cost_after_change;

        return gain;
    }

    double partial_cost(int before, int current, int after, MtspSolution solution) {
        double p_cost       = 0.0; // retorno
        int before_node     = solution.node(before);
        int current_node    = solution.node(current);
        int after_node      = solution.node(after);

        p_cost = p_cost + cost_matrix.get_value(before_node,current_node);
        p_cost = p_cost + cost_matrix.get_value(current_node,after_node);

        return p_cost;
    }

}
