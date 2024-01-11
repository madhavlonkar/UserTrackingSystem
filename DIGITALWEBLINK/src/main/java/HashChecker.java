import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;

public class HashChecker {
	 private static String calculateHash(File file) {
	        try {
	            byte[] fileContent = Files.readAllBytes(file.toPath());
	            MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            byte[] hashBytes = digest.digest(fileContent);

	            // Convert hashBytes to hexadecimal representation
	            StringBuilder hexStringBuilder = new StringBuilder();
	            for (byte b : hashBytes) {
	                hexStringBuilder.append(String.format("%02x", b));
	            }

	            return hexStringBuilder.toString();
	        } catch (IOException | java.security.NoSuchAlgorithmException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	public static void main(String args[])
	{
		
		String userHome = System.getProperty("user.home");
        File folder = new File(userHome + File.separator + "UsageTracker");
        String logFilePath = folder.getPath() + File.separator + "total_time.txt";
        String currentHash = calculateHash(new File(logFilePath));
        System.out.print(currentHash);
	}
}
