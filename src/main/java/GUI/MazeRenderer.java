package GUI;

import Algorithm.AlgorithmBfs;
import Algorithm.AlgorithmDfs;
import Algorithm.DataArray;
import Algorithm.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.*;

public class MazeRenderer {
    private BufferedImage mazeImage; // Obrazek labiryntu
    private JPanel mazePanel; // Panel, na którym jest rysowany labirynt
    private double initialZoomFactor = 1.0;
    private double zoomFactor = 1.0;
    private File temporaryMazeFile;
    private DataArray dataArray;
    private Point lastHeadPosition = null; // Przechowujemy ostatnią pozycję główki

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
    public synchronized void paintCell(int x, int y, int state) {
        if (mazeImage == null || x < 0 || y < 0 || x >= mazeImage.getWidth() || y >= mazeImage.getHeight()) return;

        // Maja być pięć stanów: 0 - ściana, 1 - start, 2 - koniec, 3 - ścieżka, 4 - nieużywana ścieżka, 5 - obecna pozycja
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
            case 6:
                color = new Color(128, 0, 128); // Fioletowy dla obecnej pozycji
                break;
            default:
                color = Color.WHITE;
        }

        // Ustawienie koloru piksela
        mazeImage.setRGB(x, y, color.getRGB());
        SwingUtilities.invokeLater(() -> mazePanel.repaint()); // Upewnienie się, że panel z labiryntem zostanie odświeżony
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
            return 'P';
        } else if (color.equals(Color.RED)) {
            return 'K';
        } else if (color.equals(Color.GRAY)) {
            return 'X';
        } else if (color.equals(Color.WHITE)) {
            return ' ';
        } else if (color.equals(Color.YELLOW)) {
            return 'U'; // Oznaczamy nieużywaną ścieżkę
        } else if (color.equals(new Color(128, 0, 128))) {
            return 'C'; // Oznaczamy obecną pozycję
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
//        resetPaths(dataArray);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                AlgorithmBfs bfs = new AlgorithmBfs(dataArray);
                bfs.runAlgorithm();
                bfs.printPathToArray();
                return null;
            }

            @Override
            protected void done() {
                // Kolorowanie ścieżki na podstawie dataArray po zakończeniu algorytmu
                SwingUtilities.invokeLater(() -> {
                    for (int y = 0; y < dataArray.getHeight(); y++) {
                        for (int x = 0; x < dataArray.getWidth(); x++) {
                            Point point = new Point(x, y);
                            if (dataArray.isPath(point)) {
                                paintCell(x, y, 3); // Ścieżka na niebiesko
                            }
                        }
                    }
                    // pokoluj start na zielono
                    paintCell(dataArray.getEntry().getX(), dataArray.getEntry().getY(), 1); // Start na zielono
                    paintCell(dataArray.getExit().getX(), dataArray.getExit().getY(), 2); // Koniec na czerwono
                    JOptionPane.showMessageDialog(null, "Ścieżka została znaleziona!", "BFS Solver", JOptionPane.INFORMATION_MESSAGE);
                });
            }
        };

        worker.execute();
    }

    // Wizualizacja algorytmu DFS
    public void visualizeDfs(DataArray dataArray) {
//        resetPaths(dataArray);
        SwingWorker<Void, Point> worker = new SwingWorker<>() {
            AlgorithmDfs dfs = new AlgorithmDfs(dataArray);
            Point entry = dataArray.getEntry();
            Point exit = dataArray.getExit();
            boolean found = false;

            @Override
            protected Void doInBackground() throws Exception {
                while (!found) {
                    found = dfs.makeMove();
                    Point currMove = dfs.getMove();
                    publish(currMove);
                    TimeUnit.MILLISECONDS.sleep(20); // Dodajemy opóźnienie dla wizualizacji
                }
                return null;
            }

            @Override
            protected void process(List<Point> chunks) {
                for (Point currMove : chunks) {
                    if (lastHeadPosition != null) {
                        // Przywracanie koloru dla ostatniej pozycji główki
                        if (dfs.isMovingBack) {
                            paintCell(lastHeadPosition.getX(), lastHeadPosition.getY(), 5); // Odwiedzone miejsce, które nie prowadzi do wyjścia (żółte)
                        } else {
                            paintCell(lastHeadPosition.getX(), lastHeadPosition.getY(), 3); // Odwiedzone miejsce, które prowadzi do wyjścia (niebieskie)
                        }
                    }

                    // Aktualizacja koloru obecnej pozycji na fioletowy
                    if (currMove.equalCoordinates(entry)) {
                        paintCell(currMove.getX(), currMove.getY(), 1);
                    } else if (currMove.equalCoordinates(exit)) {
                        paintCell(currMove.getX(), currMove.getY(), 2);
                    } else {
                        paintCell(currMove.getX(), currMove.getY(), 6); // Obecna pozycja na fioletowo
                    }

                    lastHeadPosition = currMove; // Zapisujemy aktualną pozycję główki
                }
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(null, "Ścieżka została znaleziona!", "DFS Visualization", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
    }

    public void resetPaths(DataArray dataArray) {
        if (dataArray != null) {
            dataArray.resetPaths();
            for (int y = 0; y < dataArray.getHeight(); y++) {
                for (int x = 0; x < dataArray.getWidth(); x++) {
                    if (dataArray.getCellValue(x, y) == Point.isSpace || dataArray.getCellValue(x, y) == DataArray.isPath || dataArray.getCellValue(x, y) == DataArray.isUnusedPath || dataArray.getCellValue(x, y) == Point.isVisited) {
                        paintCell(x, y, 4); // Resetowanie ścieżek do białego koloru
                    }
                }
            }
            paintCell(dataArray.getEntry().getX(), dataArray.getEntry().getY(), 1); // Start na zielono
            paintCell(dataArray.getExit().getX(), dataArray.getExit().getY(), 2); // Koniec na czerwono
            lastHeadPosition = null; // Resetujemy pozycję główki
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

    public DataArray getDataArray() {
        return dataArray;
    }
}
