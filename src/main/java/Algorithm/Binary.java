package Algorithm;
import java.io.*;

public class Binary {


    // Method for reading 2 bytes from a binary file in little-endian order
    public static int readUnsignedShortLittleEndian(DataInputStream dis) throws IOException {
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();
        return (b2 << 8) | b1;
    }

    // Method for reading 4 bytes from a binary file in little-endian order
    public static long readUnsignedIntLittleEndian(DataInputStream dis) throws IOException {
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();
        int b3 = dis.readUnsignedByte();
        int b4 = dis.readUnsignedByte();
        return ((long) b4 << 24) | ((long) b3 << 16) | (b2 << 8) | b1;
    }

    public static void convertBinaryToText(String binaryFilePath, String textFilePath) throws IOException {


        try (DataInputStream binaryFile = new DataInputStream(new FileInputStream(binaryFilePath));
             BufferedWriter textFile = new BufferedWriter(new FileWriter(textFilePath))) {


            // Loading the header
            long fileId = readUnsignedIntLittleEndian(binaryFile);
            int escape = binaryFile.readUnsignedByte();
            int columns = readUnsignedShortLittleEndian(binaryFile);
            int lines = readUnsignedShortLittleEndian(binaryFile);
            int entryX = readUnsignedShortLittleEndian(binaryFile);
            int entryY = readUnsignedShortLittleEndian(binaryFile);
            int exitX = readUnsignedShortLittleEndian(binaryFile);
            int exitY = readUnsignedShortLittleEndian(binaryFile);

            binaryFile.skipBytes(12); // Skipping the rest of the header


            // Loading the encoding
            long counter = readUnsignedIntLittleEndian(binaryFile);
            long solutionOffset = readUnsignedIntLittleEndian(binaryFile);
            int separator = binaryFile.readUnsignedByte();
            int wall = binaryFile.readUnsignedByte();
            int path = binaryFile.readUnsignedByte();



            if (solutionOffset > 0) { // If the solution offset is greater than 0, we need to skip the solution part
                // Skip the solution part
                binaryFile.skipBytes((int) (solutionOffset - 40));
            }

            // Generating the maze from the encoding
            generateMazeFromEncoding(binaryFile, textFile, counter, separator, wall, path, columns, lines, entryX, entryY, exitX, exitY);

            System.out.println("Konwertowanie zakończone. Plik znajduje się w: " + textFilePath);
        }
    }

    private static void generateMazeFromEncoding(DataInputStream binaryFile, BufferedWriter textFile, long wordCount, int separator, int wall, int path, int cols, int rows, int entryX, int entryY, int exitX, int exitY) throws IOException {
        int cellsProcessed = 0; // Counter of processed cells
        int currentRow = 1, currentCol = 1; // Current row and column

    // Loop for reading the encoding
        while (wordCount-- > 0 && cellsProcessed < cols * rows) {
            int byteRead = binaryFile.readUnsignedByte(); // Read separator
            if (byteRead != separator) {
                throw new IOException("Oczekiwano separatora, otrzymano: " + String.format("%02x", byteRead));
            }

            int value = binaryFile.readUnsignedByte(); // Load the value
            int countByte = binaryFile.readUnsignedByte(); // Load the number of cells

            for (int i = 0; i < countByte + 1 && cellsProcessed < cols * rows; i++) {
                // Check if the cell is the entry or exit
                if (currentRow == entryY && currentCol == entryX) {
                    textFile.write('P');
                } else if (currentRow == exitY && currentCol == exitX) {
                    textFile.write('K');
                } else {
                    textFile.write(value == wall ? 'X' : ' ');
                }

                cellsProcessed++;
                currentCol++;
                if (currentCol > cols) { // if the column is greater than the number of columns, we need to move to the next row
                    textFile.newLine();
                    currentCol = 1;
                    currentRow++;
                }
            }
        }
    }


}
