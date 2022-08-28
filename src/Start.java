import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Start {
    public static void main(String[] args) {
        try {
            executeCommand("openssl ca -gencrl -out resources/certificates/CA1/crl/crl.pem -config resources/certificates/CA1/openssl.cnf");
        } catch (Exception e) {
        }
    }

    private static void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(new ArrayList<>(Arrays.asList(command.split(" "))));
        Process p = builder.start();
        p.waitFor();
    }
}