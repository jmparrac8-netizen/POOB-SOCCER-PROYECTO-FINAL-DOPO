package PRESENTATION;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

/**
 * Canvas is a class to allow for simple graphical drawing on a canvas.
 * This is a modification of the general purpose Canvas, specially made for
 * the BlueJ "shapes" example. 
 *
 * @author: Bruce Quig
 * @author: Michael Kolling (mik)
 *
 * @version: 1.6 (shapes)
 */
public class Canvas
{
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 600;

    private static Canvas canvasSingleton;

    // Devuelve el unico canvas del juego, y lo crea la primera vez
    public static Canvas getCanvas()
    {
        if (canvasSingleton == null) {
            canvasSingleton = new Canvas(WIDTH, HEIGHT, new Color(80, 170, 80));
        }
        return canvasSingleton;
    }

    private CanvasPane canvas;
    private Graphics2D graphic;
    private Color backgroundColour;
    private BufferedImage canvasImage;
    private List <Object> objects;
    private HashMap <Object,ShapeDescription> shapes;
    private boolean batching;

    // Crea el canvas con el tamano y el color de fondo dados
    private Canvas(int width, int height, Color bgColour)
    {
        backgroundColour = bgColour;
        canvasImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        graphic = (Graphics2D) canvasImage.getGraphics();
        graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas = new CanvasPane();
        canvas.setPreferredSize(new Dimension(width, height));
        canvas.setBackground(Color.black);
        canvas.setFocusable(false);
        objects = new ArrayList <Object>();
        shapes = new HashMap <Object,ShapeDescription>();
        batching = false;
        erase();
    }

    // Devuelve el panel donde se ve el canvas, para ponerlo dentro del GUI
    public JPanel getPane()
    {
        return canvas;
    }

    // Empieza un lote: las figuras no se redibujan hasta llamar a endBatch
    public void startBatch()
    {
        batching = true;
    }

    // Termina el lote y redibuja todo una sola vez
    public void endBatch()
    {
        batching = false;
        redraw();
    }

    /**
     * Draw a given shape onto the canvas.
     * @param  referenceObject  an object to define identity for this shape
     * @param  color            the color of the shape
     * @param  shape            the shape object to be drawn on the canvas
     */
     // Note: this is a slightly backwards way of maintaining the shape
     // objects. It is carefully designed to keep the visible shape interfaces
     // in this project clean and simple for educational purposes.
    public void draw(Object referenceObject, String color, Shape shape)
    {
        objects.remove(referenceObject);   // just in case it was already there
        objects.add(referenceObject);      // add at the end
        shapes.put(referenceObject, new ShapeDescription(shape, color));
        redraw();
    }
 
    /**
     * Erase a given shape's from the screen.
     * @param  referenceObject  the shape object to be erased 
     */
    public void erase(Object referenceObject)
    {
        objects.remove(referenceObject);   // just in case it was already there
        shapes.remove(referenceObject);
        redraw();
    }

    /**
     * Set the foreground colour of the Canvas.
     * @param  newColour   the new colour for the foreground of the Canvas 
     */
    public void setForegroundColor(String colorString)
    {
        if (colorString.equals("red"))
            graphic.setColor(Color.red);
        else if (colorString.equals("black"))
            graphic.setColor(Color.black);
        else if (colorString.equals("blue"))
            graphic.setColor(Color.blue);
        else if (colorString.equals("yellow"))
            graphic.setColor(Color.yellow);
        else if (colorString.equals("green"))
            graphic.setColor(Color.green);
        else if (colorString.equals("magenta"))
            graphic.setColor(Color.magenta);
        else if (colorString.equals("white"))
            graphic.setColor(Color.white);
        else if (colorString.equals("cyan"))
            graphic.setColor(Color.cyan);
        else if (colorString.equals("orange"))
            graphic.setColor(Color.orange);
        else if (colorString.equals("gray"))
            graphic.setColor(Color.gray);
        else
            graphic.setColor(Color.black);
    }

    // Antes esperaba unos milisegundos para animar. En el juego no espera,
    // porque las figuras se mueven en cada tick del timer.
    public void wait(int milliseconds)
    {
    }

    // Redibuja todas las figuras del canvas, salvo que haya un lote abierto
    private void redraw()
    {
        if (batching) {
            return;
        }
        erase();
        for (Iterator<Object> i = objects.iterator(); i.hasNext(); ) {
            shapes.get(i.next()).draw(graphic);
        }
        canvas.repaint();
    }
       
    // Borra todo el canvas con el color de fondo. No repinta.
    private void erase()
    {
        Color original = graphic.getColor();
        graphic.setColor(backgroundColour);
        graphic.fill(new java.awt.Rectangle(0, 0, canvasImage.getWidth(), canvasImage.getHeight()));
        graphic.setColor(original);
    }

    // Panel que muestra la imagen del canvas, escalada y centrada
    private class CanvasPane extends JPanel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            double scale = Math.min(getWidth() / (double) canvasImage.getWidth(),
                                    getHeight() / (double) canvasImage.getHeight());
            int w = (int) (canvasImage.getWidth() * scale);
            int h = (int) (canvasImage.getHeight() * scale);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(canvasImage, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        }
    }

    // Guarda una figura y su color
    private class ShapeDescription
    {
        private Shape shape;
        private String colorString;

        public ShapeDescription(Shape shape, String color)
        {
            this.shape = shape;
            colorString = color;
        }

        public void draw(Graphics2D graphic)
        {
            setForegroundColor(colorString);
            graphic.draw(shape);
            graphic.fill(shape);
        }
    }
}
