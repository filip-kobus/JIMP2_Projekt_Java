package GUI;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;



public class MazeRenderer {
    private BufferedImage mazeImage; // Obrazek labiryntu
    private JPanel mazePanel; // Panel, na którym jest rysowany labirynt
    private double initialZoomFactor = 1.0;
    private double zoomFactor = 1.0;
    private File temporaryMazeFile;

    private int selectedState = 0; // 0 - brak wyboru, 1 - wybór wejścia, 2 - wybór wyjścia

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



    public void setSelectedState(int state) {
        this.selectedState = state;
    }

    // Rysowanie komórki labiryntu
    public void paintCell(int x, int y, int state) {
        if (mazeImage == null || x < 0 || y < 0 || x >= mazeImage.getWidth() || y >= mazeImage.getHeight()) return;


        Color color = state == 1 ? Color.GREEN : (state == 2 ? Color.RED : Color.GRAY);

        // Update the image pixel
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
        if (color.equals(Color.GREEN)) {
            return 'P';
        } else if (color.equals(Color.RED)) {
            return 'K';
        } else if (color.equals(Color.GRAY)) {
            return 'X';
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
}
