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
import Algorithm.Binary;
import java.nio.file.StandardCopyOption;

import java.io.*;



public class MainGuiPanel implements GUIInterface {
    private JPanel mazePanel; // Panel do wyświetlania labiryntu
    private BufferedImage mazeImage; // Obrazek do rysowania labiryntu
    private JFrame window;
    private JMenuBar menuBar;
    private JScrollPane scrollPane; // Suwak do przewijania labiryntu
    private double zoomFactor = 1.0; // Początkowy zoom
    private double initialZoomFactor = 1.0; // Zoom używany do inicjalnego dopasowania

    private int selectedState = 0; // 0 - Normal, 1 - Selecting Entry, 2 - Selecting Exit

    private File currentMazeFile;
    private File temporaryMazeFile; // Plik tymczasowy do zapisywania labiryntu


    private List<String> mazeData;

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
                    if (selectedState != 0){
                        handleClickOnMaze(imageX, imageY);
                    }
                    else{
                    JOptionPane.showMessageDialog(window, "Kliknięto komórkę: (" + imageY + ", " + imageX + ")", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
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
        JMenuItem saveItem = new JMenuItem("Zapisz labirynt");

        openItem.addActionListener(e -> {
            try {
                File mazeFile = openMazeFile();
                if (mazeFile != null) {
                    currentMazeFile = mazeFile; // Store the current file
                    temporaryMazeFile = createTemporaryFileCopy(mazeFile);
                    displayMaze(temporaryMazeFile);
                    if (mazeFile.getName().endsWith(".bin")) {
                        File txtFile = new File(mazeFile.getParent(), mazeFile.getName().replace(".bin", ".txt"));
                        Binary.convertBinaryToText(mazeFile.getAbsolutePath(), txtFile.getAbsolutePath());
                        displayMaze(txtFile);
                    } else {
                        displayMaze(mazeFile);
                    }
                    fitMazeToWindow();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Nie udało się odczytać pliku: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        saveItem.addActionListener(e -> {
            try {
                if (temporaryMazeFile != null) {
                    saveMazeToFile(temporaryMazeFile);
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
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki tekstowe lub binarne", "txt","bin"));

        int result = fileChooser.showOpenDialog(window);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private void displayMaze(File mazeFile) throws IOException {
        mazeData = Files.readAllLines(mazeFile.toPath());
        int rows = mazeData.size();
        int cols = mazeData.get(0).length();

        mazeImage = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = mazeImage.createGraphics();

        boolean foundP = false, foundK = false;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char ch = mazeData.get(i).charAt(j);
                if (ch == 'P') foundP = true;
                if (ch == 'K') foundK = true;
                paintCell(g2d, ch, j, i);
            }
        }

        g2d.dispose();

        // Sprawdzenie czy labirynt zawiera punkt wejścia i wyjścia
        if (!foundP || !foundK) {
            JOptionPane.showMessageDialog(window, "Brak punktu wejścia lub wyjścia. Wybierz je klikając na ścianę.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            selectedState = !foundP ? 1 : 2; // Wybierz punkt wejścia jeśli go nie ma, wyjścia jeśli go nie ma
        } else {
            selectedState = 0;
        }

        zoomFactor = 1.0;
        fitMazeToWindow();
    }

    private void handleClickOnMaze(int imageX, int imageY) {
        if (mazeImage == null) return;

        Color currentColor = new Color(mazeImage.getRGB(imageX, imageY));
        if (currentColor.equals(Color.WHITE) || currentColor.equals(Color.GRAY)) {
            Graphics2D g2d = mazeImage.createGraphics();

            if (selectedState == 1) {
                paintCell(g2d, 'P', imageX, imageY);
                selectedState = 2; // Next, select the exit
            } else if (selectedState == 2) {
                paintCell(g2d, 'K', imageX, imageY);
                selectedState = 0; // Selection is complete

                try {
                    saveMazeToFile(temporaryMazeFile); // Zapisywanie zmian do pliku
                    JOptionPane.showMessageDialog(window, "Labirynt zaktualizowany i zapisany.", "Zapisano", JOptionPane.INFORMATION_MESSAGE);
                    updateInMemoryMazeData();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(window, "Nie udało się zapisać labiryntu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }

            g2d.dispose();
            mazePanel.repaint();
        }
    }

    // Metoda do rysowania komórki labiryntu
    private void paintCell(Graphics2D g2d, char ch, int x, int y) {
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
        g2d.fillRect(x, y, 1, 1);
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
            zoomFactor = 1.0;  // Reset zooma
            updateZoom();
        }
    }

    private void saveMazeToFile(File mazeFile) throws IOException {
        if (mazeImage == null) {
            JOptionPane.showMessageDialog(window, "Nie ma labiryntu do zapisania!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(mazeFile))) {
            for (int y = 0; y < mazeImage.getHeight(); y++) {
                for (int x = 0; x < mazeImage.getWidth(); x++) {
                    Color color = new Color(mazeImage.getRGB(x, y));
                    if (color.equals(Color.GRAY)) {
                        bw.write('X');
                    } else if (color.equals(Color.WHITE)) {
                        bw.write(' ');
                    } else if (color.equals(Color.GREEN)) {
                        bw.write('P');
                    } else if (color.equals(Color.RED)) {
                        bw.write('K');
                    }
                }
                bw.newLine();
            }
        }

    }

    // Tworzenie kopii pliku do zapisu zmian
    private File createTemporaryFileCopy(File originalFile) throws IOException {
        File tempFile = File.createTempFile("maze_", ".txt");
        tempFile.deleteOnExit(); // Usunięcie pliku tymczasowego po zamknięciu programu
        Files.copy(originalFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }


    // Aktualizacja danych labiryntu w pamięci
    private void updateInMemoryMazeData() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < mazeImage.getHeight(); y++) {
            for (int x = 0; x < mazeImage.getWidth(); x++) {
                Color color = new Color(mazeImage.getRGB(x, y));
                if (color.equals(Color.GRAY)) {
                    sb.append('X');
                } else if (color.equals(Color.WHITE)) {
                    sb.append(' ');
                } else if (color.equals(Color.GREEN)) {
                    sb.append('P');
                } else if (color.equals(Color.RED)) {
                    sb.append('K');
                }
            }
            mazeData.set(y, sb.toString());
            sb.setLength(0); // Usunięcie zawartości StringBuilder
        }
    }

}
