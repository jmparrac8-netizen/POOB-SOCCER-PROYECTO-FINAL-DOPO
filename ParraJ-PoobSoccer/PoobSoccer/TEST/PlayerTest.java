package TEST;

import DOMINIO.Player;

// ParraJ
// Pruebas de unidad de Player.
public class PlayerTest
{
    private static final double FIELD_W = 1200;
    private static final double GROUND = 600;

    // Crea un jugador azul en la posicion x
    private static Player newPlayer(double x)
    {
        return new Player(x, GROUND, "blue");
    }

    // Avanza al jugador n pasos
    private static void tick(Player p, int n)
    {
        for (int i = 0; i < n; i++) {
            p.update(FIELD_W, GROUND);
        }
    }

    // El jugador deberia empezar en la posicion dada, sobre el piso
    static void playerShouldStartOnTheGroundAtGivenX()
    {
        Player p = newPlayer(300);
        Check.equal("start x", 300, p.getX());
        Check.equal("start y", GROUND - Player.HEIGHT, p.getY());
        Check.that("start on ground", p.isOnGround());
    }

    // Sin teclas no deberia moverse
    static void playerShouldStayStillWithoutInput()
    {
        Player p = newPlayer(300);
        tick(p, 10);
        Check.equal("still x", 300, p.getX());
        Check.equal("still y", GROUND - Player.HEIGHT, p.getY());
    }

    // Deberia moverse a la derecha
    static void playerShouldMoveRight()
    {
        Player p = newPlayer(300);
        p.setMovingRight(true);
        tick(p, 1);
        Check.that("moves right", p.getX() > 300);
        Check.that("vx positive", p.getVx() > 0);
    }

    // Deberia moverse a la izquierda
    static void playerShouldMoveLeft()
    {
        Player p = newPlayer(300);
        p.setMovingLeft(true);
        tick(p, 1);
        Check.that("moves left", p.getX() < 300);
        Check.that("vx negative", p.getVx() < 0);
    }

    // Izquierda y derecha a la vez deberian cancelarse
    static void leftAndRightTogetherShouldCancelOut()
    {
        Player p = newPlayer(300);
        p.setMovingLeft(true);
        p.setMovingRight(true);
        tick(p, 5);
        Check.equal("cancel out", 300, p.getX());
    }

    // Al soltar la tecla deberia detenerse
    static void playerShouldStopWhenKeyIsReleased()
    {
        Player p = newPlayer(300);
        p.setMovingRight(true);
        tick(p, 3);
        double x = p.getX();
        p.setMovingRight(false);
        tick(p, 3);
        Check.equal("stops x", x, p.getX());
        Check.equal("stops vx", 0, p.getVx());
    }

    // No deberia salirse del campo por la izquierda
    static void playerShouldNotLeaveFieldOnTheLeft()
    {
        Player p = newPlayer(0);
        p.setMovingLeft(true);
        tick(p, 10);
        Check.equal("left limit", 0, p.getX());
    }

    // No deberia salirse del campo por la derecha
    static void playerShouldNotLeaveFieldOnTheRight()
    {
        Player p = newPlayer(FIELD_W - Player.WIDTH);
        p.setMovingRight(true);
        tick(p, 10);
        Check.equal("right limit", FIELD_W - Player.WIDTH, p.getX());
    }

    // Al saltar deberia subir
    static void jumpShouldMakePlayerRise()
    {
        Player p = newPlayer(300);
        double groundY = p.getY();
        p.jump();
        tick(p, 1);
        Check.that("rises", p.getY() < groundY);
        Check.that("in the air", !p.isOnGround());
    }

    // El salto deberia alcanzar una altura apreciable
    static void jumpShouldReachSignificantHeight()
    {
        Player p = newPlayer(300);
        double groundY = p.getY();
        p.jump();
        tick(p, 10);
        Check.that("significant height", p.getY() < groundY - 100);
    }

    // Despues de saltar deberia caer solo y quedar en el piso
    static void playerShouldLandByItselfAfterJumping()
    {
        Player p = newPlayer(300);
        p.jump();
        tick(p, 300);
        Check.that("lands", p.isOnGround());
        Check.equal("lands y", GROUND - Player.HEIGHT, p.getY());
        Check.equal("lands vy", 0, p.getVy());
    }

    // No deberia poder saltar otra vez en el aire
    static void playerShouldNotJumpAgainWhileInTheAir()
    {
        Player p = newPlayer(300);
        p.jump();
        tick(p, 3);
        double vyBefore = p.getVy();
        p.jump();
        Check.equal("no double jump", vyBefore, p.getVy());
    }

    // Deberia poder saltar otra vez al aterrizar
    static void playerShouldJumpAgainAfterLanding()
    {
        Player p = newPlayer(300);
        p.jump();
        tick(p, 300);
        p.jump();
        tick(p, 1);
        Check.that("jumps again in air", !p.isOnGround());
        Check.that("jumps again rises", p.getY() < GROUND - Player.HEIGHT);
    }

    // Deberia poder moverse de lado en el aire
    static void playerShouldMoveHorizontallyWhileInTheAir()
    {
        Player p = newPlayer(300);
        p.jump();
        p.setMovingRight(true);
        tick(p, 5);
        Check.that("moves in the air", p.getX() > 300);
    }

    // Reiniciar deberia devolverlo quieto a su lugar de salida
    static void resetShouldReturnThePlayerToItsStart()
    {
        Player p = newPlayer(300);
        p.setMovingRight(true);
        p.jump();
        tick(p, 10);
        p.setMovingRight(false);
        p.reset();
        Check.equal("reset x", 300, p.getX());
        Check.equal("reset y", GROUND - Player.HEIGHT, p.getY());
        Check.equal("reset vx", 0, p.getVx());
        Check.equal("reset vy", 0, p.getVy());
        Check.that("reset on ground", p.isOnGround());
    }

    // Si la tecla sigue presionada despues de reiniciar, deberia seguir moviendose
    static void heldKeyShouldStillWorkAfterReset()
    {
        Player p = newPlayer(300);
        p.setMovingRight(true);
        p.reset();
        tick(p, 5);
        Check.that("keeps moving", p.getX() > 300);
    }

    // El color deberia ser el que se dio al crearlo
    static void playerShouldKeepItsColor()
    {
        Check.that("color", "blue".equals(newPlayer(300).getColor()));
    }

    // Avanza a dos jugadores un paso, chocando entre si (igual que lo hace el juego)
    private static void step(Player a, Player b)
    {
        a.update(FIELD_W, GROUND);
        a.collideWith(b, FIELD_W, GROUND);
        b.update(FIELD_W, GROUND);
        b.collideWith(a, FIELD_W, GROUND);
    }

    // Avanza a dos jugadores n pasos
    private static void step(Player a, Player b, int n)
    {
        for (int i = 0; i < n; i++) {
            step(a, b);
        }
    }

    // Indica si dos jugadores estan encimados (se tocan por dentro, no solo por el borde)
    private static boolean overlap(Player a, Player b)
    {
        return a.getX() < b.getX() + Player.WIDTH - 1e-6 && b.getX() < a.getX() + Player.WIDTH - 1e-6
                && a.getY() < b.getY() + Player.HEIGHT - 1e-6 && b.getY() < a.getY() + Player.HEIGHT - 1e-6;
    }

    // Dos jugadores separados no deberian chocar ni cambiar
    static void separatedPlayersShouldNotCollide()
    {
        Player a = newPlayer(100);
        Player b = newPlayer(400);
        boolean hit = a.collideWith(b, FIELD_W, GROUND);
        Check.that("no collision", !hit);
        Check.equal("a x same", 100, a.getX());
        Check.equal("b x same", 400, b.getX());
    }

    // Dos jugadores que apenas se tocan por el borde no deberian contar como choque
    static void playersTouchingByTheEdgeShouldNotCollide()
    {
        Player a = newPlayer(100);
        Player b = newPlayer(100 + Player.WIDTH);
        Check.that("edge contact is not a collision", !a.collideWith(b, FIELD_W, GROUND));
    }

    // Caminando hacia un jugador quieto deberia detenerse junto a el, sin traspasarlo
    static void playerWalkingRightShouldStopAtTheOther()
    {
        Player a = newPlayer(100);
        Player b = newPlayer(400);
        a.setMovingRight(true);
        step(a, b, 100);
        Check.equal("a stops touching b", 400 - Player.WIDTH, a.getX());
        Check.equal("b not pushed", 400, b.getX());
        Check.equal("a blocked has no speed", 0, a.getVx());
        Check.that("not overlapping", !overlap(a, b));
    }

    // Lo mismo viniendo desde la derecha
    static void playerWalkingLeftShouldStopAtTheOther()
    {
        Player a = newPlayer(700);
        Player b = newPlayer(400);
        a.setMovingLeft(true);
        step(a, b, 100);
        Check.equal("a stops touching b", 400 + Player.WIDTH, a.getX());
        Check.equal("b not pushed", 400, b.getX());
        Check.that("not overlapping", !overlap(a, b));
    }

    // Aunque siga presionando la tecla, no deberia empujar al otro jugador
    static void keepingTheKeyPressedShouldNotPushTheOther()
    {
        Player a = newPlayer(100);
        Player b = newPlayer(400);
        a.setMovingRight(true);
        step(a, b, 300);
        Check.equal("b still at 400", 400, b.getX());
        Check.equal("a still blocked", 400 - Player.WIDTH, a.getX());
    }

    // Si caminan uno hacia el otro, deberian quedar tocandose sin atravesarse
    static void playersWalkingTowardsEachOtherShouldMeetAndStop()
    {
        Player a = newPlayer(100);
        Player b = newPlayer(700);
        a.setMovingRight(true);
        b.setMovingLeft(true);
        step(a, b, 200);
        Check.that("a left of b", a.getX() + Player.WIDTH <= b.getX() + 1e-9);
        Check.that("they touch", b.getX() - (a.getX() + Player.WIDTH) < 1e-9);
        Check.that("not overlapping", !overlap(a, b));
    }

    // Al soltar la tecla y volver para atras, deberia poder alejarse
    static void playerShouldBeAbleToWalkAwayAfterBeingBlocked()
    {
        Player a = newPlayer(100);
        Player b = newPlayer(400);
        a.setMovingRight(true);
        step(a, b, 100);
        a.setMovingRight(false);
        a.setMovingLeft(true);
        step(a, b, 10);
        Check.that("moved away", a.getX() < 400 - Player.WIDTH - 20);
    }

    // Saltando desde al lado, no deberia atravesar al otro mientras sube
    static void playerJumpingNextToTheOtherShouldNotGoThroughIt()
    {
        Player a = newPlayer(400 - Player.WIDTH);
        Player b = newPlayer(400);
        a.setMovingRight(true);
        a.jump();
        boolean crossed = false;
        for (int i = 0; i < 12; i++) {
            step(a, b);
            if (overlap(a, b)) {
                crossed = true;
            }
        }
        Check.that("never overlapping while jumping", !crossed);
    }

    // Cayendo encima del otro jugador deberia quedar parado sobre su cabeza
    static void playerShouldLandOnTheOthersHead()
    {
        Player a = newPlayer(400 - Player.WIDTH);
        Player b = newPlayer(400);
        a.setMovingRight(true);
        a.jump();
        step(a, b, 12);
        a.setMovingRight(false);
        step(a, b, 80);
        Check.equal("a on top of b", b.getY() - Player.HEIGHT, a.getY());
        Check.that("a is supported", a.isOnGround());
        Check.that("not overlapping", !overlap(a, b));
    }

    // Parado sobre la cabeza del otro deberia poder saltar de nuevo
    static void playerOnTheOthersHeadShouldBeAbleToJump()
    {
        Player a = newPlayer(400 - Player.WIDTH);
        Player b = newPlayer(400);
        a.setMovingRight(true);
        a.jump();
        step(a, b, 12);
        a.setMovingRight(false);
        step(a, b, 80);
        double top = a.getY();
        a.jump();
        step(a, b, 3);
        Check.that("rises from the head", a.getY() < top);
    }

    // Si el de abajo se va, el de arriba deberia caer al piso
    static void playerShouldFallWhenTheOneUnderneathLeaves()
    {
        Player a = newPlayer(400 - Player.WIDTH);
        Player b = newPlayer(400);
        a.setMovingRight(true);
        a.jump();
        step(a, b, 12);
        a.setMovingRight(false);
        step(a, b, 80);
        b.setMovingRight(true);
        step(a, b, 60);
        Check.equal("a back on the floor", GROUND - Player.HEIGHT, a.getY());
        Check.that("a on ground", a.isOnGround());
    }

    // Con teclas al azar durante mucho tiempo, nunca deberian quedar encimados ni salirse del campo
    static void playersShouldNeverOverlapWhateverTheyDo()
    {
        Player a = newPlayer(100);
        Player b = newPlayer(1000);
        boolean overlapped = false;
        boolean outside = false;
        for (int i = 0; i < 20000; i++) {
            if (i % 17 == 0) {
                a.setMovingRight((i / 17) % 3 != 0);
                a.setMovingLeft((i / 17) % 5 == 0);
            }
            if (i % 23 == 0) {
                b.setMovingLeft((i / 23) % 3 != 0);
                b.setMovingRight((i / 23) % 7 == 0);
            }
            if (i % 31 == 0) {
                a.jump();
            }
            if (i % 37 == 0) {
                b.jump();
            }
            step(a, b);
            if (overlap(a, b)) {
                overlapped = true;
            }
            if (a.getX() < 0 || a.getX() + Player.WIDTH > FIELD_W || b.getX() < 0 || b.getX() + Player.WIDTH > FIELD_W
                    || a.getY() + Player.HEIGHT > GROUND + 1e-9 || b.getY() + Player.HEIGHT > GROUND + 1e-9) {
                outside = true;
            }
        }
        Check.that("never overlapped", !overlapped);
        Check.that("never outside the field", !outside);
    }

    // Corre todas las pruebas y devuelve cuantas fallaron
    public static int run()
    {
        System.out.println("PlayerTest");
        playerShouldStartOnTheGroundAtGivenX();
        playerShouldStayStillWithoutInput();
        playerShouldMoveRight();
        playerShouldMoveLeft();
        leftAndRightTogetherShouldCancelOut();
        playerShouldStopWhenKeyIsReleased();
        playerShouldNotLeaveFieldOnTheLeft();
        playerShouldNotLeaveFieldOnTheRight();
        jumpShouldMakePlayerRise();
        jumpShouldReachSignificantHeight();
        playerShouldLandByItselfAfterJumping();
        playerShouldNotJumpAgainWhileInTheAir();
        playerShouldJumpAgainAfterLanding();
        playerShouldMoveHorizontallyWhileInTheAir();
        resetShouldReturnThePlayerToItsStart();
        heldKeyShouldStillWorkAfterReset();
        playerShouldKeepItsColor();
        separatedPlayersShouldNotCollide();
        playersTouchingByTheEdgeShouldNotCollide();
        playerWalkingRightShouldStopAtTheOther();
        playerWalkingLeftShouldStopAtTheOther();
        keepingTheKeyPressedShouldNotPushTheOther();
        playersWalkingTowardsEachOtherShouldMeetAndStop();
        playerShouldBeAbleToWalkAwayAfterBeingBlocked();
        playerJumpingNextToTheOtherShouldNotGoThroughIt();
        playerShouldLandOnTheOthersHead();
        playerOnTheOthersHeadShouldBeAbleToJump();
        playerShouldFallWhenTheOneUnderneathLeaves();
        playersShouldNeverOverlapWhateverTheyDo();
        return Check.summary();
    }

    public static void main(String[] args)
    {
        Check.reset();
        System.exit(run() == 0 ? 0 : 1);
    }
}
