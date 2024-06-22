package Algorithm;

import java.util.LinkedList;
import java.util.Queue;

public class AlgorithmBfs {
    private Queue<Point> queue = new LinkedList<>();
    private DataArray dataArray;
    private Point entry;
    private Point exit;
    private Point currCell;
    private boolean found = false;

    public AlgorithmBfs(DataArray dataArray) {
        this.dataArray = dataArray;
        this.entry = dataArray.getEntry();
        this.exit = dataArray.getExit();
        this.currCell = this.entry;
        this.queue.add(this.entry);
        this.dataArray.setAsVisited(this.entry); // We set the entry as visited
    }

    public boolean runAlgorithm() {
        while (!queue.isEmpty() && !found) {
            currCell = queue.poll();

            if (currCell.equals(exit)) {
                found = true;
                markPath(currCell); // We mark the path
                dataArray.setAsExit(currCell); // mark the exit
                continue;
            }


            addPossibleMovesToQueue(currCell);
        }

        return true;
    }

    private void addPossibleMovesToQueue(Point point) {
        int[][] possibleRoutes = {
                {0, -1},
                {0, 1},
                {-1, 0},
                {1, 0}
        };

        for (int[] route : possibleRoutes) {
            int diffX = route[0];
            int diffY = route[1];
            Point possiblePoint = point.movePoint(diffX, diffY);
            if (isValidMove(possiblePoint)) {
                queue.add(possiblePoint);
                dataArray.setAsVisited(possiblePoint);
                possiblePoint.setParent(point); // setting the parent for tracing the path
            }
        }
    }

    private boolean isValidMove(Point point) {
        int x = point.getX();
        int y = point.getY();

        if (x < 0 || x >= dataArray.getWidth() || y < 0 || y >= dataArray.getHeight()) {
            return false;
        }

        int cellType = dataArray.getCellValue(x, y);
        return cellType == Point.IS_SPACE || cellType == Point.IS_EXIT;
    }

    private void markPath(Point end) {
        Point step = end;
        while (step != null) {
            if (!step.equals(entry) && !step.equals(exit)) {
                dataArray.setAsPath(step); // We mark the path
            }
            step = step.getParent();
        }
    }
}
