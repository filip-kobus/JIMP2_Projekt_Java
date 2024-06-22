package GUI;

import FileIO.FileIO;
import Algorithm.DataArray;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainGuiPanel {
    private MazeRenderer mazeRenderer; // Maze
    private JFrame window;
    private JMenuBar menuBar;
    private JScrollPane scrollPane; // Scroll panel

    private DataArray dataArray; // DataArray


    private static final double ZOOM_IN_FACTOR = 1.1;
    private static final double ZOOM_OUT_FACTOR = 0.9;

    // Method of creating the main panel
    public void run() {
        CreateMainPanel();
        CreateMazePanel();
        CreateFileReaderBar();
        createZoomControls();
        createOptionsBar();

        JTabbedPane tabPanel = new JTabbedPane();
        tabPanel.addTab("Maze", scrollPane);
        window.add(tabPanel, BorderLayout.CENTER);
        window.setJMenuBar(menuBar);
        window.setVisible(true);
    }

    // Method of creating the main panel
    public void CreateMainPanel() {
        window = new JFrame("Maze Solver - Kobus&Dutkiewicz");
        window.setIconImage(new ImageIcon("src/gallery/logo.png").getImage());
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(800, 600);
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (mazeRenderer != null && mazeRenderer.getMazeImage() != null) {
                    fitMazeToWindow();
                }
            }
        });
    }

    // Method of creating the maze panel
    public void CreateMazePanel() {
        mazeRenderer = new MazeRenderer(null); // Creating a maze renderer
        JPanel mazePanel = mazeRenderer.createMazePanel();

        // Adding a mouse wheel listener
        attachMouseWheelListener(mazePanel);
        attachMouseListener(mazePanel);

        // Adding a scroll panel
        configureScrollPane(mazePanel);
    }

    // Method of adding a mouse wheel listener for zooming
    private void attachMouseWheelListener(JPanel mazePanel) {
        mazePanel.addMouseWheelListener(e -> {
            double zoomFactor = mazeRenderer.getZoomFactor();
            zoomFactor *= e.getPreciseWheelRotation() < 0 ? ZOOM_IN_FACTOR : ZOOM_OUT_FACTOR;
            mazeRenderer.setZoomFactor(zoomFactor);
            updateZoom();
        });
    }

    // Method of adding a mouse listener for selecting a cell
    private void attachMouseListener(JPanel mazePanel) {
        mazePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mazeRenderer.getMazeImage() == null) return;

                int x = e.getX();
                int y = e.getY();
                int panelWidth = mazePanel.getWidth();
                int panelHeight = mazePanel.getHeight();

                int scaledWidth = (int) (mazeRenderer.getMazeImage().getWidth() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());
                int scaledHeight = (int) (mazeRenderer.getMazeImage().getHeight() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());

                int offsetX = (panelWidth - scaledWidth) / 2;
                int offsetY = (panelHeight - scaledHeight) / 2;

                // Check if the click is within the maze
                if (x >= offsetX && x < offsetX + scaledWidth && y >= offsetY && y < offsetY + scaledHeight) {
                    int imageX = (int) ((x - offsetX) / (mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor()));
                    int imageY = (int) ((y - offsetY) / (mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor()));

                    // Handle the cell selection
                    MazeUtilities.handleMazeCellSelection(mazeRenderer, imageX, imageY, window);
                }
            }
        });
    }

    // Method of configuring the scroll panel
    private void configureScrollPane(JPanel mazePanel) {
        scrollPane = new JScrollPane(mazePanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    }

    // Method of updating the zoom
    private void updateZoom() {
        if (mazeRenderer != null && mazeRenderer.getMazeImage() != null) {
            int newWidth = (int) (mazeRenderer.getMazeImage().getWidth() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());
            int newHeight = (int) (mazeRenderer.getMazeImage().getHeight() * mazeRenderer.getInitialZoomFactor() * mazeRenderer.getZoomFactor());

            // Setting the new size of the maze panel
            mazeRenderer.getMazePanel().setPreferredSize(new Dimension(newWidth, newHeight));
            mazeRenderer.getMazePanel().revalidate();

            // Updating the scroll pane
            scrollPane.revalidate();
            scrollPane.repaint();
        }
    }

    // Method of creating the file reader bar
    public void CreateFileReaderBar() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        // Elements of menu
        JMenuItem openItem = new JMenuItem("Open maze");
        JMenuItem saveItem = new JMenuItem("Save maze (txt)");
        JMenuItem saveImageItem = new JMenuItem("Save maze (image)");

        // Adding actions to the menu
        openItem.addActionListener(e -> openMaze());

        // Adding actions to save the maze as text
        saveItem.addActionListener(e -> FileIO.saveMazeAsText(mazeRenderer.getMazeImage(), window));

        // Adding actions to save the maze as an image
        saveImageItem.addActionListener(e -> FileIO.saveMazeAsImage(dataArray, window));

        // Adding elements to the menu
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveImageItem);

        // Adding the menu to the menu bar
        menuBar.add(fileMenu);
    }

    // Method of opening the maze
    private void openMaze() {
        try {
            File mazeFile = FileIO.openMazeFile(window);
            if (mazeFile != null) {
                BufferedImage image = FileIO.prepareFile(mazeFile);
                mazeRenderer.setMazeImage(image);
                dataArray = FileIO.getDataArray(); // Ustawia DataArray
                mazeRenderer.setDataArray(dataArray); // Przekazuje DataArray do mazeRenderer
                fitMazeToWindow();
                MazeUtilities.checkForEntranceAndExit(mazeRenderer, window);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(window, "The maze could not be loaded: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method of creating the zoom controls
    private void createZoomControls() {
        JPanel zoomPanel = new JPanel(new GridLayout(2, 1));
        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");

        zoomInButton.addActionListener(e -> {
            mazeRenderer.setZoomFactor(mazeRenderer.getZoomFactor() * 1.1);
            updateZoom();
        });

        zoomOutButton.addActionListener(e -> {
            mazeRenderer.setZoomFactor(mazeRenderer.getZoomFactor() / 1.1);
            updateZoom();
        });

        zoomPanel.add(zoomInButton);
        zoomPanel.add(zoomOutButton);
        window.add(zoomPanel, BorderLayout.EAST);
    }

    // Method of fitting the maze to the window
    private void fitMazeToWindow() {
        if (mazeRenderer != null && mazeRenderer.getMazeImage() != null) {
            int windowWidth = scrollPane.getViewport().getWidth();
            int windowHeight = scrollPane.getViewport().getHeight();

            double windowRatio = (double) windowWidth / windowHeight;
            double imageRatio = (double) mazeRenderer.getMazeImage().getWidth() / mazeRenderer.getMazeImage().getHeight();

            // Calculate the initial zoom factor
            double initialZoomFactor = (windowRatio > imageRatio) ? (double) windowHeight / mazeRenderer.getMazeImage().getHeight()
                    : (double) windowWidth / mazeRenderer.getMazeImage().getWidth();

            mazeRenderer.setInitialZoomFactor(initialZoomFactor);
            mazeRenderer.setZoomFactor(1.0);
            updateZoom();
        }
    }

    // Method of creating the options bar
    private void createOptionsBar() {
        JMenu optionsMenu = new JMenu("Options");

        JMenuItem setEntranceExitItem = new JMenuItem("Set entrance and exit");
        JMenuItem solveMazeItem = new JMenuItem("Find the shortest path(BFS)");
        JMenuItem visualizeMazeItem = new JMenuItem("Visualize finding path(DFS)");
        JMenuItem resetPathsItem = new JMenuItem("Reset paths");
        JMenuItem stopVisualizationItem = new JMenuItem("Stop visualization");
        stopVisualizationItem.setVisible(false); // Ukryj na początku

        String erorrMessage = "There is no maze to be solved. Please open a maze file.";

        setEntranceExitItem.addActionListener(e -> {
            if (mazeRenderer.getMazeImage() != null) {
                MazeUtilities.setSelectedState(1);
                MazeUtilities.resetEntranceAndExit(mazeRenderer, window);

                JOptionPane.showMessageDialog(window, "Select the start of the maze", "Set entrance and exit", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(window, erorrMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        solveMazeItem.addActionListener(e ->
        {
            if (dataArray != null) {
                mazeRenderer.solveMazeWithBfs(dataArray);
            } else {
                JOptionPane.showMessageDialog(window, erorrMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        visualizeMazeItem.addActionListener(e -> {
            if (dataArray != null) {
                stopVisualizationItem.setVisible(true);
                mazeRenderer.visualizeDfs(dataArray);

            } else {
                JOptionPane.showMessageDialog(window, erorrMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        resetPathsItem.addActionListener(e -> {
            if (dataArray != null) {
                mazeRenderer.stopVisualization(); // Stop the visualization
                mazeRenderer.resetPaths(dataArray);
                stopVisualizationItem.setVisible(false); // Hide the stop option
            } else {
                JOptionPane.showMessageDialog(window, erorrMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        stopVisualizationItem.addActionListener(e -> {
            mazeRenderer.stopVisualization();
            stopVisualizationItem.setVisible(false); // Hide the stop option
        });

        optionsMenu.add(setEntranceExitItem);
        optionsMenu.add(solveMazeItem);
        optionsMenu.add(visualizeMazeItem);
        optionsMenu.add(resetPathsItem);
        optionsMenu.add(stopVisualizationItem); // Add the stop option

        menuBar.add(optionsMenu);
    }
}