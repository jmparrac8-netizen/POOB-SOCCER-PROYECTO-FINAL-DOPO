package DOMINIO;

// ParraJ
// Tornado de Nigeria: un remolino a ras de suelo que rebota rapidamente.
// Si golpea al rival lo deja inconsciente: es lanzado al aire y no puede moverse, saltar ni patear durante 3 s.
public class Tornado extends Power
{
    public static final double SPEED = 12;
    public static final double BOUNCE = 7;
    public static final double THROW_SPEED = 14;

    // Crea el tornado: dano base 40, efecto de 3 s, aura verde
    public Tornado()
    {
        super("Tornado", 40, 3, "green");
    }

    // El balon baja al piso y avanza dando saltos cortos y rapidos
    @Override
    public void launch(Ball ball, Player owner, double groundY)
    {
        double direction = owner.isFacingRight() ? 1 : -1;
        ball.startBouncingShot(direction * SPEED, BOUNCE, groundY);
    }

    // Deja inconsciente al rival y lo lanza al aire
    @Override
    public void applyEffect(Player target)
    {
        target.applyEffect(Effect.KNOCKED_OUT, getEffectSeconds());
        target.throwIntoAir(THROW_SPEED);
    }
}
