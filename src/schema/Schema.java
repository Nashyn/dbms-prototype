package schema;

import query.Query;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schema {
    private Map<String, Query> tables;
    private final static String DIRECTORY = "tables"; // directory to store table files

    public Schema() {
        this.tables = new HashMap<>();
        new File(DIRECTORY).mkdirs(); // create a directory for tables if it doesn't exist
    }

    public void createTable(String query) {
        Pattern pattern = Pattern.compile("CREATE TABLE (\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            File tableFile = new File(DIRECTORY + File.separator + tableName + ".txt");
            
            if (!tables.containsKey(tableName)) { 
                if (!tableFile.exists()) {
                    try {
                        tableFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return; // Exit if there's an exception to prevent further execution
                    }
                }
                Query tableQuery = new Query(tableName);
                tableQuery.executeQuery(query);
                tables.put(tableName, tableQuery);
                System.out.println("Table " + tableName + " created.");
            } else {
                System.out.println("Table already exists.");
            }
        } else {
            System.out.println("Invalid CREATE TABLE syntax.");
        }
    }

    public void executeQuery(String tableName, String query) {
        if (tables.containsKey(tableName)) {
            Query tableQuery = tables.get(tableName);
            tableQuery.executeQuery(query);
        } else {
            System.out.println("Table does not exist.");
        }
    }
}
