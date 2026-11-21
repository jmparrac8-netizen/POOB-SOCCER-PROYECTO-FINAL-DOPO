package PRESENTATION;

import DOMINIO.Goal;

// ParraJ
// Figura de una cancha: un rectangulo blanco con el travesano (la parte solida de arriba) en negro.
// Las figuras se crean la primera vez que se dibuja.
public class GoalView
{
    private final Goal goal;
    private Rectangle frame;
    private Rectangle crossbar;
    private int xPosition;
    private int yPosition;

    // Crea la vista de una cancha
    public GoalView(Goal goal)
    {
        this.goal = goal;
    }

    // Crea el marco y el travesano en la esquina (0, 0) si todavia no existen
    private void prepareFrame()
    {
        if (frame != null) {
            return;
        }
        frame = new Rectangle();
        frame.changeSize((int) Goal.HEIGHT, (int) Goal.WIDTH);
        frame.changeColor("white");
        frame.moveHorizontal(-70);
        frame.moveVertical(-15);
        crossbar = new Rectangle();
        crossbar.changeSize((int) Goal.CROSSBAR, (int) Goal.WIDTH);
        crossbar.changeColor("black");
        crossbar.moveHorizontal(-70);
        crossbar.moveVertical(-15);
        xPosition = 0;
        yPosition = 0;
    }

    // Lleva las figuras a la posicion de la cancha. El travesano se redibuja despues del marco
    public void draw()
    {
        prepareFrame();
        int newX = (int) Math.round(goal.getX());
        int newY = (int) Math.round(goal.getY());
        frame.moveHorizontal(newX - xPosition);
        frame.moveVertical(newY - yPosition);
        crossbar.moveHorizontal(newX - xPosition);
        crossbar.moveVertical(newY - yPosition);
        xPosition = newX;
        yPosition = newY;
        frame.makeVisible();
        crossbar.makeVisible();
    }

    // Oculta la cancha del canvas
    public void hide()
    {
        if (frame != null) {
            frame.makeInvisible();
            crossbar.makeInvisible();
        }
    }
}
