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
            totalTimeWriter = new FileWriter("total_time.txt", true); // Initialize total time FileWriter
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

                Thread.sleep(1000); // Wait for 15 seconds

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
