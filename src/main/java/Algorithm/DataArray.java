package Algorithm;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DataArray {
    int[][] array;
    Point entry;
    Point exit;
    int width;
    int height;

    public static final int IS_UNUSED_PATH = 7; // Dodajemy nowy stan dla nieużywanej ścieżki
    public static final int IS_PATH = 6; // Dodajemy nowy stan dla ścieżki


    public DataArray(int columns, int rows) {
        array = new int[columns][rows];
        this.width = columns;
        this.height = rows;
    }

    public synchronized void putPointIntoArray(Point point) {
        if (point.getType() == Point.IS_ENTRY) {
            this.entry = point;
        } else if (point.getType() == Point.IS_EXIT) {
            this.exit = point;
        }
        this.array[point.getX()][point.getY()] = point.getType();
    }


    public synchronized void setNewEntry(Point newPoint) {
        if (this.entry != null) {
            this.array[this.entry.getX()][this.entry.getY()] = Point.IS_WALL;
        }
        this.entry = newPoint;
        this.array[this.entry.getX()][this.entry.getY()] = Point.IS_WALL;
    }

    public synchronized void setNewExit(Point newPoint) {
        if (this.exit != null) {
            this.array[this.exit.getX()][this.exit.getY()] = Point.IS_WALL;
        }
        this.exit = newPoint;
        this.array[this.exit.getX()][this.exit.getY()] = Point.IS_EXIT;
    }

    public synchronized void setAsVisited(Point point) {
        if (point.equals(entry)) {
            this.array[point.getX()][point.getY()] = Point.IS_ENTRY;
        } else if (point.equals(exit)) {
            this.array[point.getX()][point.getY()] = Point.IS_EXIT;
        } else {
            this.array[point.getX()][point.getY()] = Point.IS_VISITED;
        }
    }

    public synchronized void setAsPath(Point point) {
        this.array[point.getX()][point.getY()] = IS_PATH;
    }

    public synchronized void setAsUnusedPath(Point point) {
        this.array[point.getX()][point.getY()] = IS_UNUSED_PATH; // Ustawiamy jako nieużywaną ścieżkę
    }

    public synchronized void setAsExit(Point point) {
        this.array[point.getX()][point.getY()] = Point.IS_EXIT; // Ustawiamy jako wyjście
    }

    public synchronized boolean isExit(Point point) {
        return this.array[point.getX()][point.getY()] == Point.IS_EXIT;
    }

    public synchronized void printMatrix() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(array[x][y] + " ");
            }
            System.out.println();
        }
    }

    // Dodane metody dostępu
    public synchronized int getHeight() {
        return height;
    }

    public synchronized int getWidth() {
        return width;
    }

    public synchronized boolean hasPath(Point point) {
        return this.array[point.getX()][point.getY()] == IS_PATH;
    }

    public synchronized Point getEntry() {
        return entry;
    }

    public synchronized Point getExit() {
        return exit;
    }

    public synchronized void resetPaths() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (array[x][y] == IS_PATH || array[x][y] == IS_UNUSED_PATH || array[x][y] == Point.IS_VISITED) {
                    array[x][y] = Point.IS_SPACE;
                }
            }
        }
    }

    public synchronized int getCellValue(int x, int y) {
        return this.array[x][y];
    }

    public synchronized void resetEntrances() {
        if (this.entry != null) {
            this.array[this.entry.getX()][this.entry.getY()] = Point.IS_WALL;
            this.entry = null;
        }
        if (this.exit != null) {
            this.array[this.exit.getX()][this.exit.getY()] = Point.IS_WALL;
            this.exit = null;
        }
    }

    // Metoda rysująca labirynt na obrazie
    public BufferedImage toBufferedImage(int cellSize) {
        int imgWidth = width * cellSize;
        int imgHeight = height * cellSize;
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                switch (array[x][y]) {
                    case Point.IS_WALL -> g2d.setColor(Color.GRAY);
                    case Point.IS_ENTRY -> g2d.setColor(Color.GREEN);
                    case Point.IS_EXIT -> g2d.setColor(Color.RED);
                    case IS_PATH -> g2d.setColor(Color.BLUE); // Ścieżka
                    case IS_UNUSED_PATH -> g2d.setColor(Color.WHITE); // Nieużywana ścieżka
                    case Point.IS_VISITED -> g2d.setColor(Color.WHITE); //
                    default -> g2d.setColor(Color.WHITE); // Puste miejsce
                }
                g2d.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        g2d.dispose();
        return image;
    }


}
