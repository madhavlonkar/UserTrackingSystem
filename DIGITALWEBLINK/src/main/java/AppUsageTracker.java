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
//            usageLogWriter = new FileWriter("app_usage_log.txt", true); // Initialize usage log FileWriter
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

                    // Check if a log updater thread exists for this app
                    if (!logUpdaterThreads.containsKey(appName)) {
                        // If not, create a new log updater thread and start it
                        LogUpdaterThread logUpdaterThread = new LogUpdaterThread(appName, totalTimeWriter);
                        logUpdaterThreads.put(appName, logUpdaterThread);
                        logUpdaterThread.start();
                    }

                    // Log the app usage
//                    logAppUsage(currentDateTime, appName, duration);
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

//    private static void logAppUsage(LocalDateTime currentDateTime, String appName, Long duration) {
//        try (BufferedReader reader = new BufferedReader(new FileReader("app_usage_log.txt"))) {
//            String lastLine = null;
//            String line;
//            while ((line = reader.readLine()) != null) {
//                lastLine = line;
//            }
//
//            if (lastLine != null) {
//                String[] parts = lastLine.split(" - ");
//                LocalDateTime lastDateTime = LocalDateTime.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//                if (currentDateTime.isEqual(lastDateTime) || currentDateTime.isAfter(lastDateTime)) {
//                    usageLogWriter.write("\n");
//                }
//            }
//
//            usageLogWriter.write(currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + appName + " - " + duration + "s\n");
//            usageLogWriter.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    private static void printTotalAppUsage(LinkedHashMap<String, Long> appUsageToday, LocalDateTime currentDateTime) {
//        Map<String, Long> totalAppUsage = new HashMap<>();
//
//        // Iterate over the entries and accumulate total time
//        for (Map.Entry<String, Long> entry : appUsageToday.entrySet()) {
//            String appName = entry.getKey();
//            Long duration = entry.getValue();
//
//            // Accumulate the total time for each app
//            totalAppUsage.merge(appName, duration, Long::sum);
//
//            System.out.println("Total time of " + appName + ": " + totalAppUsage.get(appName) + "s");
//            writeTotalTime(currentDateTime, appName, totalAppUsage.get(appName));
//        }
//
//        // Update the appUsageToday map with the accumulated total time
//        appUsageToday.clear();
//        appUsageToday.putAll(totalAppUsage);
//    }




//    private static void writeTotalTime(LocalDateTime currentDateTime, String appName, Long totalDuration) {
//        writeTotalTime(currentDateTime.toLocalDate(), appName, totalDuration);
//    }

//    private static void writeTotalTime(LocalDate currentDate, String appName, Long totalDuration) {
//        try {
//            // Read existing contents of total_time.txt
//            List<String> lines = Files.readAllLines(Paths.get("total_time.txt"));
//
//            // Iterate over the lines to find and update the entry
//            boolean entryExists = false;
//            for (int i = 0; i < lines.size(); i++) {
//                String line = lines.get(i);
//                if (line.contains("Total time of " + appName)) {
//                    // Extract the existing total duration
//                    long existingDuration = Long.parseLong(line.split(": ")[1].replace("s", "").trim());
//
//                    // Update the total duration
//                    lines.set(i, currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - Total time of " + appName + ": " + (existingDuration + totalDuration) + "s");
//                    entryExists = true;
//                    break;
//                }
//            }
//
//            // If the entry doesn't exist, add a new one
//            if (!entryExists) {
//                lines.add(currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - Total time of " + appName + ": " + totalDuration + "s");
//            }
//
//            // Write the updated contents back to total_time.txt
//            Files.write(Paths.get("total_time.txt"), lines);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }



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
