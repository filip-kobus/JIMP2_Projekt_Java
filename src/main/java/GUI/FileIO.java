package GUI;

import Algorithm.Binary;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Graphics2D;
import java.awt.RenderingHints;




public class FileIO {

    // Czytanie pliku z labiryntem
    public static BufferedImage readMazeFromFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        int rows = lines.size();
        int cols = lines.isEmpty() ? 0 : lines.get(0).length();

        BufferedImage image = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                char ch = lines.get(y).charAt(x);
                Color color = getColorFromChar(ch);
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    // Zapisywanie labiryntu do pliku
    public static void writeMazeToFile(BufferedImage image, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    Color color = new Color(image.getRGB(x, y));
                    char ch = getCharFromColor(color);
                    writer.write(ch);
                }
                writer.newLine();
            }
        }
    }

    // Tworznie kopii pliku
    public static File createTemporaryFileCopy(File originalFile) throws IOException {
        File tempFile = File.createTempFile("maze_", ".txt");
        tempFile.deleteOnExit();
        Files.copy(originalFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    // Metoda pomocnicza do konwersji znaku na kolor
    private static Color getColorFromChar(char ch) {
        switch (ch) {
            case 'X': return Color.GRAY;
            case 'P': return Color.GREEN;
            case 'K': return Color.RED;
            case ' ':
            default:  return Color.WHITE;
        }
    }

    // Metoda pomocnicza do konwersji koloru na znak
    private static char getCharFromColor(Color color) {
        if (color.equals(Color.GRAY)) {
            return 'X';
        } else if (color.equals(Color.GREEN)) {
            return 'P';
        } else if (color.equals(Color.RED)) {
            return 'K';
        } else {
            return ' ';
        }
    }

    public static void saveMazeAsText(BufferedImage mazeImage, JFrame window) {
        if (mazeImage == null) {
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
                writeMazeToFile(mazeImage, fileToSave);
                JOptionPane.showMessageDialog(window, "Labirynt został zapisany w " + fileToSave.getPath(), "Informacja", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Nie udało się zapisać labiryntu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Zachowuje labirynt jako obraz
    public static void saveMazeAsImage(BufferedImage mazeImage, JFrame window) {
        if (mazeImage == null) {
            JOptionPane.showMessageDialog(window, "Nie ma otwartego labiryntu do zapisania jako obraz!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Zapisz obraz labiryntu");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Obrazy PNG", "png"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Obrazy JPEG", "jpg", "jpeg"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Obrazy BMP", "bmp"));

        int userSelection = fileChooser.showSaveDialog(window);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String ext = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName().endsWith("." + ext) ? fileToSave.getName() : fileToSave.getName() + "." + ext);

            try {
                // Zapisz obraz bezpośrednio bez zmiany rozmiaru
                ImageIO.write(mazeImage, ext, fileToSave);
                JOptionPane.showMessageDialog(window, "Obraz labiryntu został zapisany w " + fileToSave.getPath(), "Informacja", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Nie udało się zapisać obrazu labiryntu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }







    public static File openMazeFile(JFrame window) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz plik labiryntu");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki tekstowe lub binarne", "txt", "bin"));

        int result = fileChooser.showOpenDialog(window);
        return (result == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
    }

    public static BufferedImage prepareFile(File mazeFile) throws IOException {
        File temporaryMazeFile = createTemporaryFileCopy(mazeFile);

        // Sprawdzenie, czy plik jest plikiem binarnym i konwersja jeśli potrzeba
        if (mazeFile.getName().endsWith(".bin")) {
            File txtFile = new File(temporaryMazeFile.getParent(), temporaryMazeFile.getName().replace(".bin", ".txt"));
            Binary.convertBinaryToText(mazeFile.getAbsolutePath(), txtFile.getAbsolutePath());
            temporaryMazeFile = txtFile;
        }

        // Wczytanie przygotowanego pliku do obrazu
        return readMazeFromFile(temporaryMazeFile);
    }


    public static BufferedImage scaleImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return scaledImage;
    }


}
