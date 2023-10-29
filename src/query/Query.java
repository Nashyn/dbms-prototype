
package query;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Query {
    private String tableName;
    private final static String DIRECTORY = "tables"; // directory to store table files

    private List<String> transactionBuffer;
    private boolean inTransaction;

    public Query(String tableName) {
        this.tableName = tableName;
        this.transactionBuffer = new ArrayList<>();
        this.inTransaction = false;
    }

    public void executeQuery(String query) {
        if (query == null || query.isEmpty()) {
            System.out.println("Invalid query.");
            return;
        }

        // Use regex to identify the query type
        Pattern pattern = Pattern.compile("(CREATE|SELECT|INSERT|UPDATE|DELETE|BEGIN TRANSACTION|COMMIT|ROLLBACK).*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.matches()) {
            String queryType = matcher.group(1).toUpperCase();

            if(inTransaction && !("COMMIT".equals(queryType) || "ROLLBACK".equals(queryType))) {
                transactionBuffer.add(query);
                System.out.println("Query buffered: " + query);
                return;
            }

            switch (queryType) {
                case "CREATE":
                    createTable();
                    break;
                case "SELECT":
                    selectAll();
                    break;
                case "INSERT":
                    insertRow(query);
                    break;
                case "UPDATE":
                    updateRow(query);
                    break;
                case "DELETE":
                    deleteRow(query);
                    break;
                case "BEGIN TRANSACTION":
                    beginTransaction();
                    break;
                case "COMMIT":
                    commitTransaction();
                    break;
                case "ROLLBACK":
                    rollbackTransaction();
                    break;
                default:
                    System.out.println("Unsupported query type: " + queryType);
            }
        } else {
            System.out.println("Invalid query format.");
        }
    }

    public void createTable() {
        File tableFile = new File(DIRECTORY + File.separator + tableName + ".txt");
        if (!tableFile.exists()) {
            try {
                tableFile.createNewFile();
                System.out.println("Table " + tableName + " created.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Table already exists.");
        }
    }

    public void selectAll() {
        try (BufferedReader reader = new BufferedReader(new FileReader('.' + File.separator + tableName + ".txt"))) {
            List<String> results = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
            if (results.isEmpty()) {
                System.out.println("Table is empty.");
            } else {
                for (String row : results) {
                    System.out.println(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public void insertRow(String query) {
        // Parse the INSERT query to extract values
        Pattern pattern = Pattern.compile("INSERT INTO " + tableName + " VALUES\\('(.+)', (\\d+), '(.+)'\\);", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
    
        if (matcher.matches()) {
            String name = matcher.group(1);
            int year = Integer.parseInt(matcher.group(2));
            String address = matcher.group(3);
            String rowData = name + "," + year + "," + address;
    
            // Use DIRECTORY when writing to the file
            try (PrintWriter writer = new PrintWriter(new FileWriter('.' + File.separator + tableName + ".txt", true))) {
                writer.println(rowData);
                System.out.println("Row inserted in " + tableName + ".");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid INSERT query format.");
        }
    }
    
    public void updateRow(String query) {
        // Parse the UPDATE query to extract values
        Pattern pattern = Pattern.compile("UPDATE\\s+" + tableName + "\\s+SET\\s+year=\\s*(\\d+)\\s+WHERE\\s+name\\s*=\\s*'(.+)';", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.matches()) {
            int newAge = Integer.parseInt(matcher.group(1));
            String name = matcher.group(2);

            File inputFile = new File('.' + File.separator + tableName + ".txt");
            File tempFile = new File('.' + File.separator + "temp.txt");

            try {
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

                String line;
                boolean updated = false;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    String[] parts = line.split(",");
                    if (parts.length == 3 && parts[0].equals(name)) {
                        writer.println(name + "," + newAge);
                        updated = true;
                    } else {
                        writer.println(line);
                    }
                }
                tempFile.renameTo(inputFile);
                reader.close();
                writer.close();
                

                if (updated) {
                    System.out.println("Row updated in " + tableName + ".");
                } else {
                    System.out.println("No rows matched the UPDATE criteria.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid UPDATE query format.");
        }
    }

    public void deleteRow(String query) {
        // Parse the DELETE query to extract values
        Pattern pattern = Pattern.compile("DELETE FROM " + tableName + " WHERE name='(.+)';", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.matches()) {
            String name = matcher.group(1);

            File inputFile = new File('.' + File.separator + tableName + ".txt");
            File tempFile = new File('.' + File.separator + "temp.txt");

            try {
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                PrintWriter writer = new PrintWriter(new FileWriter(tempFile, true));

                String line;
                boolean deleted = false;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3 && parts[0].equals(name)) {
                        deleted = true;
                        continue;
                    }
                    writer.println(line);
                }
                reader.close();
                writer.close();
                tempFile.renameTo(inputFile);

                if (deleted) {
                    System.out.println("Row deleted from " + tableName + ".");
                } else {
                    System.out.println("No rows matched the DELETE criteria.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid DELETE query format.");
        }
    }
    private void beginTransaction() {
        transactionBuffer.clear();
        inTransaction = true;
        System.out.println("Transaction started.");
    }

    private void commitTransaction() {
        if (!transactionBuffer.isEmpty()) {
            for (String query : transactionBuffer) {
                Pattern pattern = Pattern.compile("(CREATE|SELECT|INSERT|UPDATE|DELETE).*", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(query);
                if (matcher.matches()) {
                    String queryType = matcher.group(1).toUpperCase();
                    switch (queryType) {
                        case "CREATE":
                            createTable();
                            break;
                        case "SELECT":
                            selectAll();
                            break;
                        case "INSERT":
                            insertRow(query);
                            break;
                        case "UPDATE":
                            updateRow(query);
                            break;
                        case "DELETE":
                            deleteRow(query);
                            break;
                        default:
                            System.out.println("Unsupported query type: " + queryType);
                    }
                }
            }
            transactionBuffer.clear();
        }
        inTransaction = false;
        System.out.println("Transaction committed.");
    }

    private void rollbackTransaction() {
        transactionBuffer.clear();
        inTransaction = false;
        System.out.println("Transaction rolled back.");
    }
}

