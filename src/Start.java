import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Start {
    public static void main(String[] args) throws IOException, InterruptedException {
        String out = "resources/certificates/CA1/userKeys/a.key";
        String pass = "a";
        String command = String.format("openssl genrsa -des3 -passout pass:%s -out %s 4096", pass, out);
        ArrayList<String> argumenti = new ArrayList<>(Arrays.asList(command.split(" ")));
        //argumenti.add("openssl");
        //argumenti.add("genrsa");
        //argumenti.add("-out");
        //argumenti.add(out);
        //argumenti.add("4096");

        ProcessBuilder builder = new ProcessBuilder(argumenti);
        Process p = builder.start();
        p.waitFor();
    }
}