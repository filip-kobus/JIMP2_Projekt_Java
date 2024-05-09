import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MainGuiPanel {
    private JFrame window;
    private JPanel mazePanel; // Panel do wyświetlania labiryntu
    private JTextPane mazeArea; // JTextPane do pokazywania labiryntu z formatowaniem HTML

    public void run() {
        // Inicjalizacja głównego okna
        window = new JFrame("Maze Solver - Kobus&Dutkiewicz");
        ImageIcon img = new ImageIcon("src/gallery/logo.png");
        window.setIconImage(img.getImage());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 600);

        // Utworzenie JTabbedPane do przechowywania zakładek
        JTabbedPane tabPanel = new JTabbedPane();

        // Utworzenie pierwszej zakładki i dodanie etykiety jako symbolu miejsca
        JPanel page1 = new JPanel();
        page1.setLayout(new BorderLayout());
        page1.add(new JLabel("To jest zakładka 1", JLabel.CENTER));

        // Utworzenie drugiej zakładki i dodanie etykiety jako symbolu miejsca
        JPanel page2 = new JPanel();
        page2.setLayout(new BorderLayout());
        page2.add(new JLabel("To jest zakładka 2", JLabel.CENTER));

        // Utworzenie trzeciej zakładki do wyświetlania labiryntu
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

        // Dodanie trzech zakładek do JTabbedPane
        tabPanel.addTab("Zakładka 1", page1);
        tabPanel.addTab("Zakładka 2", page2);
        tabPanel.addTab("Labirynt", mazePanel);

        // Dodanie panelu zakładek do głównego panelu okna
        window.add(tabPanel, BorderLayout.CENTER);

        // Ustawienie paska menu z operacjami na plikach
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        JMenuItem openItem = new JMenuItem("Otwórz labirynt");

        openItem.addActionListener(e -> openMazeFile());
        fileMenu.add(openItem);
        menuBar.add(fileMenu);
        window.setJMenuBar(menuBar);

        // Wyświetlenie głównego okna
        window.setVisible(true);
    }

    // Metoda do otwierania pliku i odczytu labiryntu
    private void openMazeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz plik labiryntu");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki tekstowe", "txt"));

        int result = fileChooser.showOpenDialog(window);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                displayMaze(selectedFile);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Nie udało się odczytać pliku: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Metoda do wyświetlania labiryntu z pliku
    private void displayMaze(File mazeFile) throws IOException {
        List<String> lines = Files.readAllLines(mazeFile.toPath());
        StringBuilder htmlMaze = new StringBuilder("<html><body style='font-family: monospace; font-size: 12px;'><pre>");

        for (String line : lines) {
            for (char ch : line.toCharArray()) {
                switch (ch) {
                    case 'X':  // Ściana
                        htmlMaze.append("<span style='color: black; background-color: #808080;'>").append(' ').append("</span>");
                        break;
                    case ' ':  // Scieżka
                        htmlMaze.append("<span style='color: black; background-color: white;'>").append(' ').append("</span>");
                        break;
                    case 'P':  // Wejście
                        htmlMaze.append("<span style='color: white; background-color: green;'>").append(ch).append("</span>");
                        break;
                    case 'K':  // Wyjście
                        htmlMaze.append("<span style='color: white; background-color: red;'>").append(ch).append("</span>");
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
