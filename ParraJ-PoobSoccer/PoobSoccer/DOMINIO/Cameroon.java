package DOMINIO;

// ParraJ
// Personaje de Camerun, de color amarillo. Su tiro de poder es el Rayo.
public class Cameroon extends GameCharacter
{
    // Crea el personaje con sus estadisticas fijas (speed, kick, jump, survival)
    public Cameroon()
    {
        super("Camerun", 4, 5, 5, 5, new Lightning(), "yellow");
    }
}
