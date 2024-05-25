package GUI;

import Algorithm.AlgorithmBfs;
import Algorithm.AlgorithmDfs;
import Algorithm.DataArray;
import Algorithm.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.util.List;

public class MazeRenderer {
    private BufferedImage mazeImage; // Obrazek labiryntu
    private JPanel mazePanel; // Panel, na którym jest rysowany labirynt
    private double initialZoomFactor = 1.0;
    private double zoomFactor = 1.0;
    private File temporaryMazeFile;
    private DataArray dataArray;

    public MazeRenderer(BufferedImage mazeImage) {
        this.mazeImage = mazeImage;
        createMazePanel(); // Utworzenie panelu z labiryntem
    }

    // Tworzenie panelu z labiryntem
    public JPanel createMazePanel() {
        mazePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mazeImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int scaledWidth = (int) (mazeImage.getWidth() * initialZoomFactor * zoomFactor);
                    int scaledHeight = (int) (mazeImage.getHeight() * initialZoomFactor * zoomFactor);
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;
                    g2d.drawImage(mazeImage, x, y, scaledWidth, scaledHeight, null);
                    g2d.dispose();
                }
            }
        };
        return mazePanel;
    }

    // Rysowanie komórki labiryntu
    public void paintCell(int x, int y, int state) {
        if (mazeImage == null || x < 0 || y < 0 || x >= mazeImage.getWidth() || y >= mazeImage.getHeight()) return;

        // Maja być pięć stanów: 0 - ściana, 1 - start, 2 - koniec, 3 - ścieżka, 4 - nieużywana ścieżka
        Color color;
        switch (state) {
            case 0:
                color = Color.GRAY;
                break;
            case 1:
                color = Color.GREEN;
                break;
            case 2:
                color = Color.RED;
                break;
            case 3:
                color = Color.BLUE;
                break;
            case 4:
                color = Color.WHITE;
                break;
            case 5:
                color = Color.YELLOW; // Dodatkowy kolor dla nieużywanej ścieżki
                break;
            default:
                color = Color.WHITE;
        }

        // Ustawienie koloru piksela
        mazeImage.setRGB(x, y, color.getRGB());
        mazePanel.repaint(); // Upewnienie się, że panel z labiryntem zostanie odświeżony
    }

    // Aktualizacja danych labiryntu
    public void updateMazeData() {
        if (temporaryMazeFile != null && mazeImage != null) {
            try {
                char[][] mazeChars = buildCharRepresentation();
                saveMazeData(mazeChars, temporaryMazeFile);
                JOptionPane.showMessageDialog(null, "Maze data updated and saved successfully.", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to save maze data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private char[][] buildCharRepresentation() {
        char[][] mazeChars = new char[mazeImage.getHeight()][mazeImage.getWidth()];

        for (int y = 0; y < mazeImage.getHeight(); y++) {
            for (int x = 0; x < mazeImage.getWidth(); x++) {
                mazeChars[y][x] = getCharForColor(new Color(mazeImage.getRGB(x, y)));
            }
        }

        return mazeChars;
    }

    private char getCharForColor(Color color) {
        if (color.equals(Color.BLUE)) {
            return 'P'; // Oznaczamy ścieżkę
        } else if (color.equals(Color.RED)) {
            return 'K'; // Oznaczamy koniec
        } else if (color.equals(Color.GRAY)) {
            return 'X'; // Oznaczamy ścianę
        } else if (color.equals(Color.WHITE)) {
            return ' '; // Oznaczamy nieużywane miejsce
        } else if (color.equals(Color.YELLOW)) {
            return 'U';  // Oznaczamy zużyte miejsce
        } else {
            return ' ';
        }
    }

    private void saveMazeData(char[][] mazeChars, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (char[] row : mazeChars) {
                writer.write(row);
                writer.newLine();
            }
        }
    }

    // Funkcja rozwiązania labiryntu za pomocą BFS w tle
    public void solveMazeWithBfs(DataArray dataArray) {
        resetPaths(dataArray); // Resetowanie ścieżek

        // Utworzenie nowego wątku dla algorytmu BFS
        SwingWorker<Void, Void> worker = new SwingWorker<>() { // SwingWorker pozwala na wykonywanie operacji w tle


            // Funkcja doInBackground() wykonuje algorytm BFS w tle
            @Override
            protected Void doInBackground() throws Exception {
                AlgorithmBfs bfs = new AlgorithmBfs(dataArray);
                bfs.runAlgorithm();
                bfs.printPathToArray();
                return null;
            }


            // Funkcja done() wykonuje się po zakończeniu algorytmu BFS
            @Override
            protected void done() {
                // Kolorowanie ścieżki na podstawie dataArray po zakończeniu algorytmu
                for (int y = 0; y < dataArray.getHeight(); y++) {
                    for (int x = 0; x < dataArray.getWidth(); x++) {
                        Point point = new Point(x, y);
                        if (dataArray.isPath(point)) {
                            paintCell(x, y, 3); // Ścieżka na niebiesko
                        }
                    }
                }

                paintCell(dataArray.getEntry().getX(), dataArray.getEntry().getY(), 1); // Start na zielono
                paintCell(dataArray.getExit().getX(), dataArray.getExit().getY(), 2); // Koniec na czerwono
                JOptionPane.showMessageDialog(null, "Ścieżka została znaleziona!", "BFS Solver", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
    }

    // Wizualizacja algorytmu DFS
    public void visualizeDfs(DataArray dataArray) {
        resetPaths(dataArray); // Resetowanie ścieżek
        SwingWorker<Void, Point> worker = new SwingWorker<>() { // SwingWorker pozwala na wykonywanie operacji w tle


            AlgorithmDfs dfs = new AlgorithmDfs(dataArray); // Utworzenie obiektu algorytmu DFS
            Point entry = dataArray.getEntry(); // Punkt startowy
            Point exit = dataArray.getExit(); // Punkt końcowy
            boolean found = false;

            @Override
            protected Void doInBackground() throws Exception {
                while (!found) {
                    found = dfs.makeMove(); // Wykonanie ruchu
                    Point currMove = dfs.getMove(); // Pobranie aktualnego ruchu
                    publish(currMove); // Publikowanie ruchu
                    TimeUnit.MILLISECONDS.sleep(100); // Dodajemy opóźnienie dla wizualizacji
                }
                return null;
            }


            // Funkcja process() wykonuje się po każdym ruchu
            @Override
            protected void process(List<Point> chunks) {
                for (Point currMove : chunks) { // Dla każdego ruchu
                    if (currMove.equalCoordinates(entry)) { // Jeśli ruch prowadzi do punktu startowego
                        paintCell(currMove.getX(), currMove.getY(), 1);
                    } else if (currMove.equalCoordinates(exit)) { // Jeśli ruch prowadzi do punktu końcowego
                        paintCell(currMove.getX(), currMove.getY(), 2);
                    } else {
                        if (dfs.isMovingBack) { // Jeśli ruch prowadzi do miejsca, z którego wracamy
                            paintCell(currMove.getX(), currMove.getY(), 5); // Odwiedzone miejsce, które nie prowadzi do wyjścia (żółte)
                        } else {
                            paintCell(currMove.getX(), currMove.getY(), 3); // Odwiedzone miejsce, które prowadzi do wyjścia (niebieskie)
                        }
                    }
                    mazePanel.repaint(); // Upewnienie się, że panel z labiryntem zostanie odświeżony
                }
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(null, "Ścieżka została znaleziona!", "DFS Visualization", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
    }


    // Resetowanie ścieżek
    public void resetPaths(DataArray dataArray) {
        if (dataArray != null) {
            dataArray.resetPaths();
            for (int y = 0; y < dataArray.getHeight(); y++) {
                for (int x = 0; x < dataArray.getWidth(); x++) {
                    if (dataArray.getCellValue(x, y) == Point.isSpace || dataArray.getCellValue(x, y) == DataArray.isPath || dataArray.getCellValue(x, y) == DataArray.isUnusedPath) {
                        paintCell(x, y, 4); // Resetowanie ścieżek do białego koloru
                    }
                }
            }
            paintCell(dataArray.getEntry().getX(), dataArray.getEntry().getY(), 1); // Start na zielono
            paintCell(dataArray.getExit().getX(), dataArray.getExit().getY(), 2); // Koniec na czerwono
        }
    }

    // Ustawienie współczynnika powiększenia
    public void setZoomFactor(double factor) {
        this.zoomFactor = factor;
        mazePanel.revalidate();
        mazePanel.repaint();
    }

    // Pobranie współczynnika powiększenia
    public double getZoomFactor() {
        return zoomFactor;
    }

    // Ustawienie początkowego współczynnika powiększenia
    public void setInitialZoomFactor(double factor) {
        this.initialZoomFactor = factor;
    }

    // Pobranie początkowego współczynnika powiększenia
    public double getInitialZoomFactor() {
        return initialZoomFactor;
    }

    // Pobranie obrazu labiryntu
    public BufferedImage getMazeImage() {
        return mazeImage;
    }

    // Ustawienie obrazu labiryntu
    public void setMazeImage(BufferedImage mazeImage) {
        this.mazeImage = mazeImage;
        mazePanel.repaint();
    }

    // Ustawienie pliku tymczasowego labiryntu
    public JPanel getMazePanel() {
        return mazePanel;
    }

    public void setDataArray(DataArray dataArray) {
        this.dataArray = dataArray;
    }
}
