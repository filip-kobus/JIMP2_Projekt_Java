package Algorithm;

import java.util.Stack;

public class AlgorithmDfs {
    private Stack<Point> pathToEnd = new Stack<>();
    private Stack<Point> nodes = new Stack<>();

    public DataArray dataArray;
    private Point entry, exit, currCell;
    private int width, height;
    public boolean isMovingBack = false;

    public AlgorithmDfs(DataArray data) {
        this.dataArray = data;
        this.width = data.width;
        this.height = data.height;
        this.entry = data.entry;
        this.exit = data.exit;
        this.currCell = this.entry;
        this.pathToEnd.push(this.entry);
        this.dataArray.setAsVisited(this.entry); // Oznaczamy punkt startowy jako odwiedzony
    }

    public boolean makeMove() {
        if (isMovingBack) {
            if (!pathToEnd.isEmpty()) {
                this.currCell = pathToEnd.pop();
                this.dataArray.setAsUnusedPath(this.currCell); // Oznaczamy nieużywaną ścieżkę
            }
            if (pathToEnd.isEmpty()) {
                return false; // Nie znaleziono ścieżki
            }
            isMovingBack = false;
        } else {
            Point nextMove = findNextMove(currCell);
            if (nextMove != null) {
                this.currCell = nextMove;
                pathToEnd.push(this.currCell);
                this.dataArray.setAsVisited(this.currCell); // Oznaczamy komórkę jako odwiedzoną
                if (this.currCell.equalCoordinates(this.exit)) {
                    return true; // Znaleziono wyjście
                }
            } else {
                isMovingBack = true;
            }
        }
        return false;
    }

    public Point getMove() {
        return currCell;
    }

    private Point findNextMove(Point point) {
        int[][] possibleRoutes = {
                {0, -1},
                {0, 1},
                {-1, 0},
                {1, 0}
        };

        for (int[] route : possibleRoutes) {
            int diffX = route[0];
            int diffY = route[1];
            Point possiblePoint = point.movePoint(diffX, diffY); // Tworzymy nowy punkt
            if (isValidMove(possiblePoint)) {
                return possiblePoint;
            }
        }

        return null; // Brak możliwych ruchów, trzeba się cofnąć
    }

    private boolean isValidMove(Point point) {
        int x = point.getX();
        int y = point.getY();

        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            return false;
        }

        int cellType = this.dataArray.getCellValue(x, y);
        return cellType == Point.isSpace || cellType == Point.isExit; // Pozwalamy na ruch do wyjścia
    }
}
