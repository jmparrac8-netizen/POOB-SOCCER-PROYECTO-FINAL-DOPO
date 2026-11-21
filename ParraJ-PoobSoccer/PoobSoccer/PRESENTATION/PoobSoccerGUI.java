package PRESENTATION;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import DOMINIO.PoobSoccer;

// ParraJ
// Ventana principal de Head Soccer. Tiene tres pantallas que se cambian
// con un CardLayout: inicio, seleccion de modo y juego.
// Por ahora solo funciona el modo P1 vs P2.
// Jugador 1: A, D mover, W saltar.
// Jugador 2: flechas izquierda y derecha mover, flecha arriba saltar.
public class PoobSoccerGUI extends JFrame
{
    private static final String INICIO = "inicio";
    private static final String MODOS = "modos";
    private static final String JUEGO = "juego";

    private PoobSoccer poob;
    private Timer timer;
    private boolean running;
    private boolean wasRunning;
    private long lastNano;

    private CardLayout cardLayout;
    private JPanel cards;

    // Pantalla de inicio
    private JButton botonPlay;
    private JButton botonExit;
    private JMenuItem itemOpen;
    private JMenuItem itemSave;

    // Pantalla de modos
    private JButton botonPvP;
    private JButton botonPvBot;
    private JButton botonBotBot;
    private JButton botonVolver;

    // Pantalla de juego
    private PanelJuego panelJuego;
    private JLabel timerLabel;
    private JLabel scoreLabel;

    // Crea la ventana y prepara la vista y el controlador
    public PoobSoccerGUI()
    {
        setTitle("Head Soccer");
        prepareView();
        prepareController();
    }

    // Prepara el menu y las tres pantallas
    private void prepareView()
    {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        prepareViewMenu();

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.add(prepareViewInicio(), INICIO);
        cards.add(prepareViewModos(), MODOS);
        cards.add(prepareViewJuego(), JUEGO);
        add(cards, BorderLayout.CENTER);

        cardLayout.show(cards, INICIO);
    }

    // Prepara el menu Archivo con Open y Save
    private void prepareViewMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");

        itemOpen = new JMenuItem("Open");
        itemSave = new JMenuItem("Save");

        archivo.add(itemOpen);
        archivo.add(itemSave);

        menuBar.add(archivo);
        setJMenuBar(menuBar);
    }

    // Pantalla de inicio: titulo pequeno arriba y botones grandes uno sobre otro
    private JPanel prepareViewInicio()
    {
        JPanel inicio = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Head Soccer", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 40f));
        titulo.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        inicio.add(titulo, BorderLayout.NORTH);

        botonPlay = new JButton("Play");
        botonExit = new JButton("Exit");
        Font fuente = botonPlay.getFont().deriveFont(Font.BOLD, 20f);
        Dimension tamano = new Dimension(300, 60);
        for (JButton b : new JButton[]{botonPlay, botonExit}) {
            b.setFont(fuente);
            b.setPreferredSize(tamano);
            b.setFocusable(false);
        }

        JPanel botones = new JPanel(new GridLayout(2, 1, 0, 20));
        botones.add(botonPlay);
        botones.add(botonExit);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.add(botones);
        inicio.add(centro, BorderLayout.CENTER);

        return inicio;
    }

    // Pantalla de seleccion de modo: tres modos y un boton para volver
    private JPanel prepareViewModos()
    {
        JPanel modos = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Selecciona el modo de juego", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 36f));
        titulo.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        modos.add(titulo, BorderLayout.NORTH);

        botonPvP = new JButton("P1 vs P2");
        botonPvBot = new JButton("P1 vs Bot");
        botonBotBot = new JButton("Bot vs Bot");
        Font fuente = botonPvP.getFont().deriveFont(Font.BOLD, 24f);
        for (JButton b : new JButton[]{botonPvP, botonPvBot, botonBotBot}) {
            b.setFont(fuente);
            b.setFocusable(false);
        }

        JPanel centro = new JPanel(new GridLayout(3, 1, 0, 20));
        centro.setBorder(BorderFactory.createEmptyBorder(60, 400, 60, 400));
        centro.add(botonPvP);
        centro.add(botonPvBot);
        centro.add(botonBotBot);
        modos.add(centro, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout());
        botonVolver = new JButton("Volver");
        botonVolver.setFocusable(false);
        sur.add(botonVolver);
        modos.add(sur, BorderLayout.SOUTH);

        return modos;
    }

    // Pantalla de juego: cronometro y marcador arriba, el campo al centro
    private JPanel prepareViewJuego()
    {
        JPanel juego = new JPanel(new BorderLayout());

        timerLabel = new JLabel("90", SwingConstants.CENTER);
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 36f));
        scoreLabel = new JLabel("0 - 0", SwingConstants.CENTER);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 24f));

        JPanel hud = new JPanel(new GridLayout(2, 1));
        hud.add(timerLabel);
        hud.add(scoreLabel);

        panelJuego = new PanelJuego();

        juego.add(hud, BorderLayout.NORTH);
        juego.add(panelJuego, BorderLayout.CENTER);
        return juego;
    }

    // Prepara el cierre de la ventana, el timer, el teclado, el menu y los botones
    private void prepareController()
    {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                exit();
            }
        });
        prepareControllerTimer();
        prepareControllerKeys();
        prepareControllerMenu();
        prepareControllerButtons();
        prepareControllerModos();
    }

    // El timer llama a tick unas 60 veces por segundo
    private void prepareControllerTimer()
    {
        timer = new Timer(16, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tick();
            }
        });
    }

    // Teclado de los dos jugadores
    private void prepareControllerKeys()
    {
        panelJuego.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (poob == null || !running) {
                    return;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:     poob.getPlayer1().setMovingLeft(true); break;
                    case KeyEvent.VK_D:     poob.getPlayer1().setMovingRight(true); break;
                    case KeyEvent.VK_W:     poob.getPlayer1().jump(); break;
                    case KeyEvent.VK_LEFT:  poob.getPlayer2().setMovingLeft(true); break;
                    case KeyEvent.VK_RIGHT: poob.getPlayer2().setMovingRight(true); break;
                    case KeyEvent.VK_UP:    poob.getPlayer2().jump(); break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                if (poob == null) {
                    return;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:     poob.getPlayer1().setMovingLeft(false); break;
                    case KeyEvent.VK_D:     poob.getPlayer1().setMovingRight(false); break;
                    case KeyEvent.VK_LEFT:  poob.getPlayer2().setMovingLeft(false); break;
                    case KeyEvent.VK_RIGHT: poob.getPlayer2().setMovingRight(false); break;
                }
            }
        });
    }

    // Acciones del menu Archivo
    private void prepareControllerMenu()
    {
        itemOpen.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                open();
            }
        });
        itemSave.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                save();
            }
        });
    }

    // Acciones de los botones de la pantalla de inicio
    private void prepareControllerButtons()
    {
        botonPlay.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                play();
            }
        });
        botonExit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                exit();
            }
        });
    }

    // Acciones de los botones de la pantalla de modos
    private void prepareControllerModos()
    {
        botonPvP.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                startGame();
            }
        });
        botonPvBot.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                modoNoDisponible();
            }
        });
        botonBotBot.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                modoNoDisponible();
            }
        });
        botonVolver.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                cardLayout.show(cards, INICIO);
            }
        });
    }

    // Detiene el juego mientras hay un dialogo abierto
    private void pause()
    {
        wasRunning = timer.isRunning();
        timer.stop();
    }

    // Continua el juego si estaba corriendo antes del dialogo
    private void resume()
    {
        if (wasRunning && running) {
            lastNano = System.nanoTime();
            timer.start();
        }
        if (running) {
            panelJuego.requestFocusInWindow();
        }
    }

    // Abre un archivo (por ahora solo muestra su nombre)
    public void open()
    {
        pause();
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            JOptionPane.showMessageDialog(this,
                "Abrir: funcionalidad en construccion.\nArchivo: " + file.getName(),
                "Abrir", JOptionPane.INFORMATION_MESSAGE);
        }
        resume();
    }

    // Guarda un archivo (por ahora solo muestra su nombre)
    public void save()
    {
        pause();
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            JOptionPane.showMessageDialog(this,
                "Salvar: funcionalidad en construccion.\nArchivo: " + file.getName(),
                "Salvar", JOptionPane.INFORMATION_MESSAGE);
        }
        resume();
    }

    // Pregunta si se quiere salir y cierra el programa si la respuesta es si
    public void exit()
    {
        pause();
        int option = JOptionPane.showConfirmDialog(this,
            "Desea salir de Head Soccer?", "Salir", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
        resume();
    }

    // Boton Play: pasa a la pantalla de seleccion de modo
    public void play()
    {
        cardLayout.show(cards, MODOS);
    }

    // Avisa que el modo elegido todavia no funciona
    private void modoNoDisponible()
    {
        JOptionPane.showMessageDialog(this, "Este modo aun no esta disponible.",
            "Modo no disponible", JOptionPane.INFORMATION_MESSAGE);
    }

    // Modo P1 vs P2: crea el juego y empieza de inmediato
    private void startGame()
    {
        timer.stop();
        poob = new PoobSoccer();
        panelJuego.setPoob(poob);
        updateHud();

        running = true;
        cardLayout.show(cards, JUEGO);
        lastNano = System.nanoTime();
        timer.start();
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                panelJuego.requestFocusInWindow();
            }
        });
    }

    // Un paso del juego: fisicas, cronometro, marcador y figuras
    private void tick()
    {
        long now = System.nanoTime();
        double dt = (now - lastNano) / 1e9;
        lastNano = now;
        if (dt > 0.1) {
            dt = 0.1;
        }

        poob.update();
        poob.advanceTime(dt);
        updateHud();
        panelJuego.refresh();

        if (poob.isGameOver()) {
            finishGame();
        }
    }

    // Actualiza el cronometro y el marcador
    private void updateHud()
    {
        timerLabel.setText(String.valueOf((int) Math.ceil(poob.getTimeLeft())));
        scoreLabel.setText(poob.getScore1() + " - " + poob.getScore2());
    }

    // Termina el juego, muestra quien gano y vuelve a la pantalla de inicio
    private void finishGame()
    {
        timer.stop();
        running = false;

        int winner = poob.getWinner();
        String mensaje = (winner == 0)
            ? "Empate " + poob.getScore1() + " - " + poob.getScore2()
            : "Gano el jugador " + winner + " " + poob.getScore1() + " - " + poob.getScore2();
        JOptionPane.showMessageDialog(this, mensaje, "Fin del juego", JOptionPane.INFORMATION_MESSAGE);

        cardLayout.show(cards, INICIO);
    }

    // Abre la ventana del juego
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new PoobSoccerGUI().setVisible(true);
            }
        });
    }
}
