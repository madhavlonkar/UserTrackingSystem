import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class LogReader {
    // Change this key and salt to your own secret key
    private static final String SECRET_KEY = "ZPLUS";
    private static final String SALT = "Maddy";

    public static void main(String[] args) {
        try {
            String userHome = System.getProperty("user.home");
            String logFilePath = userHome + "/UsageTracker/total_time.txt";
            String decryptedLogsPath = userHome + "/UsageTracker/decrypted_logs.txt";

            List<String> lines = Files.readAllLines(Paths.get(logFilePath));

            try (FileWriter writer = new FileWriter(decryptedLogsPath)) {
                for (String encryptedLine : lines) {
                    String decryptedLine = decrypt(encryptedLine);
                    System.out.println(decryptedLine);
                    writer.write(decryptedLine + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Decrypted logs have been written to: " + decryptedLogsPath);
        } catch (IOException e) {
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
