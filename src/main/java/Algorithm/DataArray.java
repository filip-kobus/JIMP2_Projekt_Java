package Algorithm;

public class DataArray {
    int[][] array;
    Point entry;
    Point exit;
    int width, height;

    public static final int isUnusedPath = 7; // Stan dla nieużywanej ścieżki
    public static final int isPath = 6; // Stan dla ścieżki (niebieskie)
    public static final int isSpace = 1; // Stan dla przestrzeni (biały)

    public DataArray(int columns, int rows) {
        array = new int[columns][rows];
        this.width = columns;
        this.height = rows;
    }

    public void putPointIntoArray(Point point) {
        switch (point.getType()) {
            case Point.isEntry -> this.entry = point;
            case Point.isExit -> this.exit = point;
        }

        this.array[point.getX()][point.getY()] = point.getType();
    }

    public void setNewEntry(Point newPoint) {
        this.array[this.entry.getX()][this.entry.getY()] = Point.isWall;
        this.entry = newPoint;
    }

    public void setNewExit(Point newPoint) {
        this.array[this.exit.getX()][this.exit.getY()] = Point.isWall;
        this.exit = newPoint;
    }

    public void setAsVisited(Point point) {
        this.array[point.getX()][point.getY()] = Point.isVisited;
    }

    public void setAsSpace(Point point) {
        this.array[point.getX()][point.getY()] = isSpace;
    }

    public void setAsPath(Point point) {
        this.array[point.getX()][point.getY()] = isPath;
    }

    public void setAsUnusedPath(Point point) {
        this.array[point.getX()][point.getY()] = isUnusedPath; // Ustawiamy jako nieużywaną ścieżkę
    }

    public void switchPoint(Point point) {
        if (this.array[point.getX()][point.getY()] == Point.isSpace) {
            this.array[point.getX()][point.getY()] = Point.isVisited;
        } else if (this.array[point.getX()][point.getY()] == Point.isVisited) {
            this.array[point.getX()][point.getY()] = isPath;
        }
    }

    public boolean isExit(Point point) {
        return this.array[point.getX()][point.getY()] == Point.isExit;
    }

    public void printMatrix() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(array[x][y] + " ");
            }
            System.out.println();
        }
    }

    // Dodane metody dostępu
    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isPath(Point point) {
        return this.array[point.getX()][point.getY()] == isPath;
    }

    public Point getEntry() {
        return entry;
    }

    public Point getExit() {
        return exit;
    }

    // Resetowanie ścieżek
    public void resetPaths() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (array[x][y] == isPath || array[x][y] == isUnusedPath || array[x][y] == Point.isVisited) {
                    array[x][y] = isSpace;
                }
            }
        }
    }

    // Metoda do pobierania wartości komórki
    public int getCellValue(int x, int y) {
        return this.array[x][y];
    }
}
