import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogUpdaterThread extends Thread {
	private final String appName;
	private final FileWriter totalTimeWriter;
	private LocalDateTime startTime;

	public LogUpdaterThread(String appName, FileWriter totalTimeWriter) {
		this.appName = appName;
		this.totalTimeWriter = totalTimeWriter;
		this.startTime =  LocalDateTime.now().minusSeconds(getLastRecordedSeconds());;
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				// Sleep for 1 second
				Thread.sleep(1000);

				// Check if the current app is the one being tracked by this thread
				if (isActiveApp()) {
					// Update the log file
					updateLogFile();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private boolean isActiveApp() {
		ActiveAPP a = new ActiveAPP();
		String activeApp = a.active();
		return appName.equals(activeApp);
	}

	private void updateLogFile() {
		// Read existing contents of total_time.txt
		try {
			List<String> lines = Files.readAllLines(Paths.get("total_time.txt"));

			// Calculate total duration
			long totalDuration = calculateTotalDuration();

			// Iterate over the lines to find and update the entry
			boolean entryExists = false;
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.contains("Total time of " + appName)) {
					// Upadate the total duration
					lines.set(i, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - Total time of "
							+ appName + ": " + totalDuration + "s");
					entryExists = true;
					break;
				}
			}

			// If the entry doesn't exist, add a new one
			if (!entryExists) {
				lines.add(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - Total time of "
						+ appName + ": " + totalDuration + "s");
			}

			// Write the updated contents back to total_time.txt
			Files.write(Paths.get("total_time.txt"), lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private long calculateTotalDuration() {
	    // Calculate the elapsed time since the start
	    Duration elapsedTime = Duration.between(startTime, LocalDateTime.now());

	    // Return the total duration in seconds
	    return elapsedTime.toSeconds();
	}


	private long getLastRecordedSeconds() {
	    try {
	        List<String> lines = Files.readAllLines(Paths.get("total_time.txt"));

	        for (int i = lines.size() - 1; i >= 0; i--) {
	            String line = lines.get(i);
	            if (line.contains("Total time of " + appName)) {
	                String secondsString = line.split(": ")[1].replace("s", "").trim();
	                return Long.parseLong(secondsString);
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    // If no recorded time is found, return 0 seconds
	    return 0;
	}


}
