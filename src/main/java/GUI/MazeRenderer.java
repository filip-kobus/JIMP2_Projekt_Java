package GUI;

import Algorithm.AlgorithmBfs;
import Algorithm.AlgorithmDfs;
import Algorithm.DataArray;
import Algorithm.Point;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class MazeRenderer {
    private BufferedImage mazeImage; // Obrazek labiryntu

    private DataArray dataArray;

    private JPanel mazePanel; // Panel, na którym jest rysowany labirynt
    private double initialZoomFactor = 1.0;
    private double zoomFactor = 1.0;
    private Point lastHeadPosition = null; // Pozycja główki

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

        // Maja być pięć stanów: 0 - ściana, 1 - start, 2 - koniec, 3 - ścieżka, 4 - nieużywana ścieżka, 5 - główka
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
                color = Color.MAGENTA; // Główka
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
        // implementacja jeśli konieczna
    }

    // Funkcja rozwiązania labiryntu za pomocą BFS
    public void solveMazeWithBfs(DataArray dataArray) {
        AlgorithmBfs bfs = new AlgorithmBfs(dataArray);
        bfs.runAlgorithm();

        // Kolorowanie ścieżki na podstawie dataArray
        for (int y = 0; y < dataArray.getHeight(); y++) {
            for (int x = 0; x < dataArray.getWidth(); x++) {
                Point point = new Point(x, y);
                if (dataArray.isPath(point)) {
                    paintCell(x, y, 3); // Ścieżka na niebiesko
                }
            }
        }
        // pokoluj start na zielono
        paintCell(dataArray.getEntry().getX(), dataArray.getEntry().getY(), 1);
        paintCell(dataArray.getExit().getX(), dataArray.getExit().getY(), 2); // Koniec na czerwono
    }

    // Wizualizacja algorytmu DFS
    public void visualizeDfs(DataArray dataArray) {
        resetPaths(dataArray);
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
                    TimeUnit.MILLISECONDS.sleep(60); // Zmniejszamy opóźnienie dla bardziej płynnej wizualizacji
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Point> chunks) {
                for (Point currMove : chunks) {
                    if (currMove.equalCoordinates(entry)) { // Jeśli punkt jest punktem startowym
                        paintCell(currMove.getX(), currMove.getY(), 1);
                    } else if (currMove.equalCoordinates(exit)) { // Jeśli punkt jest punktem końcowym
                        paintCell(currMove.getX(), currMove.getY(), 2);
                    } else {
                        if (dfs.isMovingBack) { // Jeśli algorytm wraca do poprzedniego punktu

                            paintCell(currMove.getX(), currMove.getY(), 4); // Nieużywana ścieżka
                            dataArray.setAsUnusedPath(currMove); // Ustawienie punktu jako nieużywana ścieżka
                        } else {
                            paintCell(currMove.getX(), currMove.getY(), 3); // Niebieska ścieżka
                            dataArray.setAsPath(currMove); // Ustawienie punktu jako część ścieżki
                        }
                    }


                }
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(null, "Ścieżka została znaleziona!", "DFS Visualization", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
    }

    private boolean isIntersection(Point point) {
        int x = point.getX();
        int y = point.getY();
        int count = 0;

        int[][] directions = {
                {0, -1}, {0, 1}, {-1, 0}, {1, 0}
        };

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && ny >= 0 && nx < dataArray.getWidth() && ny < dataArray.getHeight()) {
                if (dataArray.getCellValue(nx, ny) == Point.isSpace || dataArray.getCellValue(nx, ny) == Point.isVisited) {
                    count++;
                }
            }
        }

        return count >= 3;
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
