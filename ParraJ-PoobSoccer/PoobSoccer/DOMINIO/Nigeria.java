package DOMINIO;

// ParraJ
// Personaje de Nigeria, de color verde. Su tiro de poder es el Tornado.
public class Nigeria extends GameCharacter
{
    // Crea el personaje con sus estadisticas fijas (speed, kick, jump, survival)
    public Nigeria()
    {
        super("Nigeria", 5, 5, 5, 4, new Tornado(), "green");
    }
}
