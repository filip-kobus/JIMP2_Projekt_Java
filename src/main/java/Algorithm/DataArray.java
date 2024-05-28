package Algorithm;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DataArray {
    int[][] array;
    Point entry;
    Point exit;
    int width, height;

    public static final int isUnusedPath = 7; // Dodajemy nowy stan dla nieużywanej ścieżki
    public static final int isPath = 6; // Dodajemy nowy stan dla ścieżki

    public DataArray(int columns, int rows) {
        array = new int[columns][rows];
        this.width = columns;
        this.height = rows;
    }

    public synchronized void putPointIntoArray(Point point) {
        switch (point.getType()) {
            case Point.isEntry -> this.entry = point;
            case Point.isExit -> this.exit = point;
        }
        this.array[point.getX()][point.getY()] = point.getType();
    }

    public synchronized void setNewEntry(Point newPoint) {
        if (this.entry != null) {
            this.array[this.entry.getX()][this.entry.getY()] = Point.isWall;
        }
        this.entry = newPoint;
        this.array[this.entry.getX()][this.entry.getY()] = Point.isEntry;
    }

    public synchronized void setNewExit(Point newPoint) {
        if (this.exit != null) {
            this.array[this.exit.getX()][this.exit.getY()] = Point.isWall;
        }
        this.exit = newPoint;
        this.array[this.exit.getX()][this.exit.getY()] = Point.isExit;
    }

    public synchronized void setAsVisited(Point point) {
        if (point.equalCoordinates(entry)) {
            this.array[point.getX()][point.getY()] = Point.isEntry;
        } else if (point.equalCoordinates(exit)) {
            this.array[point.getX()][point.getY()] = Point.isExit;
        } else {
            this.array[point.getX()][point.getY()] = Point.isVisited;
        }
    }

    public synchronized void setAsSpace(Point point) {
        this.array[point.getX()][point.getY()] = Point.isSpace;
    }

    public synchronized void setAsPath(Point point) {
        this.array[point.getX()][point.getY()] = isPath;
    }

    public synchronized void setAsUnusedPath(Point point) {
        this.array[point.getX()][point.getY()] = isUnusedPath; // Ustawiamy jako nieużywaną ścieżkę
    }

    public synchronized void setAsExit(Point point) {
        this.array[point.getX()][point.getY()] = Point.isExit; // Ustawiamy jako wyjście
    }

    public synchronized void switchPoint(Point point) {
        if (this.array[point.getX()][point.getY()] == Point.isSpace) {
            this.array[point.getX()][point.getY()] = Point.isVisited;
        } else if (this.array[point.getX()][point.getY()] == Point.isVisited) {
            this.array[point.getX()][point.getY()] = isPath;
        }
    }

    public synchronized boolean isExit(Point point) {
        return this.array[point.getX()][point.getY()] == Point.isExit;
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

    public synchronized boolean isPath(Point point) {
        return this.array[point.getX()][point.getY()] == isPath;
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
                if (array[x][y] == isPath || array[x][y] == isUnusedPath || array[x][y] == Point.isVisited) {
                    array[x][y] = Point.isSpace;
                }
            }
        }
    }

    public synchronized int getCellValue(int x, int y) {
        return this.array[x][y];
    }

    public synchronized void resetEntrances() {
        if (this.entry != null) {
            this.array[this.entry.getX()][this.entry.getY()] = Point.isWall;
            this.entry = null;
        }
        if (this.exit != null) {
            this.array[this.exit.getX()][this.exit.getY()] = Point.isWall;
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
                    case Point.isWall -> g2d.setColor(Color.GRAY);
                    case Point.isEntry -> g2d.setColor(Color.GREEN);
                    case Point.isExit -> g2d.setColor(Color.RED);
                    case isPath -> g2d.setColor(Color.BLUE); // Ścieżka
                    case isUnusedPath -> g2d.setColor(Color.YELLOW); // Nieużywana ścieżka
                    case Point.isVisited -> g2d.setColor(Color.WHITE); // Odwiedzony
                    default -> g2d.setColor(Color.WHITE); // Puste miejsce
                }
                g2d.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        g2d.dispose();
        return image;
    }


}
