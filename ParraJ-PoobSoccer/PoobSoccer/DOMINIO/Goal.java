package DOMINIO;

// ParraJ
// Cancha ubicada a un costado del campo, apoyada en el piso. (x, y) es su esquina superior izquierda.
// La parte de arriba es un travesano solido (el balon rebota en el); por debajo del travesano
// la cancha esta abierta y, si el balon entra, es gol.
// No sabe dibujarse: de eso se encarga la presentacion.
public class Goal
{
    public static final double WIDTH = 70;
    public static final double HEIGHT = 180;
    public static final double CROSSBAR = 12;

    private final double x;
    private final double y;

    // Crea una cancha en la posicion x, apoyada en el piso
    public Goal(double x, double groundY)
    {
        this.x = x;
        this.y = groundY - HEIGHT;
    }

    // Devuelve la posicion x de la esquina superior izquierda
    public double getX()
    {
        return x;
    }

    // Devuelve la posicion y de la esquina superior izquierda
    public double getY()
    {
        return y;
    }

    // Indica si un punto (el centro del balon) esta adentro de la cancha, por debajo del travesano
    public boolean isInside(double px, double py)
    {
        return px >= x && px <= x + WIDTH && py > y + CROSSBAR;
    }
}
