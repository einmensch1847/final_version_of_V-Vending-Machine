package security;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

public class SecureStorage {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int SALT_LENGTH = 32;
    private static final int ITERATIONS = 100000;

    private final String storagePath;
    private final String deviceId;
    private final Map<String, Long> accessLog;
    private final List<String> securityAlerts;

    // امضاهای دیجیتال برای تشخیص دستکاری
    private Map<String, String> dataSignatures;

    public SecureStorage() {
        this.storagePath = System.getProperty("user.home") + "/.vwm_secure_cache/";
        this.deviceId = generateDeviceId();
        this.accessLog = new HashMap<>();
        this.securityAlerts = new ArrayList<>();
        this.dataSignatures = new HashMap<>();

        initializeStorage();
    }

    private void initializeStorage() {
        try {
            // ایجاد دایرکتوری ذخیره‌سازی
            Files.createDirectories(Paths.get(storagePath));

            // ایجاد فایل امنیتی
            createSecurityFile();

            System.out.println("💾 ذخیره‌سازی امن راه‌اندازی شد: " + storagePath);
            System.out.println("📱 Device ID: " + deviceId);

        } catch (Exception e) {
            System.err.println("خطا در راه‌اندازی ذخیره‌سازی امن: " + e.getMessage());
        }
    }

    /**
     * ذخیره اطلاعات با رمزنگاری و امضای دیجیتال
     */
    public boolean saveData(String key, String value, String masterPassword) {
        try {
            // تولید کلید از رمز عبور اصلی
            SecretKey secretKey = deriveKey(masterPassword);

            // رمزنگاری داده
            EncryptedData encryptedData = encryptData(value, secretKey);

            // ایجاد امضای دیجیتال
            String signature = generateSignature(key, encryptedData.encryptedBytes, masterPassword);

            // ذخیره فایل
            String filename = storagePath + hashKey(key) + ".dat";
            try (DataOutputStream dos = new DataOutputStream(
                    new FileOutputStream(filename))) {

                // نوشتن IV
                dos.writeInt(encryptedData.iv.length);
                dos.write(encryptedData.iv);

                // نوشتن داده رمزنگاری شده
                dos.writeInt(encryptedData.encryptedBytes.length);
                dos.write(encryptedData.encryptedBytes);

                // نوشتن امضا
                dos.writeUTF(signature);

                // نوشتن timestamp
                dos.writeLong(System.currentTimeMillis());

                // نوشتن metadata
                dos.writeUTF(key);
                dos.writeUTF(deviceId);
            }

            // ذخیره امضا برای تأیید بعدی
            dataSignatures.put(key, signature);

            // لاگ دسترسی
            logAccess(key, "SAVE");

            // بررسی امنیت
            runSecurityCheck();

            return true;

        } catch (Exception e) {
            logSecurityAlert("SAVE_FAILED", key, e.getMessage());
            return false;
        }
    }

    /**
     * بازیابی اطلاعات با تأیید یکپارچگی
     */
    public String loadData(String key, String masterPassword) {
        try {
            String filename = storagePath + hashKey(key) + ".dat";
            File file = new File(filename);

            if (!file.exists()) {
                return null;
            }

            // خواندن فایل
            try (DataInputStream dis = new DataInputStream(
                    new FileInputStream(filename))) {

                // خواندن IV
                int ivLength = dis.readInt();
                byte[] iv = new byte[ivLength];
                dis.readFully(iv);

                // خواندن داده رمزنگاری شده
                int dataLength = dis.readInt();
                byte[] encryptedData = new byte[dataLength];
                dis.readFully(encryptedData);

                // خواندن امضا
                String storedSignature = dis.readUTF();

                // خواندن timestamp
                long timestamp = dis.readLong();

                // خواندن metadata
                String storedKey = dis.readUTF();
                String storedDeviceId = dis.readUTF();

                // تأیید یکپارچگی
                if (!validateIntegrity(key, encryptedData, storedSignature,
                        storedDeviceId, masterPassword, timestamp)) {
                    logSecurityAlert("INTEGRITY_CHECK_FAILED", key, "Data tampered");
                    clearSensitiveData(); // پاک کردن داده‌های حساس
                    return null;
                }

                // رمزگشایی داده
                SecretKey secretKey = deriveKey(masterPassword);
                String decryptedData = decryptData(encryptedData, iv, secretKey);

                // لاگ دسترسی موفق
                logAccess(key, "LOAD_SUCCESS");

                return decryptedData;
            }

        } catch (Exception e) {
            logSecurityAlert("LOAD_FAILED", key, e.getMessage());

            // اگر مشکوک به حمله باشیم، داده‌ها را پاک می‌کنیم
            if (e instanceof SecurityException || e.getMessage().contains("corrupt")) {
                clearSensitiveData();
            }

            return null;
        }
    }

    /**
     * بررسی آیا اطلاعات لاگین ذخیره شده وجود دارد
     */
    public boolean hasCachedLogin() {
        String[] loginKeys = {"last_username", "login_token", "user_profile"};

        for (String key : loginKeys) {
            String filename = storagePath + hashKey(key) + ".dat";
            if (new File(filename).exists()) {
                return true;
            }
        }

        return false;
    }

    /**
     * ذخیره اطلاعات لاگین
     */
    public boolean cacheLoginData(String username, String token,
                                  Map<String, String> profile, String masterPassword) {
        try {
            // ذخیره username
            saveData("last_username", username, masterPassword);

            // ذخیره token
            saveData("login_token", token, masterPassword);

            // ذخیره پروفایل
            StringBuilder profileBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : profile.entrySet()) {
                profileBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append(";");
            }
            saveData("user_profile", profileBuilder.toString(), masterPassword);

            // ذخیره timestamp
            saveData("last_login_time", String.valueOf(System.currentTimeMillis()),
                    masterPassword);

            // ذخیره device info
            saveData("login_device_info", getDeviceInfo(), masterPassword);

            // ایجاد فایل لاگ
            logAccess("LOGIN_CACHE", "CACHE_CREATED");

            return true;

        } catch (Exception e) {
            logSecurityAlert("CACHE_LOGIN_FAILED", username, e.getMessage());
            return false;
        }
    }

    /**
     * بازیابی اطلاعات لاگین
     */
    public LoginCacheData getCachedLogin(String masterPassword) {
        try {
            String username = loadData("last_username", masterPassword);
            String token = loadData("login_token", masterPassword);
            String profileStr = loadData("user_profile", masterPassword);
            String lastLoginTime = loadData("last_login_time", masterPassword);
            String deviceInfo = loadData("login_device_info", masterPassword);

            if (username == null || token == null) {
                return null;
            }

            // تجزیه پروفایل
            Map<String, String> profile = new HashMap<>();
            if (profileStr != null) {
                String[] pairs = profileStr.split(";");
                for (String pair : pairs) {
                    if (pair.contains("=")) {
                        String[] keyValue = pair.split("=", 2);
                        if (keyValue.length == 2) {
                            profile.put(keyValue[0], keyValue[1]);
                        }
                    }
                }
            }

            // بررسی زمان انقضا (7 روز)
            if (lastLoginTime != null) {
                long lastLogin = Long.parseLong(lastLoginTime);
                long sevenDays = 7 * 24 * 60 * 60 * 1000L;

                if (System.currentTimeMillis() - lastLogin > sevenDays) {
                    clearLoginCache();
                    logSecurityAlert("CACHE_EXPIRED", username,
                            "Cache expired after 7 days");
                    return null;
                }
            }

            // بررسی تغییر دستگاه
            if (deviceInfo != null && !deviceInfo.equals(getDeviceInfo())) {
                logSecurityAlert("DEVICE_CHANGED", username,
                        "Device changed, clearing cache");
                clearLoginCache();
                return null;
            }

            logAccess("LOGIN_CACHE", "CACHE_USED");

            return new LoginCacheData(username, token, profile);

        } catch (Exception e) {
            logSecurityAlert("LOAD_CACHE_FAILED", "unknown", e.getMessage());
            return null;
        }
    }

    /**
     * پاک کردن کش لاگین
     */
    public void clearLoginCache() {
        String[] loginKeys = {"last_username", "login_token", "user_profile",
                "last_login_time", "login_device_info"};

        for (String key : loginKeys) {
            String filename = storagePath + hashKey(key) + ".dat";
            new File(filename).delete();
        }

        logAccess("LOGIN_CACHE", "CACHE_CLEARED");
    }

    /**
     * پاک کردن تمام داده‌های حساس در صورت تشخیص نفوذ
     */
    public void clearSensitiveData() {
        System.out.println("⚠️ پاک کردن داده‌های حensitive به دلیل تشخیص نفوذ");

        File dir = new File(storagePath);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".dat")) {
                        // بازنویسی فایل با داده تصادفی قبل از حذف
                        secureDelete(file);
                    }
                }
            }
        }

        dataSignatures.clear();
        logSecurityAlert("DATA_WIPED", "ALL", "Sensitive data cleared due to security threat");
    }

    /**
     * حذف امن فایل با بازنویسی
     */
    private void secureDelete(File file) {
        try {
            // بازنویسی با داده تصادفی
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            long length = raf.length();

            SecureRandom random = new SecureRandom();
            byte[] randomData = new byte[1024];

            for (long i = 0; i < length; i += randomData.length) {
                random.nextBytes(randomData);
                raf.write(randomData);
            }

            raf.close();

            // تغییر نام و حذف
            String newName = file.getAbsolutePath() + ".deleted";
            file.renameTo(new File(newName));
            new File(newName).delete();

        } catch (Exception e) {
            // حذف ساده در صورت خطا
            file.delete();
        }
    }

    /**
     * دریافت گزارش امنیتی
     */
    public List<String> getSecurityReport() {
        List<String> report = new ArrayList<>();

        report.add("=== گزارش امنیتی ذخیره‌سازی ===");
        report.add("Device ID: " + deviceId);
        report.add("Storage Path: " + storagePath);
        report.add("Total Access Logs: " + accessLog.size());
        report.add("Security Alerts: " + securityAlerts.size());

        if (!securityAlerts.isEmpty()) {
            report.add("\nاخطارهای امنیتی:");
            for (int i = 0; i < Math.min(securityAlerts.size(), 10); i++) {
                report.add("  " + (i + 1) + ". " + securityAlerts.get(i));
            }
        }

        return report;
    }

    // --- متدهای رمزنگاری ---

    private SecretKey deriveKey(String password) throws Exception {
        // استفاده از salt ثابت برای هر دستگاه (بر اساس deviceId)
        byte[] salt = Arrays.copyOf(deviceId.getBytes(), SALT_LENGTH);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_SIZE
        );

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private EncryptedData encryptData(String plainText, SecretKey key) throws Exception {
        byte[] iv = generateIV();

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));

        return new EncryptedData(iv, encryptedBytes);
    }

    private String decryptData(byte[] encryptedData, byte[] iv, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes, "UTF-8");
    }

    private String generateSignature(String key, byte[] data, String password) throws Exception {
        // ترکیب key + data + password + timestamp + deviceId
        String toSign = key +
                Base64.getEncoder().encodeToString(data) +
                password +
                System.currentTimeMillis() +
                deviceId;

        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hash = digest.digest(toSign.getBytes("UTF-8"));

        // چندین بار هش کردن
        for (int i = 0; i < 1000; i++) {
            hash = digest.digest(hash);
        }

        return Base64.getEncoder().encodeToString(hash);
    }

    // --- متدهای امنیتی ---

    private boolean validateIntegrity(String key, byte[] data, String storedSignature,
                                      String storedDeviceId, String password,
                                      long timestamp) throws Exception {

        // بررسی timestamp (نمی‌تواند از آینده باشد)
        if (timestamp > System.currentTimeMillis() + 60000) { // 1 دقیقه تلرانس
            return false;
        }

        // بررسی deviceId
        if (!storedDeviceId.equals(deviceId)) {
            return false;
        }

        // محاسبه امضای جدید
        String toSign = key +
                Base64.getEncoder().encodeToString(data) +
                password +
                timestamp +
                deviceId;

        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hash = digest.digest(toSign.getBytes("UTF-8"));

        for (int i = 0; i < 1000; i++) {
            hash = digest.digest(hash);
        }

        String calculatedSignature = Base64.getEncoder().encodeToString(hash);

        return calculatedSignature.equals(storedSignature);
    }

    private void runSecurityCheck() {
        try {
            // بررسی تعداد دسترسی‌های مشکوک
            long suspiciousAttempts = accessLog.values().stream()
                    .filter(time -> System.currentTimeMillis() - time < 60000) // در 1 دقیقه گذشته
                    .count();

            if (suspiciousAttempts > 10) {
                logSecurityAlert("BRUTE_FORCE_ATTEMPT", "SYSTEM",
                        "Multiple access attempts detected");
                clearSensitiveData();
            }

            // بررسی تغییرات فایل امنیتی
            checkSecurityFile();

        } catch (Exception e) {
            System.err.println("خطا در بررسی امنیتی: " + e.getMessage());
        }
    }

    private void createSecurityFile() throws Exception {
        String securityFile = storagePath + ".security_info";

        try (PrintWriter writer = new PrintWriter(securityFile)) {
            writer.println("VWM_SECURE_STORAGE_V1.0");
            writer.println("DeviceID: " + deviceId);
            writer.println("Created: " + System.currentTimeMillis());
            writer.println("LastCheck: " + System.currentTimeMillis());
        }

        // مخفی کردن فایل در سیستم‌عامل‌های مختلف
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            Runtime.getRuntime().exec("attrib +H \"" + securityFile + "\"");
        }
    }

    private void checkSecurityFile() throws Exception {
        String securityFile = storagePath + ".security_info";
        File file = new File(securityFile);

        if (!file.exists()) {
            logSecurityAlert("SECURITY_FILE_MISSING", "SYSTEM",
                    "Security file deleted or moved");
            clearSensitiveData();
            return;
        }

        // خواندن و بررسی فایل
        try (BufferedReader reader = new BufferedReader(new FileReader(securityFile))) {
            String line = reader.readLine();
            if (!"VWM_SECURE_STORAGE_V1.0".equals(line)) {
                logSecurityAlert("SECURITY_FILE_TAMPERED", "SYSTEM",
                        "Security file modified");
                clearSensitiveData();
            }
        }
    }

    // --- متدهای کمکی ---

    private String generateDeviceId() {
        try {
            String os = System.getProperty("os.name");
            String user = System.getProperty("user.name");
            String arch = System.getProperty("os.arch");

            String uniqueString = os + user + arch +
                    Runtime.getRuntime().availableProcessors() +
                    System.getProperty("user.home");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uniqueString.getBytes("UTF-8"));

            return Base64.getEncoder().encodeToString(hash).substring(0, 32);

        } catch (Exception e) {
            // اگر خطا داد، از یک شناسه تصادفی استفاده کن
            return "dev_" + System.currentTimeMillis() + "_" +
                    new SecureRandom().nextInt(10000);
        }
    }

    public String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash)
                    .replace("/", "_").replace("+", "-").substring(0, 16);
        } catch (Exception e) {
            return Integer.toHexString(key.hashCode());
        }
    }

    private byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private String getDeviceInfo() {
        return System.getProperty("os.name") + ";" +
                System.getProperty("os.version") + ";" +
                System.getProperty("user.name") + ";" +
                deviceId;
    }

    private void logAccess(String key, String action) {
        accessLog.put(key + "_" + action, System.currentTimeMillis());
    }

    private void logSecurityAlert(String type, String target, String message) {
        String alert = "[" + new Date() + "] " + type + " | " + target + " | " + message;
        securityAlerts.add(alert);
        System.err.println("⚠️ " + alert);

        // ذخیره در فایل لاگ
        try {
            Files.write(Paths.get(storagePath + "security_alerts.log"),
                    Arrays.asList(alert),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (Exception e) {
            // ignore
        }
    }

    // --- کلاس‌های داخلی ---

    private static class EncryptedData {
        byte[] iv;
        byte[] encryptedBytes;

        EncryptedData(byte[] iv, byte[] encryptedBytes) {
            this.iv = iv;
            this.encryptedBytes = encryptedBytes;
        }
    }

    public static class LoginCacheData {
        public final String username;
        public final String token;
        public final Map<String, String> profile;

        LoginCacheData(String username, String token, Map<String, String> profile) {
            this.username = username;
            this.token = token;
            this.profile = profile;
        }
    }
}