package GUI;

import Algorithm.AlgorithmBfs;
import Algorithm.AlgorithmDfs;
import Algorithm.DataArray;
import Algorithm.Point;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MazeRenderer {
    private BufferedImage mazeImage; // Picture of the maze

    private DataArray dataArray;

    private JPanel mazePanel; // Panel with the maze
    private double initialZoomFactor = 1.0;
    private double zoomFactor = 1.0;

    private SwingWorker<Void, Point> dfsWorker;
    private SwingWorker<Void, Void> bfsWorker;
    private AtomicBoolean stopFlag = new AtomicBoolean(false);


    public MazeRenderer(BufferedImage mazeImage) {
        this.mazeImage = mazeImage;
        createMazePanel(); // Create a panel with the maze
    }

    // Tworzenie panelu z labiryntem
    public JPanel createMazePanel() {
        mazePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mazeImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int scaledWidth = (int) (mazeImage.getWidth() * initialZoomFactor * zoomFactor);
                    int scaledHeight = (int) (mazeImage.getHeight() * initialZoomFactor * zoomFactor);
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;
                    g2d.drawImage(mazeImage, x, y, scaledWidth, scaledHeight, null);
                    g2d.dispose();
                }
            }
        };
        return mazePanel;
    }

    // Draw a cell in the maze
    public void paintCell(int x, int y, int state) {
        if (mazeImage == null || x < 0 || y < 0 || x >= mazeImage.getWidth() || y >= mazeImage.getHeight()) return;


        // There should be five states: 0 - wall, 1 - start, 2 - exit, 3 - path, 4 - unused path
        Color color;
        switch (state) {
            case 0:
                color = Color.GRAY;
                break;
            case 1:
                color = Color.GREEN;
                break;
            case 2:
                color = Color.RED;
                break;
            case 3:
                color = Color.BLUE;
                break;
            case 4:
                color = Color.WHITE;
                break;
            case 5:
                color = Color.YELLOW; // Extra color for the solution
                break;
            default:
                color = Color.WHITE;
        }

        // Setting the color of the cell
        mazeImage.setRGB(x, y, color.getRGB());
        mazePanel.repaint(); // Ensuring that the maze is repainted
    }


    // Method to solve the maze using the BFS algorithm
    public void solveMazeWithBfs(DataArray dataArray) {
        resetPaths(dataArray);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                AlgorithmBfs bfs = new AlgorithmBfs(dataArray);
                bfs.runAlgorithm();

                return null;
            }

            @Override
            protected void done() {
                // Coloring the path
                for (int y = 0; y < dataArray.getHeight(); y++) {
                    for (int x = 0; x < dataArray.getWidth(); x++) {
                        Point point = new Point(x, y);
                        if (dataArray.hasPath(point)) {
                            paintCell(x, y, 3); // Blue path
                        }
                    }
                }
                // Coloring the start and the exit
                paintCell(dataArray.getEntry().getX(), dataArray.getEntry().getY(), 1);
                paintCell(dataArray.getExit().getX(), dataArray.getExit().getY(), 2); // Koniec na czerwono
                JOptionPane.showMessageDialog(null, "The path was found!", "BFS Solver", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
    }

    // Visualizing the BFS algorithm
    public void visualizeDfs(DataArray dataArray) {
        resetPaths(dataArray);
        stopFlag.set(false); // Resetting the stop flag
        dfsWorker = createDfsWorker(dataArray);
        dfsWorker.execute();
    }

    private SwingWorker<Void, Point> createDfsWorker(DataArray dataArray) {
        return new SwingWorker<>() {
            AlgorithmDfs dfs = new AlgorithmDfs(dataArray);
            Point entry = dataArray.getEntry();
            Point exit = dataArray.getExit();
            boolean found = false;

            @Override
            protected Void doInBackground() throws Exception {
                while (!found && !stopFlag.get()) { // Add a condition to stop the algorithm
                    found = dfs.makeMove();
                    Point currMove = dfs.getMove();
                    publish(currMove);
                    TimeUnit.MILLISECONDS.sleep(60); // Delay for visualization
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Point> chunks) {
                for (Point currMove : chunks) {
                    processPoint(currMove, entry, exit, dfs, dataArray);
                }
            }

            @Override
            protected void done() {
                if (!stopFlag.get()) { // Check if the algorithm was stopped
                    JOptionPane.showMessageDialog(null, "The path was found!", "DFS Visualization", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
    }

    private void processPoint(Point currMove, Point entry, Point exit, AlgorithmDfs dfs, DataArray dataArray) {
        if (currMove.equals(entry)) { // If the point is the starting point
            paintCell(currMove.getX(), currMove.getY(), 1);
        } else if (currMove.equals(exit)) { // If the point is the exit
            paintCell(currMove.getX(), currMove.getY(), 2);
        } else {
            if (dfs.isMovingBack()) { // If the point is moving back
                paintCell(currMove.getX(), currMove.getY(), 4); // Resetting the path to white
                dataArray.setAsUnusedPath(currMove); // Setting the point as an unused path
            } else {
                paintCell(currMove.getX(), currMove.getY(), 3); // Setting the path to blue
                dataArray.setAsPath(currMove); // Setting the point as a path
            }
        }
    }


    public void stopVisualization() {
        stopFlag.set(true); // Ustawiamy flagę stopu
        if (dfsWorker != null && !dfsWorker.isDone()) {
            dfsWorker.cancel(true); // Cancel the SwingWorker
        }
        if (bfsWorker != null && !bfsWorker.isDone()) {
            bfsWorker.cancel(true); //
        }
    }

    public void resetPaths(DataArray dataArray) {
        stopVisualization(); // Stop the visualization
        if (dataArray != null) {
            dataArray.resetPaths();
            for (int y = 0; y < dataArray.getHeight(); y++) {
                for (int x = 0; x < dataArray.getWidth(); x++) {
                    // If the cell is a space, path, unused path, or visited, reset it to white
                    if (dataArray.getCellValue(x, y) == Point.IS_SPACE || dataArray.getCellValue(x, y) == DataArray.IS_PATH || dataArray.getCellValue(x, y) == DataArray.IS_UNUSED_PATH || dataArray.getCellValue(x, y) == Point.IS_VISITED) {
                        paintCell(x, y, 4);
                    }
                }
            }
            paintCell(dataArray.getEntry().getX(), dataArray.getEntry().getY(), 1); // Start as green
            paintCell(dataArray.getExit().getX(), dataArray.getExit().getY(), 2); // End as red

        }
    }


    // Set the zoom factor
    public void setZoomFactor(double factor) {
        this.zoomFactor = factor;
        mazePanel.revalidate();
        mazePanel.repaint();
    }

    // Get the zoom factor
    public double getZoomFactor() {
        return zoomFactor;
    }

    // Set the initial zoom factor
    public void setInitialZoomFactor(double factor) {
        this.initialZoomFactor = factor;
    }

    // Get the initial zoom factor
    public double getInitialZoomFactor() {
        return initialZoomFactor;
    }

    // Get the maze image
    public BufferedImage getMazeImage() {
        return mazeImage;
    }

    // Set the maze image
    public void setMazeImage(BufferedImage mazeImage) {
        this.mazeImage = mazeImage;
        mazePanel.repaint();
    }

    // Get the maze panel
    public JPanel getMazePanel() {
        return mazePanel;
    }

    public void setDataArray(DataArray dataArray) {
        this.dataArray = dataArray;
    }

    public DataArray getDataArray() {
        return dataArray;
    }
}