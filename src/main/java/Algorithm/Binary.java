package Algorithm;
import java.io.*;

public class Binary {


    // Metoda do odczytywania 2 bajtów z pliku binarnego w kolejności little-endian
    public static int readUnsignedShortLittleEndian(DataInputStream dis) throws IOException {
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();
        return (b2 << 8) | b1;
    }

    // Metoda do odczytywania 4 bajtów z pliku binarnego w kolejności little-endian
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


            // Wczytanie nagłówka pliku binarnego
            long fileId = readUnsignedIntLittleEndian(binaryFile);
            int escape = binaryFile.readUnsignedByte();
            int columns = readUnsignedShortLittleEndian(binaryFile);
            int lines = readUnsignedShortLittleEndian(binaryFile);
            int entryX = readUnsignedShortLittleEndian(binaryFile);
            int entryY = readUnsignedShortLittleEndian(binaryFile);
            int exitX = readUnsignedShortLittleEndian(binaryFile);
            int exitY = readUnsignedShortLittleEndian(binaryFile);

            binaryFile.skipBytes(12); // Pominięcie 12 bajtów


            // Wczytanie reszty nagłówka
            long counter = readUnsignedIntLittleEndian(binaryFile);
            long solutionOffset = readUnsignedIntLittleEndian(binaryFile);
            int separator = binaryFile.readUnsignedByte();
            int wall = binaryFile.readUnsignedByte();
            int path = binaryFile.readUnsignedByte();


            if (solutionOffset > 0) { // Jeśli istnieje rozwiązanie
                // Przesunięcie wskaźnika pliku na początek kodowania
                binaryFile.skipBytes((int) (solutionOffset - 40));
            }

            // Generowanie labiryntu na podstawie kodowania
            generateMazeFromEncoding(binaryFile, textFile, counter, separator, wall, path, columns, lines, entryX, entryY, exitX, exitY);


        }
    }

    private static void generateMazeFromEncoding(DataInputStream binaryFile, BufferedWriter textFile, long wordCount, int separator, int wall, int path, int cols, int rows, int entryX, int entryY, int exitX, int exitY) throws IOException {
        int cellsProcessed = 0; // Licznik przetworzonych komórek
        int currentRow = 1, currentCol = 1; // Aktualne współrzędne


    // Pętla wczytująca kolejne słowa kodowe
        while (wordCount-- > 0 && cellsProcessed < cols * rows) {
            int byteRead = binaryFile.readUnsignedByte(); // Read separator
            if (byteRead != separator) {
                throw new IOException("Oczekiwano separatora, otrzymano: " + String.format("%02x", byteRead));
            }

            int value = binaryFile.readUnsignedByte(); // Wczytanie wartości
            int countByte = binaryFile.readUnsignedByte(); // Wczytanie liczby powtórzeń

            for (int i = 0; i < countByte + 1 && cellsProcessed < cols * rows; i++) {
                // Sprawdzenie czy aktualna komórka to punkt wejścia lub wyjścia
                if (currentRow == entryY && currentCol == entryX) {
                    textFile.write('P');
                } else if (currentRow == exitY && currentCol == exitX) {
                    textFile.write('K');
                } else { // Sprawdzenie czy komórka to ściana czy ścieżka
                    textFile.write(value == wall ? 'X' : ' ');
                }

                cellsProcessed++;
                currentCol++;
                if (currentCol > cols) { // jeśli przekroczono liczbę kolumn to przejdź do nowego wiersza
                    textFile.newLine();
                    currentCol = 1;
                    currentRow++;
                }
            }
        }
    }


}
