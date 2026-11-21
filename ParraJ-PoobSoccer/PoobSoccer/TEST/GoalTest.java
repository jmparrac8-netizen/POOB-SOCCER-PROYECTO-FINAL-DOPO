package TEST;

import DOMINIO.Goal;

// ParraJ
// Pruebas de unidad de Goal.
public class GoalTest
{
    private static final double GROUND = 600;

    // La cancha deberia quedar apoyada en el piso
    static void goalShouldBePlacedOnTheGround()
    {
        Goal g = new Goal(0, GROUND);
        Check.equal("goal x", 0, g.getX());
        Check.equal("goal y", GROUND - Goal.HEIGHT, g.getY());
    }

    // La cancha derecha deberia quedar en el costado derecho
    static void rightGoalShouldBePlacedOnTheRightSide()
    {
        Goal g = new Goal(1200 - Goal.WIDTH, GROUND);
        Check.equal("right goal x", 1200 - Goal.WIDTH, g.getX());
        Check.equal("right goal y", GROUND - Goal.HEIGHT, g.getY());
    }

    // Un punto por debajo del travesano y dentro de la cancha deberia contar como adentro
    static void pointInsideTheGoalShouldBeInside()
    {
        Goal left = new Goal(0, GROUND);
        Goal right = new Goal(1200 - Goal.WIDTH, GROUND);
        Check.that("left inside", left.isInside(35, 500));
        Check.that("left inside on the floor", left.isInside(30, GROUND - 20));
        Check.that("right inside", right.isInside(1165, 500));
    }

    // Un punto afuera de la cancha o por encima del travesano no deberia contar
    static void pointOutsideTheGoalShouldNotBeInside()
    {
        Goal left = new Goal(0, GROUND);
        Goal right = new Goal(1200 - Goal.WIDTH, GROUND);
        Check.that("left: beside the goal", !left.isInside(100, 500));
        Check.that("left: over the crossbar", !left.isInside(35, left.getY() + Goal.CROSSBAR - 1));
        Check.that("left: above the goal", !left.isInside(35, 100));
        Check.that("right: beside the goal", !right.isInside(1100, 500));
    }

    // Corre todas las pruebas y devuelve cuantas fallaron
    public static int run()
    {
        System.out.println("GoalTest");
        goalShouldBePlacedOnTheGround();
        rightGoalShouldBePlacedOnTheRightSide();
        pointInsideTheGoalShouldBeInside();
        pointOutsideTheGoalShouldNotBeInside();
        return Check.summary();
    }

    public static void main(String[] args)
    {
        Check.reset();
        System.exit(run() == 0 ? 0 : 1);
    }
}
