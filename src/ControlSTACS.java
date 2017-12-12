/**
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public interface ControlSTACS {
    int APP_OBJECTIVE    = 1;  // objetivo da aplicação: (1)minimizar rota mais longa (workload balance), (2)minimizar total da solução (soma das rotas)

    // Critérios de parada de cada execução (o que ocorrer primeiro; se todos = -1, loop infinito):
    int NO_IMP_CYCLES   = -1;  // ciclos sem que a solução seja melhorada. Se -1, não há limitação de não melhoramento
    int MAX_TIME_EXEC   = -1;  // tempo máximo de execução, em segundos. Se -1, não há limitação de tempo
    int MAX_CYCLES      = 10;  // número máximo de iterações por execução do algorimto ACO. Se -1, não há limitação de número de iterações

    // parâmetros ACO:
    int N               = 10;  // ACS = 10 (número de formigas que geram soluções simultaneamente). Aqui, número de soluções geradas num ciclo
    double Q0           = 0.9; // ACS = 0.9 (1: apenas escolhas determinísticas; 0: apenas escolhas aleatórias; os nós iniciais continuam aleatórios, se configurado)
    int ALFA            = 1;   // ACS = 1  relevância do feromônio
    int BETA            = 2;   // ACS = 2  relevância da visibilidade
    double KSI          = 0.1; // ACS = 0.1 (persistência do feromônio, local update)
    double RO           = 0.1; // ACS = 0.1 (persistência do feromônio, global update)
    int CL_LENGTH       = 20;  // TACO = 20 (número de itens da candidate list (max = n-1))
    int CBA_REPET       = 1;   // TACO = 1 (CBA = check beter ant) número máximo de repetições da escolha por uma formiga melhor em cada iteração)

    // opções apenas para soluções com rotas fechadas:
    int ANTS_INIT_NODES  = 1;  // posicionamento inicial das formigas: (1) val08 = todas no depósito , (2) nós aleários distintos em cada solução, (3) nós aleatórios, alterados a cada CHANGE_INI_NODES ciclos sem melhora
    int CHG_INIT_NODES   = 0;  // usado apenas na opção 3 de ANTS_INITIAL_NODES

    // parâmetros para experimentos com feromônio:
    int LPU           = 1;  // atualização local de feromônio:  (1)ON, (0)OFF
    int GPU           = 1;  // atualização global de feromônio: (1)ON, (0)OFF
    int GPUBSF        = 1;  // (1) GPU pela best_so_far, (0) GPU pela melhor do ciclo. GPU tem que estar ligada.
    int GPUNODEP      = 0;  // Se != 0, a GPU e a LPU não ocorrem nas arestas conectadas ao depósito.

    // Buscas Locais (funcionando apenas para soluções com finais fechados):
    int LS2O    = 1;  // busca local 2-opt em todas as soluções geradas: (1)ON, (0)OFF
    int LS3O    = 0;  // busca local 3-opt apenas na melhor solução do ciclo: (1)ON, (0)OFF

}
