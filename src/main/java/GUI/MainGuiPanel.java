package GUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class MainGuiPanel implements GUIInterface {
    private JPanel mazePanel; // Panel do wyświetlania labiryntu
    private JTextPane mazeArea; // JTextPane do pokazywania labiryntu z formatowaniem HTML
    private JFrame window;
    private JMenuBar menuBar;

    public void run() {
        CreateMainPanel();

        // Utworzenie JTabbedPane do przechowywania zakładek
        JTabbedPane tabPanel = new JTabbedPane();
        CreateMazePanel();
        tabPanel.addTab("Labirynt", mazePanel);
        window.add(tabPanel, BorderLayout.CENTER);

        CreateFileReaderBar();
        window.setJMenuBar(menuBar);

        // Wyświetlenie głównego okna
        window.setVisible(true);
    }

    @Override
    public void CreateMainPanel() {
        window = new JFrame("Maze Solver - Kobus&Dutkiewicz");
        ImageIcon img = new ImageIcon("src/gallery/logo.png");
        window.setIconImage(img.getImage());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 600);
    }

    // Utworzenie zakładki do wyświetlania labiryntu
    @Override
    public void CreateMazePanel() {
        mazePanel = new JPanel();
        mazePanel.setLayout(new BorderLayout());

        // Utworzenie JTextPane do wyświetlania labiryntu z formatowaniem HTML
        mazeArea = new JTextPane();
        mazeArea.setContentType("text/html");
        mazeArea.setEditable(false);
        mazeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Początkowe wyświetlenie wiadomości w obszarze labiryntu
        mazeArea.setText("<html><body>Labirynt zostanie wyświetlony tutaj.</body></html>");
        JScrollPane scrollPane = new JScrollPane(mazeArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Dodanie marginesu
        mazePanel.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void CreateFileReaderBar() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        JMenuItem openItem = new JMenuItem("Otwórz labirynt");

        openItem.addActionListener(e -> {
            try {
                displayMaze(Objects.requireNonNull(openMazeFile()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (NullPointerException nu) {
                //kiedy plik nie został wybrany nie rób nic
            }
        });

        fileMenu.add(openItem);
        menuBar.add(fileMenu);
    }

    // Metoda do otwierania pliku i odczytu labiryntu
    public File openMazeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz plik labiryntu");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki tekstowe", "txt"));

        int result = fileChooser.showOpenDialog(window);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    // Metoda do wyświetlania labiryntu z pliku
    private void displayMaze(File mazeFile) throws IOException {
        List<String> lines = Files.readAllLines(mazeFile.toPath());
        StringBuilder htmlMaze = new StringBuilder("<html><body style='font-family: monospace; font-size: 12px;'><pre>");

        for (String line : lines) {
            for (char ch : line.toCharArray()) {
                switch (ch) {
                    case 'X':  // Ściana
                        htmlMaze.append("<span style='color: black; background-color: #808080;'>").append("  ").append("</span>");
                        break;
                    case ' ':  // Scieżka
                        htmlMaze.append("<span style='color: black; background-color: white;'>").append("  ").append("</span>");
                        break;
                    case 'P':  // Wejście
                        htmlMaze.append("<span style='color: white; background-color: green;'>").append("P ").append("</span>");
                        break;
                    case 'K':  // Wyjście
                        htmlMaze.append("<span style='color: white; background-color: red;'>").append(" K").append("</span>");
                        break;
                    default:   // Domyślnie, zachowaj tekstowy charakter labiryntu
                        htmlMaze.append(ch);
                        break;
                }
            }
            htmlMaze.append("<br>"); // HTML line break for the next line of the maze
        }
        htmlMaze.append("</pre></body></html>");
        mazeArea.setText(htmlMaze.toString());

        // Aktualizacja panelu z labiryntem
        mazePanel.revalidate();
        mazePanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new MainGuiPanel()::run);
    }
}
