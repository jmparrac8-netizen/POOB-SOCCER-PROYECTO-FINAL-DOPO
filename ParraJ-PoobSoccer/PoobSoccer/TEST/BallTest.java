package TEST;

import DOMINIO.Ball;

// ParraJ
// Pruebas de unidad de Ball (posicion, gravedad, rebotes, roce y choques con rectangulos).
public class BallTest
{
    private static final double FIELD_W = 1200;
    private static final double GROUND = 600;

    // Avanza al balon n pasos
    private static void tick(Ball b, int n)
    {
        for (int i = 0; i < n; i++) {
            b.update(FIELD_W, GROUND);
        }
    }

    // El balon deberia quedar con el centro en la posicion dada y quieto
    static void ballShouldBeCreatedAtGivenPosition()
    {
        Ball b = new Ball(600, 580);
        Check.equal("ball x", 600, b.getX());
        Check.equal("ball y", 580, b.getY());
        Check.equal("ball vx", 0, b.getVx());
        Check.equal("ball vy", 0, b.getVy());
    }

    // El radio del balon deberia ser 20
    static void ballShouldHaveRadiusTwenty()
    {
        Check.equal("ball radius", 20, Ball.RADIUS);
    }

    // Un balon apoyado en el piso deberia quedarse quieto
    static void ballOnTheFloorShouldStayStill()
    {
        Ball b = new Ball(600, GROUND - Ball.RADIUS);
        tick(b, 200);
        Check.equal("rest x", 600, b.getX());
        Check.equal("rest y", GROUND - Ball.RADIUS, b.getY());
        Check.equal("rest vy", 0, b.getVy());
        Check.that("rest on ground", b.isOnGround());
    }

    // En el aire deberia caer por la gravedad
    static void ballInTheAirShouldFall()
    {
        Ball b = new Ball(600, 100);
        tick(b, 5);
        Check.that("falls down", b.getY() > 100);
        Check.that("speeds up downwards", b.getVy() > 0);
        Check.that("not on ground in the air", !b.isOnGround());
    }

    // Al caer deberia rebotar en el piso, subir menos que antes y no hundirse
    static void ballShouldBounceOnTheFloorAndLoseEnergy()
    {
        Ball b = new Ball(600, 100);
        boolean bounced = false;
        boolean underFloor = false;
        double highestAfterBounce = Double.MAX_VALUE;
        for (int i = 0; i < 400; i++) {
            double before = b.getVy();
            b.update(FIELD_W, GROUND);
            if (before > 0 && b.getVy() < 0) {
                bounced = true;
            }
            if (bounced && b.getY() < highestAfterBounce) {
                highestAfterBounce = b.getY();
            }
            if (b.getY() + Ball.RADIUS > GROUND + 1e-9) {
                underFloor = true;
            }
            if (bounced && i > 60) {
                break;
            }
        }
        Check.that("bounces", bounced);
        Check.that("never under the floor", !underFloor);
        Check.that("rebounds lower than it fell", highestAfterBounce > 100);
    }

    // Despues de muchos rebotes deberia quedar quieto en el piso
    static void ballShouldEventuallyComeToRest()
    {
        Ball b = new Ball(600, 100);
        tick(b, 3000);
        Check.equal("final y", GROUND - Ball.RADIUS, b.getY());
        Check.equal("final vy", 0, b.getVy());
        Check.that("final on ground", b.isOnGround());
    }

    // Deberia rebotar en la pared izquierda
    static void ballShouldBounceOffTheLeftWall()
    {
        Ball b = new Ball(30, 300);
        b.setVelocity(-10, 0);
        tick(b, 3);
        Check.that("x inside", b.getX() >= Ball.RADIUS);
        Check.that("goes right after bounce", b.getVx() > 0);
    }

    // Deberia rebotar en la pared derecha
    static void ballShouldBounceOffTheRightWall()
    {
        Ball b = new Ball(FIELD_W - 30, 300);
        b.setVelocity(10, 0);
        tick(b, 3);
        Check.that("x inside", b.getX() <= FIELD_W - Ball.RADIUS);
        Check.that("goes left after bounce", b.getVx() < 0);
    }

    // Deberia rebotar en el techo
    static void ballShouldBounceOffTheCeiling()
    {
        Ball b = new Ball(600, 30);
        b.setVelocity(0, -15);
        tick(b, 2);
        Check.that("y inside", b.getY() >= Ball.RADIUS);
        Check.that("goes down after bounce", b.getVy() > 0);
    }

    // Rodando por el piso deberia frenarse hasta quedar quieto
    static void rollingBallShouldSlowDownAndStop()
    {
        Ball b = new Ball(300, GROUND - Ball.RADIUS);
        b.setVelocity(10, 0);
        tick(b, 40);
        Check.that("slower", b.getVx() < 10 && b.getVx() > 0);
        tick(b, 2000);
        Check.equal("stopped", 0, b.getVx());
    }

    // En el aire el roce con el aire deberia frenar un poco la velocidad horizontal
    static void airDragShouldSlowTheBallSlightly()
    {
        Ball b = new Ball(300, 300);
        b.setVelocity(10, 0);
        tick(b, 1);
        Check.that("slightly slower", b.getVx() < 10 && b.getVx() > 9.9);
    }

    // La velocidad no deberia pasar del maximo
    static void speedShouldBeLimited()
    {
        Ball b = new Ball(600, 300);
        b.setVelocity(100, 100);
        double speed = Math.sqrt(b.getVx() * b.getVx() + b.getVy() * b.getVy());
        Check.that("limited", speed <= 25 + 1e-9);
    }

    // Reiniciar deberia dejarlo quieto en la posicion dada
    static void resetShouldPlaceTheBallStill()
    {
        Ball b = new Ball(600, 300);
        b.setVelocity(8, -8);
        b.reset(100, 200);
        Check.equal("reset x", 100, b.getX());
        Check.equal("reset y", 200, b.getY());
        Check.equal("reset vx", 0, b.getVx());
        Check.equal("reset vy", 0, b.getVy());
    }

    // Contra un rectangulo quieto deberia rebotar con menos velocidad
    static void ballShouldBounceOffAStillRectangle()
    {
        Ball b = new Ball(85, 550);
        b.setVelocity(5, 0);
        boolean hit = b.collideWithRectangle(100, 500, 60, 100, 0, 0);
        Check.that("hit", hit);
        Check.equal("pushed out x", 80, b.getX());
        Check.equal("bounce speed", -4, b.getVx());
    }

    // Sin tocar el rectangulo no deberia pasar nada
    static void ballFarFromRectangleShouldNotCollide()
    {
        Ball b = new Ball(20, 550);
        b.setVelocity(5, 0);
        boolean hit = b.collideWithRectangle(100, 500, 60, 100, 0, 0);
        Check.that("no hit", !hit);
        Check.equal("same x", 20, b.getX());
        Check.equal("same vx", 5, b.getVx());
    }

    // Un rectangulo que se mueve hacia el balon deberia patearlo
    static void movingRectangleShouldKickTheBall()
    {
        Ball b = new Ball(85, 550);
        b.collideWithRectangle(100, 500, 60, 100, -6, 0);
        Check.equal("kicked speed", -10.8, b.getVx());
    }

    // Un rectangulo que se aleja no deberia frenar ni empujar al balon
    static void rectangleMovingAwayShouldNotAddSpeed()
    {
        Ball b = new Ball(85, 550);
        b.collideWithRectangle(100, 500, 60, 100, 6, 0);
        Check.equal("no impulse", 0, b.getVx());
    }

    // Si el centro queda adentro del rectangulo deberia salir por la cara mas cercana
    static void ballInsideARectangleShouldBePushedOut()
    {
        Ball b = new Ball(130, 550);
        boolean hit = b.collideWithRectangle(100, 500, 60, 100, 0, 0);
        Check.that("hit", hit);
        Check.that("no longer inside", !b.overlapsRectangle(100, 500, 60, 100));
        Check.equal("left face", 80, b.getX());
    }

    // Contra una esquina deberia salir en diagonal
    static void ballShouldBounceOffACorner()
    {
        Ball b = new Ball(90, 490);
        b.setVelocity(4, 4);
        boolean hit = b.collideWithRectangle(100, 500, 60, 100, 0, 0);
        Check.that("hit corner", hit);
        Check.that("goes left", b.getVx() < 0);
        Check.that("goes up", b.getVy() < 0);
    }

    // Detecta si esta metido en el rectangulo
    static void overlapShouldBeDetected()
    {
        Check.that("overlap", new Ball(110, 550).overlapsRectangle(100, 500, 60, 100));
        Check.that("no overlap", !new Ball(50, 550).overlapsRectangle(100, 500, 60, 100));
    }

    // Sacar al balon por arriba deberia dejarlo encima y subiendo
    static void popOverShouldPlaceTheBallAboveTheRectangle()
    {
        Ball b = new Ball(130, 550);
        b.popOver(500);
        Check.equal("above", 500 - Ball.RADIUS, b.getY());
        Check.that("going up", b.getVy() < 0);
    }

    // El balon nunca deberia salirse del campo, ni siquiera lanzado con fuerza
    static void ballShouldNeverLeaveTheField()
    {
        Ball b = new Ball(600, 300);
        boolean inside = true;
        for (int i = 0; i < 5000; i++) {
            if (i % 97 == 0) {
                b.setVelocity((i % 7) * 4 - 12, (i % 5) * 6 - 15);
            }
            b.update(FIELD_W, GROUND);
            if (b.getX() < Ball.RADIUS || b.getX() > FIELD_W - Ball.RADIUS
                    || b.getY() < Ball.RADIUS || b.getY() > GROUND - Ball.RADIUS) {
                inside = false;
            }
        }
        Check.that("always inside the field", inside);
    }

    // Corre todas las pruebas y devuelve cuantas fallaron
    public static int run()
    {
        System.out.println("BallTest");
        ballShouldBeCreatedAtGivenPosition();
        ballShouldHaveRadiusTwenty();
        ballOnTheFloorShouldStayStill();
        ballInTheAirShouldFall();
        ballShouldBounceOnTheFloorAndLoseEnergy();
        ballShouldEventuallyComeToRest();
        ballShouldBounceOffTheLeftWall();
        ballShouldBounceOffTheRightWall();
        ballShouldBounceOffTheCeiling();
        rollingBallShouldSlowDownAndStop();
        airDragShouldSlowTheBallSlightly();
        speedShouldBeLimited();
        resetShouldPlaceTheBallStill();
        ballShouldBounceOffAStillRectangle();
        ballFarFromRectangleShouldNotCollide();
        movingRectangleShouldKickTheBall();
        rectangleMovingAwayShouldNotAddSpeed();
        ballInsideARectangleShouldBePushedOut();
        ballShouldBounceOffACorner();
        overlapShouldBeDetected();
        popOverShouldPlaceTheBallAboveTheRectangle();
        ballShouldNeverLeaveTheField();
        return Check.summary();
    }

    public static void main(String[] args)
    {
        Check.reset();
        System.exit(run() == 0 ? 0 : 1);
    }
}
