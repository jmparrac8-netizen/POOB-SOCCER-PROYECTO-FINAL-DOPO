package TEST;

import DOMINIO.Ball;
import DOMINIO.Goal;
import DOMINIO.Player;
import DOMINIO.PoobSoccer;

// ParraJ
// Pruebas de unidad de PoobSoccer.
public class PoobSoccerTest
{
    // Avanza el juego n pasos
    private static void tick(PoobSoccer g, int n)
    {
        for (int i = 0; i < n; i++) {
            g.update();
        }
    }

    // El balon deberia empezar en el centro de abajo
    static void gameShouldStartWithBallAtBottomCenter()
    {
        PoobSoccer g = new PoobSoccer();
        Check.equal("ball x", PoobSoccer.WIDTH / 2, g.getBall().getX());
        Check.equal("ball y", PoobSoccer.HEIGHT - Ball.RADIUS, g.getBall().getY());
    }

    // Los jugadores deberian empezar en el piso, cada uno en su lado
    static void playersShouldStartOnTheGroundOnOppositeSides()
    {
        PoobSoccer g = new PoobSoccer();
        Check.that("player1 left", g.getPlayer1().getX() < PoobSoccer.WIDTH / 2);
        Check.that("player2 right", g.getPlayer2().getX() > PoobSoccer.WIDTH / 2);
        Check.that("player1 on ground", g.getPlayer1().isOnGround());
        Check.that("player2 on ground", g.getPlayer2().isOnGround());
    }

    // Deberia haber una cancha a cada costado
    static void gameShouldHaveOneGoalOnEachSide()
    {
        PoobSoccer g = new PoobSoccer();
        Check.equal("left goal x", 0, g.getLeftGoal().getX());
        Check.equal("right goal x", PoobSoccer.WIDTH - Goal.WIDTH, g.getRightGoal().getX());
    }

    // Al empezar el marcador deberia estar en cero y el tiempo completo
    static void gameShouldStartWithZeroScoreAndFullTime()
    {
        PoobSoccer g = new PoobSoccer();
        Check.equal("score1", 0, g.getScore1());
        Check.equal("score2", 0, g.getScore2());
        Check.equal("time left", PoobSoccer.GAME_SECONDS, g.getTimeLeft());
        Check.equal("game seconds", 90, PoobSoccer.GAME_SECONDS);
        Check.that("not over", !g.isGameOver());
        Check.equal("no winner", 0, g.getWinner());
    }

    // El cronometro deberia descontar el tiempo
    static void clockShouldCountDown()
    {
        PoobSoccer g = new PoobSoccer();
        g.advanceTime(30);
        Check.equal("time after 30s", 60, g.getTimeLeft());
        Check.that("not over yet", !g.isGameOver());
    }

    // El juego deberia terminar al llegar a cero
    static void gameShouldEndWhenClockReachesZero()
    {
        PoobSoccer g = new PoobSoccer();
        g.advanceTime(90);
        Check.that("game over", g.isGameOver());
        Check.equal("time zero", 0, g.getTimeLeft());
    }

    // El cronometro no deberia quedar negativo
    static void clockShouldNeverGoNegative()
    {
        PoobSoccer g = new PoobSoccer();
        g.advanceTime(500);
        Check.equal("time not negative", 0, g.getTimeLeft());
        Check.that("game over", g.isGameOver());
    }

    // Despues de terminar no deberia moverse nada
    static void nothingShouldMoveAfterGameOver()
    {
        PoobSoccer g = new PoobSoccer();
        double x = g.getPlayer1().getX();
        g.advanceTime(90);
        g.getPlayer1().setMovingRight(true);
        g.update();
        Check.equal("player1 still", x, g.getPlayer1().getX());
    }

    // El balon deberia quedarse quieto en el piso si nadie lo toca
    static void ballShouldRestOnTheFloorIfNobodyTouchesIt()
    {
        PoobSoccer g = new PoobSoccer();
        tick(g, 100);
        Check.equal("ball x", PoobSoccer.WIDTH / 2, g.getBall().getX());
        Check.equal("ball y", PoobSoccer.HEIGHT - Ball.RADIUS, g.getBall().getY());
        Check.equal("no goals", 0, g.getScore1() + g.getScore2());
    }

    // Un balon en el aire deberia caer mientras corre el juego
    static void ballInTheAirShouldFallDuringTheGame()
    {
        PoobSoccer g = new PoobSoccer();
        g.getBall().reset(600, 100);
        tick(g, 10);
        Check.that("ball fell", g.getBall().getY() > 100);
    }

    // Un jugador que camina hacia el balon deberia empujarlo
    static void walkingPlayerShouldPushTheBall()
    {
        PoobSoccer g = new PoobSoccer();
        double startX = g.getPlayer1().getX() + Player.WIDTH + Ball.RADIUS + 30;
        g.getBall().reset(startX, PoobSoccer.HEIGHT - Ball.RADIUS);
        g.getPlayer1().setMovingRight(true);
        tick(g, 30);
        Check.that("ball pushed right", g.getBall().getX() > startX + 20);
        Check.that("ball moving right", g.getBall().getVx() > 0);
    }

    // Un balon que cae sobre un jugador quieto deberia rebotar y quedar encima, sin atravesarlo
    static void ballShouldLandOnAStillPlayer()
    {
        PoobSoccer g = new PoobSoccer();
        Player p = g.getPlayer1();
        g.getBall().reset(p.getX() + Player.WIDTH / 2, 300);
        boolean crossed = false;
        for (int i = 0; i < 400; i++) {
            g.update();
            if (g.getBall().getY() + Ball.RADIUS > p.getY() + 1) {
                crossed = true;
            }
        }
        Check.that("ball never goes through the player", !crossed);
    }

    // El balon deberia rebotar en el travesano sin contar como gol
    static void ballShouldBounceOnTheCrossbar()
    {
        PoobSoccer g = new PoobSoccer();
        g.getBall().reset(Goal.WIDTH / 2, 300);
        tick(g, 400);
        Check.equal("no goal for player 2", 0, g.getScore2());
        Check.that("ball stays on top", g.getBall().getY() <= g.getLeftGoal().getY() - Ball.RADIUS + 1);
    }

    // Si el balon entra a la cancha derecha, gol del jugador 1
    static void ballInTheRightGoalShouldScoreForPlayer1()
    {
        PoobSoccer g = new PoobSoccer();
        g.getBall().reset(PoobSoccer.WIDTH - Goal.WIDTH - 20, PoobSoccer.HEIGHT - Ball.RADIUS);
        g.getBall().setVelocity(10, 0);
        tick(g, 20);
        Check.equal("score1", 1, g.getScore1());
        Check.equal("score2", 0, g.getScore2());
        Check.equal("winner", 1, g.getWinner());
    }

    // Si el balon entra a la cancha izquierda, gol del jugador 2
    static void ballInTheLeftGoalShouldScoreForPlayer2()
    {
        PoobSoccer g = new PoobSoccer();
        g.getBall().reset(Goal.WIDTH + 20, PoobSoccer.HEIGHT - Ball.RADIUS);
        g.getBall().setVelocity(-10, 0);
        tick(g, 20);
        Check.equal("score1", 0, g.getScore1());
        Check.equal("score2", 1, g.getScore2());
        Check.equal("winner", 2, g.getWinner());
    }

    // Despues de un gol el balon y los jugadores deberian volver a su lugar de salida
    static void afterAGoalEverythingShouldGoBackToTheStart()
    {
        PoobSoccer g = new PoobSoccer();
        double p1x = g.getPlayer1().getX();
        double p2x = g.getPlayer2().getX();
        g.getPlayer1().setMovingLeft(true);
        tick(g, 10);
        g.getBall().reset(PoobSoccer.WIDTH - Goal.WIDTH - 20, PoobSoccer.HEIGHT - Ball.RADIUS);
        g.getBall().setVelocity(10, 0);
        for (int i = 0; i < 20 && g.getScore1() == 0; i++) {
            g.update();
        }
        Check.equal("score1", 1, g.getScore1());
        Check.equal("ball x", PoobSoccer.WIDTH / 2, g.getBall().getX());
        Check.equal("ball y", PoobSoccer.HEIGHT - Ball.RADIUS, g.getBall().getY());
        Check.equal("ball vx", 0, g.getBall().getVx());
        Check.equal("player1 x", p1x, g.getPlayer1().getX());
        Check.equal("player2 x", p2x, g.getPlayer2().getX());
    }

    // Avanza el juego n pasos manteniendo el balon lejos arriba, para probar solo a los jugadores
    private static void tickWithoutBall(PoobSoccer g, int n)
    {
        for (int i = 0; i < n; i++) {
            g.getBall().reset(PoobSoccer.WIDTH / 2, 30);
            g.update();
        }
    }

    // Dos jugadores que caminan uno hacia el otro deberian chocar y no atravesarse
    static void playersShouldCollideInsteadOfGoingThroughEachOther()
    {
        PoobSoccer g = new PoobSoccer();
        g.getPlayer1().setMovingRight(true);
        g.getPlayer2().setMovingLeft(true);
        tickWithoutBall(g, 200);
        Player p1 = g.getPlayer1();
        Player p2 = g.getPlayer2();
        Check.that("player1 stays on the left", p1.getX() + Player.WIDTH <= p2.getX() + 1e-9);
        Check.that("they are touching", p2.getX() - (p1.getX() + Player.WIDTH) < 1e-9);
    }

    // Un jugador que camina contra el otro no deberia moverlo
    static void aPlayerShouldNotPushTheOtherOne()
    {
        PoobSoccer g = new PoobSoccer();
        double p2x = g.getPlayer2().getX();
        g.getPlayer1().setMovingRight(true);
        tickWithoutBall(g, 300);
        Check.equal("player2 not moved", p2x, g.getPlayer2().getX());
        Check.equal("player1 stopped at player2", p2x - Player.WIDTH, g.getPlayer1().getX());
    }

    // Con el juego terminado el balon tampoco deberia moverse
    static void ballShouldNotMoveAfterGameOver()
    {
        PoobSoccer g = new PoobSoccer();
        g.getBall().reset(600, 100);
        g.advanceTime(90);
        tick(g, 10);
        Check.equal("ball y", 100, g.getBall().getY());
    }

    // Jugando con los dos jugadores moviendose y saltando, el balon nunca deberia salirse del campo
    static void ballShouldStayInsideTheFieldWhileTheGameRuns()
    {
        PoobSoccer g = new PoobSoccer();
        boolean inside = true;
        g.getBall().setVelocity(9, -14);
        for (int i = 0; i < 6000; i++) {
            if (i % 40 == 0) {
                g.getPlayer1().setMovingRight(!g.getPlayer1().isOnGround() || i % 80 == 0);
                g.getPlayer1().setMovingLeft(i % 120 == 0);
                g.getPlayer1().jump();
                g.getPlayer2().setMovingLeft(i % 80 == 40);
                g.getPlayer2().setMovingRight(i % 160 == 0);
                g.getPlayer2().jump();
            }
            if (i % 211 == 0) {
                g.getBall().setVelocity((i % 9) * 3 - 12, -(i % 6) * 3);
            }
            g.update();
            Ball b = g.getBall();
            if (b.getX() < Ball.RADIUS - 1e-9 || b.getX() > PoobSoccer.WIDTH - Ball.RADIUS + 1e-9
                    || b.getY() < Ball.RADIUS - 1e-9 || b.getY() > PoobSoccer.HEIGHT - Ball.RADIUS + 1e-9) {
                inside = false;
            }
        }
        Check.that("ball always inside", inside);
    }

    // Corre todas las pruebas y devuelve cuantas fallaron
    public static int run()
    {
        System.out.println("PoobSoccerTest");
        gameShouldStartWithBallAtBottomCenter();
        playersShouldStartOnTheGroundOnOppositeSides();
        gameShouldHaveOneGoalOnEachSide();
        gameShouldStartWithZeroScoreAndFullTime();
        clockShouldCountDown();
        gameShouldEndWhenClockReachesZero();
        clockShouldNeverGoNegative();
        nothingShouldMoveAfterGameOver();
        ballShouldRestOnTheFloorIfNobodyTouchesIt();
        ballInTheAirShouldFallDuringTheGame();
        walkingPlayerShouldPushTheBall();
        ballShouldLandOnAStillPlayer();
        ballShouldBounceOnTheCrossbar();
        ballInTheRightGoalShouldScoreForPlayer1();
        ballInTheLeftGoalShouldScoreForPlayer2();
        afterAGoalEverythingShouldGoBackToTheStart();
        playersShouldCollideInsteadOfGoingThroughEachOther();
        aPlayerShouldNotPushTheOtherOne();
        ballShouldNotMoveAfterGameOver();
        ballShouldStayInsideTheFieldWhileTheGameRuns();
        return Check.summary();
    }

    public static void main(String[] args)
    {
        Check.reset();
        System.exit(run() == 0 ? 0 : 1);
    }
}
