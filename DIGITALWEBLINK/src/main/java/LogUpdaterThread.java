import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogUpdaterThread extends Thread {
    private final String appName;
    private final FileWriter totalTimeWriter;
    private LocalDateTime startTime;
    private String logFilePath = "";

    public LogUpdaterThread(String appName, FileWriter totalTimeWriter) {
        String userHome = System.getProperty("user.home");
        File folder = new File(userHome + File.separator + "UsageTracker");
        logFilePath = folder.getPath() + File.separator + "total_time.txt";

        this.appName = appName;
        this.totalTimeWriter = totalTimeWriter;
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

    private boolean dateChanged() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime lastRecordedDateTime = getLastRecordedDateTime();
        return !currentDateTime.toLocalDate().equals(lastRecordedDateTime.toLocalDate());
    }

    private LocalDateTime getLastRecordedDateTime() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(logFilePath));

            for (int i = lines.size() - 1; i >= 0; i--) {
                String line = lines.get(i);
                if (line.contains("Total time of " + appName)) {
                    String dateTimeString = line.split(" - ")[0];
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

            // Find the entry for the current app
            int entryIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("Total time of " + appName)) {
                    entryIndex = i;
                    totalDuration = calculateTotalDuration(line);
                    break;
                }
            }

            // Update the total duration for the current app
            totalDuration++;
            String todayEntry = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"))
                    + " - Total time of " + appName + ": " + totalDuration + "s";

            if (entryIndex != -1) {
                // Update the existing entry
                lines.set(entryIndex, todayEntry);
            } else {
                // If no entry for the current app exists, add a new one
                lines.add(todayEntry);
            }

            // Write the updated contents back to total_time.txt
            Files.write(Paths.get(logFilePath), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
