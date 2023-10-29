import authentication.Authentication;
import schema.Schema;
import query.Query;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Authentication authenticator = new Authentication();
        Scanner scanner = new Scanner(System.in);

        // Authentication
        while (true) {
            System.out.println("Type 'register' to register, 'login' to authenticate, or 'exit' to quit:");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                return;
            } else if (input.equalsIgnoreCase("register")) {
                System.out.print("Enter new username: ");
                String username = scanner.nextLine();

                System.out.print("Enter new password: ");
                String password = scanner.nextLine();

                authenticator.register(username, password);
                System.out.println("Registration successful.");
            } else if (input.equalsIgnoreCase("login")) {
                System.out.print("Enter username: ");
                String username = scanner.nextLine();

                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                String captcha = authenticator.generateCaptcha();
                authenticator.storeCaptcha(username, captcha);
                System.out.println("Captcha: " + captcha);
                System.out.print("Enter captcha: ");
                String userCaptcha = scanner.nextLine();

                if (authenticator.authenticate(username, password, userCaptcha)) {
                    System.out.println("Authentication successful.");
                    handleSchema(scanner);
                } else {
                    System.out.println("Authentication failed.");
                }
            } else {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private static void handleSchema(Scanner scanner) {
        Schema schema = new Schema();
        System.out.println("Type 'create' to create a table or 'use' to use an existing table:");

        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("create")) {
                System.out.print("Enter table name to create: ");
                String tableName = scanner.nextLine();
                schema.createTable(tableName);
                System.out.println("Table created.");

            } else if (input.equalsIgnoreCase("use")) {
                System.out.print("Enter table name: ");
                String tableName = scanner.nextLine();
                Query query = new Query(tableName);
          
                handleQueries(scanner, query);
                
            } else if (input.equalsIgnoreCase("exit")) {
                return;
            } else {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private static void handleQueries(Scanner scanner, Query query) {
        System.out.println("Enter your SQL-like queries. Type 'EXIT' to quit.");

        while (true) {
            System.out.print(">> ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                return;
            }

            try {
                query.executeQuery(userInput);
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }
}
