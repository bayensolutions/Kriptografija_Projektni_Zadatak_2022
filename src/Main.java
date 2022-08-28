import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final String USERS_PATH = "resources/users/users.txt";
    public static final String CERTIFICATES_PATH = "resources/certificates";
    public static final String RESULTS_PATH = "resources/results/results.txt";

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
        Path of = Path.of(USERS_PATH);
        List<String> lines = Files.readAllLines(of, StandardCharsets.UTF_8);
        boolean usernameAlreadyExists = false;
        for (String line : lines) {
            if (username.equals(line.split("@")[0])) {
                System.out.println("Username " + username + " is already used. Please try with the another one.");
                usernameAlreadyExists = true;
                break;
            }
        }
        if (lines.size() == 0 || !usernameAlreadyExists) {
            Cryptography.generateCertificate(username, password);
            Files.write(of, (username + "@" + Cryptography.getHash(CERTIFICATES_PATH, username, password) + "@3" + "\n").getBytes(), StandardOpenOption.APPEND);
        }
    }

    public static void login() {
        Scanner entry = new Scanner(System.in);

        System.out.print("Input username: ");
        String username = entry.nextLine();
        System.out.print("Input password: ");
        String password = entry.nextLine();
        int participationsLeft = 0;
        String rightLine = null;

        try {
            String hashInFile = null;
            String thirdArgument;
            boolean usernameExists = false;
            boolean isExit = false;
            Path of = Path.of(USERS_PATH);
            List<String> lines = Files.readAllLines(of, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (username.equals(line.split("@")[0])) {
                    rightLine = line;
                    usernameExists = true;
                    hashInFile = line.split("@")[1];
                    thirdArgument = line.split("@")[2];
                    participationsLeft = Integer.parseInt(thirdArgument);
                    break;
                }
            }
            if (!usernameExists) {
                System.out.println("Username " + username + " doesn't exist in database.");
                return;
            }
            if (!Cryptography.getHash(CERTIFICATES_PATH, username, password).equals(hashInFile)) {
                System.out.println("The entered password is incorrect.");
                return;
            }
            if (!Cryptography.verifyCertificate(username)) {
                System.out.println("Your certificate is not valid!");
                return;
            }

            System.out.println("*********************************");
            System.out.println("* Choose the option:            *");
            System.out.println("* 1. Access the quiz            *");
            System.out.println("* 2. Show results               *");
            System.out.println("* 3. Exit                       *");
            System.out.println("*********************************");
            System.out.print("Your selection: ");
            Scanner s = new Scanner(System.in);
            int option = s.nextInt();

            while (!isExit) {
                switch (option) {
                    case 1:
                        new Quiz().startQuiz(username, participationsLeft);
                        int newParticipations = participationsLeft-1;
                        System.out.println("NEW PARTICIPATIONS LEFT: "+newParticipations);
                        replaceLine(USERS_PATH, rightLine, rightLine.replace("@" + participationsLeft, "@" + newParticipations));
                        if (newParticipations == 0) {
                            System.out.println("DOSLI SMO DO REVOKE CERTIFICATE");
                            Cryptography.revokeCertificate(username);
                        }
                        isExit = true;
                        break;
                    case 2:
                        Quiz.showResults(RESULTS_PATH);
                        isExit = true;
                        break;
                    case 3:
                        isExit = true;
                    default:
                        System.out.println("You chose non-existent option. Select 1, 2 or 3.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void replaceLine(String filePath, String originalLineText, String newLineText) {
        Path path = Paths.get(filePath);
        // Get all the lines
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            // Do the line replace
            List<String> list = stream.map(line -> line.equals(originalLineText) ? newLineText : line)
                    .collect(Collectors.toList());
            // Write the content back
            Files.write(path, list, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}