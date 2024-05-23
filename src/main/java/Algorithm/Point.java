package Algorithm;

public class Point {
    private int x;
    private int y;
    private int type;

    public static final int isWall = 0;
    public static final int isSpace = 1;
    public static final int isEntry = 2;
    public static final int isExit = 3;
    public static final int isVisited = 4;

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getType() {
        return this.type;
    }

    public void setPoint(int x, int y, char symbol) {
        this.x = x;
        this.y = y;
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
            default: throw new Error("Błąd: Nieprawidłowy typ.");
        }

    }
}
