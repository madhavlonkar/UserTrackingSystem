import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppUsageTracker {
    private static Map<LocalDate, LinkedHashMap<String, Long>> appUsageByDay = new HashMap<>();
    private static FileWriter usageLogWriter; // FileWriter instance for usage log
    private static FileWriter totalTimeWriter; // FileWriter instance for total time
    private static Map<String, LogUpdaterThread> logUpdaterThreads = new HashMap<>();

    public static void main(String[] args) {
        try {
            // Get the user's home directory
            String userHome = System.getProperty("user.home");

            // Create a folder named "UsageTracker" in the user's home directory
            File folder = new File(userHome + File.separator + "UsageTracker");
            folder.mkdirs();  // Create the folder if it doesn't exist

            // Create the path to the "total_time.txt" file within the folder
            String filePath = folder.getPath() + File.separator + "total_time.txt";

            // Initialize total time FileWriter with the specified file path
            totalTimeWriter = new FileWriter(filePath, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeWriters(); // Close both writers on program termination
        }));

        while (true) {
            try {
                LocalDateTime currentDateTime = LocalDateTime.now();
                Map<String, Long> runningApps = getRunningApps();

                for (Map.Entry<String, Long> entry : runningApps.entrySet()) {
                    String appName = entry.getKey();
                    Long duration = entry.getValue();

                    if (!logUpdaterThreads.containsKey(appName)) {
                        LogUpdaterThread logUpdaterThread = new LogUpdaterThread(appName, totalTimeWriter);
                        logUpdaterThreads.put(appName, logUpdaterThread);
                        logUpdaterThread.start();
                    }
                }

                Thread.sleep(1000); // Wait for 1 second

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, Long> getRunningApps() {
        ActiveAPP a = new ActiveAPP();
        Map<String, Long> runningApps = new HashMap<>();
        String activeApp = a.active();
        runningApps.put(activeApp, (long) (Math.random() * 10)); // Random duration in seconds
        return runningApps;
    }

    private static void closeWriters() {
        try {
            if (usageLogWriter != null) {
                usageLogWriter.close(); // Close the usage log FileWriter
            }
            if (totalTimeWriter != null) {
                totalTimeWriter.close(); // Close the total time FileWriter
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
