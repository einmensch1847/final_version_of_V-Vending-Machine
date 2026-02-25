//package api;
//
//public class CacheInitializer {
//
//    private static boolean initialized = false;
//
//    public static synchronized void initialize() {
//        if (initialized) return;
//
//        System.out.println("🚀 Initializing CacheManager...");
//
//        // گرم کردن کش (preload برخی داده‌ها)
//        new Thread(() -> {
//            try {
//                CacheManager cache = CacheManager.getInstance();
//                CachedApiClient client = new CachedApiClient();
//
//                // کش کردن داده‌های پرکاربرد
//                System.out.println("📦 Preloading common data...");
//
//                // لیست تبلیغات
//                client.getAdImages();
//
//                // دکمه‌های پیش‌فرض
//                client.getDefaultButtons();
//
//                // پس‌زمینه‌ها
//                client.getBackgroundImages();
//
//                System.out.println("✅ Cache preloading completed!");
//
//            } catch (Exception e) {
//                System.err.println("⚠️ Cache preloading error: " + e.getMessage());
//            }
//        }).start();
//
//        // ثبت shutdown hook برای پاکسازی
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            System.out.println("🔄 Shutting down CacheManager...");
//            CacheManager.getInstance().clearAll();
//        }));
//
//        initialized = true;
//    }
//}