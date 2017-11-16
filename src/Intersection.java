/**
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class Intersection {
    int n;
    Node nodes_vector[];

    public Intersection(int n, Node nodes_vector[]) {
        this.n = n;
        this.nodes_vector = nodes_vector.clone();
    }

    /**
     * Método que retorna verdadeiro se as arestas cruzarem
     * @param ai
     * @param aj
     * @param bi
     * @param bj
     * @return
     */
    public boolean verify(int ai, int aj, int bi, int bj) {
        double ai_x = nodes_vector[ai].x();
        double ai_y = nodes_vector[ai].y();
        double aj_x = nodes_vector[aj].x();
        double aj_y = nodes_vector[aj].y();
        double bi_x = nodes_vector[bi].x();
        double bi_y = nodes_vector[bi].y();
        double bj_x = nodes_vector[bj].x();
        double bj_y = nodes_vector[bj].y();
//-----          cout << "  " << ai << " -> " << aj << "  X  " << bi << " -> " << bj << "  =  ";

        boolean intersect = true;

        // se as arestas tiverem extremos em comum, não se intersectam
        if ((ai == aj) || (ai == bi) || (ai == bj) || (aj == bi) || (aj == bj) || (bi == bj)) {
            intersect = false;

        } else {  // as arestas não tem extremos em comum
            double aux = (bj_x - bi_x) * (aj_y - ai_y) - (bj_y - bi_y) * (aj_x - ai_x);

            if (aux == 0.0) {  // se aux = 0.0, as retas são paralelas
                intersect = false;

            } else {  // as retas não são paralelas

                // definindo as coordenadas do ponto de intersecção:
                double par_a = ((bj_x - bi_x) * (bi_y - ai_y) - (bj_y - bi_y) * (bi_x - ai_x)) / aux;  // paramêtro da equação da reta definida pela aresta a
                double intersect_point_x = ai_x + (aj_x - ai_x) * par_a;  // coordenada x do ponto de intersecção
                double intersect_point_y = ai_y + (aj_y - ai_y) * par_a;  // coordenada y do ponto de intersecção

                boolean intersect_edge_a = false;
                boolean intersect_edge_b = false;

                // verificando se o ponto de intersecção pertence à aresta a:
                double larger_ax;
                double lower_ax;

                if (ai_x > aj_x) {
                    larger_ax = ai_x;
                    lower_ax = aj_x;
                } else {
                    larger_ax = aj_x;
                    lower_ax = ai_x;
                }

                double larger_ay;
                double lower_ay;

                if (ai_y > aj_y) {
                    larger_ay = ai_y;
                    lower_ay = aj_y;
                } else {
                    larger_ay = aj_y;
                    lower_ay = ai_y;
                }

                if ((intersect_point_x >= lower_ax) && (intersect_point_x <= larger_ax)) {  // intersecta em x
                    if ((intersect_point_y >= lower_ay) && (intersect_point_y <= larger_ay)) { // intersecta em y
                        intersect_edge_a = true;

                    } else {
                        intersect_edge_a = false;

                    }
                } else {

                    intersect_edge_a = false;
                }

                if (intersect_edge_a) { // o ponto de intersecção pertence à aresta a; verificar em b:
                    // verificando se o ponto de intersecção pertence à aresta b:
                    double larger_bx;
                    double lower_bx;

                    if (bi_x > bj_x) {
                        larger_bx = bi_x;
                        lower_bx = bj_x;
                    } else {
                        larger_bx = bj_x;
                        lower_bx = bi_x;
                    }

                    double larger_by;
                    double lower_by;

                    if (bi_y > bj_y) {
                        larger_by = bi_y;
                        lower_by = bj_y;
                    } else {
                        larger_by = bj_y;
                        lower_by = bi_y;
                    }

                    if ((intersect_point_x >= lower_bx) && (intersect_point_x <= larger_bx)) {  // intersecta em x
                        if ((intersect_point_y >= lower_by) && (intersect_point_y <= larger_by)) { // intersecta em y
                            intersect_edge_b = true;

                        } else {
                            intersect_edge_b = false;

                        }
                    } else {
                        intersect_edge_b = false;

                    }
                }

                if (intersect_edge_b) {  // as arestas se cruzam
                    intersect = true;
                } else {
                    intersect = false;
                }
            }
        }
        return intersect;
    }

}
