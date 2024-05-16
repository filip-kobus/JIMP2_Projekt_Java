package GUI;

import  Algorithm.Binary;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.nio.file.Files;



public class MainGuiPanel implements GUIInterface {
    private MazeRenderer mazeRenderer; // Labirynt
    private JFrame window;
    private JMenuBar menuBar;
    private JScrollPane scrollPane; // Scroll panel
    private double initialZoomFactor = 1.0; // Początkowy zoom

    private int selectedState = 0; // 0 - nic, 1 - punkt początkowy, 2 - punkt końcowy

    private File currentMazeFile;
    private File temporaryMazeFile; // Tymczasowy plik

    public void run() {
        CreateMainPanel();
        CreateMazePanel();
        CreateFileReaderBar();
        CreateZoomControls();

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
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        mazeRenderer = new MazeRenderer(null); // Utworzenie labiryntu
        JPanel mazePanel = mazeRenderer.createMazePanel(); // Utworzenie panelu z labiryntem


        // Scrollowanie
        mazePanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = mazeRenderer.getZoomFactor();
                zoomFactor *= e.getPreciseWheelRotation() < 0 ? 1.1 : 0.9;
                mazeRenderer.setZoomFactor(zoomFactor);
                updateZoom();
            }
        });


        // Kliknięcie myszy
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


                // Sprawdzenie czy kliknięto w labirynt
                if (x >= offsetX && x < offsetX + scaledWidth && y >= offsetY && y < offsetY + scaledHeight) {
                    int imageX = (int) ((x - offsetX) / (mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor()));
                    int imageY = (int) ((y - offsetY) / (mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor()));


                    // Ustawienie punktu początkowego i końcowego jeśli nie zostały jeszcze ustawione
                    if (selectedState != 0) {
                        mazeRenderer.paintCell(imageX, imageY, selectedState);
                        if (selectedState == 1) {
                            JOptionPane.showMessageDialog(window, "Wybierz punkt końcowy.", "Dalej", JOptionPane.INFORMATION_MESSAGE);
                            selectedState = 2;  // Move to selecting exit
                        } else if (selectedState == 2) {
                            selectedState = 0;  // Reset after setting exit
                            mazeRenderer.updateMazeData();
                            JOptionPane.showMessageDialog(window, "Punkt początkowy i końcowy zostały wybrane", "Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(window, "Kliknięto komórkę: (" + imageY + ", " + imageX + ")", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });






        // Scrollowanie
        scrollPane = new JScrollPane(mazePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);


    }


    // Metoda aktualizująca zoom
    private void updateZoom() {
        if (mazeRenderer != null && mazeRenderer.getMazeImage() != null) {
            int newWidth = (int) (mazeRenderer.getMazeImage().getWidth() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());
            int newHeight = (int) (mazeRenderer.getMazeImage().getHeight() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());

            // Set the preferred size based on the zoomed dimensions
            mazeRenderer.getMazePanel().setPreferredSize(new Dimension(newWidth, newHeight));
            mazeRenderer.getMazePanel().revalidate();

            // Update the scroll pane to adjust to the new size of the panel
            scrollPane.revalidate();
            scrollPane.repaint();
        }
    }






    // Metoda tworząca pasek z plikami
    @Override
    public void CreateFileReaderBar() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        JMenuItem openItem = new JMenuItem("Otwórz labirynt");
        JMenuItem saveItem = new JMenuItem("Zapisz labirynt");


        // Otwieranie pliku
        openItem.addActionListener(e -> {
            try {
                File mazeFile = openMazeFile();
                if (mazeFile != null) {
                    currentMazeFile = mazeFile;
                    temporaryMazeFile = FileIO.createTemporaryFileCopy(mazeFile);

                    // Check if the file is a binary file
                    if (mazeFile.getName().endsWith(".bin")) {
                        // printuj
                        System.out.println("Odczytano plik binarny");
                        File txtFile = new File(temporaryMazeFile.getParent(), temporaryMazeFile.getName().replace(".bin", ".txt"));

                        Binary.convertBinaryToText(currentMazeFile.getAbsolutePath(), txtFile.getAbsolutePath());
                        temporaryMazeFile = txtFile;
                        BufferedImage image = FileIO.readMazeFromFile(temporaryMazeFile);
                        mazeRenderer.setMazeImage(image);
                    } else {
                        BufferedImage image = FileIO.readMazeFromFile(temporaryMazeFile);
                        mazeRenderer.setMazeImage(image);
                    }
                    fitMazeToWindow();
                    checkForEntranceAndExit();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Nie udało się odczytać pliku: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });


        // Zapisywanie pliku
        saveItem.addActionListener(e -> {
            try {
                if (temporaryMazeFile != null) {
                    FileIO.writeMazeToFile(mazeRenderer.getMazeImage(), temporaryMazeFile);
                    JOptionPane.showMessageDialog(window, "Labirynt został zapisany.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(window, "Nie ma otwartego labiryntu do zapisania!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Nie udało się zapisać labiryntu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });


        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);
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
            mazeRenderer.setZoomFactor(1.0); // Reset zoom
            updateZoom(); // Update zoom based on the new initial factor
        }
    }




    // Metoda sprawdzająca czy są punkty początkowy i końcowy
    private void checkForEntranceAndExit() {
        boolean foundP = false, foundK = false;
        BufferedImage image = mazeRenderer.getMazeImage();
        if (image != null) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int color = image.getRGB(x, y);
                    if (color == Color.GREEN.getRGB()) foundP = true;
                    if (color == Color.RED.getRGB()) foundK = true;
                    if (foundP && foundK) break;
                }
            }
        }

        if (!foundP || !foundK) {
            JOptionPane.showMessageDialog(window, "Brak punktu wejścia lub wyjścia. Wybierz je klikając na ścianę.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            selectedState = !foundP ? 1 : 2; // Start with selecting entry if 'P' is missing
        } else {
            selectedState = 0;
        }
    }

}
