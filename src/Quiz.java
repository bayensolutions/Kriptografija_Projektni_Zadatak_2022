import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Quiz {
    public static String QUESTIONS_PATH = "resources/questions/";
    public static String IMAGES_PATH = "resources/images/";

    public static final String password="sigurnost";

    public static int[] arrayNumbers = new int[20];
    public static int serialNumber = 0;
    public static int correctAnswersCounter = 0;

    public static void hideQuestions() {

        List<String> questions = null;
        try {
            questions = Files.readAllLines(Paths.get(QUESTIONS_PATH + "pitanja.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int number = 0;
        try {
            for (String question : questions) {
                String encodedQuestion= AES.encrypt(question,password);
                //System.out.println(encodedQuestion);
                number++;
                Steganography.encode(new File(IMAGES_PATH + number + ".bmp"), encodedQuestion,
                        new File(QUESTIONS_PATH + number + ".bmp"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String showQuestion(int number) {
        AES aes_ =new AES();
        String[] array = new String[0];
        try {
            String encryptedQuestion=Steganography.decode(new File(QUESTIONS_PATH + (number + 1) + ".bmp"));
            String decryptedQuestion= AES.decrypt(encryptedQuestion,password);
            array = decryptedQuestion.split("#");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String rightAnswer = null;
        System.out.println(array[0]);
        if (number < 10) {
            showAnswers(array);
            rightAnswer = array[5];
        } else {
            rightAnswer = array[1];
        }
        return rightAnswer;
    }

    private static void showAnswers(String[] array) {

        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        Collections.shuffle(list);
        System.out.println("A) " + array[list.get(0)]);
        System.out.println("B) " + array[list.get(1)]);
        System.out.println("C) " + array[list.get(2)]);
        System.out.println("D) " + array[list.get(3)]);
    }

    public void startQuiz(String user, int participationsLeft) {
        System.out.println("WELCOME TO QUIZ!");
        System.out.println("Attempts remaining: " + --participationsLeft);
        for (int i = 0; i < 5; i++) {
            selectQuestion();
        }
        exportResults(user, correctAnswersCounter);
        System.out.println("Your score: " + correctAnswersCounter + "/5");
        correctAnswersCounter = 0;
        serialNumber = 0;
        for (int number = 0; number < 20; number++) {
            arrayNumbers[number] = 0;
        }
    }

    public static void selectQuestion() {
        int number = (int) (Math.random() * 20);
        Scanner scanner = new Scanner(System.in);
        String answer;
        if (arrayNumbers[number] == 0) {
            System.out.println(++serialNumber + ". pitanje: ");
            arrayNumbers[number] = 1;
            String rightAnswer = showQuestion(number);
            if (number < 10) {
                System.out.println("Vas odgovor [izaberite 1 od 4 odgovora] ");
                scanner = new Scanner(System.in);
            } else {
                System.out.println("Vas odgovor: [Unesite odgovor]");

            }
            answer = scanner.nextLine();
            if (rightAnswer.equals(answer)) {
                System.out.println("Tacan odgovor!");
                correctAnswersCounter++;
            } else {
                System.out.println("Netacan odgovor!");
            }
        } else {
            selectQuestion();
        }
    }

    public void exportResults(String user, int correctAnswers) {
        try {
            String contentToEncrypt=user + "\t" + LocalDate.now() + "\t" + LocalTime.now() + "\t" + correctAnswers + "\n";
            Files.write(Paths.get(Main.RESULTS_PATH),AES.encrypt(contentToEncrypt,password).getBytes() , StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Neuspjesan ispis.");
            e.printStackTrace();
        }
    }

    public static void showResults(String resultsPath) {
        System.out.println("QUIZ RESULTS:");
        System.out.println("=============================================");
        BufferedReader br = null;
        try {
            String wholeContent=Files.readString(Path.of(resultsPath));
            System.out.print(AES.decrypt(wholeContent,password));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("=============================================");
    }
}