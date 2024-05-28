package Algorithm;

import java.util.Stack;

public class AlgorithmDfs {
    private DataArray dataArray;
    private Stack<Point> stack;
    private boolean[][] visited;
    public boolean isMovingBack = false;

    private Point lastMove;

    public AlgorithmDfs(DataArray dataArray) {
        this.dataArray = dataArray;
        this.stack = new Stack<>();
        this.visited = new boolean[dataArray.getWidth()][dataArray.getHeight()];
        this.stack.push(dataArray.getEntry());
        this.visited[dataArray.getEntry().getX()][dataArray.getEntry().getY()] = true;
        this.lastMove = dataArray.getEntry();
    }

    public boolean makeMove() {
        if (stack.isEmpty()) {
            return true; // No path found, but finished
        }

        Point current = stack.peek();

        if (dataArray.isExit(current)) {
            dataArray.setAsPath(current); // Mark the exit as part of the path
            return true; // Path found
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
                return false; // Continue searching
            }
        }

        // If no valid moves, backtrack
        stack.pop();
        isMovingBack = true;
        lastMove = current;
        return false; // Continue searching
    }

    private boolean isValidMove(Point point) {
        int x = point.getX();
        int y = point.getY();
        return x >= 0 && y >= 0 && x < dataArray.getWidth() && y < dataArray.getHeight()
                && (dataArray.getCellValue(x, y) == Point.isSpace || dataArray.isExit(point)) && !visited[x][y];
    }

    public Point getMove() {
        return lastMove;
    }
}
