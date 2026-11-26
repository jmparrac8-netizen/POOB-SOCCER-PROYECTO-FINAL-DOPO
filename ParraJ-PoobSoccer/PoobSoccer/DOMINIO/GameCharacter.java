package DOMINIO;

// ParraJ
// Personaje que elige un jugador: representa a un pais, tiene sus estadisticas (de 1 a 10)
// y, si tiene, un tiro de poder. Las estadisticas son fijas para cada personaje.
// Por ahora las estadisticas de los personajes disponibles no pasan de 5.
// Los personajes en construccion no tienen estadisticas y no se pueden usar.
// Cada punto por encima de 1 suma un 10 % al valor base:
//   multiplicador = 1 + 0,10 * (nivel - 1)      vida maxima = 100 * multiplicador de survival
//   dano de patada = 10 * multiplicador de kick  dano del tiro de poder = danoTiro * multiplicador de kick
public class GameCharacter
{
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 10;
    public static final double BASE_LIFE = 100;
    public static final double BASE_KICK_DAMAGE = 10;

    private final String name;
    private final int speed;
    private final int kick;
    private final int jump;
    private final int survival;
    private final Power power;
    private final String color;
    private final boolean underConstruction;

    // Crea un personaje sin color propio. El poder puede ser null si el personaje no tiene
    public GameCharacter(String name, int speed, int kick, int jump, int survival, Power power)
    {
        this(name, speed, kick, jump, survival, power, null);
    }

    // Crea un personaje con el color de su cuerpo. El poder puede ser null si el personaje no tiene
    public GameCharacter(String name, int speed, int kick, int jump, int survival, Power power, String color)
    {
        checkLevel("speed", speed);
        checkLevel("kick", kick);
        checkLevel("jump", jump);
        checkLevel("survival", survival);
        this.name = name;
        this.speed = speed;
        this.kick = kick;
        this.jump = jump;
        this.survival = survival;
        this.power = power;
        this.color = color;
        this.underConstruction = false;
    }

    // Crea un personaje en construccion: no tiene estadisticas (todas en 0), ni poder, ni color,
    // y no se puede usar en un partido
    protected GameCharacter(String name)
    {
        this.name = name;
        this.speed = 0;
        this.kick = 0;
        this.jump = 0;
        this.survival = 0;
        this.power = null;
        this.color = null;
        this.underConstruction = true;
    }

    // Personaje neutro: todas las estadisticas en 1 y sin poder
    public static GameCharacter neutral()
    {
        return new GameCharacter("Neutro", 1, 1, 1, 1, null);
    }

    // Los personajes que se pueden elegir, en el orden en que se muestran
    public static GameCharacter[] available()
    {
        return new GameCharacter[]{new SouthKorea(), new Cameroon(), new Nigeria(), new USA()};
    }

    // Verifica que una estadistica este entre 1 y 10
    private static void checkLevel(String stat, int level)
    {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            throw new IllegalArgumentException("La estadistica " + stat + " debe estar entre "
                + MIN_LEVEL + " y " + MAX_LEVEL + " (recibio " + level + ")");
        }
    }

    // Multiplicador de una estadistica segun su nivel: 1,0 para nivel 1, 1,1 para nivel 2...
    private static double multiplier(int level)
    {
        return (10 + (level - 1)) / 10.0;
    }

    // Devuelve el nombre del pais
    public String getName()
    {
        return name;
    }

    // Devuelve el color del cuerpo del personaje, o null si no tiene uno propio
    public String getColor()
    {
        return color;
    }

    // Indica si el personaje todavia esta en construccion (no se puede usar)
    public boolean isUnderConstruction()
    {
        return underConstruction;
    }

    // Devuelve el nivel de velocidad
    public int getSpeed()
    {
        return speed;
    }

    // Devuelve el nivel de patada
    public int getKick()
    {
        return kick;
    }

    // Devuelve el nivel de salto
    public int getJump()
    {
        return jump;
    }

    // Devuelve el nivel de vida
    public int getSurvival()
    {
        return survival;
    }

    // Devuelve el tiro de poder, o null si no tiene
    public Power getPower()
    {
        return power;
    }

    // Indica si el personaje tiene tiro de poder
    public boolean hasPower()
    {
        return power != null;
    }

    // Multiplicador de la velocidad de desplazamiento
    public double getSpeedMultiplier()
    {
        return multiplier(speed);
    }

    // Multiplicador de la fuerza del salto
    public double getJumpMultiplier()
    {
        return multiplier(jump);
    }

    // Multiplicador del dano (multiplicadorKick)
    public double getKickMultiplier()
    {
        return multiplier(kick);
    }

    // Vida maxima: 100 * (1 + 0,10 * (survival - 1))
    public double getMaxLife()
    {
        return BASE_LIFE * multiplier(survival);
    }

    // Dano de una patada que conecta al rival: 10 * multiplicadorKick
    public double getKickDamage()
    {
        return BASE_KICK_DAMAGE * getKickMultiplier();
    }

    // Dano del tiro de poder: danoTiro * multiplicadorKick (0 si no tiene poder)
    public double getPowerDamage()
    {
        return power == null ? 0 : power.getShotDamage() * getKickMultiplier();
    }
}
