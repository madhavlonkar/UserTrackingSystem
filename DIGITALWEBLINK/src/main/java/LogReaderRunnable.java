import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class LogReaderRunnable {
    // Change this key and salt to your own secret key
    private static final String SECRET_KEY = "ZPLUS";
    private static final String SALT = "Maddy";

    public static void main(String[] args) {
        try {
            Scanner sn = new Scanner(System.in);
            System.out.print("Enter Path Of Total_Time Text File :");
            String logFilePath = sn.nextLine();

            System.out.print("\nEnter Path Where you want to store decrypted file :");
            String decryptedLogsPath = sn.nextLine();

            // Check if the specified log file exists
            if (!Files.exists(Paths.get(logFilePath))) {
                System.err.println("Error: Log file not found at the specified path.");
                return;
            }

            // Create the target directory if it doesn't exist
            File decryptedLogsDir = new File(decryptedLogsPath);
            decryptedLogsDir.mkdirs();

            // Check if the target directory is a file or if there are issues creating it
            if (!decryptedLogsDir.isDirectory()) {
                System.err.println("Error: Invalid target directory.");
                return;
            }

            // Combine the target directory path with the file name
            String combinedPath = decryptedLogsDir.getPath() + File.separator + "decrypted_logs.txt";

            // Attempt to create the FileWriter
            try (FileWriter writer = new FileWriter(combinedPath)) {
                List<String> lines = Files.readAllLines(Paths.get(logFilePath));

                for (String encryptedLine : lines) {
                    String decryptedLine = decrypt(encryptedLine);
                    System.out.println(decryptedLine);
                    writer.write(decryptedLine + System.lineSeparator());
                }

                System.out.println("Decrypted logs have been written to: " + combinedPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String decrypt(String encryptedData) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey tmp = factory.generateSecret(new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256));
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secret);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
