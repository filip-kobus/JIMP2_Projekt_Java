package Algorithm;

public class DataArray {
    int[][] array;
    Point entry;
    Point exit;
    int width, height;

    public DataArray(int columns, int rows){
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

    public void switchPoint(Point point) {
        if(this.array[point.getX()][point.getY()] == Point.isSpace) this.array[point.getX()][point.getY()] = Point.isVisited;
        else if(this.array[point.getX()][point.getY()] == Point.isVisited) this.array[point.getX()][point.getY()] = 6;
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

}
