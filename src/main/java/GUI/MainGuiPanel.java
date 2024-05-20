package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainGuiPanel implements GUIInterface {
    private MazeRenderer mazeRenderer; // Labirynt
    private JFrame window;
    private JMenuBar menuBar;
    private JScrollPane scrollPane; // Scroll panel
    private double initialZoomFactor = 1.0; // Początkowy zoom



    private static final double ZOOM_IN_FACTOR = 1.1;
    private static final double ZOOM_OUT_FACTOR = 0.9;





    // Metoda uruchamiająca GUI
    public void run() {
        CreateMainPanel();
        CreateMazePanel();
        CreateFileReaderBar();
        CreateZoomControls();
        createOptionsBar();

        JTabbedPane tabPanel = new JTabbedPane();
        tabPanel.addTab("Labirynt", scrollPane);
        window.add(tabPanel, BorderLayout.CENTER);
        window.setJMenuBar(menuBar);
        window.setVisible(true);
    }


    // Metoda tworząca główny panel
    @Override
    public void CreateMainPanel() {
        window = new JFrame("Maze Solver - Kobus&Dutkiewicz");
        window.setIconImage(new ImageIcon("src/gallery/logo.png").getImage());
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(800, 600);
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (mazeRenderer != null && mazeRenderer.getMazeImage() != null) {
                    fitMazeToWindow();
                }
            }
        });
    }


    // Metoda tworząca panel z labiryntem
    @Override
    public void CreateMazePanel() {
        mazeRenderer = new MazeRenderer(null); // Twożenie labiryntu
        JPanel mazePanel = mazeRenderer.createMazePanel(); // Twożenie panelu z labiryntem

        // Dodanie listenerów do panelu
        attachMouseWheelListener(mazePanel);
        attachMouseListener(mazePanel);

        // Konfiguracja scroll panelu
        configureScrollPane(mazePanel);
    }


    // Metoda obsługująca scroll myszki
    private void attachMouseWheelListener(JPanel mazePanel) {
        mazePanel.addMouseWheelListener(e -> {
            double zoomFactor = mazeRenderer.getZoomFactor();
            zoomFactor *= e.getPreciseWheelRotation() < 0 ? ZOOM_IN_FACTOR : ZOOM_OUT_FACTOR;
            mazeRenderer.setZoomFactor(zoomFactor);
            updateZoom();
        });
    }


    // Metoda obsługująca kliknięcie myszką
    private void attachMouseListener(JPanel mazePanel) {
        mazePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mazeRenderer.getMazeImage() == null) return;

                int x = e.getX();
                int y = e.getY();
                int panelWidth = mazePanel.getWidth();
                int panelHeight = mazePanel.getHeight();

                int scaledWidth = (int) (mazeRenderer.getMazeImage().getWidth() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());
                int scaledHeight = (int) (mazeRenderer.getMazeImage().getHeight() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());

                int offsetX = (panelWidth - scaledWidth) / 2;
                int offsetY = (panelHeight - scaledHeight) / 2;

                // Sprawdzenie czy kliknięto w obszar labiryntu
                if (x >= offsetX && x < offsetX + scaledWidth && y >= offsetY && y < offsetY + scaledHeight) {
                    int imageX = (int) ((x - offsetX) / (mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor()));
                    int imageY = (int) ((y - offsetY) / (mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor()));



                    // Obsługa kliknięcia w komórkę labiryntu
                    MazeUtilities.handleMazeCellSelection(mazeRenderer, imageX, imageY, window);




                }
            }
        });
    }





    // Metoda konfigurująca scroll panel
    private void configureScrollPane(JPanel mazePanel) {
        scrollPane = new JScrollPane(mazePanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    }



    // Metoda aktualizująca zoom
    private void updateZoom() {
        if (mazeRenderer != null && mazeRenderer.getMazeImage() != null) {
            int newWidth = (int) (mazeRenderer.getMazeImage().getWidth() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());
            int newHeight = (int) (mazeRenderer.getMazeImage().getHeight() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());

            // Ustawienie nowych wymiarów panelu z labiryntem
            mazeRenderer.getMazePanel().setPreferredSize(new Dimension(newWidth, newHeight));
            mazeRenderer.getMazePanel().revalidate();

            // Aktualizacja scroll panelu
            scrollPane.revalidate();
            scrollPane.repaint();
        }
    }


    // Metoda tworząca pasek z plikami
    @Override
    public void CreateFileReaderBar() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");

        // Elementy menu
        JMenuItem openItem = new JMenuItem("Otwórz labirynt");
        JMenuItem saveItem = new JMenuItem("Zapisz labirynt (txt)");
        JMenuItem saveImageItem = new JMenuItem("Zapisz obraz labiryntu");

        // Dołączanie akcji do otwierania pliku z labiryntem
        openItem.addActionListener(e -> openMaze());

        // Dołączanie akcji do zapisywania labiryntu jako pliku tekstowego
        saveItem.addActionListener(e -> FileIO.saveMazeAsText(mazeRenderer.getMazeImage(), window));

        // Dołączanie akcji do zapisywania labiryntu jako obrazu
        saveImageItem.addActionListener(e -> FileIO.saveMazeAsImage(mazeRenderer.getMazeImage(), window));


        // Dodawanie pozycji do menu pliku
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveImageItem);

        // Dodawanie menu pliku do paska menu
        menuBar.add(fileMenu);
    }


    // Metoda otwierająca labirynt
    private void openMaze() {
        try {
            File mazeFile = FileIO.openMazeFile(window);
            if (mazeFile != null) {
                BufferedImage image = FileIO.prepareFile(mazeFile);
                mazeRenderer.setMazeImage(image);
                fitMazeToWindow();
                MazeUtilities.checkForEntranceAndExit(mazeRenderer, window);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(window, "Nie udało się odczytać pliku: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Metoda tworząca przyciski do zoomowania
    private void CreateZoomControls() {
        JPanel zoomPanel = new JPanel(new GridLayout(2, 1));
        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");

        zoomInButton.addActionListener(e -> {
            mazeRenderer.setZoomFactor(mazeRenderer.getZoomFactor() * 1.1);
            updateZoom();
        });

        zoomOutButton.addActionListener(e -> {
            mazeRenderer.setZoomFactor(mazeRenderer.getZoomFactor() / 1.1);
            updateZoom();
        });

        zoomPanel.add(zoomInButton);
        zoomPanel.add(zoomOutButton);
        window.add(zoomPanel, BorderLayout.EAST);
    }






    // Metoda dopasowująca labirynt do okna
    private void fitMazeToWindow() {
        if (mazeRenderer != null && mazeRenderer.getMazeImage() != null) {
            int windowWidth = scrollPane.getViewport().getWidth();
            int windowHeight = scrollPane.getViewport().getHeight();

            double windowRatio = (double) windowWidth / windowHeight;
            double imageRatio = (double) mazeRenderer.getMazeImage().getWidth() / mazeRenderer.getMazeImage().getHeight();

            initialZoomFactor = (windowRatio > imageRatio) ? (double) windowHeight / mazeRenderer.getMazeImage().getHeight()
                    : (double) windowWidth / mazeRenderer.getMazeImage().getWidth();

            mazeRenderer.setInitialZoomFactor(initialZoomFactor);
            mazeRenderer.setZoomFactor(1.0); // Resetowanie zoomu
            updateZoom(); // Aktualizacja zoomu
        }
    }



    // Metoda tworząca pasek opcji
    private void createOptionsBar() {
        JMenu optionsMenu = new JMenu("Opcje");

        JMenuItem setEntranceExitItem = new JMenuItem("Ustaw wejście i wyjście");

        setEntranceExitItem.addActionListener(e -> {
            if (mazeRenderer.getMazeImage() != null) {
                MazeUtilities.setSelectedState(1);
                MazeUtilities.resetEntranceAndExit(mazeRenderer, window);

                JOptionPane.showMessageDialog(window, "Wybierz punkt początkowy na labiryncie.", "Ustaw wejście i wyjście", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(window, "Najpierw załaduj labirynt.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        optionsMenu.add(setEntranceExitItem);
        menuBar.add(optionsMenu);
    }





}
