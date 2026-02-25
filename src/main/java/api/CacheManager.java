//package api;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.time.LocalDateTime;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//public class CacheManager {
//
//    private static CacheManager instance;
//    private final String cacheDir;
//    private final ConcurrentHashMap<String, CacheEntry> memoryCache;
//    private final ScheduledExecutorService cleanupScheduler;
//
//    // تنظیمات کش
//    private static final long MAX_MEMORY_ENTRIES = 100;
//    private static final long MAX_DISK_SIZE_MB = 500;
//    private static final long DEFAULT_EXPIRY_MINUTES = 30;
//
//    private CacheManager() {
//        // ایجاد پوشه کش در دایرکتوری temp سیستم
//        String userHome = System.getProperty("user.home");
//        this.cacheDir = userHome + File.separator + ".vwm_cache";
//        new File(cacheDir).mkdirs();
//
//        this.memoryCache = new ConcurrentHashMap<>();
//        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
//
//        // پاکسازی دوره‌ای کش (هر 1 ساعت)
//        cleanupScheduler.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.HOURS);
//
//        System.out.println("✅ CacheManager initialized at: " + cacheDir);
//    }
//
//    public static synchronized CacheManager getInstance() {
//        if (instance == null) {
//            instance = new CacheManager();
//        }
//        return instance;
//    }
//
//    // ==================== کلاس داخلی برای entries کش ====================
//    private static class CacheEntry {
//        private final byte[] data;
//        private final LocalDateTime timestamp;
//        private final String contentType;
//        private final long expiryMinutes;
//
//        public CacheEntry(byte[] data, String contentType) {
//            this(data, contentType, DEFAULT_EXPIRY_MINUTES);
//        }
//
//        public CacheEntry(byte[] data, String contentType, long expiryMinutes) {
//            this.data = data;
//            this.timestamp = LocalDateTime.now();
//            this.contentType = contentType;
//            this.expiryMinutes = expiryMinutes;
//        }
//
//        public boolean isExpired() {
//            return timestamp.plusMinutes(expiryMinutes).isBefore(LocalDateTime.now());
//        }
//
//        public byte[] getData() { return data; }
//        public String getContentType() { return contentType; }
//    }
//
//    // ==================== کلاس پاسخ کش شده ====================
//    public static class CachedResponse {
//        private final byte[] data;
//        private final String contentType;
//        private final boolean fromMemory;
//
//        public CachedResponse(byte[] data, String contentType, boolean fromMemory) {
//            this.data = data;
//            this.contentType = contentType;
//            this.fromMemory = fromMemory;
//        }
//
//        public byte[] getData() { return data; }
//        public String getContentType() { return contentType; }
//        public boolean isFromMemory() { return fromMemory; }
//
//        public String getText() {
//            try {
//                return new String(data, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                return new String(data);
//            }
//        }
//    }
//
//    // ==================== متدهای اصلی ====================
//
//    /**
//     * دریافت داده از کش (اگر وجود داشته باشد)
//     */
//    public CachedResponse get(String url) {
//        String key = generateKey(url);
//
//        // 1. بررسی در حافظه RAM
//        CacheEntry memoryEntry = memoryCache.get(key);
//        if (memoryEntry != null && !memoryEntry.isExpired()) {
//            System.out.println("✅ Cache HIT (memory): " + url);
//            return new CachedResponse(memoryEntry.getData(), memoryEntry.getContentType(), true);
//        }
//
//        // 2. بررسی در دیسک
//        CachedResponse diskResponse = getFromDisk(key);
//        if (diskResponse != null) {
//            System.out.println("✅ Cache HIT (disk): " + url);
//            // انتقال به حافظه برای دسترسی سریع‌تر
//            memoryCache.put(key, new CacheEntry(diskResponse.getData(), diskResponse.getContentType()));
//            return diskResponse;
//        }
//
//        System.out.println("❌ Cache MISS: " + url);
//        return null;
//    }
//
//    /**
//     * دریافت داده از کش با کلید مستقیم
//     */
//    public CachedResponse getByKey(String key) {
//        // 1. بررسی در حافظه RAM
//        CacheEntry memoryEntry = memoryCache.get(key);
//        if (memoryEntry != null && !memoryEntry.isExpired()) {
//            return new CachedResponse(memoryEntry.getData(), memoryEntry.getContentType(), true);
//        }
//
//        // 2. بررسی در دیسک
//        CachedResponse diskResponse = getFromDisk(key);
//        if (diskResponse != null) {
//            memoryCache.put(key, new CacheEntry(diskResponse.getData(), diskResponse.getContentType()));
//            return diskResponse;
//        }
//
//        return null;
//    }
//
//    /**
//     * ذخیره داده در کش
//     */
//    public void put(String url, byte[] data, String contentType) {
//        if (data == null || data.length == 0) return;
//
//        String key = generateKey(url);
//
//        // ذخیره در حافظه
//        memoryCache.put(key, new CacheEntry(data, contentType));
//
//        // ذخیره در دیسک (در پس‌زمینه)
//        saveToDiskAsync(key, data);
//
//        // مدیریت اندازه حافظه
//        if (memoryCache.size() > MAX_MEMORY_ENTRIES) {
//            removeOldestMemoryEntries();
//        }
//    }
//
//    /**
//     * پاک کردن کش برای یک URL خاص
//     */
//    public void invalidate(String url) {
//        String key = generateKey(url);
//        memoryCache.remove(key);
//        deleteFromDisk(key);
//    }
//
//    /**
//     * پاک کردن کش بر اساس پیشوند کلید
//     */
//    public void invalidateByPrefix(String prefix) {
//        System.out.println("🧹 Invalidating cache with prefix: " + prefix);
//
//        // پاک کردن از حافظه
//        memoryCache.keySet().removeIf(key -> key.startsWith(prefix));
//
//        // پاک کردن از دیسک (در پس‌زمینه)
//        new Thread(() -> {
//            File cacheFolder = new File(cacheDir);
//            File[] files = cacheFolder.listFiles((dir, name) -> name.startsWith(prefix));
//            if (files != null) {
//                for (File file : files) {
//                    file.delete();
//                }
//            }
//        }).start();
//    }
//
//    /**
//     * پاک کردن همه کش
//     */
//    public void clearAll() {
//        memoryCache.clear();
//
//        // پاک کردن فایل‌های دیسک در پس‌زمینه
//        new Thread(() -> {
//            File cacheFolder = new File(cacheDir);
//            File[] files = cacheFolder.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    file.delete();
//                }
//            }
//            System.out.println("🧹 Cache cleared");
//        }).start();
//    }
//
//    // ==================== متدهای کمکی ====================
//
//    private String generateKey(String url) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            byte[] hash = md.digest(url.getBytes());
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : hash) {
//                hexString.append(String.format("%02x", b));
//            }
//            return hexString.toString();
//        } catch (NoSuchAlgorithmException e) {
//            // fallback به خود URL
//            return url.replaceAll("[^a-zA-Z0-9]", "_");
//        }
//    }
//
//    private CachedResponse getFromDisk(String key) {
//        try {
//            File cacheFile = new File(cacheDir + File.separator + key + ".cache");
//            File metaFile = new File(cacheDir + File.separator + key + ".meta");
//
//            if (!cacheFile.exists() || !metaFile.exists()) return null;
//
//            // خواندن متادیتا
//            Properties meta = new Properties();
//            try (FileInputStream fis = new FileInputStream(metaFile)) {
//                meta.load(fis);
//            }
//
//            // بررسی انقضا
//            long timestamp = Long.parseLong(meta.getProperty("timestamp", "0"));
//            long expiry = Long.parseLong(meta.getProperty("expiry", String.valueOf(DEFAULT_EXPIRY_MINUTES)));
//
//            if (System.currentTimeMillis() - timestamp > expiry * 60 * 1000) {
//                cacheFile.delete();
//                metaFile.delete();
//                return null;
//            }
//
//            // خواندن داده
//            byte[] data = Files.readAllBytes(cacheFile.toPath());
//            String contentType = meta.getProperty("contentType", "application/octet-stream");
//
//            return new CachedResponse(data, contentType, false);
//
//        } catch (Exception e) {
//            System.err.println("Error reading from disk cache: " + e.getMessage());
//            return null;
//        }
//    }
//
//    private void saveToDiskAsync(String key, byte[] data) {
//        new Thread(() -> {
//            try {
//                File cacheFile = new File(cacheDir + File.separator + key + ".cache");
//                File metaFile = new File(cacheDir + File.separator + key + ".meta");
//
//                // ذخیره داده
//                Files.write(cacheFile.toPath(), data);
//
//                // ذخیره متادیتا
//                Properties meta = new Properties();
//                meta.setProperty("timestamp", String.valueOf(System.currentTimeMillis()));
//                meta.setProperty("expiry", String.valueOf(DEFAULT_EXPIRY_MINUTES));
//                meta.setProperty("contentType", "text/plain");
//
//                try (FileOutputStream fos = new FileOutputStream(metaFile)) {
//                    meta.store(fos, null);
//                }
//
//            } catch (IOException e) {
//                System.err.println("Error saving to disk cache: " + e.getMessage());
//            }
//        }).start();
//    }
//
//    private void deleteFromDisk(String key) {
//        new File(cacheDir + File.separator + key + ".cache").delete();
//        new File(cacheDir + File.separator + key + ".meta").delete();
//    }
//
//    private void removeOldestMemoryEntries() {
//        // حذف قدیمی‌ترین entries
//        memoryCache.entrySet()
//                .stream()
//                .limit(memoryCache.size() - MAX_MEMORY_ENTRIES)
//                .forEach(entry -> memoryCache.remove(entry.getKey()));
//    }
//
//    private void cleanup() {
//        // پاکسازی حافظه
//        memoryCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
//
//        // پاکسازی دیسک
//        new Thread(() -> {
//            File cacheFolder = new File(cacheDir);
//            File[] files = cacheFolder.listFiles();
//            if (files == null) return;
//
//            long totalSize = 0;
//            for (File file : files) {
//                totalSize += file.length();
//            }
//
//            // اگر حجم از حد مجاز بیشتر بود، قدیمی‌ترین فایل‌ها را حذف کن
//            if (totalSize > MAX_DISK_SIZE_MB * 1024 * 1024) {
//                java.util.List<File> fileList = new java.util.ArrayList<>(java.util.List.of(files));
//                fileList.sort((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
//
//                while (totalSize > MAX_DISK_SIZE_MB * 1024 * 1024 && !fileList.isEmpty()) {
//                    File oldest = fileList.remove(0);
//                    totalSize -= oldest.length();
//                    oldest.delete();
//                }
//            }
//        }).start();
//    }
//
//    // ==================== کلاس Properties ساده ====================
//
//    private static class Properties {
//        private final java.util.Properties props = new java.util.Properties();
//
//        public void load(FileInputStream fis) throws IOException {
//            props.load(fis);
//        }
//
//        public void store(FileOutputStream fos, String comment) throws IOException {
//            props.store(fos, comment);
//        }
//
//        public String getProperty(String key, String defaultValue) {
//            return props.getProperty(key, defaultValue);
//        }
//
//        public void setProperty(String key, String value) {
//            props.setProperty(key, value);
//        }
//    }
//}