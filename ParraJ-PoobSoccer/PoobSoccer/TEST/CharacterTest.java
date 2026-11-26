package TEST;

import DOMINIO.Cameroon;
import DOMINIO.GameCharacter;
import DOMINIO.MatchType;
import DOMINIO.Nigeria;
import DOMINIO.Player;
import DOMINIO.PoobSoccer;
import DOMINIO.SouthKorea;
import DOMINIO.USA;

// ParraJ
// Pruebas de unidad de GameCharacter y de los cuatro personajes.
public class CharacterTest
{
    // Deberia haber cuatro personajes en la lista: Corea del Sur, Camerun, Nigeria y USA (Corea y USA en construccion)
    static void fourCharactersShouldBeAvailable()
    {
        GameCharacter[] all = GameCharacter.available();
        Check.equal("four characters", 4, all.length);
        Check.that("korea", all[0] instanceof SouthKorea);
        Check.that("cameroon", all[1] instanceof Cameroon);
        Check.that("nigeria", all[2] instanceof Nigeria);
        Check.that("usa", all[3] instanceof USA);
    }

    // Solo Camerun y Nigeria deberian tener tiro de poder
    static void onlyCameroonAndNigeriaShouldHavePower()
    {
        Check.that("korea no power", !new SouthKorea().hasPower());
        Check.that("usa no power", !new USA().hasPower());
        Check.that("cameroon power", new Cameroon().hasPower());
        Check.that("nigeria power", new Nigeria().hasPower());
    }

    // Las estadisticas de Camerun y Nigeria deberian estar entre 1 y 5 por ahora
    static void statsShouldBeBetweenOneAndFive()
    {
        for (GameCharacter c : new GameCharacter[]{new Cameroon(), new Nigeria()}) {
            Check.that(c.getName() + " stats in range",
                c.getSpeed() >= 1 && c.getSpeed() <= 5 && c.getKick() >= 1 && c.getKick() <= 5
                && c.getJump() >= 1 && c.getJump() <= 5 && c.getSurvival() >= 1 && c.getSurvival() <= 5);
        }
    }

    // Corea del Sur y USA deberian estar en construccion, sin estadisticas ni poder
    static void koreaAndUsaShouldBeUnderConstruction()
    {
        for (GameCharacter c : new GameCharacter[]{new SouthKorea(), new USA()}) {
            Check.that(c.getName() + " under construction", c.isUnderConstruction());
            Check.that(c.getName() + " no stats",
                c.getSpeed() == 0 && c.getKick() == 0 && c.getJump() == 0 && c.getSurvival() == 0);
            Check.that(c.getName() + " no power", !c.hasPower());
        }
        Check.that("cameroon ready", !new Cameroon().isUnderConstruction());
        Check.that("nigeria ready", !new Nigeria().isUnderConstruction());
    }

    // Un personaje en construccion no deberia poder jugar
    static void underConstructionCharactersShouldNotPlay()
    {
        boolean playerRejected = false;
        try {
            new Player(300, 600, "blue", new USA(), true);
        } catch (IllegalArgumentException e) {
            playerRejected = true;
        }
        Check.that("player with usa rejected", playerRejected);

        boolean gameRejected = false;
        try {
            new PoobSoccer(MatchType.TIME, 90, new Cameroon(), new SouthKorea());
        } catch (IllegalArgumentException e) {
            gameRejected = true;
        }
        Check.that("game with korea rejected", gameRejected);
    }

    // Nigeria deberia ser verde y Camerun amarillo, tambien en el partido
    static void charactersShouldHaveTheirColor()
    {
        Check.that("nigeria green", "green".equals(new Nigeria().getColor()));
        Check.that("cameroon yellow", "yellow".equals(new Cameroon().getColor()));
        PoobSoccer g = new PoobSoccer(MatchType.TIME, 90, new Nigeria(), new Cameroon());
        Check.that("player 1 green", "green".equals(g.getPlayer1().getColor()));
        Check.that("player 2 yellow", "yellow".equals(g.getPlayer2().getColor()));
    }

    // Las formulas deberian coincidir con las tablas del enunciado
    static void formulasShouldMatchTheTables()
    {
        GameCharacter nivel1 = new GameCharacter("a", 1, 1, 1, 1, null);
        Check.equal("kick 1 multiplier", 1.0, nivel1.getKickMultiplier());
        Check.equal("survival 1 life", 100, nivel1.getMaxLife());
        Check.equal("kick 1 damage", 10, nivel1.getKickDamage());

        GameCharacter nivel10 = new GameCharacter("b", 10, 10, 10, 10, null);
        Check.equal("kick 10 multiplier", 1.9, nivel10.getKickMultiplier());
        Check.equal("survival 10 life", 190, nivel10.getMaxLife());
        Check.equal("kick 10 damage", 19, nivel10.getKickDamage());

        GameCharacter ejemplo = new GameCharacter("c", 1, 5, 1, 3, null);
        Check.equal("example multiplier", 1.4, ejemplo.getKickMultiplier());
        Check.equal("example life", 120, ejemplo.getMaxLife());
        Check.equal("example kick damage", 14, ejemplo.getKickDamage());
    }

    // El dano del tiro de poder deberia ser danoTiro por multiplicadorKick
    static void powerDamageShouldUseTheKickMultiplier()
    {
        GameCharacter camerun = new GameCharacter("Camerun", 1, 5, 1, 1, new Cameroon().getPower());
        Check.equal("cameroon power damage", 84, camerun.getPowerDamage());
        Check.equal("no power no damage", 0, new USA().getPowerDamage());
    }

    // Una estadistica fuera de rango deberia ser rechazada
    static void statOutOfRangeShouldBeRejected()
    {
        boolean rejected = false;
        try {
            new GameCharacter("x", 11, 1, 1, 1, null);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        Check.that("stat 11 rejected", rejected);
    }

    // Corre todas las pruebas y devuelve cuantas fallaron
    public static int run()
    {
        System.out.println("CharacterTest");
        fourCharactersShouldBeAvailable();
        onlyCameroonAndNigeriaShouldHavePower();
        statsShouldBeBetweenOneAndFive();
        koreaAndUsaShouldBeUnderConstruction();
        underConstructionCharactersShouldNotPlay();
        charactersShouldHaveTheirColor();
        formulasShouldMatchTheTables();
        powerDamageShouldUseTheKickMultiplier();
        statOutOfRangeShouldBeRejected();
        return Check.summary();
    }

    public static void main(String[] args)
    {
        Check.reset();
        System.exit(run() == 0 ? 0 : 1);
    }
}
