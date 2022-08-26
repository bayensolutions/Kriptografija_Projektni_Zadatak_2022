using System;
        using System.Collections.Generic;
        using System.Diagnostics;
        using System.Linq;
        using System.Security.Cryptography.X509Certificates;
        using System.Text;
        using System.Threading.Tasks;
        namespace Crypto
        {
public class DigitalCertificate
{
    public static void CreateCACertificate()
    {
        System.Console.Clear();
        System.Console.WriteLine("[ Kreiranje root CA tijela : rootCA]\n");
        Utility.ExecuteShellCommand($"openssl genrsa -out {Utility.PRIVATE}\\private4096.key 4096 2>error");
        Utility.ExecuteShellCommand($"openssl req -x509 -new -out rootCA.pem -config rootCA.cnf -days 365 -key {Utility.PRIVATE}\\private4096.key");
        System.Console.Clear();
    }

    public static void CreateIntermediateCACertificate(string name, string path, int days, int keylength = 4096)
    {
        System.Console.WriteLine($"\n[ Kreiranje podređenog CA tijela : {name}]\n");
        Utility.ExecuteShellCommand($"openssl genrsa -out {path}\\private\\{name}.key {keylength}");
        Utility.ExecuteShellCommand($"openssl rsa -in {path}\\private\\{name}.key -inform PEM -pubout -out {path}\\private\\PUB_{name}.key"); // TODO: Provjeriti da li funkcioniše
        Utility.ExecuteShellCommand($"openssl req -new -out requests\\{name}.csr -key {path}\\private\\{name}.key -config {Utility.CERTIFICATES_ROOT}\\rootCA.cnf -days {days}");
        Utility.ExecuteShellCommand($"openssl ca -config rootCA.cnf -in requests\\{name}.csr -out {path}\\{name}.pem -days {days}");
        System.Console.Clear();
    }

    public static void CreateUserCertificate(string username, string password, string caFolder, string caType)
    {
        System.Console.WriteLine($"[ Kreiranje korisničkog sertifikata : {username}]\n");
        Directory.CreateDirectory(caFolder + "\\userKeys");
        Utility.ExecuteShellCommand($"openssl genrsa -des3 -passout pass:{password} -out {caFolder}\\userKeys\\{username}.key 4096");
        Utility.ExecuteShellCommand($"openssl req -subj \"/C=BA/ST=RS/L=BL/O=Elektrotehnicki fakultet/OU=CA_Vanja/CN={username}\" -new -key {caFolder}\\userKeys\\{username}.key -passin pass:{password} -config {caFolder}\\{caType}.cnf -out {caFolder}\\requests\\{username}.csr");
        Utility.ExecuteShellCommand($"openssl ca -batch -in {caFolder}\\requests\\{username}.csr -out {caFolder}\\certs\\{username}.crt -keyfile {caFolder}\\private\\{caType}.key -config {caFolder}\\{caType}.cnf");
        Thread.Sleep(10_000);
        System.Console.Clear();
    }

    public static bool VerifyCertificate(string username, string caFolder, string caType)
    {
        // provjerava da je chain validan
        var valid = (Utility.ExecuteShellCommand($"openssl verify -CAfile {Utility.CERTIFICATES_ROOT}\\rootCA.pem -untrusted {caFolder}\\{caType}.pem {caFolder}\\certs\\{username}.crt 2>error.txt")).Contains("OK");
        if (!valid)
            Console.WriteLine("\nSertifikat nije validan.");
        X509Certificate cert = new X509Certificate($"{caFolder}\\certs\\{username}.crt");
        string serialNumber = BitConverter.ToString(cert.GetSerialNumber());
        var lines = File.ReadAllLines(caFolder + "\\index.txt");
        foreach (var line in lines)
        {
            // provjerava da li je cert povučen
            if (line.Contains($"\t{serialNumber}\t") && line.StartsWith("R"))
            {
                valid = false;
                Console.WriteLine("\nSertfikat je povučen.");
            }
        }
        // ispituje da li se CN poklapa sa username u korisničkom sertifikatu
        if (!cert.Subject.Contains("CN=" + username + ","))
        {
            Console.WriteLine("\nNeodgovarajući sertifikat.");
            valid = false;
        }
        return valid;
    }

    public static void RevokeCertificate(User user)
    {
        if (File.Exists(Utility.CA1 + "\\certs\\" + user.Username + ".crt")) // CA1
        {
            Console.WriteLine("Sertifikat se nalazi u CA1");
            UpdateIndexFileGenerateCRL(Utility.CA1, user.Username, "CA1");

        }
        else if (File.Exists(Utility.CA2 + "\\certs\\" + user.Username + ".crt"))
        {  // CA2
            Console.WriteLine("Sertifikat se nalazi u CA2");
            UpdateIndexFileGenerateCRL(Utility.CA2, user.Username, "CA2");
        }
        Thread.Sleep(6_000);
    }

    private static void UpdateIndexFileGenerateCRL(string caFolder, string username, string caType)
    {
        X509Certificate cert = new X509Certificate($"{caFolder}\\certs\\{username}.crt");
        string serialNumber = BitConverter.ToString(cert.GetSerialNumber());
        var lines = File.ReadAllLines(caFolder + "\\index.txt");
        for (int i = 0; i < lines.Length; i++)
        {
            if (lines[i].Contains($"\t{serialNumber}\t"))
            {
                StringBuilder builder = new StringBuilder(lines[i]);
                if ('R' == builder[0])
                {
                    Console.WriteLine("Nemoj ga opet povući");
                    return;
                }// Ako je već povučen, ne povlačimo ga opet

                builder[0] = 'R';
                lines[i] = builder.ToString();
                DateTime d = DateTime.Now.ToUniversalTime();
                lines[i] = lines[i].Insert(lines[i].IndexOf('Z') + 2,
                        $"{d.Year % 100}{String.Format("{0:00}", d.Month)}{String.Format("{0:00}", d.Day)}{String.Format("{0:00}", d.Hour)}{String.Format("{0:00}", d.Minute)}{String.Format("{0:00}", d.Second)}Z,cessationOfOperation");
            }
        }
        File.WriteAllLines(caFolder + "\\index.txt", lines);
        string command = $"openssl ca -gencrl -out {caFolder}\\crl\\crl.pem -config {caFolder}\\{caType}.cnf";
        new Process() { StartInfo = new ProcessStartInfo("cmd.exe", "/c " + command) { CreateNoWindow = false, WindowStyle = ProcessWindowStyle.Hidden, UseShellExecute = true, WorkingDirectory = Utility.CERTIFICATES_ROOT } }.Start();
        return;
    }
}
}
