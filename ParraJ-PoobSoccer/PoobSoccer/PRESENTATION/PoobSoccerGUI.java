package PRESENTATION;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import DOMINIO.Effect;
import DOMINIO.GameCharacter;
import DOMINIO.MatchType;
import DOMINIO.Player;
import DOMINIO.PoobSoccer;

// ParraJ
// Ventana principal de Head Soccer. Tiene seis pantallas que se cambian
// con un CardLayout: inicio, seleccion de modo, seleccion de modalidad (goles o tiempo),
// seleccion de personajes, controles (5 segundos) y juego. Durante la partida hay un boton de pausa.
// Por ahora solo funciona el modo P1 vs P2.
// Jugador 1: A, D mover, W saltar, S patear, Q activar el poder.
// Jugador 2: flechas izquierda y derecha mover, flecha arriba saltar, flecha abajo patear,
// Ctrl derecho activar el poder.
public class PoobSoccerGUI extends JFrame
{
    private static final String INICIO = "inicio";
    private static final String MODOS = "modos";
    private static final String MODALIDAD = "modalidad";
    private static final String PERSONAJES = "personajes";
    private static final String CONTROLES = "controles";
    private static final int CONTROLES_SEGUNDOS = 5;
    private static final String[] CONTROLES_P1 = {"A / D: mover", "W: saltar", "S: patear", "Q: usar poder"};
    private static final String[] CONTROLES_P2 = {"Flechas izquierda / derecha: mover", "Flecha arriba: saltar",
        "Flecha abajo: patear", "Ctrl derecho: usar poder"};
    private static final String JUEGO = "juego";

    private PoobSoccer poob;
    private Timer timer;
    private boolean running;
    private boolean wasRunning;
    private boolean paused;
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
    private JButton botonVolver;

    // Pantalla de modalidad
    private JComboBox<String> comboGoles;
    private JComboBox<String> comboTiempo;
    private JButton botonStartGoles;
    private JButton botonStartTiempo;
    private JButton botonVolverModalidad;
    private MatchType tipoPartido;
    private int metaPartido;

    // Pantalla de personajes (un juego de componentes por jugador)
    private GameCharacter[] personajes;
    private JToggleButton[][] botonesPersonaje;
    private ButtonGroup[] gruposPersonaje;
    private JLabel[][] valoresStats;
    private int[] elegido;
    private JButton botonStartPersonajes;
    private JButton botonVolverPersonajes;

    // Pantalla de controles
    private Timer timerControles;

    // Pantalla de juego
    private JButton botonPausa;
    private PanelJuego panelJuego;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JLabel infoJugador1;
    private JLabel infoJugador2;

    // Crea la ventana y prepara la vista y el controlador
    public PoobSoccerGUI()
    {
        setTitle("Head Soccer");
        prepareView();
        prepareController();
    }

    // Prepara el menu y las seis pantallas
    private void prepareView()
    {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        prepareViewMenu();

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.add(prepareViewInicio(), INICIO);
        cards.add(prepareViewModos(), MODOS);
        cards.add(prepareViewModalidad(), MODALIDAD);
        cards.add(prepareViewPersonajes(), PERSONAJES);
        cards.add(prepareViewControles(), CONTROLES);
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

    // Pantalla de seleccion de modo: dos modos y un boton para volver
    private JPanel prepareViewModos()
    {
        JPanel modos = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Selecciona el modo de juego", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 36f));
        titulo.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        modos.add(titulo, BorderLayout.NORTH);

        botonPvP = new JButton("P1 vs P2");
        botonPvBot = new JButton("P1 vs Bot");
        Font fuente = botonPvP.getFont().deriveFont(Font.BOLD, 24f);
        for (JButton b : new JButton[]{botonPvP, botonPvBot}) {
            b.setFont(fuente);
            b.setFocusable(false);
        }

        JPanel centro = new JPanel(new GridLayout(2, 1, 0, 20));
        centro.setBorder(BorderFactory.createEmptyBorder(60, 400, 60, 400));
        centro.add(botonPvP);
        centro.add(botonPvBot);
        modos.add(centro, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout());
        botonVolver = new JButton("Volver");
        botonVolver.setFocusable(false);
        sur.add(botonVolver);
        modos.add(sur, BorderLayout.SOUTH);

        return modos;
    }

    // Pantalla de modalidad: a la izquierda el partido por goles (3, 6 o 9) y a la derecha
    // el partido por tiempo (60, 90 o 120 segundos). Cada lado tiene su boton Start
    private JPanel prepareViewModalidad()
    {
        JPanel modalidad = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Selecciona Modalidad", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 36f));
        titulo.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        modalidad.add(titulo, BorderLayout.NORTH);

        String[] goles = new String[PoobSoccer.GOAL_OPTIONS.length];
        for (int i = 0; i < goles.length; i++) {
            goles[i] = PoobSoccer.GOAL_OPTIONS[i] + " goles";
        }
        String[] tiempos = new String[PoobSoccer.TIME_OPTIONS.length];
        for (int i = 0; i < tiempos.length; i++) {
            tiempos[i] = PoobSoccer.TIME_OPTIONS[i] + " segundos";
        }
        comboGoles = new JComboBox<String>(goles);
        comboTiempo = new JComboBox<String>(tiempos);
        botonStartGoles = new JButton("Start");
        botonStartTiempo = new JButton("Start");

        JPanel mitades = new JPanel(new GridLayout(1, 2));
        JPanel izquierda = prepareViewMitadModalidad("Goles", comboGoles, botonStartGoles);
        izquierda.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.BLACK));
        mitades.add(izquierda);
        mitades.add(prepareViewMitadModalidad("Tiempo", comboTiempo, botonStartTiempo));
        modalidad.add(mitades, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout());
        botonVolverModalidad = new JButton("Volver");
        botonVolverModalidad.setFocusable(false);
        sur.add(botonVolverModalidad);
        modalidad.add(sur, BorderLayout.SOUTH);

        return modalidad;
    }

    // Una mitad de la pantalla de modalidad: titulo, lista de opciones y boton Start
    private JPanel prepareViewMitadModalidad(String texto, JComboBox<String> combo, JButton start)
    {
        JLabel etiqueta = new JLabel(texto, SwingConstants.CENTER);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 32f));
        combo.setFont(combo.getFont().deriveFont(20f));
        start.setFont(start.getFont().deriveFont(Font.BOLD, 24f));
        start.setFocusable(false);

        JPanel columna = new JPanel(new GridLayout(3, 1, 0, 30));
        columna.setPreferredSize(new Dimension(300, 240));
        columna.add(etiqueta);
        columna.add(combo);
        columna.add(start);

        JPanel mitad = new JPanel(new GridBagLayout());
        mitad.add(columna);
        return mitad;
    }

    // Pantalla de seleccion de personajes: una mitad por jugador y un boton Start abajo
    private JPanel prepareViewPersonajes()
    {
        personajes = GameCharacter.available();
        botonesPersonaje = new JToggleButton[2][personajes.length];
        gruposPersonaje = new ButtonGroup[]{new ButtonGroup(), new ButtonGroup()};
        valoresStats = new JLabel[2][5];
        elegido = new int[]{-1, -1};

        JPanel panel = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Selecciona tu personaje", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 36f));
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel mitades = new JPanel(new GridLayout(1, 2));
        JPanel izquierda = prepareViewMitadPersonajes(0, "Jugador 1  (A/D mover, W saltar, S patear, Q poder)");
        izquierda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 2, Color.BLACK), izquierda.getBorder()));
        mitades.add(izquierda);
        mitades.add(prepareViewMitadPersonajes(1, "Jugador 2  (flechas mover y saltar, flecha abajo patear, Ctrl derecho poder)"));
        panel.add(mitades, BorderLayout.CENTER);

        botonStartPersonajes = new JButton("Start");
        botonStartPersonajes.setFont(botonStartPersonajes.getFont().deriveFont(Font.BOLD, 24f));
        botonStartPersonajes.setPreferredSize(new Dimension(200, 60));
        botonStartPersonajes.setFocusable(false);
        botonVolverPersonajes = new JButton("Volver");
        botonVolverPersonajes.setFocusable(false);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        sur.add(botonVolverPersonajes);
        sur.add(botonStartPersonajes);
        panel.add(sur, BorderLayout.SOUTH);

        return panel;
    }

    // Mitad de un jugador: arriba la tabla de estadisticas (PS, DMG, Speed, Jump, Power)
    // y abajo un boton con el nombre de cada pais
    private JPanel prepareViewMitadPersonajes(int jugador, String titulo)
    {
        JPanel mitad = new JPanel(new BorderLayout());
        mitad.setBorder(BorderFactory.createTitledBorder(titulo));

        String[] nombres = {"PS", "DMG", "Speed", "Jump", "Power"};
        JPanel tabla = new JPanel(new GridLayout(2, 5));
        tabla.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        for (String nombre : nombres) {
            JLabel cabecera = new JLabel(nombre, SwingConstants.CENTER);
            cabecera.setFont(cabecera.getFont().deriveFont(Font.BOLD, 18f));
            tabla.add(cabecera);
        }
        for (int i = 0; i < nombres.length; i++) {
            valoresStats[jugador][i] = new JLabel("-", SwingConstants.CENTER);
            valoresStats[jugador][i].setFont(valoresStats[jugador][i].getFont().deriveFont(Font.BOLD, 22f));
            tabla.add(valoresStats[jugador][i]);
        }
        tabla.setPreferredSize(new Dimension(100, 90));
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        arriba.add(tabla, BorderLayout.CENTER);
        mitad.add(arriba, BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(2, 2, 30, 30));
        botones.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        for (int i = 0; i < personajes.length; i++) {
            JToggleButton boton = new JToggleButton(personajes[i].getName());
            boton.setFont(boton.getFont().deriveFont(Font.BOLD, 22f));
            boton.setFocusable(false);
            botonesPersonaje[jugador][i] = boton;
            gruposPersonaje[jugador].add(boton);
            botones.add(boton);
        }
        mitad.add(botones, BorderLayout.CENTER);
        return mitad;
    }

    // Pantalla de controles: dividida a la mitad, a la izquierda los controles del jugador 1
    // y a la derecha los del jugador 2. Dura unos segundos y despues empieza el partido
    private JPanel prepareViewControles()
    {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Controles", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 36f));
        titulo.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel mitades = new JPanel(new GridLayout(1, 2));
        JPanel izquierda = prepareViewMitadControles("Jugador 1", CONTROLES_P1);
        izquierda.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.BLACK));
        mitades.add(izquierda);
        mitades.add(prepareViewMitadControles("Jugador 2", CONTROLES_P2));
        panel.add(mitades, BorderLayout.CENTER);

        return panel;
    }

    // Una mitad de la pantalla de controles: el nombre del jugador y una linea por control
    private JPanel prepareViewMitadControles(String jugador, String[] lineas)
    {
        JLabel nombre = new JLabel(jugador, SwingConstants.CENTER);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 30f));

        JPanel columna = new JPanel(new GridLayout(lineas.length + 1, 1, 0, 20));
        columna.add(nombre);
        for (String linea : lineas) {
            JLabel etiqueta = new JLabel(linea, SwingConstants.CENTER);
            etiqueta.setFont(etiqueta.getFont().deriveFont(22f));
            columna.add(etiqueta);
        }

        JPanel mitad = new JPanel(new GridBagLayout());
        mitad.add(columna);
        return mitad;
    }

    // Pantalla de juego: informacion de cada jugador, cronometro y marcador arriba, el campo al centro
    private JPanel prepareViewJuego()
    {
        JPanel juego = new JPanel(new BorderLayout());

        timerLabel = new JLabel("90", SwingConstants.CENTER);
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 36f));
        scoreLabel = new JLabel("0 - 0", SwingConstants.CENTER);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 24f));

        botonPausa = new JButton("Pausa");
        botonPausa.setFocusable(false);
        JPanel panelPausa = new JPanel(new FlowLayout());
        panelPausa.add(botonPausa);

        JPanel centroHud = new JPanel(new GridLayout(3, 1));
        centroHud.add(timerLabel);
        centroHud.add(scoreLabel);
        centroHud.add(panelPausa);

        infoJugador1 = new JLabel("", SwingConstants.LEFT);
        infoJugador2 = new JLabel("", SwingConstants.RIGHT);
        infoJugador1.setFont(infoJugador1.getFont().deriveFont(Font.BOLD, 15f));
        infoJugador2.setFont(infoJugador2.getFont().deriveFont(Font.BOLD, 15f));
        infoJugador1.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        infoJugador2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        JPanel hud = new JPanel(new GridLayout(1, 3));
        hud.add(infoJugador1);
        hud.add(centroHud);
        hud.add(infoJugador2);

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
        prepareControllerModalidad();
        prepareControllerPersonajes();
        prepareControllerPausa();
        prepareControllerControles();
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
                if (poob == null || !running || paused) {
                    return;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:     poob.getPlayer1().setMovingLeft(true); break;
                    case KeyEvent.VK_D:     poob.getPlayer1().setMovingRight(true); break;
                    case KeyEvent.VK_W:     poob.getPlayer1().jump(); break;
                    case KeyEvent.VK_S:     poob.getPlayer1().kick(); break;
                    case KeyEvent.VK_Q:     poob.getPlayer1().activatePower(); break;
                    case KeyEvent.VK_LEFT:  poob.getPlayer2().setMovingLeft(true); break;
                    case KeyEvent.VK_RIGHT: poob.getPlayer2().setMovingRight(true); break;
                    case KeyEvent.VK_UP:    poob.getPlayer2().jump(); break;
                    case KeyEvent.VK_DOWN:  poob.getPlayer2().kick(); break;
                    case KeyEvent.VK_CONTROL:
                        if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
                            poob.getPlayer2().activatePower();
                        }
                        break;
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
                cardLayout.show(cards, MODALIDAD);
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
        botonVolver.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                cardLayout.show(cards, INICIO);
            }
        });
    }

    // Acciones de los botones de la pantalla de modalidad
    private void prepareControllerModalidad()
    {
        botonStartGoles.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tipoPartido = MatchType.GOALS;
                metaPartido = PoobSoccer.GOAL_OPTIONS[comboGoles.getSelectedIndex()];
                showPersonajes();
            }
        });
        botonStartTiempo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tipoPartido = MatchType.TIME;
                metaPartido = PoobSoccer.TIME_OPTIONS[comboTiempo.getSelectedIndex()];
                showPersonajes();
            }
        });
        botonVolverModalidad.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                cardLayout.show(cards, MODOS);
            }
        });
    }

    // Acciones de los botones de la pantalla de personajes
    private void prepareControllerPersonajes()
    {
        for (int jugador = 0; jugador < 2; jugador++) {
            for (int i = 0; i < personajes.length; i++) {
                final int j = jugador;
                final int k = i;
                botonesPersonaje[j][k].addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        if (personajes[k].isUnderConstruction()) {
                            JOptionPane.showMessageDialog(PoobSoccerGUI.this,
                                "Personaje en construccion",
                                personajes[k].getName(), JOptionPane.INFORMATION_MESSAGE);
                            if (elegido[j] >= 0) {
                                botonesPersonaje[j][elegido[j]].setSelected(true);
                            } else {
                                gruposPersonaje[j].clearSelection();
                            }
                            return;
                        }
                        elegido[j] = k;
                        showStats(j, personajes[k]);
                    }
                });
            }
        }
        botonStartPersonajes.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (elegido[0] < 0 || elegido[1] < 0) {
                    JOptionPane.showMessageDialog(PoobSoccerGUI.this,
                        "Cada jugador debe elegir un personaje antes de empezar.",
                        "Falta elegir personaje", JOptionPane.WARNING_MESSAGE);
                } else {
                    showControles();
                }
            }
        });
        botonVolverPersonajes.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                cardLayout.show(cards, MODALIDAD);
            }
        });
    }

    // Muestra la pantalla de personajes sin ninguno elegido
    private void showPersonajes()
    {
        for (int jugador = 0; jugador < 2; jugador++) {
            gruposPersonaje[jugador].clearSelection();
            elegido[jugador] = -1;
            for (JLabel valor : valoresStats[jugador]) {
                valor.setText("-");
            }
        }
        cardLayout.show(cards, PERSONAJES);
    }

    // Muestra las estadisticas del personaje en la tabla del jugador:
    // PS (vida maxima), DMG (dano de patada), Speed, Jump y Power (dano del tiro de poder, "-" si no tiene)
    private void showStats(int jugador, GameCharacter c)
    {
        valoresStats[jugador][0].setText(String.valueOf(Math.round(c.getMaxLife())));
        valoresStats[jugador][1].setText(String.valueOf(Math.round(c.getKickDamage())));
        valoresStats[jugador][2].setText(String.valueOf(c.getSpeed()));
        valoresStats[jugador][3].setText(String.valueOf(c.getJump()));
        valoresStats[jugador][4].setText(c.hasPower() ? String.valueOf(Math.round(c.getPowerDamage())) : "-");
    }

    // Boton de pausa: detiene o reanuda el partido
    private void prepareControllerPausa()
    {
        botonPausa.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                togglePause();
            }
        });
    }

    // El timer de la pantalla de controles espera unos segundos (una sola vez) y empieza el partido
    private void prepareControllerControles()
    {
        timerControles = new Timer(CONTROLES_SEGUNDOS * 1000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                startGame();
            }
        });
        timerControles.setRepeats(false);
    }

    // Muestra la pantalla de controles y empieza a contar el tiempo
    private void showControles()
    {
        cardLayout.show(cards, CONTROLES);
        timerControles.restart();
    }

    // Pausa el partido si esta corriendo, o lo reanuda si estaba en pausa
    private void togglePause()
    {
        if (!running) {
            return;
        }
        if (paused) {
            paused = false;
            botonPausa.setText("Pausa");
            lastNano = System.nanoTime();
            timer.start();
            panelJuego.requestFocusInWindow();
        } else {
            paused = true;
            botonPausa.setText("Continuar");
            timer.stop();
            poob.getPlayer1().setMovingLeft(false);
            poob.getPlayer1().setMovingRight(false);
            poob.getPlayer2().setMovingLeft(false);
            poob.getPlayer2().setMovingRight(false);
        }
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

    // Crea el juego con la modalidad y los personajes elegidos y empieza de inmediato
    private void startGame()
    {
        timer.stop();
        poob = new PoobSoccer(tipoPartido, metaPartido, personajes[elegido[0]], personajes[elegido[1]]);
        panelJuego.setPoob(poob);
        updateHud();

        running = true;
        paused = false;
        botonPausa.setText("Pausa");
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

    // Actualiza el cronometro (o la meta de goles), el marcador y la informacion de cada jugador
    private void updateHud()
    {
        if (poob.getMatchType() == MatchType.GOALS) {
            timerLabel.setText("Primero a " + poob.getTarget() + " goles");
        } else if (poob.isSuddenDeath()) {
            timerLabel.setText("Muerte subita");
        } else {
            timerLabel.setText(String.valueOf((int) Math.ceil(poob.getTimeLeft())));
        }
        scoreLabel.setText(poob.getScore1() + " - " + poob.getScore2());
        infoJugador1.setText(playerInfo("Jugador 1", poob.getPlayer1()));
        infoJugador2.setText(playerInfo("Jugador 2", poob.getPlayer2()));
    }

    // Texto con el pais, la vida, la barra de poder y el estado (aura, efecto o aturdimiento) de un jugador
    private String playerInfo(String titulo, Player p)
    {
        String poder;
        if (!p.getCharacter().hasPower()) {
            poder = "no tiene";
        } else {
            poder = Math.round(p.getPowerBar() * 100) + "%";
        }
        String estado = "normal";
        if (p.isStunned()) {
            estado = "ATURDIDO";
        } else if (p.getEffect() == Effect.ELECTROCUTED) {
            estado = "ELECTROCUTADO";
        } else if (p.getEffect() == Effect.KNOCKED_OUT) {
            estado = "INCONSCIENTE";
        } else if (p.hasPower()) {
            estado = "AURA: " + p.getCharacter().getPower().getName();
        }
        return "<html>" + titulo + " - " + p.getCharacter().getName()
            + "<br>Vida: " + (int) Math.ceil(p.getLife()) + " / " + (int) Math.round(p.getMaxLife())
            + "<br>Poder: " + poder
            + "<br>Estado: " + estado + "</html>";
    }

    // Termina el juego, muestra quien gano y vuelve a la pantalla de inicio
    private void finishGame()
    {
        timer.stop();
        running = false;
        paused = false;

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
