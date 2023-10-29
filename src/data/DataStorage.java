package data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    private String fileName;

    public DataStorage(String fileName) {
        this.fileName = fileName;
    }

    public void writeToDataFile(String data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, true))) {
            writer.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> readFromDataFile() {
        List<String> dataLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dataLines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataLines;
    }

    // Optional: Method to clear the content of the file
    public void clearDataFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, false))) {
            writer.print("");  // Clearing file content
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
