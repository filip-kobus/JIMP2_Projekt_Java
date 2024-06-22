package Algorithm;

import java.util.Objects;

public class Point {

    private int x;
    private int y;
    private int type;
    private Point parent; // Parent for tracing the path

    public static final int IS_WALL = 0;
    public static final int IS_SPACE = 1;
    public static final int IS_ENTRY = 2;
    public static final int IS_EXIT = 3;
    public static final int IS_VISITED = 4;


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

    public Point getParent() {
        return this.parent;
    }

    public void setParent(Point parent) {
        this.parent = parent;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setType(int type) {
        if (type > 4 || type < 0) {
            throw new IllegalArgumentException("Błąd: Nieprawidłowy typ.");
        }
        this.type = type;
    }

    public void setType(char symbol) {
        switch (symbol) {
            case 'X':
                this.type = IS_WALL;
                break;
            case ' ':
                this.type = IS_SPACE;
                break;
            case 'P':
                this.type = IS_ENTRY;
                break;
            case 'K':
                this.type = IS_EXIT;
                break;
            default:
                throw new IllegalArgumentException("Błąd: Nieprawidłowy typ.");
        }
    }

    protected Point movePoint(int xDiff, int yDiff) {
        int currentX = this.x + xDiff;
        int currentY = this.y + yDiff;
        Point point = new Point(currentX, currentY);
        point.setType(IS_VISITED);
        point.setParent(this); // Setting the parent for tracing the path
        return point;
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
