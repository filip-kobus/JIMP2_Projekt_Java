package Algorithm;

import java.awt.*;

public class DataArray {
    int[][] array;
    Point entry;
    Point exit;
    Point currentCell;

    public static final int isWall = 0;
    public static final int isSpace = 1;
    public static final int isEntry = 2;
    public static final int isExit = 3;

    public DataArray(int columns, int rows){
        array = new int[columns][rows];
    }

    public void putPointIntoArray(Point point) {
        this.array[point.getX()][point.getY()] = point.getType();
    }

}
