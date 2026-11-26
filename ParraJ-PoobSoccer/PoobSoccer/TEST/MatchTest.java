package TEST;

import DOMINIO.Ball;
import DOMINIO.GameCharacter;
import DOMINIO.Goal;
import DOMINIO.MatchType;
import DOMINIO.PoobSoccer;

// ParraJ
// Pruebas de unidad de los tipos de partido (por tiempo y por goles) de PoobSoccer.
public class MatchTest
{
    // Avanza el juego n pasos
    private static void tick(PoobSoccer g, int n)
    {
        for (int i = 0; i < n; i++) {
            g.update();
        }
    }

    // Gol del jugador 1 (el balon entra a la cancha derecha)
    private static void goalForPlayer1(PoobSoccer g)
    {
        g.getBall().reset(PoobSoccer.WIDTH - Goal.WIDTH - 20, PoobSoccer.HEIGHT - Ball.RADIUS);
        g.getBall().setVelocity(10, 0);
        tick(g, 20);
    }

    // Gol del jugador 2 (el balon entra a la cancha izquierda)
    private static void goalForPlayer2(PoobSoccer g)
    {
        g.getBall().reset(Goal.WIDTH + 20, PoobSoccer.HEIGHT - Ball.RADIUS);
        g.getBall().setVelocity(-10, 0);
        tick(g, 20);
    }

    private static PoobSoccer game(MatchType type, int target)
    {
        return new PoobSoccer(type, target, GameCharacter.neutral(), GameCharacter.neutral());
    }

    // Los tiempos permitidos deberian ser 60, 90 y 120 segundos y los goles 3, 6 y 9
    static void onlyTheAllowedValuesShouldBeAccepted()
    {
        for (int t : PoobSoccer.TIME_OPTIONS) {
            Check.equal("time " + t, t, game(MatchType.TIME, t).getTimeLeft());
        }
        for (int goals : PoobSoccer.GOAL_OPTIONS) {
            Check.equal("goals " + goals, goals, game(MatchType.GOALS, goals).getTarget());
        }
        boolean rejectedTime = false;
        boolean rejectedGoals = false;
        try {
            game(MatchType.TIME, 45);
        } catch (IllegalArgumentException e) {
            rejectedTime = true;
        }
        try {
            game(MatchType.GOALS, 4);
        } catch (IllegalArgumentException e) {
            rejectedGoals = true;
        }
        Check.that("45 s rejected", rejectedTime);
        Check.that("4 goals rejected", rejectedGoals);
    }

    // Por tiempo gana quien tenga mas goles al terminar
    static void timeMatchShouldBeWonByTheLeader()
    {
        PoobSoccer g = game(MatchType.TIME, 60);
        goalForPlayer2(g);
        g.advanceTime(60);
        Check.that("over", g.isGameOver());
        Check.equal("winner", 2, g.getWinner());
    }

    // Por tiempo con empate deberia haber muerte subita y ganar el primero que marque
    static void tiedTimeMatchShouldGoToSuddenDeath()
    {
        PoobSoccer g = game(MatchType.TIME, 60);
        g.advanceTime(60);
        Check.that("not over", !g.isGameOver());
        Check.that("sudden death", g.isSuddenDeath());
        goalForPlayer1(g);
        Check.that("over after the golden goal", g.isGameOver());
        Check.equal("winner", 1, g.getWinner());
    }

    // Por goles gana el primero que llegue a la meta, sin limite de tiempo
    static void goalsMatchShouldEndWhenSomeoneReachesTheTarget()
    {
        PoobSoccer g = game(MatchType.GOALS, 3);
        g.advanceTime(1000);
        Check.that("no time limit", !g.isGameOver());
        goalForPlayer1(g);
        goalForPlayer1(g);
        goalForPlayer2(g);
        Check.that("not over at 2-1", !g.isGameOver());
        goalForPlayer1(g);
        Check.that("over at 3-1", g.isGameOver());
        Check.equal("winner", 1, g.getWinner());
    }

    // Corre todas las pruebas y devuelve cuantas fallaron
    public static int run()
    {
        System.out.println("MatchTest");
        onlyTheAllowedValuesShouldBeAccepted();
        timeMatchShouldBeWonByTheLeader();
        tiedTimeMatchShouldGoToSuddenDeath();
        goalsMatchShouldEndWhenSomeoneReachesTheTarget();
        return Check.summary();
    }

    public static void main(String[] args)
    {
        Check.reset();
        System.exit(run() == 0 ? 0 : 1);
    }
}
