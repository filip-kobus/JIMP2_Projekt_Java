package GUI;

import Algorithm.DataArray;
import Algorithm.Point;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class MazeUtilities {

    private MazeUtilities(){
    }

    private static int selectedState = 0; // 0 - normal, 1 - entry, 2 - exit

    // Set selected state
    public static void setSelectedState(int state) {
        selectedState = state;
    }

    // Check if the cell is an edge
    public static boolean isEdge(int x, int y, int width, int height) {
        return x == 0 || x == width - 1 || y == 0 || y == height - 1;
    }

    // Handle maze cell selection
    public static void handleMazeCellSelection(MazeRenderer mazeRenderer, int imageX, int imageY, JFrame window) {
        DataArray dataArray = mazeRenderer.getDataArray();
        if (selectedState != 0) { // If we are in the entry or exit selection mode
            if (isEdge(imageX, imageY, mazeRenderer.getMazeImage().getWidth(), mazeRenderer.getMazeImage().getHeight())) {

                mazeRenderer.paintCell(imageX, imageY, selectedState); // Paint the cell

                Point point = new Point(imageX, imageY);
                if (selectedState == 1) { // If we are in the entry selection mode
                    point.setType(Point.IS_ENTRY);
                    dataArray.setNewEntry(point);
                    JOptionPane.showMessageDialog(window, "Select the exit in the narrow points of the maze.", "Next", JOptionPane.INFORMATION_MESSAGE);
                    setSelectedState(2);
                } else if (selectedState == 2) { // Jeśli wybrano punkt wyjściowy
                    point.setType(Point.IS_EXIT);
                    dataArray.setNewExit(point);
                    JOptionPane.showMessageDialog(window, "Entrance and exit points were set", "Info", JOptionPane.INFORMATION_MESSAGE);
                    setSelectedState(0);
                }
            } else {
                JOptionPane.showMessageDialog(window, "Entrance and exit must be put on narrow points of the maze.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(window, "The cell: (" + imageY + ", " + imageX + ")" + "was clicked.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Check for entrance and exit in the maze
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

    // Find entrance and exit in the maze
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

    // Shows warning about missing entrance or exit
    private static void showWarning(JFrame window) {
        JOptionPane.showMessageDialog(window, "There is no entrance or exit. Please select it by clicking on the wall", "Warining", JOptionPane.WARNING_MESSAGE);
    }

    // Handing stituation when there is no image
    private static void handleNoImage(JFrame window) {
        JOptionPane.showMessageDialog(window, "There is no picture of the maze", "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Reset entrance and exit
    public static void resetEntranceAndExit(MazeRenderer mazeRenderer, JFrame window) {
        BufferedImage image = mazeRenderer.getMazeImage();
        DataArray dataArray = mazeRenderer.getDataArray();
        if (image != null && dataArray != null) {
            dataArray.resetEntrances(); // Resetting the entrances in the data array
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (new Color(image.getRGB(x, y)).equals(Color.GREEN) || new Color(image.getRGB(x, y)).equals(Color.RED)) {
                        image.setRGB(x, y, Color.GRAY.getRGB()); //Changing the color of the entrance and exit to gray
                    }
                }
            }
            mazeRenderer.setMazeImage(image); // Updating the image
            mazeRenderer.getMazePanel().repaint();
            JOptionPane.showMessageDialog(window, "Entrance and exit points were set as walls.", "Reset", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(window, "First load the maze.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
