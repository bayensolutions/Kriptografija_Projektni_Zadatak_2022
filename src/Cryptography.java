import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

public class Cryptography {

    public static void generateCertificate(String username, String password) {
        String caChosen = Math.random()<0.5 ? "CA1" :"CA2";
        String keyPath = "resources/certificates/" + caChosen + "/userKeys/" + username + ".key";
        System.out.println(keyPath);
        try {
            executeCommand(String.format("openssl genrsa -des3 -passout pass:%s -out %s 4096", password, keyPath));
            executeCommand(String.format("openssl req -subj \"/C=BA/ST=RS/L=BL/O=Elektrotehnicki fakultet/OU=CA_Vanja/CN=%s\" " +
                    "-new -key %s -passin pass:%s -config resources/certificates/CA1/openssl.cnf -out resources/certificates/CA1/requests/%s.csr", username, keyPath, password, username));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(new ArrayList<>(Arrays.asList(command.split(" "))));
        Process p = builder.start();
        p.waitFor();
    }
}