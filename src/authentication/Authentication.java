package authentication;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Authentication {
    private Map<String, String> userDatabase;
    private Map<String, String> captchaDatabase;

    public Authentication() {
        userDatabase = new HashMap<>();
        captchaDatabase = new HashMap<>();
        loadUserDataFromFile();
    }

    private void loadUserDataFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("user_data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|<>\\|");  // Using custom delimiter for split
                if (parts.length == 2) {
                    userDatabase.put(decode(parts[0]), decode(parts[1]));  // Decoding the parts
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUserDataToFile(String username, String password) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("user_data.txt", true))) {
            writer.println(encode(username) + "|<>|" + encode(password));  // Using custom delimiter for saving
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeCaptcha(String username, String captcha) {
        captchaDatabase.put(username, captcha);
        saveCaptchaToFile(username, captcha); // Saving captcha to file
    }

    private void saveCaptchaToFile(String username, String captcha) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("captcha_data.txt", true))) {
            writer.println(encode(username) + "|<>|" + encode(captcha));  // Using custom delimiter for saving
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String generateCaptcha() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            captcha.append(characters.charAt(random.nextInt(characters.length())));
        }
        return captcha.toString();
    }

    private String encode(String data) {
        return new StringBuilder(data).reverse().toString();  // Encoding by reversing the string
    }

    private String decode(String data) {
        return new StringBuilder(data).reverse().toString();  // Decoding by reversing the string
    }

    public void register(String username, String password) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword != null) {
            userDatabase.put(username, hashedPassword);
            saveUserDataToFile(username, hashedPassword);
        }
    }

    public boolean authenticate(String username, String password, String captcha) {
        if (userDatabase.containsKey(username)) {
            String storedPassword = userDatabase.get(username);
            if (storedPassword.equals(hashPassword(password))) {
                if (captchaDatabase.containsKey(username)) {
                    String storedCaptcha = captchaDatabase.get(username);
                    return storedCaptcha.equals(captcha);
                }
            }
        }
        return false;
    }
}
