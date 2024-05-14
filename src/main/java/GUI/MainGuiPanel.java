package GUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;


public class MainGuiPanel implements GUIInterface {
    private JPanel mazePanel; // Panel do wyświetlania labiryntu
    private BufferedImage mazeImage; // Obrazek do rysowania labiryntu
    private JFrame window;
    private JMenuBar menuBar;
    private JScrollPane scrollPane; // Suwak do przewijania labiryntu
    private double zoomFactor = 1.0; // Początkowy zoom
    private double initialZoomFactor = 1.0; // Zoom używany do inicjalnego dopasowania

    public void run() {

        // Utworzenie głównego okna
        CreateMainPanel();
        CreateMazePanel();
        CreateFileReaderBar();
        CreateZoomControls();

        // Utworzenie JTabbedPane do przechowywania zakładek
        JTabbedPane tabPanel = new JTabbedPane();
        tabPanel.addTab("Labirynt", scrollPane);
        window.add(tabPanel, BorderLayout.CENTER);

        window.setJMenuBar(menuBar);

        // Wyświetlenie głównego okna
        window.setVisible(true);
    }

    @Override
    public void CreateMainPanel() {
        window = new JFrame("Maze Solver - Kobus&Dutkiewicz");
        ImageIcon img = new ImageIcon("src/gallery/logo.png");
        window.setIconImage(img.getImage());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 600);
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (mazeImage != null) {
                    fitMazeToWindow();
                }
            }
        });
    }

    // Metoda do tworzenia panelu z labiryntem
    @Override
    public void CreateMazePanel() {
        mazePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) { // Metoda rysująca labirynt
                super.paintComponent(g);
                if (mazeImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();

                    // Obliczenie wymiarów obrazka z uwzględnieniem zooma
                    int scaledWidth = (int) (mazeImage.getWidth() * initialZoomFactor * zoomFactor);
                    int scaledHeight = (int) (mazeImage.getHeight() * initialZoomFactor * zoomFactor);

                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;

                    // Rysowanie obrazka
                    g2d.drawImage(mazeImage, x, y, scaledWidth, scaledHeight, this);
                    g2d.dispose();
                }
            }
        };


        // Obsługa zdarzenia przewijania scrollem w myszce
        // Zoomowanie obrazka
        mazePanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getPreciseWheelRotation() < 0) {
                    zoomFactor *= 1.1;
                } else {
                    zoomFactor /= 1.1;
                }
                updateZoom();
            }
        });

        // Obsługa zdarzenia kliknięcia w labirynt
        mazePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mazeImage == null) return;

                // Obliczenie współrzędnych kliknięcia w obrazek
                int x = e.getX();
                int y = e.getY();
                int panelWidth = mazePanel.getWidth();
                int panelHeight = mazePanel.getHeight();

                int scaledWidth = (int) (mazeImage.getWidth() * initialZoomFactor * zoomFactor);
                int scaledHeight = (int) (mazeImage.getHeight() * initialZoomFactor * zoomFactor);

                int offsetX = (panelWidth - scaledWidth) / 2;
                int offsetY = (panelHeight - scaledHeight) / 2;

                // Sprawdzenie czy kliknięto w obszar obrazka
                if (x >= offsetX && x < offsetX + scaledWidth && y >= offsetY && y < offsetY + scaledHeight) {
                    int imageX = (int) ((x - offsetX) / (initialZoomFactor * zoomFactor));
                    int imageY = (int) ((y - offsetY) / (initialZoomFactor * zoomFactor));
                    // Wyświetlenie informacji o klikniętej komórce
                    JOptionPane.showMessageDialog(window, "Kliknięto komórkę: (" + imageY + ", " + imageX + ")", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Utworzenie suwaka do przewijania labiryntu
        scrollPane = new JScrollPane(mazePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    }


    // Metoda do aktualizacji zooma
    private void updateZoom() {
        if (mazeImage != null) {
            int newWidth = (int) (mazeImage.getWidth() * initialZoomFactor * zoomFactor);
            int newHeight = (int) (mazeImage.getHeight() * initialZoomFactor * zoomFactor);
            mazePanel.setPreferredSize(new Dimension(newWidth, newHeight));
            mazePanel.revalidate();
            mazePanel.repaint();
            scrollPane.revalidate();
        }
    }


    // Metoda do tworzenia paska menu
    @Override
    public void CreateFileReaderBar() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        JMenuItem openItem = new JMenuItem("Otwórz labirynt");

        openItem.addActionListener(e -> {
            try {
                File mazeFile = openMazeFile();
                if (mazeFile != null) {
                    displayMaze(mazeFile);
                    fitMazeToWindow();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Nie udało się odczytać pliku: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        fileMenu.add(openItem);
        menuBar.add(fileMenu);
    }


    // Metoda do tworzenia przycisków do zoomowania
    private void CreateZoomControls() {
        JPanel zoomPanel = new JPanel(new GridLayout(2, 1));
        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");


        // Obsługa zdarzenia kliknięcia przycisków zoomowania
        zoomInButton.addActionListener(e -> {
            zoomFactor *= 1.1;
            updateZoom();
        });

        zoomOutButton.addActionListener(e -> {
            zoomFactor /= 1.1;
            updateZoom();
        });

        // Dodanie przycisków do panelu
        zoomPanel.add(zoomInButton);
        zoomPanel.add(zoomOutButton);

        window.add(zoomPanel, BorderLayout.EAST);
    }

    public File openMazeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz plik labiryntu");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki tekstowe", "txt"));

        int result = fileChooser.showOpenDialog(window);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private void displayMaze(File mazeFile) throws IOException {
        List<String> lines = Files.readAllLines(mazeFile.toPath()); // Odczytanie linii z pliku
        int rows = lines.size();
        int cols = lines.get(0).length();

        mazeImage = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB); // Utworzenie obrazka
        Graphics2D g2d = mazeImage.createGraphics();

        for (int i = 0; i < rows; i++) { // Rysowanie labiryntu
            for (int j = 0; j < cols; j++) {
                char ch = lines.get(i).charAt(j);
                switch (ch) {
                    case 'X':
                        g2d.setColor(Color.GRAY);
                        break;
                    case ' ':
                        g2d.setColor(Color.WHITE);
                        break;
                    case 'P':
                        g2d.setColor(Color.GREEN);
                        break;
                    case 'K':
                        g2d.setColor(Color.RED);
                        break;
                    default:
                        g2d.setColor(Color.WHITE);
                        break;
                }
                g2d.fillRect(j, i, 1, 1); // Rysowanie piksela
            }
        }
        g2d.dispose();

        // Reset zoom and fit maze to window
        zoomFactor = 1.0;
        fitMazeToWindow(); // Dopasowanie labiryntu do okna
    }


    // Dopasowanie labiryntu do okna
    private void fitMazeToWindow() {
        if (mazeImage != null) {

            // Obliczenie wymiarów okna i obrazka
            int windowWidth = scrollPane.getViewport().getWidth();
            int windowHeight = scrollPane.getViewport().getHeight();

            // Obliczenie proporcji okna i obrazka
            double windowRatio = (double) windowWidth / windowHeight;
            double imageRatio = (double) mazeImage.getWidth() / mazeImage.getHeight();



            if (windowRatio > imageRatio) {
                initialZoomFactor = (double) windowHeight / mazeImage.getHeight();
            } else {
                initialZoomFactor = (double) windowWidth / mazeImage.getWidth();
            }
            zoomFactor = 1.0;  // Reset the zoom factor to 1.0 for new image
            updateZoom();
        }
    }

}
