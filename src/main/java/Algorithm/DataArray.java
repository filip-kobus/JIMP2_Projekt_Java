package Algorithm;

public class DataArray {
    int[][] array;
    Point entry;
    Point exit;
    Point currentCell;

    public DataArray(int columns, int rows){
        array = new int[columns][rows];
    }

    public void putPointIntoArray(Point point) {
        switch(point.getType()) {
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

    public void setCurrentCell() {
        this.currentCell = this.entry;
    }

    public void setAsVisited(Point newPoint) {
        this.array[this.currentCell.getX()][this.currentCell.getY()] = Point.isVisited;
        this.currentCell = newPoint;
    }

}
