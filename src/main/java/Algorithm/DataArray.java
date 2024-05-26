package Algorithm;

public class DataArray {
    int[][] array;
    Point entry;
    Point exit;
    int width, height;

    public static final int isUnusedPath = 7; // Dodajemy nowy stan dla nieużywanej ścieżki

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
        this.array[point.getX()][point.getY()] = Point.isSpace;
    }

    public void setAsPath(Point point) {
        this.array[point.getX()][point.getY()] = 6;
    }

    public void setAsUnusedPath(Point point) {
        this.array[point.getX()][point.getY()] = isUnusedPath; // Ustawiamy jako nieużywaną ścieżkę
    }

    public void switchPoint(Point point) {
        if (this.array[point.getX()][point.getY()] == Point.isSpace) {
            this.array[point.getX()][point.getY()] = Point.isVisited;
        } else if (this.array[point.getX()][point.getY()] == Point.isVisited) {
            this.array[point.getX()][point.getY()] = 6;
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
        return this.array[point.getX()][point.getY()] == 6;
    }

    public Point getEntry() {
        return entry;
    }

    public Point getExit() {
        return exit;
    }

    public void resetPaths() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (array[x][y] == 6 || array[x][y] == isUnusedPath || array[x][y] == Point.isVisited) {
                    array[x][y] = Point.isSpace;
                }
            }
        }
    }

    public int getCellValue(int x, int y) {
        return this.array[x][y];
    }
}
