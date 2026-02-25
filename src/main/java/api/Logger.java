package api;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Logger {

    private static final String LOG_API_URL = "https://menschwoodworks.ir/API/addLog.php";

    public static void log(String adminUsername, String action, String description,
                           String targetId, String targetType, String severity) {
        try {
            String data = "adminUsername=" + URLEncoder.encode(adminUsername, "UTF-8") +
                    "&action=" + URLEncoder.encode(action, "UTF-8") +
                    "&description=" + URLEncoder.encode(description, "UTF-8") +
                    "&targetId=" + URLEncoder.encode(targetId, "UTF-8") +
                    "&targetType=" + URLEncoder.encode(targetType, "UTF-8") +
                    "&severity=" + URLEncoder.encode(severity, "UTF-8");

            URL url = new URL(LOG_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes());
                os.flush();
            }

            if(conn.getResponseCode() != 200){
                System.out.println("Logger failed, response code: " + conn.getResponseCode());
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
