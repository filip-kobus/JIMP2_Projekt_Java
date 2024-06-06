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
        this.dataArray.setAsVisited(this.entry); // Oznaczamy punkt startowy jako odwiedzony
    }

    public boolean runAlgorithm() {
        while (!queue.isEmpty() && !found) {
            currCell = queue.poll();

            if (currCell.equals(exit)) {
                found = true;
                markPath(currCell); // Zaznaczamy ścieżkę od wyjścia do wejścia
                dataArray.setAsExit(currCell); // Oznaczamy wyjście jako wyjście
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
                possiblePoint.setParent(point); // Ustawiamy rodzica, aby śledzić ścieżkę
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
                dataArray.setAsPath(step); // Oznaczamy ścieżkę
            }
            step = step.getParent();
        }
    }
}
