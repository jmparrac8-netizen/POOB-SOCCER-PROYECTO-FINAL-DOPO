package TEST;

import DOMINIO.Ball;
import DOMINIO.Cameroon;
import DOMINIO.Effect;
import DOMINIO.GameCharacter;
import DOMINIO.MatchType;
import DOMINIO.Nigeria;
import DOMINIO.Player;
import DOMINIO.PoobSoccer;

// ParraJ
// Pruebas de unidad de las patadas, el aturdimiento, la barra de poder y los tiros de poder
// de Camerun y Nigeria.
public class PowerTest
{
    private static PoobSoccer game(GameCharacter c1, GameCharacter c2)
    {
        return new PoobSoccer(MatchType.TIME, 90, c1, c2);
    }

    // Lleva al jugador 1 hasta quedar pegado al jugador 2, con el balon guardado arriba
    private static void walkNextToPlayer2(PoobSoccer g)
    {
        g.getPlayer1().setMovingRight(true);
        for (int i = 0; i < 400; i++) {
            g.getBall().reset(600, 20);
            g.update();
        }
        g.getPlayer1().setMovingRight(false);
        g.getBall().reset(600, 20);
    }

    // Una patada que alcanza al rival deberia hacerle 10 * multiplicadorKick de dano
    static void kickShouldDamageTheRivalWithinReach()
    {
        GameCharacter cameroon = new Cameroon();
        GameCharacter rival = GameCharacter.neutral();
        PoobSoccer g = game(cameroon, rival);
        walkNextToPlayer2(g);
        Check.that("kick started", g.getPlayer1().kick());
        g.update();
        Check.equal("rival life", rival.getMaxLife() - cameroon.getKickDamage(), g.getPlayer2().getLife());
    }

    // Una patada no deberia hacer dano si el rival esta lejos
    static void kickShouldNotDamageTheRivalOutOfReach()
    {
        PoobSoccer g = game(new Cameroon(), GameCharacter.neutral());
        g.getPlayer1().kick();
        for (int i = 0; i < 15; i++) {
            g.getBall().reset(600, 20);
            g.update();
        }
        Check.equal("rival life", g.getPlayer2().getMaxLife(), g.getPlayer2().getLife());
    }

    // Una patada que toca el balon solo lo impulsa (no hace dano) y no cambia con la estadistica
    static void kickThatTouchesTheBallShouldOnlyPushTheBall()
    {
        PoobSoccer g = game(new Cameroon(), GameCharacter.neutral());
        Player p1 = g.getPlayer1();
        g.getBall().reset(p1.getX() + Player.WIDTH + 20, PoobSoccer.HEIGHT - Ball.RADIUS);
        p1.kick();
        g.update();
        Check.that("ball goes right", g.getBall().getVx() > 0);
        Check.that("ball goes up", g.getBall().getVy() < 0);
        Check.equal("rival life", g.getPlayer2().getMaxLife(), g.getPlayer2().getLife());
    }

    // Una patada no se puede repetir mientras la anterior sigue en curso
    static void kickShouldHaveACooldown()
    {
        PoobSoccer g = game(new Cameroon(), GameCharacter.neutral());
        Check.that("first kick", g.getPlayer1().kick());
        Check.that("second kick right away", !g.getPlayer1().kick());
    }

    // Con la vida en cero el jugador queda aturdido 3 s y despues recupera su vida maxima
    static void playerShouldBeStunnedWhenLifeReachesZero()
    {
        PoobSoccer g = game(GameCharacter.neutral(), GameCharacter.neutral());
        Player p2 = g.getPlayer2();
        p2.takeDamage(10000);
        Check.equal("life zero", 0, p2.getLife());
        Check.that("stunned", p2.isStunned());
        Check.that("cannot act", !p2.canAct());
        p2.jump();
        Check.that("cannot jump", p2.isOnGround());
        Check.that("cannot kick", !p2.kick());
        g.advanceTime(2.9);
        Check.that("still stunned at 2.9 s", p2.isStunned());
        g.advanceTime(0.2);
        Check.that("recovered", !p2.isStunned());
        Check.equal("full life", p2.getMaxLife(), p2.getLife());
    }

    // La vida nunca baja de cero
    static void lifeShouldNeverBeNegative()
    {
        Player p = new Player(300, 600, "blue", GameCharacter.neutral(), true);
        p.takeDamage(5000);
        Check.equal("life", 0, p.getLife());
    }

    // La barra de poder se llena en 10 s y sube un cuarto al recibir un gol
    static void powerBarShouldFillInTenSecondsAndGrowWithGoals()
    {
        Player p = new Player(300, 600, "blue", new Cameroon(), true);
        Check.equal("starts empty", 0, p.getPowerBar());
        p.advanceTime(5);
        Check.equal("half after 5 s", 0.5, p.getPowerBar());
        p.goalConceded();
        Check.equal("three quarters after a goal", 0.75, p.getPowerBar());
        p.advanceTime(10);
        Check.equal("never above full", 1, p.getPowerBar());
    }

    // El poder solo se activa con la barra llena, vacia la barra y no se puede activar otro mientras dura
    static void powerShouldNeedAFullBarAndCannotBeStacked()
    {
        Player p = new Player(300, 600, "blue", new Cameroon(), true);
        p.advanceTime(9);
        Check.that("bar not full", !p.activatePower());
        p.advanceTime(1);
        Check.that("bar full", p.activatePower());
        Check.that("aura on", p.hasPower());
        Check.equal("bar emptied", 0, p.getPowerBar());
        p.advanceTime(1);
        Check.that("bar recharges", p.getPowerBar() > 0);
        p.advanceTime(10);
        Check.that("cannot activate a second power", !p.activatePower());
    }

    // Los personajes sin poder no pueden activarlo
    static void charactersWithoutPowerCannotActivateIt()
    {
        Player p = new Player(300, 600, "blue", GameCharacter.neutral(), true);
        p.advanceTime(10);
        Check.that("neutral cannot", !p.activatePower());
        Check.that("no aura", !p.hasPower());
    }

    // Un efecto nuevo reemplaza al anterior, y un jugador aturdido no recibe efectos
    static void effectsShouldReplaceEachOtherAndNotStack()
    {
        Player p = new Player(300, 600, "blue", GameCharacter.neutral(), true);
        p.applyEffect(Effect.ELECTROCUTED, 3);
        p.applyEffect(Effect.KNOCKED_OUT, 3);
        Check.that("replaced", p.getEffect() == Effect.KNOCKED_OUT);
        p.advanceTime(3);
        Check.that("effect ended", p.getEffect() == Effect.NONE);
        p.takeDamage(10000);
        p.applyEffect(Effect.ELECTROCUTED, 3);
        Check.that("stunned player gets no effect", p.getEffect() == Effect.NONE);
    }

    // Un jugador electrocutado no salta y se mueve al 50 % de su velocidad
    static void electrocutedPlayerShouldNotJumpAndMoveAtHalfSpeed()
    {
        Player normal = new Player(300, 600, "blue", GameCharacter.neutral(), true);
        normal.setMovingRight(true);
        normal.update(1200, 600);
        double fullSpeed = normal.getVx();

        Player p = new Player(300, 600, "blue", GameCharacter.neutral(), true);
        p.applyEffect(Effect.ELECTROCUTED, 3);
        p.setMovingRight(true);
        p.update(1200, 600);
        Check.equal("half speed", fullSpeed / 2, p.getVx());
        p.jump();
        Check.that("cannot jump", p.isOnGround());
    }

    // Un jugador inconsciente no se mueve, no salta ni patea, pero cae por la gravedad
    static void knockedOutPlayerShouldNotMoveJumpOrKick()
    {
        Player p = new Player(300, 600, "blue", GameCharacter.neutral(), true);
        p.applyEffect(Effect.KNOCKED_OUT, 3);
        p.throwIntoAir(14);
        p.setMovingRight(true);
        double x = p.getX();
        for (int i = 0; i < 60; i++) {
            p.update(1200, 600);
        }
        Check.equal("did not move", x, p.getX());
        Check.that("fell back to the ground", p.isOnGround());
        Check.that("cannot kick", !p.kick());
        p.jump();
        Check.that("cannot jump", p.isOnGround());
    }

    // Deja al jugador 1 con el poder activo y el balon tocandolo
    private static void launchShot(PoobSoccer g)
    {
        Player p1 = g.getPlayer1();
        g.advanceTime(10);
        p1.activatePower();
        g.getBall().reset(p1.getX() + Player.WIDTH + 10, PoobSoccer.HEIGHT - Ball.RADIUS);
        g.update();
    }

    // Al tocar el balon con el poder activo se ejecuta el tiro y el aura desaparece
    static void touchingTheBallWithThePowerShouldLaunchTheShot()
    {
        PoobSoccer g = game(new Cameroon(), GameCharacter.neutral());
        launchShot(g);
        Check.that("shot active", g.isShotActive());
        Check.that("aura gone", !g.getPlayer1().hasPower());
        Check.that("ball is a shot", g.getBall().isShot());
        Check.that("ball goes right", g.getBall().getVx() > 0);
    }

    // Sin el poder activo, tocar el balon no lanza ningun tiro
    static void touchingTheBallWithoutPowerShouldNotLaunchAShot()
    {
        PoobSoccer g = game(new Cameroon(), GameCharacter.neutral());
        Player p1 = g.getPlayer1();
        g.getBall().reset(p1.getX() + Player.WIDTH + 10, PoobSoccer.HEIGHT - Ball.RADIUS);
        g.update();
        Check.that("no shot", !g.isShotActive());
    }

    // El rayo de Camerun: recto, hace 60 * multiplicadorKick de dano y electrocuta al rival
    static void lightningShouldDamageAndElectrocuteTheRival()
    {
        GameCharacter cameroon = new Cameroon();
        GameCharacter rival = GameCharacter.neutral();
        PoobSoccer g = game(cameroon, rival);
        launchShot(g);
        double y = g.getBall().getY();
        for (int i = 0; i < 80 && g.isShotActive(); i++) {
            g.update();
            Check.that("straight line", Math.abs(g.getBall().getY() - y) < 1e-6 || !g.isShotActive());
        }
        Check.that("shot ended on the rival", !g.isShotActive());
        Check.equal("lightning damage", rival.getMaxLife() - cameroon.getPowerDamage(), g.getPlayer2().getLife());
        Check.that("electrocuted", g.getPlayer2().getEffect() == Effect.ELECTROCUTED);
        Check.that("ball is normal again", !g.getBall().isShot());
    }

    // El tornado de Nigeria: hace 40 * multiplicadorKick de dano, deja inconsciente y lanza al aire al rival
    static void tornadoShouldDamageAndKnockOutTheRival()
    {
        GameCharacter nigeria = new Nigeria();
        GameCharacter rival = GameCharacter.neutral();
        PoobSoccer g = game(nigeria, rival);
        launchShot(g);
        boolean threw = false;
        for (int i = 0; i < 120 && g.isShotActive(); i++) {
            g.update();
        }
        threw = g.getPlayer2().getVy() < 0 || !g.getPlayer2().isOnGround();
        Check.that("shot ended on the rival", !g.isShotActive());
        Check.equal("tornado damage", rival.getMaxLife() - nigeria.getPowerDamage(), g.getPlayer2().getLife());
        Check.that("knocked out", g.getPlayer2().getEffect() == Effect.KNOCKED_OUT);
        Check.that("thrown into the air", threw);
    }

    // Si el tiro de poder entra al arco sin tocar al rival, cuenta el gol pero no hay dano ni efecto
    static void shotThatScoresWithoutTouchingTheRivalShouldOnlyCountTheGoal()
    {
        PoobSoccer g = game(new Cameroon(), GameCharacter.neutral());
        Player p1 = g.getPlayer1();
        g.advanceTime(10);
        p1.activatePower();
        p1.jump();
        g.update();
        g.getBall().reset(p1.getX() + Player.WIDTH / 2, p1.getY() - 10);
        g.update();
        Check.that("shot launched", g.isShotActive());
        for (int i = 0; i < 100 && g.getScore1() == 0; i++) {
            g.update();
        }
        Check.equal("goal counts", 1, g.getScore1());
        Check.equal("no damage", g.getPlayer2().getMaxLife(), g.getPlayer2().getLife());
        Check.that("no effect", g.getPlayer2().getEffect() == Effect.NONE);
        Check.that("shot over", !g.isShotActive());
    }

    // Corre todas las pruebas y devuelve cuantas fallaron
    public static int run()
    {
        System.out.println("PowerTest");
        kickShouldDamageTheRivalWithinReach();
        kickShouldNotDamageTheRivalOutOfReach();
        kickThatTouchesTheBallShouldOnlyPushTheBall();
        kickShouldHaveACooldown();
        playerShouldBeStunnedWhenLifeReachesZero();
        lifeShouldNeverBeNegative();
        powerBarShouldFillInTenSecondsAndGrowWithGoals();
        powerShouldNeedAFullBarAndCannotBeStacked();
        charactersWithoutPowerCannotActivateIt();
        effectsShouldReplaceEachOtherAndNotStack();
        electrocutedPlayerShouldNotJumpAndMoveAtHalfSpeed();
        knockedOutPlayerShouldNotMoveJumpOrKick();
        touchingTheBallWithThePowerShouldLaunchTheShot();
        touchingTheBallWithoutPowerShouldNotLaunchAShot();
        lightningShouldDamageAndElectrocuteTheRival();
        tornadoShouldDamageAndKnockOutTheRival();
        shotThatScoresWithoutTouchingTheRivalShouldOnlyCountTheGoal();
        return Check.summary();
    }

    public static void main(String[] args)
    {
        Check.reset();
        System.exit(run() == 0 ? 0 : 1);
    }
}
