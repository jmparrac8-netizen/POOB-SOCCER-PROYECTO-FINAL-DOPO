package PRESENTATION;

import DOMINIO.Ball;

// ParraJ
// Figura del balon: un circulo blanco. Lee la posicion del balon del dominio.
// La figura se crea la primera vez que se dibuja.
public class BallView
{
    private final Ball ball;
    private Circle circle;
    private int xPosition;
    private int yPosition;

    // Crea la vista de un balon
    public BallView(Ball ball)
    {
        this.ball = ball;
    }

    // Crea el circulo en la esquina (0, 0) si todavia no existe
    private void prepareCircle()
    {
        if (circle != null) {
            return;
        }
        circle = new Circle();
        circle.changeSize((int) (Ball.RADIUS * 2));
        circle.changeColor("white");
        circle.moveHorizontal(-20);
        circle.moveVertical(-15);
        xPosition = 0;
        yPosition = 0;
    }

    // Lleva el circulo a la posicion del balon. Se redibuja para quedar encima de las demas figuras
    public void draw()
    {
        prepareCircle();
        int newX = (int) Math.round(ball.getX() - Ball.RADIUS);
        int newY = (int) Math.round(ball.getY() - Ball.RADIUS);
        circle.moveHorizontal(newX - xPosition);
        circle.moveVertical(newY - yPosition);
        xPosition = newX;
        yPosition = newY;
        circle.makeVisible();
    }

    // Oculta el balon del canvas
    public void hide()
    {
        if (circle != null) {
            circle.makeInvisible();
        }
    }
}
