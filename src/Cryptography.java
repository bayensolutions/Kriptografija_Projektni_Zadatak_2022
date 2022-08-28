import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cryptography {

    public static String getHash(String certificatesPath, String username, String password) throws IOException {
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

    public static void generateCertificate(String username, String password) {
        String caChosen = Math.random() < 0.5 ? "CA1" : "CA2";
        String keyPath = "resources/certificates/" + caChosen + "/userKeys/" + username + ".key";
        try {
            executeCommand(String.format("openssl genrsa -des3 -passout pass:%s -out %s 4096", password, keyPath));
            executeCommand(String.format("openssl req -subj \"/C=BA/ST=RS/L=BL/O=Elektrotehnicki fakultet/OU=UNIBL/CN=%s\" -new -key %s -passin pass:%s -config resources/certificates/%s/openssl.cnf -out resources/certificates/%s/requests/%s.csr", username, keyPath, password, caChosen, caChosen, username));
            executeCommand(String.format("openssl ca -batch -in resources/certificates/%s/requests/%s.csr -out resources/certificates/%s/certs/%s.crt -keyfile resources/certificates/%s/private/%s.key -config resources/certificates/%s/openssl.cnf -passin pass:%s", caChosen, username, caChosen, username, caChosen, caChosen, caChosen, password));
            System.out.println("path to certificate: " + "resources/certificates/" + caChosen + "/certs/" + username + ".crt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean verifyCertificate(String username) {
        if (checkValidity("resources/certificates/CA1/index.txt", username) || checkValidity("resources/certificates/CA2/index.txt", username))
            return true;
        return false;
    }

    private static boolean checkValidity(String indexPath, String username) {
        File file = new File(indexPath);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("V") && line.endsWith("CN=" + username)) {
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void revokeCertificate(String username) {
        try {
            if (checkValidity("resources/certificates/CA1/index.txt", username)) {
                changeContentOfIndexFile("resources/certificates/CA1/index.txt", username);
                executeCommand("openssl ca -gencrl -out resources/certificates/CA1/crl/crl.pem -config resources/certificates/CA1/openssl.cnf");
            } else if (checkValidity("resources/certificates/CA2/index.txt", username)) {
                changeContentOfIndexFile("resources/certificates/CA2/index.txt", username);
                executeCommand("openssl ca -gencrl -out resources/certificates/CA2/crl/crl.pem -config resources/certificates/CA2/openssl.cnf");
            } else
                System.out.println("Certificate " + username + ".crt doesn't exist.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeContentOfIndexFile(String path, String username) {
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            ArrayList<String> lines = br.lines().collect(Collectors.toCollection(ArrayList<String>::new));
            for(String line : lines){
                if (line.endsWith("CN=" + username)) {
                    String[] lineParts = line.split("\t");
                    if("R".equals(lineParts[0])) // if already revoked, do not revoke again
                        return;
                    lineParts[0] = "R";
                    lineParts[2] = getCurrentlyDateTime().toString().substring(2, 14) + "Z,cessationOfOperation\t" + lineParts[2];
                    String newLine = lineParts[0] + "\t" + lineParts[1] + "\t" + lineParts[2] + lineParts[3] + "\t" + lineParts[4] + "\t" + lineParts[5];
                    replaceLine(path, line, newLine);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(new ArrayList<>(Arrays.asList(command.split(" "))));
        Process p = builder.start();
        p.waitFor();
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

    public static String getCurrentlyDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date());
    }
}