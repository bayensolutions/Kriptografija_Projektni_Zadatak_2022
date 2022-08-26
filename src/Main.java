import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class Main {

    public static final String usersPath = "resources/users/users.txt";
    public static final String certificatesPath = "resources/certificates";
    public static final String resultsPath = "resources/results/results.txt";

    public static void main(String[] args) throws Exception {
        Quiz.hideQuestions();
        createMenu();
    }

    public static void createMenu() throws Exception {
        boolean isExit = false;

        while (!isExit) {
            System.out.println("*********************************");
            System.out.println("* Choose the option:            *");
            System.out.println("* 1. Register                   *");
            System.out.println("* 2. Login                      *");
            System.out.println("* 3. Exit                       *");
            System.out.println("*********************************");
            System.out.print("Your selection: ");
            Scanner s = new Scanner(System.in);
            int option = s.nextInt();

            switch (option) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    System.out.println("Goodbye.");
                    isExit = true;
                default:
                    System.out.println("You chose non-existent option. Select 1, 2 or 3.");
            }
        }
    }

    public static void register() throws Exception {
        System.out.println("REGISTRATION");
        Scanner entry = new Scanner(System.in);

        System.out.print("Input username: ");
        String username = entry.nextLine();
        if (username.length() < 8) {
            System.out.println("The username must contain at least 8 characters.");
            return;
        }

        System.out.print("Input password: ");
        String password = entry.nextLine();

        Path of = Path.of(usersPath);
        List<String> lines = Files.readAllLines(of, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (username.equals(line.split("@")[0])) {
                System.out.println("Username " + username + " is already used. Please try with the another one.");
                break;
            }
        }
        Cryptography.generateCertificate(username, password);
        Files.write(of, (username + "@" + getHash(username, password) + "\n").getBytes(), StandardOpenOption.APPEND);
    }

    public static void login() {
        Scanner entry = new Scanner(System.in);

        System.out.print("Input username: ");
        String username = entry.nextLine();
        System.out.print("Input password: ");
        String password = entry.nextLine();

        try {
            String hashInFile = null;
            boolean usernameExists = false;
            Path of = Path.of(usersPath);
            List<String> lines = Files.readAllLines(of, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (username.equals(line.split("@")[0])) {
                    usernameExists = true;
                    hashInFile = line.split("@")[1];
                    break;
                }
            }
            if (!usernameExists) {
                System.out.println("Username " + username + " doesn't exist in database.");
                return;
            }
            if (!getHash(username, password).equals(hashInFile)) {
                System.out.println("The entered password is incorrect.");
                return;
            }

            User user = new User(username, password);
            if (user.getQuizPlayed() < 3) {
                user.incrementQuizPlayed();
                System.out.println("BROJ UCESCA: " + user.getQuizPlayed());
                System.out.print("Welcome to quiz " + username + ". Good luck!\n");
                new Quiz().startQuiz(username);
            } else {
                System.out.println("You have already taken the quiz 3 times!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getHash(String username, String password) throws IOException {
        String salt = "";
        for (int i = 0; i < 2; i++) {
            salt += username;
        }
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "openssl passwd -5 -salt " + salt + " " + password);
        builder.redirectErrorStream(true);
        builder.directory(new File(certificatesPath));
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        return r.readLine();
    }
}