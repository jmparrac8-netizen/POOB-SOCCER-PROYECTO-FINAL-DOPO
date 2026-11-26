package DOMINIO;

// ParraJ
// Juego: un balon, dos jugadores y dos canchas, una a cada costado.
// La presentacion lee su estado y llama a update() para mover a los jugadores y al balon
// y a advanceTime() para el cronometro, las barras de poder y los efectos.
// El piso esta en la parte de abajo del campo (y = HEIGHT).
// El jugador 1 defiende la cancha izquierda y el jugador 2 la derecha.
// Si el balon entra a una cancha, es gol para el jugador que NO la defiende: el marcador
// sube y el balon y los jugadores vuelven a su lugar de salida.
// Tipos de partido:
//   TIME:  dura un numero de segundos (60, 90 o 120). Gana quien tenga mas goles; si hay empate
//          hay muerte subita y gana el primero que marque.
//   GOALS: gana el primero que llegue a la meta (3, 6 o 9 goles), sin limite de tiempo.
// Patadas: si el balon esta al alcance del pie, solo se impulsa el balon; si no, y el rival esta
// al alcance, recibe el dano de la patada.
// Tiros de poder: el jugador con aura que toca el balon ejecuta su tiro de poder. Si el balon golpea
// al rival hay dano y efecto; si entra al arco sin tocarlo, solo cuenta el gol.
public class PoobSoccer
{
    public static final double WIDTH = 1200;
    public static final double HEIGHT = 600;
    public static final double GAME_SECONDS = 90;
    public static final int[] GOAL_OPTIONS = {3, 6, 9};
    public static final int[] TIME_OPTIONS = {60, 90, 120};

    private static final double KICK_BALL_VX = 15;
    private static final double KICK_BALL_VY = -8;
    private static final double SHOT_MAX_SECONDS = 4;

    private final Ball ball;
    private final Player player1;
    private final Player player2;
    private final Goal leftGoal;
    private final Goal rightGoal;
    private final MatchType matchType;
    private final int target;

    private double timeLeft;
    private double elapsed;
    private int score1;
    private int score2;
    private boolean suddenDeath;
    private boolean gameOver;

    private Player shotOwner;
    private Power shotPower;
    private double shotTime;

    // Crea un juego por tiempo de 90 s con personajes neutros (sin poderes)
    public PoobSoccer()
    {
        this(MatchType.TIME, (int) GAME_SECONDS, GameCharacter.neutral(), GameCharacter.neutral());
    }

    // Crea un juego nuevo: balon en el centro de abajo, jugadores a cada lado y marcador en cero.
    // target son los segundos (60, 90 o 120) si es por tiempo, o los goles (3, 6 o 9) si es por goles
    public PoobSoccer(MatchType matchType, int target, GameCharacter character1, GameCharacter character2)
    {
        checkTarget(matchType, target);
        this.matchType = matchType;
        this.target = target;
        ball = new Ball(WIDTH / 2, HEIGHT - Ball.RADIUS);
        player1 = new Player(WIDTH * 0.25 - Player.WIDTH / 2, HEIGHT, colorOf(character1, "blue"), character1, true);
        player2 = new Player(WIDTH * 0.75 - Player.WIDTH / 2, HEIGHT, colorOf(character2, "red"), character2, false);
        leftGoal = new Goal(0, HEIGHT);
        rightGoal = new Goal(WIDTH - Goal.WIDTH, HEIGHT);
        timeLeft = matchType == MatchType.TIME ? target : 0;
        score1 = 0;
        score2 = 0;
        gameOver = false;
    }

    // Color del cuerpo del jugador: el del personaje, o el color dado si el personaje no tiene uno
    private static String colorOf(GameCharacter character, String defaultColor)
    {
        return character.getColor() != null ? character.getColor() : defaultColor;
    }

    // Verifica que la meta sea una de las permitidas para ese tipo de partido
    private static void checkTarget(MatchType type, int target)
    {
        int[] options = type == MatchType.TIME ? TIME_OPTIONS : GOAL_OPTIONS;
        for (int option : options) {
            if (option == target) {
                return;
            }
        }
        throw new IllegalArgumentException("Valor no permitido para el partido " + type + ": " + target);
    }

    // Avanza el juego un paso: mueve a los jugadores (que chocan entre si como contra una pared),
    // resuelve las patadas, mueve al balon (con sus choques) y revisa si hubo gol
    public void update()
    {
        if (gameOver) {
            return;
        }
        player1.update(WIDTH, HEIGHT);
        player1.collideWith(player2, WIDTH, HEIGHT);
        player2.update(WIDTH, HEIGHT);
        player2.collideWith(player1, WIDTH, HEIGHT);
        resolveKick(player1, player2);
        resolveKick(player2, player1);
        updateBall();
        checkGoals();
    }

    // Una patada en curso le pega al balon si esta al alcance del pie (solo lo impulsa)
    // y si no, le hace dano al rival si esta al alcance
    private void resolveKick(Player attacker, Player defender)
    {
        if (!attacker.isKickPending()) {
            return;
        }
        double fx = attacker.getFootX();
        double fy = attacker.getFootY();
        if (ball.touchesRectangle(fx, fy, Player.FOOT_WIDTH, Player.FOOT_HEIGHT)) {
            attacker.markKickResolved();
            if (attacker.hasPower() && shotOwner == null) {
                launchShot(attacker);
            } else {
                endShot();
                double direction = attacker.isFacingRight() ? 1 : -1;
                ball.setVelocity(direction * KICK_BALL_VX, KICK_BALL_VY);
            }
        } else if (overlaps(fx, fy, Player.FOOT_WIDTH, Player.FOOT_HEIGHT, defender)) {
            attacker.markKickResolved();
            defender.takeDamage(attacker.getCharacter().getKickDamage());
        }
    }

    // Indica si el rectangulo dado se cruza con el cuerpo del jugador
    private boolean overlaps(double rx, double ry, double rw, double rh, Player p)
    {
        return rx < p.getX() + Player.WIDTH && p.getX() < rx + rw
            && ry < p.getY() + Player.HEIGHT && p.getY() < ry + rh;
    }

    // Mueve el balon y lo hace rebotar contra los travesanos y los jugadores
    private void updateBall()
    {
        ball.update(WIDTH, HEIGHT);
        collideWithCrossbar(leftGoal);
        collideWithCrossbar(rightGoal);
        collideWithPlayer(player1, player2);
        collideWithPlayer(player2, player1);
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

    // El jugador es un rectangulo que empuja al balon con su propia velocidad.
    // El balon de un tiro de poder atraviesa a quien lo lanzo. Si toca al rival, le hace el dano y el efecto.
    // Si lo toca un jugador con el poder activo, ejecuta su tiro de poder
    private void collideWithPlayer(Player player, Player other)
    {
        if (shotOwner == player) {
            return;
        }
        boolean hit = ball.collideWithRectangle(player.getX(), player.getY(), Player.WIDTH, Player.HEIGHT,
                player.getVx(), player.getVy());
        if (!hit) {
            return;
        }
        if (shotOwner == other) {
            hitWithShot(player);
        } else if (shotOwner == null && player.hasPower()) {
            launchShot(player);
        }
    }

    // Saca al balon por encima del jugador si siguen encimados despues de acomodar todo
    private void popOverIfSqueezed(Player player)
    {
        if (shotOwner == player) {
            return;
        }
        if (ball.overlapsRectangle(player.getX(), player.getY(), Player.WIDTH, Player.HEIGHT)) {
            ball.popOver(player.getY());
        }
    }

    // El jugador ejecuta su tiro de poder: el aura desaparece y el balon sale como lo define el poder
    private void launchShot(Player owner)
    {
        shotPower = owner.getCharacter().getPower();
        shotOwner = owner;
        shotTime = 0;
        owner.consumePower();
        shotPower.launch(ball, owner, HEIGHT);
        ball.setColor(shotPower.getAuraColor());
    }

    // El tiro de poder golpeo al rival: dano = danoTiro * multiplicadorKick del dueno, mas el efecto del poder.
    // El aturdimiento (si la vida llega a cero) reemplaza al efecto
    private void hitWithShot(Player victim)
    {
        double damage = shotPower.getShotDamage() * shotOwner.getCharacter().getKickMultiplier();
        shotPower.applyEffect(victim);
        victim.takeDamage(damage);
        endShot();
    }

    // Termina el tiro de poder y el balon vuelve a ser normal
    private void endShot()
    {
        ball.endShot();
        ball.setColor("white");
        shotOwner = null;
        shotPower = null;
        shotTime = 0;
    }

    // Si el balon entro a una cancha, suma el gol al otro jugador y reinicia las posiciones.
    // El jugador que recibio el gol carga un cuarto de su barra de poder.
    // Un gol no hace dano ni efecto, aunque venga de un tiro de poder
    private void checkGoals()
    {
        if (leftGoal.isInside(ball.getX(), ball.getY())) {
            score2++;
            player1.goalConceded();
            afterGoal(2);
        } else if (rightGoal.isInside(ball.getX(), ball.getY())) {
            score1++;
            player2.goalConceded();
            afterGoal(1);
        }
    }

    // Revisa si el gol termina el partido y deja todo listo para el siguiente saque
    private void afterGoal(int scorer)
    {
        endShot();
        int goals = scorer == 1 ? score1 : score2;
        if (matchType == MatchType.GOALS && goals >= target) {
            gameOver = true;
        } else if (matchType == MatchType.TIME && suddenDeath) {
            gameOver = true;
        }
        kickOff();
    }

    // Deja al balon quieto en el centro de abajo y a los jugadores en su lugar de salida
    private void kickOff()
    {
        ball.reset(WIDTH / 2, HEIGHT - Ball.RADIUS);
        player1.reset();
        player2.reset();
    }

    // Descuenta los segundos reales que pasaron: cronometro, barras de poder, aturdimientos y efectos.
    // En un partido por tiempo, al llegar a cero gana quien tenga mas goles; si hay empate sigue en muerte subita
    public void advanceTime(double seconds)
    {
        if (gameOver) {
            return;
        }
        elapsed += seconds;
        player1.advanceTime(seconds);
        player2.advanceTime(seconds);
        if (shotOwner != null) {
            shotTime += seconds;
            if (shotTime >= SHOT_MAX_SECONDS) {
                endShot();
            }
        }
        if (matchType == MatchType.TIME && !suddenDeath) {
            timeLeft -= seconds;
            if (timeLeft <= 0) {
                timeLeft = 0;
                if (score1 == score2) {
                    suddenDeath = true;
                } else {
                    gameOver = true;
                }
            }
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

    // Indica si el partido esta en muerte subita (empate al terminar el tiempo)
    public boolean isSuddenDeath()
    {
        return suddenDeath;
    }

    // Devuelve el tipo de partido
    public MatchType getMatchType()
    {
        return matchType;
    }

    // Devuelve los segundos del partido (por tiempo) o los goles para ganar (por goles)
    public int getTarget()
    {
        return target;
    }

    // Devuelve los segundos que faltan (solo tiene sentido en el partido por tiempo)
    public double getTimeLeft()
    {
        return timeLeft;
    }

    // Devuelve los segundos que lleva jugados el partido
    public double getElapsed()
    {
        return elapsed;
    }

    // Indica si hay un tiro de poder en juego
    public boolean isShotActive()
    {
        return shotOwner != null;
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
