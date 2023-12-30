import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

public class ActiveAPP {

    public interface User32 extends WinUser {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        WinDef.HWND GetForegroundWindow();

        int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);
    }

    public static String getApplicationName(String fullTitle) {
        if (fullTitle != null && !fullTitle.isEmpty()) {
            // Split by the delimiter '-' and get the last part
            String[] parts = fullTitle.split("-");
            if (parts.length > 0) {
                return parts[parts.length - 1].trim();
            }
        }
        return fullTitle; // Return the entire title if the delimiter isn't found
    }

    public  String active() {
        try {
            char[] windowText = new char[512];
            WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
            User32.INSTANCE.GetWindowTextW(hwnd, windowText, 512);
            return Native.toString(windowText).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null in case of failure
    }

    public static void main(String[] args) {
    	ActiveAPP a=new ActiveAPP();
        String activeWindowTitle = a.active();
        String applicationName = getApplicationName(activeWindowTitle);
        System.out.println("Application Name: " + activeWindowTitle);
    }
}
