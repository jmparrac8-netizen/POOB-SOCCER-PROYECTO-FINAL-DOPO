package DOMINIO;

// ParraJ
// Juego: un balon, dos jugadores y dos canchas, una a cada costado.
// La presentacion lee su estado y llama a update() para mover a los jugadores y al balon
// y a advanceTime() para el cronometro.
// El piso esta en la parte de abajo del campo (y = HEIGHT).
// El jugador 1 defiende la cancha izquierda y el jugador 2 la derecha.
// Si el balon entra a una cancha, es gol para el jugador que NO la defiende: el marcador
// sube y el balon y los jugadores vuelven a su lugar de salida.
public class PoobSoccer
{
    public static final double WIDTH = 1200;
    public static final double HEIGHT = 600;
    public static final double GAME_SECONDS = 90;

    private final Ball ball;
    private final Player player1;
    private final Player player2;
    private final Goal leftGoal;
    private final Goal rightGoal;

    private double timeLeft;
    private int score1;
    private int score2;
    private boolean gameOver;

    // Crea un juego nuevo: balon en el centro de abajo, jugadores a cada lado y marcador en cero
    public PoobSoccer()
    {
        ball = new Ball(WIDTH / 2, HEIGHT - Ball.RADIUS);
        player1 = new Player(WIDTH * 0.25 - Player.WIDTH / 2, HEIGHT, "blue");
        player2 = new Player(WIDTH * 0.75 - Player.WIDTH / 2, HEIGHT, "red");
        leftGoal = new Goal(0, HEIGHT);
        rightGoal = new Goal(WIDTH - Goal.WIDTH, HEIGHT);
        timeLeft = GAME_SECONDS;
        score1 = 0;
        score2 = 0;
        gameOver = false;
    }

    // Avanza el juego un paso: mueve a los jugadores (que chocan entre si como contra una pared),
    // luego al balon (con sus choques) y revisa si hubo gol
    public void update()
    {
        if (gameOver) {
            return;
        }
        player1.update(WIDTH, HEIGHT);
        player1.collideWith(player2, WIDTH, HEIGHT);
        player2.update(WIDTH, HEIGHT);
        player2.collideWith(player1, WIDTH, HEIGHT);
        updateBall();
        checkGoals();
    }

    // Mueve el balon y lo hace rebotar contra los travesanos y los jugadores
    private void updateBall()
    {
        ball.update(WIDTH, HEIGHT);
        collideWithCrossbar(leftGoal);
        collideWithCrossbar(rightGoal);
        collideWithPlayer(player1);
        collideWithPlayer(player2);
        ball.keepInside(WIDTH, HEIGHT);
        // Si un jugador lo dejo apretado contra una pared o el piso, sale por arriba
        popOverIfSqueezed(player1);
        popOverIfSqueezed(player2);
    }

    // El travesano es un rectangulo solido en la parte de arriba de la cancha
    private void collideWithCrossbar(Goal goal)
    {
        ball.collideWithRectangle(goal.getX(), goal.getY(), Goal.WIDTH, Goal.CROSSBAR, 0, 0);
    }

    // El jugador es un rectangulo que empuja al balon con su propia velocidad
    private void collideWithPlayer(Player player)
    {
        ball.collideWithRectangle(player.getX(), player.getY(), Player.WIDTH, Player.HEIGHT,
                player.getVx(), player.getVy());
    }

    // Saca al balon por encima del jugador si siguen encimados despues de acomodar todo
    private void popOverIfSqueezed(Player player)
    {
        if (ball.overlapsRectangle(player.getX(), player.getY(), Player.WIDTH, Player.HEIGHT)) {
            ball.popOver(player.getY());
        }
    }

    // Si el balon entro a una cancha, suma el gol al otro jugador y reinicia las posiciones
    private void checkGoals()
    {
        if (leftGoal.isInside(ball.getX(), ball.getY())) {
            score2++;
            kickOff();
        } else if (rightGoal.isInside(ball.getX(), ball.getY())) {
            score1++;
            kickOff();
        }
    }

    // Deja al balon quieto en el centro de abajo y a los jugadores en su lugar de salida
    private void kickOff()
    {
        ball.reset(WIDTH / 2, HEIGHT - Ball.RADIUS);
        player1.reset();
        player2.reset();
    }

    // Descuenta del cronometro los segundos reales que pasaron. Al llegar a cero termina el juego
    public void advanceTime(double seconds)
    {
        if (gameOver) {
            return;
        }
        timeLeft -= seconds;
        if (timeLeft <= 0) {
            timeLeft = 0;
            gameOver = true;
        }
    }

    // Devuelve 1 o 2 si ese jugador va ganando, o 0 si hay empate
    public int getWinner()
    {
        if (score1 > score2) {
            return 1;
        }
        if (score2 > score1) {
            return 2;
        }
        return 0;
    }

    // Indica si el juego ya termino
    public boolean isGameOver()
    {
        return gameOver;
    }

    // Devuelve los segundos que faltan
    public double getTimeLeft()
    {
        return timeLeft;
    }

    // Devuelve los goles del jugador 1
    public int getScore1()
    {
        return score1;
    }

    // Devuelve los goles del jugador 2
    public int getScore2()
    {
        return score2;
    }

    // Devuelve el balon
    public Ball getBall()
    {
        return ball;
    }

    // Devuelve el jugador 1
    public Player getPlayer1()
    {
        return player1;
    }

    // Devuelve el jugador 2
    public Player getPlayer2()
    {
        return player2;
    }

    // Devuelve la cancha izquierda
    public Goal getLeftGoal()
    {
        return leftGoal;
    }

    // Devuelve la cancha derecha
    public Goal getRightGoal()
    {
        return rightGoal;
    }
}
