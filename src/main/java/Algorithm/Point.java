package Algorithm;

import java.util.Objects;

public class Point {
    private int x;
    private int y;
    private int type;

    public static final int isWall = 0;
    public static final int isSpace = 1;
    public static final int isEntry = 2;
    public static final int isExit = 3;
    public static final int isVisited = 4;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getType() {
        return this.type;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
       this.y = y;
    }

    public void setTypeByInt(int type) {
        if(type > 4 || type < 0) {
            throw new Error("Błąd: Nieprawidłowy typ.");
        }
        this.type = type;
    }

    public void setTypeByChar(char symbol) {
        switch (symbol) {
            case 'X':
                this.type = isWall;
                break;
            case ' ':
                this.type = isSpace;
                break;
            case 'P':
                this.type = isEntry;
                break;
            case 'K':
                this.type = isExit;
                break;
            default:
                throw new Error("Błąd: Nieprawidłowy typ.");
        }
    }

    protected Point movePoint(int xDiff, int yDiff) {
        int x = this.x + xDiff;
        int y = this.y + yDiff;
        Point point = new Point(x, y);
        point.setTypeByInt(isVisited);
        return point;
    }

    public boolean equalCoordinates(Point point2) {
        return this.getX() == point2.getX() && this.getY() == point2.getY();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
