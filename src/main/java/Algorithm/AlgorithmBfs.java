package Algorithm;

import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

public class AlgorithmBfs {
    private final DataArray dataArray;
    private final Queue<Point> queue = new LinkedList<>();
    private final HashMap<Point, Point> childParentMap = new HashMap<>();
    private final Point entry;
    private final Point exit;

    public AlgorithmBfs(DataArray data) {
        this.dataArray = data;
        this.entry = data.entry;
        this.exit = data.exit;
    }

    public void runAlgorithm() {
        Point currentCell = this.entry;
        Point exit = this.exit;
        currentCell = findFirstMove(currentCell);

        // Ustawiam exit jako puste miejsce w tablicy aby program do niego dotarl
        this.dataArray.setAsSpace(exit);
        queue.add(currentCell);

        while (!currentCell.equalCoordinates(exit)) {
            addPossibleMovesToQueue(currentCell);
            if (queue.isEmpty()) {
                break;
            }
            currentCell = queue.remove();
        }
    }

    private void addPossibleMovesToQueue(Point point) {
        int x = point.getX();
        int y = point.getY();
        int[][] possibleRoutes = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        for (int[] route : possibleRoutes) {
            int diffX = route[0];
            int diffY = route[1];
            int newX = x + diffX;
            int newY = y + diffY;

            // Sprawdzenie, czy nowe współrzędne są w zakresie
            if (newX >= 0 && newY >= 0 && newX < dataArray.getWidth() && newY < dataArray.getHeight()) {
                Point newPoint = point.movePoint(diffX, diffY);
                if (this.dataArray.array[newX][newY] == Point.isSpace) {
                    queue.add(newPoint);
                    this.dataArray.setAsVisited(newPoint);
                    this.childParentMap.put(newPoint, point);
                }
            }
        }
    }

    // Pierwszy punkt może mieć jedną współrzędna = 0, dlatego wykonuje ruch w odzielnej metodzie
    private Point findFirstMove(Point point) {
        int x = point.getX();
        int y = point.getY();
        int width = this.dataArray.width;
        int height = this.dataArray.height;

        if (x == width - 1) x--;
        else if (x == 0) x++;

        if (y == height - 1) y--;
        else if (y == 0) y++;

        Point newPoint = new Point(x, y);
        newPoint.setTypeByInt(Point.isVisited);
        this.dataArray.setAsVisited(newPoint);
        return newPoint;
    }

    public void printPathToArray() {
        Point nextPoint = childParentMap.get(this.exit);

        while (nextPoint != null) {
            // cords to kolejny punkt to wyjscia do wejscia
            this.dataArray.setAsPath(nextPoint);
            nextPoint = childParentMap.get(nextPoint);
        }
    }
}
