package Algorithm;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmDfsTest {

    @Test
    void makeMove() {
        String fileName = "maze.txt";
        File file = new File(fileName);
        DataArray dataArray;
        try {
            dataArray = readMazeFromFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AlgorithmDfs algorytm = new AlgorithmDfs(dataArray);
        //dopoki algorytm zwraca false czyli nie znalazl wyjscia
        while(!algorytm.makeMove()) {
            //co iteracje pobieram z algorytmu kolejny punkt
            Point currCell = algorytm.getMove();

            //wypisywanie na potrzeby testu
            algorytm.dataArray.switchPoint(currCell);
            algorytm.dataArray.printMatrix();
            System.out.println();
        }
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