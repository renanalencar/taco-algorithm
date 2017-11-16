/**
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class TacoAlgorithm {
    private int m;
    private DoubleMatrix cost_matrix;
    private Ant ants[];

    public TacoAlgorithm(int m, DoubleMatrix cost_matrix, Ant ants[]) {
        this.m           = m;
        this.cost_matrix = cost_matrix;
        this.ants        = ants.clone();
    }

    /**
     * Método que retorna o índice da formiga com menor rota parcial
     * @return
     */
    public int select_ant() {
        int selected     = 0;
        double less_cost = 0.0;

        for (int k = 0; k < m; k++) {
            if (k == 0) {
                selected  = k;
                less_cost = this.ants[k].cost_route();

            } else {
                if (this.ants[k].cost_route() < less_cost) {
                    selected  = k;
                    less_cost = this.ants[k].cost_route();
                }
            }
        }

        return selected;
    }

    /**
     * Método que retorna o índice da formiga que resulta no melhor movimento
     * @param next_node
     * @return
     */
    public int check_better_ant(int next_node) {
        int better_ant              = 0;
        double better_predict_route = 0.0;
        double predict_route        = 0.0;

        for (int k = 0; k < m; k++) {
            predict_route = this.predict_route(k, next_node);

            if (k == 0) {
                better_ant           = k;
                better_predict_route = predict_route;

            } else {
                if (predict_route < better_predict_route) {
                    better_ant           = k;
                    better_predict_route = predict_route;
                }
            }
        }

        return better_ant;
    }

    /**
     * Método que calcula a previsão da rota de uma formiga caso adicione o nó escolhido
     * @param k
     * @param next_node
     * @return
     */
    public double predict_route(int k, int next_node) {
        // rota parcial da formiga em análise:
        double part_route = this.ants[k].cost_route();

        // distância entre o nó atual da formiga em análise e o próximo nó escolhido:
        double dist_choosed = this.cost_matrix.get_value(this.ants[k].current_node(), next_node);

        // distância entre o próximo nó e o nó de partida da formiga em análise:
        double dist_starting = cost_matrix.get_value(next_node, this.ants[k].starting_node());

        return part_route + dist_choosed + dist_starting;

    }
}
