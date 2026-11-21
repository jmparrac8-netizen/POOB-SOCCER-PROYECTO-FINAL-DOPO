package PRESENTATION;

import DOMINIO.Player;

// ParraJ
// Figura de un jugador: un rectangulo de color (el cuerpo) y un circulo amarillo (la cabeza)
// dentro de la parte de arriba del cuerpo. Las figuras se crean la primera vez que se dibuja.
public class PlayerView
{
    private static final int HEAD_SIZE = 36;
    private static final int HEAD_X = 12;
    private static final int HEAD_Y = 6;

    private final Player player;
    private Rectangle body;
    private Circle head;
    private int xPosition;
    private int yPosition;

    // Crea la vista de un jugador
    public PlayerView(Player player)
    {
        this.player = player;
    }

    // Crea el cuerpo y la cabeza en la esquina (0, 0) si todavia no existen
    private void prepareFigures()
    {
        if (body != null) {
            return;
        }
        body = new Rectangle();
        body.changeSize((int) Player.HEIGHT, (int) Player.WIDTH);
        body.changeColor(player.getColor());
        body.moveHorizontal(-70);
        body.moveVertical(-15);
        head = new Circle();
        head.changeSize(HEAD_SIZE);
        head.changeColor("yellow");
        head.moveHorizontal(HEAD_X - 20);
        head.moveVertical(HEAD_Y - 15);
        xPosition = 0;
        yPosition = 0;
    }

    // Lleva las figuras a la posicion del jugador. La cabeza se redibuja despues del cuerpo
    public void draw()
    {
        prepareFigures();
        int newX = (int) Math.round(player.getX());
        int newY = (int) Math.round(player.getY());
        body.moveHorizontal(newX - xPosition);
        body.moveVertical(newY - yPosition);
        head.moveHorizontal(newX - xPosition);
        head.moveVertical(newY - yPosition);
        xPosition = newX;
        yPosition = newY;
        body.makeVisible();
        head.makeVisible();
    }

    // Oculta el jugador del canvas
    public void hide()
    {
        if (body != null) {
            body.makeInvisible();
            head.makeInvisible();
        }
    }
}
