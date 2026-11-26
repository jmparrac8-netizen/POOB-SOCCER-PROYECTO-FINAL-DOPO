package DOMINIO;

// ParraJ
// Rayo de Camerun: el balon sale recto, cubierto de electricidad, a alta velocidad.
// Si golpea al rival lo electrocuta: no puede saltar y se mueve al 50 % de su velocidad durante 3 s.
public class Lightning extends Power
{
    public static final double SPEED = 22;

    // Crea el rayo: dano base 60, efecto de 3 s, aura amarilla
    public Lightning()
    {
        super("Rayo", 60, 3, "yellow");
    }

    // El balon sale en linea recta hacia donde mira el jugador
    @Override
    public void launch(Ball ball, Player owner, double groundY)
    {
        double direction = owner.isFacingRight() ? 1 : -1;
        ball.startStraightShot(direction * SPEED);
    }

    // Electrocuta al rival
    @Override
    public void applyEffect(Player target)
    {
        target.applyEffect(Effect.ELECTROCUTED, getEffectSeconds());
    }
}
