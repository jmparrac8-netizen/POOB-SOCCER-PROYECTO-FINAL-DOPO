package DOMINIO;

// ParraJ
// Balon: un objeto redondo con fisica. (x, y) es el CENTRO del balon y y crece hacia abajo.
// Cae por la gravedad, rebota en el piso, el techo y las paredes, va perdiendo velocidad
// por el roce con el aire y con el piso, y rebota contra rectangulos (jugadores y travesanos).
// No sabe dibujarse: de eso se encarga la presentacion.
public class Ball
{
    public static final double RADIUS = 20;

    private static final double GRAVITY = 0.5;
    private static final double FLOOR_BOUNCE = 0.75;
    private static final double WALL_BOUNCE = 0.8;
    private static final double OBJECT_BOUNCE = 0.8;
    private static final double AIR_DRAG = 0.999;
    private static final double FLOOR_FRICTION = 0.99;
    private static final double REST_SPEED = 1.0;
    private static final double STOP_SPEED = 0.05;
    private static final double MAX_SPEED = 25;
    private static final double OVERLAP_MARGIN = 0.5;
    private static final double POP_SPEED = 6;

    private double x;
    private double y;
    private double vx;
    private double vy;
    private boolean onGround;

    // Crea un balon quieto con el centro en la posicion dada
    public Ball(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    // Devuelve la posicion x del centro
    public double getX()
    {
        return x;
    }

    // Devuelve la posicion y del centro
    public double getY()
    {
        return y;
    }

    // Devuelve la velocidad horizontal
    public double getVx()
    {
        return vx;
    }

    // Devuelve la velocidad vertical (positiva hacia abajo)
    public double getVy()
    {
        return vy;
    }

    // Indica si el balon esta apoyado en el piso
    public boolean isOnGround()
    {
        return onGround;
    }

    // Pone el balon quieto en la posicion dada
    public void reset(double x, double y)
    {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.onGround = false;
    }

    // Le da al balon una velocidad (por ejemplo, para un saque o una prueba)
    public void setVelocity(double vx, double vy)
    {
        this.vx = vx;
        this.vy = vy;
        limitSpeed();
    }

    // Avanza al balon un paso: gravedad, roce, movimiento y rebote contra los bordes del campo
    public void update(double fieldWidth, double groundY)
    {
        vy += GRAVITY;
        vx *= AIR_DRAG;
        limitSpeed();
        x += vx;
        y += vy;
        keepInside(fieldWidth, groundY);
        if (onGround) {
            vx *= FLOOR_FRICTION;
            if (Math.abs(vx) < STOP_SPEED) {
                vx = 0;
            }
        }
    }

    // Mantiene al balon dentro del campo: rebota en las paredes, el techo y el piso
    public void keepInside(double fieldWidth, double groundY)
    {
        if (x - RADIUS < 0) {
            x = RADIUS;
            if (vx < 0) {
                vx = -vx * WALL_BOUNCE;
            }
        }
        if (x + RADIUS > fieldWidth) {
            x = fieldWidth - RADIUS;
            if (vx > 0) {
                vx = -vx * WALL_BOUNCE;
            }
        }
        if (y - RADIUS < 0) {
            y = RADIUS;
            if (vy < 0) {
                vy = -vy * FLOOR_BOUNCE;
            }
        }
        onGround = false;
        if (y + RADIUS >= groundY) {
            y = groundY - RADIUS;
            onGround = true;
            if (vy > 0) {
                vy = -vy * FLOOR_BOUNCE;
                if (Math.abs(vy) < REST_SPEED) {
                    vy = 0;
                }
            }
        }
    }

    // Rebota contra un rectangulo que se mueve con (rvx, rvy). El rectangulo no se ve afectado
    // (es mas pesado que el balon). Devuelve true si hubo choque
    public boolean collideWithRectangle(double rx, double ry, double rw, double rh, double rvx, double rvy)
    {
        double closestX = clamp(x, rx, rx + rw);
        double closestY = clamp(y, ry, ry + rh);
        double dx = x - closestX;
        double dy = y - closestY;
        double distance2 = dx * dx + dy * dy;
        if (distance2 >= RADIUS * RADIUS) {
            return false;
        }

        double nx;
        double ny;
        double penetration;
        if (distance2 > 1e-12) {
            double distance = Math.sqrt(distance2);
            nx = dx / distance;
            ny = dy / distance;
            penetration = RADIUS - distance;
        } else {
            // El centro quedo adentro del rectangulo: sale por la cara mas cercana
            double toLeft = x - rx;
            double toRight = rx + rw - x;
            double toTop = y - ry;
            double toBottom = ry + rh - y;
            double min = Math.min(Math.min(toLeft, toRight), Math.min(toTop, toBottom));
            nx = 0;
            ny = 0;
            if (min == toTop) {
                ny = -1;
            } else if (min == toBottom) {
                ny = 1;
            } else if (min == toLeft) {
                nx = -1;
            } else {
                nx = 1;
            }
            penetration = min + RADIUS;
        }

        x += nx * penetration;
        y += ny * penetration;

        double relativeSpeed = (vx - rvx) * nx + (vy - rvy) * ny;
        if (relativeSpeed < 0) {
            double impulse = -(1 + OBJECT_BOUNCE) * relativeSpeed;
            vx += impulse * nx;
            vy += impulse * ny;
            limitSpeed();
        }
        return true;
    }

    // Indica si el balon se esta metiendo claramente en el rectangulo
    public boolean overlapsRectangle(double rx, double ry, double rw, double rh)
    {
        double dx = x - clamp(x, rx, rx + rw);
        double dy = y - clamp(y, ry, ry + rh);
        double limit = RADIUS - OVERLAP_MARGIN;
        return dx * dx + dy * dy < limit * limit;
    }

    // Saca al balon por encima del rectangulo (cuando quedo apretado contra una pared)
    public void popOver(double ry)
    {
        y = ry - RADIUS;
        vy = -Math.max(POP_SPEED, Math.abs(vy));
    }

    // Evita velocidades tan grandes que el balon atraviese los objetos
    private void limitSpeed()
    {
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed > MAX_SPEED) {
            vx = vx * MAX_SPEED / speed;
            vy = vy * MAX_SPEED / speed;
        }
    }

    // Deja un numero dentro del rango [min, max]
    private static double clamp(double value, double min, double max)
    {
        return Math.max(min, Math.min(max, value));
    }
}
