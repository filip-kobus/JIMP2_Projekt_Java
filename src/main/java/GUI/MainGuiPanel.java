package GUI;

import  Algorithm.Binary;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;





public class MainGuiPanel implements GUIInterface {
    private MazeRenderer mazeRenderer; // Labirynt
    private JFrame window;
    private JMenuBar menuBar;
    private JScrollPane scrollPane; // Scroll panel
    private double initialZoomFactor = 1.0; // Początkowy zoom

    private int selectedState = 0; // 0 - nic, 1 - punkt początkowy, 2 - punkt końcowy

    private File currentMazeFile;
    private File temporaryMazeFile; // Tymczasowy plik

    private static final double ZOOM_IN_FACTOR = 1.1;
    private static final double ZOOM_OUT_FACTOR = 0.9;

    private String info = "Informacja";

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
                    handleMazeCellSelection(imageX, imageY);
                }
            }
        });
    }

    // Metoda obsługująca wybór komórki labiryntu
    private void handleMazeCellSelection(int imageX, int imageY) {
        if (selectedState != 0) { // Jeśli jesteśmy w trakcie wybierania wejścia lub wyjścia
            if (isEdge(imageX, imageY)) { // Sprawdza, czy punkt znajduje się na brzegu labiryntu
                mazeRenderer.paintCell(imageX, imageY, selectedState); // Ustawia komórkę labiryntu

                if (selectedState == 1) { // Jeśli wybrano punkt początkowy
                    JOptionPane.showMessageDialog(window, "Wybierz punkt końcowy na krawędzi labiryntu.", "Dalej", JOptionPane.INFORMATION_MESSAGE);
                    selectedState = 2; // Ustawienie stanu na wybór punktu końcowego
                } else if (selectedState == 2) { // Jeśli wybrano punkt końcowy
                    selectedState = 0; // Resetowanie stanu wyboru
                    mazeRenderer.updateMazeData(); // Aktualizacja danych labiryntu
                    JOptionPane.showMessageDialog(window, "Punkt początkowy i końcowy zostały wybrane", info, JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(window, "Wejście i wyjście można ustawiać tylko na krawędziach labiryntu.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(window, "Kliknięto komórkę: (" + imageY + ", " + imageX + ")", info, JOptionPane.INFORMATION_MESSAGE);
        }
    }


    // Metoda sprawdzająca czy punkt znajduje się na brzegu labiryntu
    private boolean isEdge(int x, int y) {
        int width = mazeRenderer.getMazeImage().getWidth();
        int height = mazeRenderer.getMazeImage().getHeight();
        return x == 0 || x == width - 1 || y == 0 || y == height - 1;
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
        saveItem.addActionListener(e -> saveMazeAsText());

        // Dołączanie akcji do zapisywania labiryntu jako obrazu
        saveImageItem.addActionListener(e -> saveMazeAsImage());

        // Dodawanie pozycji do menu pliku
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveImageItem);

        // Dodawanie menu pliku do paska menu
        menuBar.add(fileMenu);
    }

    private void openMaze() {
        try {
            File mazeFile = openMazeFile();
            if (mazeFile != null) {
                currentMazeFile = mazeFile;
                temporaryMazeFile = FileIO.createTemporaryFileCopy(mazeFile);

                // Sprawdzenie, czy plik jest plikiem binarnym
                if (mazeFile.getName().endsWith(".bin")) {
                    System.out.println("Odczytano plik binarny");
                    File txtFile = new File(temporaryMazeFile.getParent(), temporaryMazeFile.getName().replace(".bin", ".txt"));

                    Binary.convertBinaryToText(currentMazeFile.getAbsolutePath(), txtFile.getAbsolutePath());
                    temporaryMazeFile = txtFile;
                }

                BufferedImage image = FileIO.readMazeFromFile(temporaryMazeFile);
                mazeRenderer.setMazeImage(image);
                fitMazeToWindow();
                checkForEntranceAndExit();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(window, "Nie udało się odczytać pliku: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveMazeAsText() {
        if (mazeRenderer.getMazeImage() == null) {
            JOptionPane.showMessageDialog(window, "Nie ma otwartego labiryntu do zapisania!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Zapisz labirynt jako tekst");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki tekstowe", "txt"));

        int userSelection = fileChooser.showSaveDialog(window);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName().endsWith(".txt") ? fileToSave.getName() : fileToSave.getName() + ".txt");

            try {
                FileIO.writeMazeToFile(mazeRenderer.getMazeImage(), fileToSave);
                JOptionPane.showMessageDialog(window, "Labirynt został zapisany w " + fileToSave.getPath(), info, JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Nie udało się zapisać labiryntu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveMazeAsImage() {
        if (mazeRenderer.getMazeImage() == null) {
            JOptionPane.showMessageDialog(window, "Nie ma otwartego labiryntu do zapisania jako obraz!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Zapisz obraz labiryntu");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Ustawienie filtrów dla popularnych typów obrazów
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Obrazy PNG", "png"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Obrazy JPEG", "jpg", "jpeg"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Obrazy Bitmap", "bmp"));


        int userSelection = fileChooser.showSaveDialog(window);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String ext = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName().endsWith("." + ext) ? fileToSave.getName() : fileToSave.getName() + "." + ext);

            try {
                // Zapisywanie pliku obrazu
                ImageIO.write(mazeRenderer.getMazeImage(), ext, fileToSave);
                JOptionPane.showMessageDialog(window, "Obraz labiryntu został zapisany w " + fileToSave.getPath(), info, JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Nie udało się zapisać obrazu labiryntu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
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


    // Metoda otwierająca plik z labiryntem
    public File openMazeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz plik labiryntu");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki tekstowe lub binarne", "txt", "bin"));

        int result = fileChooser.showOpenDialog(window);
        return (result == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
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




    // Metoda sprawdzająca czy są punkty początkowy i końcowy
    private void checkForEntranceAndExit() {
        BufferedImage image = mazeRenderer.getMazeImage();
        if (image == null) {
            handleNoImage();
            return;
        }

        boolean[] found = findEntranceAndExit(image);
        boolean foundP = found[0];
        boolean foundK = found[1];

        if (!foundP || !foundK) {
            showWarning(foundP, foundK);
        } else {
            selectedState = 0;
        }
    }

    private boolean[] findEntranceAndExit(BufferedImage image) {
        boolean foundP = false, foundK = false;
        for (int y = 0; y < image.getHeight() && !(foundP && foundK); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getRGB(x, y);
                if (color == Color.GREEN.getRGB()) foundP = true;
                if (color == Color.RED.getRGB()) foundK = true;
                if (foundP && foundK) break;
            }
        }
        return new boolean[]{foundP, foundK};
    }

    private void showWarning(boolean foundP, boolean foundK) {
        JOptionPane.showMessageDialog(window, "Brak punktu wejścia lub wyjścia. Wybierz je klikając na ścianę.", "Uwaga", JOptionPane.WARNING_MESSAGE);
        selectedState = !foundP ? 1 : 2;
    }

    private void handleNoImage() {
        JOptionPane.showMessageDialog(window, "No maze image found.", "Error", JOptionPane.ERROR_MESSAGE);
    }


    private void createOptionsBar() {
        JMenu optionsMenu = new JMenu("Opcje");

        JMenuItem setEntranceExitItem = new JMenuItem("Ustaw wejście i wyjście");

        setEntranceExitItem.addActionListener(e -> {
            if (mazeRenderer.getMazeImage() != null) {
                resetEntranceAndExit();
                selectedState = 1; // Set state to choose entrance first
                JOptionPane.showMessageDialog(window, "Wybierz punkt początkowy na labiryncie.", "Ustaw wejście i wyjście", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(window, "Najpierw załaduj labirynt.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        optionsMenu.add(setEntranceExitItem);
        menuBar.add(optionsMenu);
    }

    private void resetEntranceAndExit() {
        // Sprawdza, czy obraz labiryntu jest dostępny
        if (mazeRenderer.getMazeImage() != null) {
            BufferedImage image = mazeRenderer.getMazeImage();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    // Sprawdza, czy obecny piksel to wejście lub wyjście
                    if (new Color(image.getRGB(x, y)).equals(Color.GREEN) || new Color(image.getRGB(x, y)).equals(Color.RED)) {
                        image.setRGB(x, y, Color.GRAY.getRGB());  // Zmienia kolor na szary (ściana)
                    }
                }
            }
            mazeRenderer.setMazeImage(image); // Aktualizuje obraz labiryntu
            mazeRenderer.getMazePanel().repaint(); // Odświeża panel z labiryntem
            selectedState = 0; // Resetuje stan wyboru
            JOptionPane.showMessageDialog(window, "Wejście i wyjście zostały zresetowane jako ściany.", "Reset", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(window, "Najpierw załaduj labirynt.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }



}
