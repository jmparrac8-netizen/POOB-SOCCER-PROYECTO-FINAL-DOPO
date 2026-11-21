package DOMINIO;

// ParraJ
// Jugador: se mueve de izquierda a derecha sobre el piso y puede saltar.
// El salto sube y baja solo por la gravedad.
// Dos jugadores no se pueden traspasar: cuando uno choca con el otro se detiene como contra una pared,
// y si cae encima de su cabeza se queda parado ahi.
// (x, y) es la esquina superior izquierda y y crece hacia abajo.
// No sabe dibujarse: de eso se encarga la presentacion.
public class Player
{
    public static final double WIDTH = 60;
    public static final double HEIGHT = 90;

    private static final double SPEED = 6;
    private static final double JUMP_SPEED = -16;
    private static final double GRAVITY = 0.8;
    private static final double EPSILON = 1e-9;

    private final double startX;
    private final double startY;
    private final String color;

    private double x;
    private double y;
    private double vx;
    private double vy;
    private boolean movingLeft;
    private boolean movingRight;
    private boolean onGround;

    // Crea un jugador del color dado en la posicion x, parado sobre el piso
    public Player(double x, double groundY, String color)
    {
        this.startX = x;
        this.startY = groundY - HEIGHT;
        this.x = startX;
        this.y = startY;
        this.onGround = true;
        this.color = color;
    }

    // Devuelve al jugador a su lugar de salida, quieto y parado en el piso.
    // Las teclas que sigan presionadas siguen valiendo
    public void reset()
    {
        x = startX;
        y = startY;
        vx = 0;
        vy = 0;
        onGround = true;
    }

    // Indica si el jugador debe moverse a la izquierda
    public void setMovingLeft(boolean movingLeft)
    {
        this.movingLeft = movingLeft;
    }

    // Indica si el jugador debe moverse a la derecha
    public void setMovingRight(boolean movingRight)
    {
        this.movingRight = movingRight;
    }

    // Hace saltar al jugador. No hace nada si ya esta en el aire
    public void jump()
    {
        if (onGround) {
            vy = JUMP_SPEED;
            onGround = false;
        }
    }

    // Avanza al jugador un paso: se mueve, no se sale del campo y cae por la gravedad
    public void update(double fieldWidth, double groundY)
    {
        vx = 0;
        if (movingLeft) {
            vx -= SPEED;
        }
        if (movingRight) {
            vx += SPEED;
        }
        x += vx;
        keepInsideField(fieldWidth);

        if (onGround && y + HEIGHT < groundY - EPSILON) {
            onGround = false;
        }
        if (!onGround) {
            vy += GRAVITY;
            y += vy;
            if (y + HEIGHT >= groundY) {
                y = groundY - HEIGHT;
                vy = 0;
                onGround = true;
            }
        }
    }

    // Choca contra otro jugador como contra una pared: este jugador se acomoda y el otro no se mueve.
    // Si venia de lado se detiene junto a el; si cae encima queda parado sobre su cabeza;
    // si sube y se golpea con sus pies, no lo atraviesa. Devuelve true si estaban encimados
    public boolean collideWith(Player other, double fieldWidth, double groundY)
    {
        double overlapX = Math.min(x + WIDTH - other.x, other.x + WIDTH - x);
        double overlapY = Math.min(y + HEIGHT - other.y, other.y + HEIGHT - y);
        if (overlapX <= 0 || overlapY <= 0) {
            return false;
        }

        boolean above = y < other.y;
        boolean vertical = overlapY <= overlapX;
        if (vertical && !above && other.y + 2 * HEIGHT > groundY + EPSILON) {
            vertical = false;
        }

        if (vertical && above) {
            landOn(other.y);
        } else if (vertical) {
            y = other.y + HEIGHT;
            if (vy < 0) {
                vy = 0;
            }
        } else {
            if (x < other.x) {
                x = other.x - WIDTH;
            } else {
                x = other.x + WIDTH;
            }
            vx = 0;
            keepInsideField(fieldWidth);
            if (x + WIDTH > other.x && x < other.x + WIDTH) {
                landOn(other.y);
            }
        }
        return true;
    }

    // Se apoya sobre una superficie que esta a la altura surfaceY (el piso o la cabeza de otro jugador)
    private void landOn(double surfaceY)
    {
        y = surfaceY - HEIGHT;
        vy = 0;
        onGround = true;
    }

    // No deja que el jugador se salga del campo por los costados
    private void keepInsideField(double fieldWidth)
    {
        if (x < 0) {
            x = 0;
        }
        if (x + WIDTH > fieldWidth) {
            x = fieldWidth - WIDTH;
        }
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

    // Devuelve la velocidad horizontal
    public double getVx()
    {
        return vx;
    }

    // Devuelve la velocidad vertical
    public double getVy()
    {
        return vy;
    }

    // Indica si el jugador esta en el piso
    public boolean isOnGround()
    {
        return onGround;
    }

    // Devuelve el color del jugador (el nombre que entiende el canvas)
    public String getColor()
    {
        return color;
    }
}
