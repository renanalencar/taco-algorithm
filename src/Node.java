/**
 * Classe que representa um nó euclidiano do grafo
 *
 * @author renanalencar
 * @version 1.0
 * @since 2017-11-01
 *
 */
public class Node {     // vértice de uma grafo euclidiano
    private double cx;  // coordenada plana x
    private double cy;  // coordenada plana y

    public Node() {

    }

    public void create(double x, double y) {
        this.cx = x;
        this.cy = y;
    }

    public double x() {
        return this.cx;
    }

    public double y() {
        return this.cy;
    }
}
