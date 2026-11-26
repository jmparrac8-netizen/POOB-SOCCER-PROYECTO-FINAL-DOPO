package DOMINIO;

// ParraJ
// Tiro de poder de un personaje. Cada poder sabe como sale el tiro (launch), cuanto dano hace
// (shotDamage, antes del multiplicador de patada), cuanto dura su efecto y de que color es su aura.
// Para agregar un poder nuevo basta con crear una subclase.
public abstract class Power
{
    private final String name;
    private final double shotDamage;
    private final double effectSeconds;
    private final String auraColor;

    // Crea un poder con su nombre, dano base, duracion del efecto y color del aura
    protected Power(String name, double shotDamage, double effectSeconds, String auraColor)
    {
        this.name = name;
        this.shotDamage = shotDamage;
        this.effectSeconds = effectSeconds;
        this.auraColor = auraColor;
    }

    // Lanza el balon como lo hace este poder. groundY es la altura del piso
    public abstract void launch(Ball ball, Player owner, double groundY);

    // Deja el efecto propio del poder en el rival que fue golpeado
    public abstract void applyEffect(Player target);

    // Devuelve el nombre del poder
    public String getName()
    {
        return name;
    }

    // Devuelve el dano base del tiro (se multiplica por el multiplicador de patada)
    public double getShotDamage()
    {
        return shotDamage;
    }

    // Devuelve los segundos que dura el efecto
    public double getEffectSeconds()
    {
        return effectSeconds;
    }

    // Devuelve el color del aura y del balon mientras el poder esta activo
    public String getAuraColor()
    {
        return auraColor;
    }
}
