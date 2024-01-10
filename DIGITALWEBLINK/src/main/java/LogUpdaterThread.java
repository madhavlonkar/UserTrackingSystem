import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class LogUpdaterThread extends Thread {
    private final String appName;
    private final FileWriter totalTimeWriter;
    private LocalDateTime startTime;
    private String logFilePath = "";
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    // Change this key and salt to your own secret key
    private static final String SECRET_KEY = "ZPLUS";
    private static final String SALT = "Maddy";

    public LogUpdaterThread(String appName, FileWriter totalTimeWriter) {
        String userHome = System.getProperty("user.home");
        File folder = new File(userHome + File.separator + "UsageTracker");
        logFilePath = folder.getPath() + File.separator + "total_time.txt";

        this.appName = appName;
        this.totalTimeWriter = totalTimeWriter;

        // Initialize ciphers with the secret key
        cipherInit();
        this.startTime = getLastRecordedDateTime();
    }


    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                // Sleep for 1 second
                Thread.sleep(1000);

                // Check if the current app is the one being tracked by this thread
                if (isActiveApp()) {
                    // Check if the date has changed since the last update
                    if (dateChanged()) {
                        // If the date has changed, reset the startTime
                        startTime = getLastRecordedDateTime();
                    }

                    // Update the log file
                    updateLogFile();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void cipherInit() {
        try {
            // Derive the key from the password and salt
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            // Initialize encrypt cipher
            encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secret);

            // Initialize decrypt cipher
            decryptCipher = Cipher.getInstance("AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, secret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean dateChanged() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime lastRecordedDateTime = getLastRecordedDateTime();
        return !currentDateTime.toLocalDate().equals(lastRecordedDateTime.toLocalDate());
    }

    private LocalDateTime getLastRecordedDateTime() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(logFilePath));

            for (int i = lines.size() - 1; i >= 0; i--) {
                String encryptedLine = lines.get(i);
                String decryptedLine = decrypt(encryptedLine);

                if (decryptedLine.contains("Total time of " + appName)) {
                    String dateTimeString = decryptedLine.split(" - ")[0];
                    return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If no recorded date and time are found, return the current date and time
        return LocalDateTime.now();
    }

    private boolean isActiveApp() {
        ActiveAPP a = new ActiveAPP();
        String activeApp = a.active();
        return appName.equals(activeApp);
    }

    private void updateLogFile() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(logFilePath));

            // Calculate total duration
            long totalDuration = 0;
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String logEntryToSearch = "Total time of " + appName;

            // Find the entry for the current app
            int entryIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                String encryptedLine = lines.get(i);
                String decryptedLine = decrypt(encryptedLine);
                String line = decryptedLine;

                String entryDate = line.split(" ")[0];
                if (entryDate.equals(currentDate) && line.contains(logEntryToSearch)) {
                    entryIndex = i;
                    totalDuration = calculateTotalDuration(line);
                    break;
                }
            }

            // Check if the date has changed since the last update
            if (dateChanged()) {
                // If the date has changed, create a new entry with total duration starting from 1
                totalDuration = 1;
                String todayEntry = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"))
                        + " - Total time of " + appName + ": " + totalDuration + "s";

                // Remove the entry for the current app if it already exists
                // if (entryIndex != -1) {
                //     lines.remove(entryIndex);
                // }

                lines.add(encrypt(todayEntry));
            } else if (entryIndex != -1) {
                // Update the total duration for the current app
                totalDuration = calculateTotalDuration(decrypt(lines.get(entryIndex))) + 1;
                String updatedEntry = decrypt(lines.get(entryIndex)).split(" - ")[0] + " - Total time of " + appName + ": " + totalDuration + "s";
                lines.set(entryIndex, encrypt(updatedEntry));
            } else {
                // If no entry for the current app exists, add a new one
                totalDuration = 1;
                String todayEntry = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"))
                        + " - Total time of " + appName + ": " + totalDuration + "s";
                lines.add(encrypt(todayEntry));
            }

            // Write the updated contents back to total_time.txt
            Files.write(Paths.get(logFilePath), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String encrypt(String data) {
        try {
            byte[] encryptedBytes = encryptCipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String decrypt(String encryptedData) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = decryptCipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private long calculateTotalDuration(String line) {
        long lastRecordedSeconds = getLastRecordedSeconds(line);
        return lastRecordedSeconds;
    }

    private long getLastRecordedSeconds(String line) {
        if (line.contains("Total time of " + appName)) {
            String secondsString = line.split(": ")[1].replace("s", "").trim();
            return Long.parseLong(secondsString);
        }

        // If no recorded time is found, return 0 seconds
        return 0;
    }
}
