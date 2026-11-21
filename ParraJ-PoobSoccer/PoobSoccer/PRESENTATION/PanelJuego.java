package PRESENTATION;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import DOMINIO.PoobSoccer;

// ParraJ
// Panel donde se ve el juego. Contiene el canvas. Cada objeto del dominio
// (canchas, jugadores y balon) tiene una vista que lo dibuja en el canvas.
public class PanelJuego extends JPanel
{
    private PoobSoccer poob;
    private GoalView leftGoalView;
    private GoalView rightGoalView;
    private PlayerView player1View;
    private PlayerView player2View;
    private BallView ballView;

    // Crea el panel con el canvas adentro
    public PanelJuego()
    {
        super(new BorderLayout());
        add(Canvas.getCanvas().getPane(), BorderLayout.CENTER);
        setFocusable(true);
    }

    // Cambia el juego que se muestra
    public void setPoob(PoobSoccer poob)
    {
        hideViews();
        this.poob = poob;
        leftGoalView = new GoalView(poob.getLeftGoal());
        rightGoalView = new GoalView(poob.getRightGoal());
        player1View = new PlayerView(poob.getPlayer1());
        player2View = new PlayerView(poob.getPlayer2());
        ballView = new BallView(poob.getBall());
        refresh();
    }

    // Mueve las figuras a la posicion actual del juego y las redibuja una sola vez.
    // Las canchas van primero y el balon al final para que quede encima.
    public void refresh()
    {
        if (poob == null) {
            return;
        }
        Canvas canvas = Canvas.getCanvas();
        canvas.startBatch();
        leftGoalView.draw();
        rightGoalView.draw();
        player1View.draw();
        player2View.draw();
        ballView.draw();
        canvas.endBatch();
    }

    // Oculta las figuras del juego anterior, si hay
    private void hideViews()
    {
        if (poob == null) {
            return;
        }
        Canvas canvas = Canvas.getCanvas();
        canvas.startBatch();
        leftGoalView.hide();
        rightGoalView.hide();
        player1View.hide();
        player2View.hide();
        ballView.hide();
        canvas.endBatch();
    }
}
