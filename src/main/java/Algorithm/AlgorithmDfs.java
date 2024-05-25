package Algorithm;

import java.util.Stack;

public class AlgorithmDfs {
    private MyStack<Integer> distance = new MyStack<>();
    private MyStack<Point> pathToEnd = new MyStack<>();
    private MyStack<Point> nodes = new MyStack<>();

    public DataArray dataArray;
    private Point entry, exit, currCell;
    private int width, height;
    boolean isMovingBack = false;

    public AlgorithmDfs(DataArray data) {
        this.dataArray = data;
        this.width = data.width;
        this.height = data.height;
        this.entry = data.entry;
        this.exit = data.exit;
        this.currCell = this.entry;
        this.distance.push(0);
    }

    public boolean makeMove() {
        if(isMovingBack) {
            if(distance.decrementTopElement()) this.currCell = pathToEnd.pop();
            else {
                this.currCell = nodes.pop();
                isMovingBack = false;
            }
            return false;
        }
        else {
            currCell = findNextMove(currCell);
            return this.currCell.equalCoordinates(this.exit);
        }

    }

    public Point getMove() {
        return currCell;
    }

    private Point findNextMove(Point point) {
        int routesCount = 0;
        int [][] possibleRoutes = {
                {0, -1},
                {0, 1},
                {-1, 0},
                {1, 0}
        };

        Point newPoint = null;
        for(int[] route : possibleRoutes) {
            int diffX = route[0];
            int diffY = route[1];
            Point possbilePoint = point.movePoint(diffX, diffY);
            if (isValidMove(possbilePoint)) {
                newPoint = possbilePoint;
                if(this.dataArray.isExit(possbilePoint)) return newPoint;
                routesCount++;
            }
        }

        switch (routesCount) {
            case 0 -> {
                newPoint = pathToEnd.pop();
                this.isMovingBack = true;
                distance.decrementTopElement();
            }

            case 1 -> {
                distance.incrementTopElement();
                pathToEnd.push(newPoint);
            }

            default -> {
                pathToEnd.push(newPoint);
                distance.push(1);
                nodes.push(point);
            }
        }

        return newPoint;
    }

    private boolean isValidMove(Point point) {
        int x = point.getX();
        int y = point.getY();

        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            return false;
        }

        int cellType = this.dataArray.array[x][y];
        return cellType == Point.isSpace || cellType == Point.isExit; // Allow move to exit
    }


}
