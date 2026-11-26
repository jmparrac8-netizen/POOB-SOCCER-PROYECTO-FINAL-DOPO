package PRESENTATION;

import DOMINIO.Effect;
import DOMINIO.Player;

// ParraJ
// Figura de un jugador: un rectangulo de color (el cuerpo) y un circulo blanco (la cabeza)
// dentro de la parte de arriba del cuerpo. Detras del cuerpo hay un halo que muestra el aura del poder
// activo o el efecto que tiene el jugador (electrocutado, inconsciente o aturdido), y delante
// aparece el pie mientras patea. Las figuras se crean la primera vez que se dibuja.
public class PlayerView
{
    private static final int HEAD_SIZE = 36;
    private static final int HEAD_X = 12;
    private static final int HEAD_Y = 6;
    private static final int HALO_MARGIN = 8;

    private final Player player;
    private Rectangle halo;
    private Rectangle body;
    private Circle head;
    private Rectangle foot;
    private int xPosition;
    private int yPosition;
    private int footX;
    private int footY;

    // Crea la vista de un jugador
    public PlayerView(Player player)
    {
        this.player = player;
    }

    // Crea el halo, el cuerpo, la cabeza y el pie en la esquina (0, 0) si todavia no existen
    private void prepareFigures()
    {
        if (body != null) {
            return;
        }
        halo = new Rectangle();
        halo.changeSize((int) Player.HEIGHT + 2 * HALO_MARGIN, (int) Player.WIDTH + 2 * HALO_MARGIN);
        halo.moveHorizontal(-70 - HALO_MARGIN);
        halo.moveVertical(-15 - HALO_MARGIN);
        body = new Rectangle();
        body.changeSize((int) Player.HEIGHT, (int) Player.WIDTH);
        body.changeColor(player.getColor());
        body.moveHorizontal(-70);
        body.moveVertical(-15);
        head = new Circle();
        head.changeSize(HEAD_SIZE);
        head.changeColor("white");
        head.moveHorizontal(HEAD_X - 20);
        head.moveVertical(HEAD_Y - 15);
        foot = new Rectangle();
        foot.changeSize((int) Player.FOOT_HEIGHT, (int) Player.FOOT_WIDTH);
        foot.changeColor("black");
        foot.moveHorizontal(-70);
        foot.moveVertical(-15);
        xPosition = 0;
        yPosition = 0;
        footX = 0;
        footY = 0;
    }

    // Color del halo: el efecto o el aturdimiento tienen prioridad sobre el aura. Null si no hay nada que mostrar
    private String haloColor()
    {
        if (player.isStunned()) {
            return "orange";
        }
        if (player.getEffect() == Effect.ELECTROCUTED) {
            return "cyan";
        }
        if (player.getEffect() == Effect.KNOCKED_OUT) {
            return "gray";
        }
        if (player.hasPower()) {
            return player.getCharacter().getPower().getAuraColor();
        }
        return null;
    }

    // Lleva las figuras a la posicion del jugador. El halo se dibuja primero (queda atras)
    // y la cabeza y el pie despues del cuerpo
    public void draw()
    {
        prepareFigures();
        int newX = (int) Math.round(player.getX());
        int newY = (int) Math.round(player.getY());

        String color = haloColor();
        halo.moveHorizontal(newX - xPosition);
        halo.moveVertical(newY - yPosition);
        if (color == null) {
            halo.makeInvisible();
        } else {
            halo.changeColor(color);
            halo.makeVisible();
        }

        body.moveHorizontal(newX - xPosition);
        body.moveVertical(newY - yPosition);
        head.moveHorizontal(newX - xPosition);
        head.moveVertical(newY - yPosition);
        xPosition = newX;
        yPosition = newY;
        body.makeVisible();
        head.makeVisible();

        int newFootX = (int) Math.round(player.getFootX());
        int newFootY = (int) Math.round(player.getFootY());
        foot.moveHorizontal(newFootX - footX);
        foot.moveVertical(newFootY - footY);
        footX = newFootX;
        footY = newFootY;
        if (player.isKicking()) {
            foot.makeVisible();
        } else {
            foot.makeInvisible();
        }
    }

    // Oculta el jugador del canvas
    public void hide()
    {
        if (body != null) {
            halo.makeInvisible();
            body.makeInvisible();
            head.makeInvisible();
            foot.makeInvisible();
        }
    }
}
