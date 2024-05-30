package Algorithm;

import java.util.ArrayDeque;
import java.util.Deque;

public class AlgorithmDfs {
    private DataArray dataArray;
    private Deque<Point> stack;
    private boolean[][] visited;
    private boolean isMovingBack = false;
    private Point lastMove;

    public AlgorithmDfs(DataArray dataArray) {
        this.dataArray = dataArray;
        this.stack = new ArrayDeque<>();
        this.visited = new boolean[dataArray.getWidth()][dataArray.getHeight()];
        this.stack.push(dataArray.getEntry());
        this.visited[dataArray.getEntry().getX()][dataArray.getEntry().getY()] = true;
        this.lastMove = dataArray.getEntry();
    }

    public boolean makeMove() {
        if (stack.isEmpty()) {
            return true; // Brak ścieżki
        }

        Point current = stack.peek();

        if (dataArray.isExit(current)) {
            dataArray.setAsPath(current); // Ustawienie punktu jako część ścieżki
            return true; // Ścieżka znaleziona
        }

        Point[] neighbors = {
                new Point(current.getX() - 1, current.getY()),
                new Point(current.getX() + 1, current.getY()),
                new Point(current.getX(), current.getY() - 1),
                new Point(current.getX(), current.getY() + 1)
        };

        for (Point neighbor : neighbors) {
            if (isValidMove(neighbor)) {
                stack.push(neighbor);
                visited[neighbor.getX()][neighbor.getY()] = true;
                dataArray.setAsVisited(neighbor);
                lastMove = neighbor;
                isMovingBack = false;
                return false; // Kontynuuj przeszukiwanie
            }
        }

        // Jeśli nie ma żadnych sąsiadów, wróć do poprzedniego punktu
        stack.pop();
        isMovingBack = true;
        lastMove = current;
        return false; // Kontynuuj przeszukiwanie
    }

    private boolean isValidMove(Point point) {
        int x = point.getX();
        int y = point.getY();
        return x >= 0 && y >= 0 && x < dataArray.getWidth() && y < dataArray.getHeight()
                && (dataArray.getCellValue(x, y) == Point.IS_SPACE || dataArray.isExit(point)) && !visited[x][y];
    }

    public Point getMove() {
        return lastMove;
    }

    public boolean isMovingBack() {
        return isMovingBack;
    }

    public void setMovingBack(boolean isMovingBack) {
        this.isMovingBack = isMovingBack;
    }
}
