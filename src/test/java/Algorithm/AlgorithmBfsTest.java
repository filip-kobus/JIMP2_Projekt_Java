package Algorithm;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

class AlgorithmBfsTest {

    @Test
    public void runAlgorithm() {
        String fileName = "maze.txt";
        File file = new File(fileName);
        DataArray dataArray;
        try {
            dataArray = readMazeFromFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AlgorithmBfs algorytm = new AlgorithmBfs(dataArray);
        algorytm.printPathToArray();
        dataArray.printMatrix();
    }

    public static DataArray readMazeFromFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        int rows = lines.size();
        int cols = lines.isEmpty() ? 0 : lines.get(0).length();

        DataArray dataArray = new DataArray(cols, rows);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Point currPoint = new Point(x, y);
                char ch = lines.get(y).charAt(x);
                currPoint.setTypeByChar(ch);
                dataArray.putPointIntoArray(currPoint);
            }
        }

        return dataArray;
    }
}
