/**
 * Geração de pseudo randomicos adaptado do livro "Numerical Recipes in C"
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class Random {
    final int IA = 16807;
    final long IM = 2147483647;
    final double AM = 1.0 / IM;
    final long IQ = 127773;
    final int IR = 2836;

    private long seed;

    public Random(long s) {
        this.seed = s;
    }



    /**
     * Método que retorna a semente utilizada
     * @return
     */
    public long seed_used() {
        return this.seed;
    }

    /**
     * Método que retorna valores uniformemente distribuidos em [0,1]
     * @return
     */
    public double random_number() {
        long idum = this.seed;
        long k;
        double ans;

        k       = (idum) / IQ;
        idum    = IA * (idum - (k * IQ)) - (IR * k);

        if (idum < 0)
            idum += IM;

        ans         = AM * (idum);
        this.seed   = idum;

        return ans;
    }

    /**
     * Método que recebe um inteiro e retorna outro (aleatório) entre 1 e o inteiro recebido
     * @param n_itens
     * @return
     */
    public int raffle_int(int n_itens) {
        double rn       = this.random_number();
        double aux_rn   = rn * n_itens;

        for (int i = 1; i <= n_itens; i++) {
            if (aux_rn > 1) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Método que recebe um vetor de doubles e retorna um índice sorteado
     * os doubles representam a probabilidade de seu índice ser sorteado
     * @param n_itens
     * @param double_vector
     * @return
     */
    public int raffle_double_vector(int n_itens, double double_vector[]) {
        // criar vetor com as probabilidades acumuladas:
        double acc_probs[] = new double[n_itens];

        for (int i = 0; i < n_itens; i++) {
            if (i == 0) {
                acc_probs[i] = double_vector[i];
            } else {
                acc_probs[i] = acc_probs[i-1] + double_vector[i];
            }
        }

        // criando o randômico no intervalo das probabilidades:
        double rn = this.random_number();
        double aux_rn = rn * acc_probs[n_itens-1];

        // realizando o sorteio:
        int raffled = 0;

        for (int i = 0; i < n_itens; i++) {
            if (acc_probs[i] > aux_rn) {
                raffled = i;
                i = n_itens;
            }
        }

        return raffled;
    }

}
