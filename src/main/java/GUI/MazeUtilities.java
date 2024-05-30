package GUI;

import Algorithm.DataArray;
import Algorithm.Point;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class MazeUtilities {

    private MazeUtilities(){
    }

    private static int selectedState = 0; // 0 - brak wyboru, 1 - wejście, 2 - wyjście

    // Ustawia stan wyboru
    public static void setSelectedState(int state) {
        selectedState = state;
    }

    // Sprawdza, czy punkt (x, y) jest na krawędzi labiryntu
    public static boolean isEdge(int x, int y, int width, int height) {
        return x == 0 || x == width - 1 || y == 0 || y == height - 1;
    }

    // Obsługuje wybór komórki labiryntu
    public static void handleMazeCellSelection(MazeRenderer mazeRenderer, int imageX, int imageY, JFrame window) {
        DataArray dataArray = mazeRenderer.getDataArray();
        if (selectedState != 0) { // Jeśli wybieramy wejście lub wyjście
            if (isEdge(imageX, imageY, mazeRenderer.getMazeImage().getWidth(), mazeRenderer.getMazeImage().getHeight())) {

                mazeRenderer.paintCell(imageX, imageY, selectedState); // Ustawia komórkę labiryntu

                Point point = new Point(imageX, imageY);
                if (selectedState == 1) { // Jeśli wybrano punkt wejściowy
                    point.setTypeByInt(Point.IS_ENTRY);
                    dataArray.setNewEntry(point);
                    JOptionPane.showMessageDialog(window, "Wybierz punkt końcowy na krawędzi labiryntu.", "Dalej", JOptionPane.INFORMATION_MESSAGE);
                    setSelectedState(2);
                } else if (selectedState == 2) { // Jeśli wybrano punkt wyjściowy
                    point.setTypeByInt(Point.IS_EXIT);
                    dataArray.setNewExit(point);
                    JOptionPane.showMessageDialog(window, "Punkt początkowy i końcowy zostały wybrane", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                    setSelectedState(0);
                }
            } else {
                JOptionPane.showMessageDialog(window, "Wejście i wyjście można ustawiać tylko na krawędziach labiryntu.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(window, "Kliknięto komórkę: (" + imageY + ", " + imageX + ")", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Sprawdza obecność wejścia i wyjścia w labiryncie
    public static void checkForEntranceAndExit(MazeRenderer mazeRenderer, JFrame window) {
        BufferedImage image = mazeRenderer.getMazeImage();
        if (image == null) {
            handleNoImage(window);
            return;
        }

        boolean[] found = findEntranceAndExit(image);
        boolean foundP = found[0];
        boolean foundK = found[1];

        if (!foundP || !foundK) {
            showWarning(window);
            setSelectedState(1);
        }
    }

    // Znajduje wejście i wyjście w labiryncie
    public static boolean[] findEntranceAndExit(BufferedImage image) {
        boolean foundP = false;
        boolean foundK = false;
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

    // Pokazuje ostrzeżenie, jeśli brakuje wejścia lub wyjścia
    private static void showWarning(JFrame window) {
        JOptionPane.showMessageDialog(window, "Brak punktu wejścia lub wyjścia. Wybierz je klikając na ścianę.", "Uwaga", JOptionPane.WARNING_MESSAGE);
    }

    // Obsługuje sytuację, gdy nie znaleziono obrazu labiryntu
    private static void handleNoImage(JFrame window) {
        JOptionPane.showMessageDialog(window, "Nie znaleziono obrazu labiryntu.", "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    // Resetuje punkty wejścia i wyjścia w labiryncie
    public static void resetEntranceAndExit(MazeRenderer mazeRenderer, JFrame window) {
        BufferedImage image = mazeRenderer.getMazeImage();
        DataArray dataArray = mazeRenderer.getDataArray();
        if (image != null && dataArray != null) {
            dataArray.resetEntrances(); // Resetujemy wejścia i wyjścia w DataArray
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (new Color(image.getRGB(x, y)).equals(Color.GREEN) || new Color(image.getRGB(x, y)).equals(Color.RED)) {
                        image.setRGB(x, y, Color.GRAY.getRGB()); // Zmienia kolor na szary (ściana)
                    }
                }
            }
            mazeRenderer.setMazeImage(image); // Aktualizuje obraz labiryntu
            mazeRenderer.getMazePanel().repaint(); // Odświeża panel z labiryntem
            JOptionPane.showMessageDialog(window, "Wejście i wyjście zostały zresetowane jako ściany.", "Reset", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(window, "Najpierw załaduj labirynt.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}
