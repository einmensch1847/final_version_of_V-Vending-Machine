package api;

import security.SecureEncryption;
import security.SecureStorage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class SecureApiClientV2 {

    private static final String BASE_URL = "https://menschwoodworks.ir/API/MESSENGER/";
    private SecureEncryption encryption;
    private SecureStorage secureStorage;
    private String sessionToken;
    private String userId;
    private Map<String, String> headers;
    private String masterPassword;

    public SecureApiClientV2() {
        this.encryption = new SecureEncryption();
        this.secureStorage = new SecureStorage();
        this.headers = new HashMap<>();
        this.masterPassword = generateMasterPassword();

        initializeSecurity();
        loadCachedCredentials();
    }

    private void initializeSecurity() {
        // افزودن هدرهای امنیتی
        headers.put("X-Session-ID", encryption.getSessionId());
        headers.put("X-Client-Public-Key", encryption.getPublicKeyBase64());
        headers.put("X-Client-Version", "2.0.0");
        headers.put("X-Encryption-Type", "VWM_SECURE_V1");

        System.out.println("🔒 کلاینت امن V2 با موفقیت راه‌اندازی شد");
    }

    private String generateMasterPassword() {
        // تولید رمز عبور اصلی از مشخصات سیستم
        String systemInfo = System.getProperty("os.name") +
                System.getProperty("user.name") +
                System.getProperty("user.home") +
                Runtime.getRuntime().availableProcessors();

        try {
            // استفاده از SecureStorage برای هش کردن
            return secureStorage.hashKey(systemInfo + System.currentTimeMillis());
        } catch (Exception e) {
            return "vwm_secure_master_" + System.currentTimeMillis();
        }
    }

    /**
     * لاگین امن با کش کردن خودکار
     */
    public LoginResult secureLogin(String username, String password, boolean isAdmin,
                                   boolean rememberMe) {
        try {
            // ساخت داده‌های لاگین
            Map<String, String> loginData = new LinkedHashMap<>();
            loginData.put("action", "login");
            loginData.put("username", username);
            loginData.put("password_hash", encryption.hashPassword(password));
            loginData.put("is_admin", isAdmin ? "1" : "0");
            loginData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            loginData.put("client_version", "VWM_2.0");
            loginData.put("device_id", secureStorage.hashKey(username));

            // ارسال درخواست
            String response = sendSecureRequest("auth.php", loginData);

            if (response.startsWith("ERROR") || response.equals("CONNECTION_FAILED")) {
                return new LoginResult(false, "Connection failed", null);
            }

            // تجزیه پاسخ
            Map<String, String> responseData = parseResponse(response);

            if ("success".equals(responseData.get("status"))) {
                this.sessionToken = responseData.get("session_token");
                this.userId = responseData.get("user_id");

                // تنظیم کلید عمومی سرور
                String serverPublicKey = responseData.get("server_public_key");
                if (serverPublicKey != null) {
                    encryption.setServerPublicKey(serverPublicKey);
                }

                // کش کردن اطلاعات اگر کاربر خواست
                if (rememberMe) {
                    cacheLoginInfo(username, responseData);
                }

                System.out.println("✅ ورود امن موفقیت‌آمیز: " + username);
                return new LoginResult(true, "Login successful", responseData);

            } else {
                String errorMsg = responseData.getOrDefault("message", "Login failed");
                return new LoginResult(false, errorMsg, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResult(false, "System error: " + e.getMessage(), null);
        }
    }

    /**
     * بررسی آیا کش لاگین وجود دارد
     */
    public boolean hasCachedLogin() {
        return secureStorage.hasCachedLogin();
    }

    /**
     * لاگین با کش ذخیره شده
     */
    public LoginResult loginWithCache() {
        SecureStorage.LoginCacheData cachedData =
                secureStorage.getCachedLogin(masterPassword);

        if (cachedData == null) {
            return new LoginResult(false, "No valid cache found", null);
        }

        try {
            // تأیید اعتبار token با سرور
            Map<String, String> verifyData = new LinkedHashMap<>();
            verifyData.put("action", "verify_token");
            verifyData.put("username", cachedData.username);
            verifyData.put("token", cachedData.token);
            verifyData.put("timestamp", String.valueOf(System.currentTimeMillis()));

            String response = sendSecureRequest("auth.php", verifyData);
            Map<String, String> responseData = parseResponse(response);

            if ("success".equals(responseData.get("status"))) {
                this.sessionToken = cachedData.token;
                this.userId = responseData.get("user_id");

                // تنظیم کلید عمومی سرور
                String serverPublicKey = responseData.get("server_public_key");
                if (serverPublicKey != null) {
                    encryption.setServerPublicKey(serverPublicKey);
                }

                System.out.println("✅ ورود با کش موفقیت‌آمیز: " + cachedData.username);
                return new LoginResult(true, "Auto-login successful", responseData);

            } else {
                // کش نامعتبر، پاک کردن آن
                secureStorage.clearLoginCache();
                return new LoginResult(false, "Cache expired", null);
            }

        } catch (Exception e) {
            secureStorage.clearLoginCache();
            return new LoginResult(false, "Cache login failed", null);
        }
    }

    /**
     * کش کردن اطلاعات لاگین
     */
    private void cacheLoginInfo(String username, Map<String, String> userData) {
        try {
            Map<String, String> profile = new HashMap<>();
            profile.put("fullname", userData.get("fullname"));
            profile.put("email", userData.get("email"));
            profile.put("phone", userData.get("phone"));
            profile.put("user_type", userData.get("user_type"));
            profile.put("last_login", String.valueOf(System.currentTimeMillis()));

            secureStorage.cacheLoginData(username, sessionToken, profile, masterPassword);
            System.out.println("💾 اطلاعات لاگین کش شد");

        } catch (Exception e) {
            System.err.println("خطا در کش کردن لاگین: " + e.getMessage());
        }
    }

    /**
     * بارگذاری اولیه اطلاعات کش شده
     */
    private void loadCachedCredentials() {
        if (secureStorage.hasCachedLogin()) {
            System.out.println("🔍 کش لاگین پیدا شد");
        }
    }

    /**
     * ارسال درخواست امن
     */
    private String sendSecureRequest(String endpoint, Map<String, String> data) {
        try {
            // تبدیل Map به رشته key=value&...
            StringBuilder dataBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                if (dataBuilder.length() > 0) {
                    dataBuilder.append("&");
                }
                dataBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            String plainData = dataBuilder.toString();

            // رمزنگاری داده
            String encryptedData = encryption.encrypt(plainData);

            // ایجاد درخواست
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            // افزودن هدرها
            headers.forEach(conn::setRequestProperty);
            if (sessionToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + sessionToken);
            }
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // ارسال داده رمزنگاری شده
            String postData = "data=" + URLEncoder.encode(encryptedData, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes());
                os.flush();
            }

            // دریافت پاسخ
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                }

                String response = responseBuilder.toString().trim();

                // اگر پاسخ رمزنگاری شده است، رمزگشایی کن
                if (response.contains("|")) {
                    return encryption.decrypt(response);
                } else {
                    return response;
                }

            } else {
                System.err.println("خطای HTTP: " + responseCode);
                return "ERROR_HTTP_" + responseCode;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "CONNECTION_FAILED";
        }
    }

    /**
     * تجزیه پاسخ سرور (بدون JSON)
     */
    private Map<String, String> parseResponse(String response) {
        Map<String, String> result = new LinkedHashMap<>();

        if (response == null || response.isEmpty()) {
            result.put("status", "error");
            result.put("message", "Empty response");
            return result;
        }

        // فرمت پاسخ: key1=value1;key2=value2;key3=value3
        String[] pairs = response.split(";");

        for (String pair : pairs) {
            if (pair.contains("=")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    result.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }

        return result;
    }

    /**
     * ساخت رشته از Map برای ارسال
     */
    private String buildDataString(Map<String, String> data) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append(";");
            }
            builder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }

        return builder.toString();
    }

    /**
     * لاگاوت و پاک کردن کش
     */
    public void logout() {
        if (sessionToken != null) {
            try {
                Map<String, String> logoutData = new LinkedHashMap<>();
                logoutData.put("action", "logout");
                logoutData.put("token", sessionToken);
                logoutData.put("timestamp", String.valueOf(System.currentTimeMillis()));

                sendSecureRequest("auth.php", logoutData);
            } catch (Exception e) {
                // ignore
            }
        }

        // پاک کردن کش
        secureStorage.clearLoginCache();

        // ریست کردن session
        sessionToken = null;
        userId = null;

        System.out.println("🚪 خروج و پاک کردن کش انجام شد");
    }

    /**
     * پاک کردن اجباری تمام داده‌های حساس
     */
    public void emergencyClear() {
        secureStorage.clearSensitiveData();
        sessionToken = null;
        userId = null;
        headers.clear();

        System.out.println("🚨 پاک کردن اضطراری تمام داده‌های حساس");
    }

    // --- کلاس نتیجه لاگین ---

    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final Map<String, String> userData;

        public LoginResult(boolean success, String message, Map<String, String> userData) {
            this.success = success;
            this.message = message;
            this.userData = userData;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Map<String, String> getUserData() { return userData; }
    }

    // --- Getter ها ---

    public String getSessionToken() { return sessionToken; }
    public String getUserId() { return userId; }
    public SecureStorage getStorage() { return secureStorage; }
}