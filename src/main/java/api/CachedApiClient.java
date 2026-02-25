//package api;
//
//import View.ManageProductsPage;
//import View.ManageUsersPage;
//import View.UserPanel;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CachedApiClient extends ApiClient {
//
//    private final CacheManager cache = CacheManager.getInstance();
//
//    // ==================== متدهای GET با کش ====================
//
//    @Override
//    public List<String> getAvailableBackgrounds() {
//        String cacheKey = "getAvailableBackgrounds";
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseStringList(cached.getText());
//        }
//
//        List<String> result = super.getAvailableBackgrounds();
//        if (result != null && !result.isEmpty()) {
//            cache.put(cacheKey, listToString(result).getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public List<BackgroundImage> getBackgroundImages() {
//        String cacheKey = "getBackgroundImages";
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseBackgroundImages(cached.getText());
//        }
//
//        List<BackgroundImage> result = super.getBackgroundImages();
//        if (result != null && !result.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (BackgroundImage bg : result) {
//                sb.append(bg.id).append(";").append(bg.filename).append(";").append(bg.isDefault ? "1" : "0").append("\n");
//            }
//            cache.put(cacheKey, sb.toString().getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public List<ButtonItem> getDefaultButtons() {
//        String cacheKey = "getDefaultButtons";
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseButtonItems(cached.getText());
//        }
//
//        List<ButtonItem> result = super.getDefaultButtons();
//        if (result != null && !result.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (ButtonItem btn : result) {
//                sb.append(btn.getId()).append(";")
//                        .append(btn.getTitle()).append(";")
//                        .append(btn.getCaption()).append(";")
//                        .append(btn.getImage()).append(";")
//                        .append(btn.getPrice()).append(";")
//                        .append(btn.getSweetness()).append(";")
//                        .append(btn.getCaffeine()).append(";")
//                        .append(btn.getTemperature()).append(";")
//                        .append(btn.getStock()).append("\n");
//            }
//            cache.put(cacheKey, sb.toString().getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public List<ManageUsersPage.UserModel> getAllUsers() {
//        String cacheKey = "getAllUsers";
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseUsers(cached.getText());
//        }
//
//        List<ManageUsersPage.UserModel> result = super.getAllUsers();
//        if (result != null && !result.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (ManageUsersPage.UserModel user : result) {
//                sb.append(user.getId()).append(";")
//                        .append(user.getUsername()).append(";")
//                        .append(user.getFullname()).append(";")
//                        .append(user.getPhone()).append(";")
//                        .append(user.getEmail()).append(";")
//                        .append(user.getAddress()).append(";")
//                        .append(user.getCompanyName()).append(";")
//                        .append(user.getCeoName()).append(";")
//                        .append(user.getCeoPhone()).append(";")
//                        .append(user.getCeoEmail()).append(";")
//                        .append(user.getDeviceLocation()).append(";")
//                        .append(user.getBackgroundImage()).append(";")
//                        .append(user.getCreatedAt()).append(";")
//                        .append(user.getCreatedByAdmin()).append("\n");
//            }
//            cache.put(cacheKey, sb.toString().getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public List<ManageProductsPage.ButtonModel> getAllButtons() {
//        String cacheKey = "getAllButtons";
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseAllButtons(cached.getText());
//        }
//
//        List<ManageProductsPage.ButtonModel> result = super.getAllButtons();
//        if (result != null && !result.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (ManageProductsPage.ButtonModel btn : result) {
//                sb.append(btn.getId()).append(";")
//                        .append(btn.getTitle()).append(";")
//                        .append(btn.getCaption()).append(";")
//                        .append(btn.getImage()).append(";")
//                        .append(btn.getPrice()).append(";")
//                        .append(btn.getSweetnessLevel()).append(";")
//                        .append(btn.getCaffeineLevel()).append(";")
//                        .append(btn.getTemperatureLevel()).append(";")
//                        .append(btn.getStock()).append(";")
//                        .append(btn.getUserId()).append(";")
//                        .append(btn.getUserName()).append("\n");
//            }
//            cache.put(cacheKey, sb.toString().getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public List<UserPanel.ButtonModel> getUserButtons(int userId) {
//        String cacheKey = "getUserButtons_" + userId;
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseUserButtons(cached.getText());
//        }
//
//        List<UserPanel.ButtonModel> result = super.getUserButtons(userId);
//        if (result != null && !result.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (UserPanel.ButtonModel btn : result) {
//                sb.append(btn.getId()).append(";")
//                        .append(btn.getTitle()).append(";")
//                        .append(btn.getCaption()).append(";")
//                        .append(btn.getImage()).append(";")
//                        .append(btn.getPrice()).append(";")
//                        .append(btn.getSweetness()).append(";")
//                        .append(btn.getCaffeine()).append(";")
//                        .append(btn.getTemperature()).append(";")
//                        .append(btn.getStock()).append("\n");
//            }
//            cache.put(cacheKey, sb.toString().getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public List<UserLoginLog> getUserLoginLogs(int userId, String action, int limit) {
//        String cacheKey = "getUserLoginLogs_" + userId + "_" + action + "_" + limit;
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseUserLoginLogs(cached.getText());
//        }
//
//        List<UserLoginLog> result = super.getUserLoginLogs(userId, action, limit);
//        if (result != null && !result.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (UserLoginLog log : result) {
//                sb.append(log.getId()).append("|")
//                        .append(log.getUserId()).append("|")
//                        .append(log.getUsername()).append("|")
//                        .append(log.getFullname()).append("|")
//                        .append(log.getAction()).append("|")
//                        .append(log.getIpAddress()).append("|")
//                        .append(log.getUserAgent()).append("|")
//                        .append(log.getDeviceInfo()).append("|")
//                        .append(log.getStatus()).append("|")
//                        .append(log.getErrorMessage()).append("|")
//                        .append(log.getCreatedAt()).append("\n");
//            }
//            cache.put(cacheKey, sb.toString().getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public List<SalesLog> getSalesLogs(int userId, String username, int buttonId,
//                                       String status, String fromDate, String toDate, int limit) {
//        String cacheKey = "getSalesLogs_" + userId + "_" + username + "_" + buttonId + "_" +
//                status + "_" + fromDate + "_" + toDate + "_" + limit;
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseSalesLogs(cached.getText());
//        }
//
//        List<SalesLog> result = super.getSalesLogs(userId, username, buttonId, status, fromDate, toDate, limit);
//        if (result != null && !result.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (SalesLog log : result) {
//                sb.append(log.getId()).append("|")
//                        .append(log.getUserId()).append("|")
//                        .append(log.getUsername()).append("|")
//                        .append(log.getFullname()).append("|")
//                        .append(log.getButtonId()).append("|")
//                        .append(log.getButtonTitle()).append("|")
//                        .append(log.getButtonImage()).append("|")
//                        .append(log.getQuantity()).append("|")
//                        .append(log.getPricePerUnit()).append("|")
//                        .append(log.getTotalPrice()).append("|")
//                        .append(log.getSweetnessLevel()).append("|")
//                        .append(log.getCaffeineLevel()).append("|")
//                        .append(log.getTemperatureLevel()).append("|")
//                        .append(log.getPaymentMethod()).append("|")
//                        .append(log.getStatus()).append("|")
//                        .append(log.getCreatedAt()).append("\n");
//            }
//            cache.put(cacheKey, sb.toString().getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public List<String> getAdImages() {
//        String cacheKey = "getAdImages";
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseStringList(cached.getText());
//        }
//
//        List<String> result = super.getAdImages();
//        if (result != null && !result.isEmpty()) {
//            cache.put(cacheKey, listToString(result).getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public List<String> getAllAdmins() {
//        String cacheKey = "getAllAdmins";
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return parseStringList(cached.getText());
//        }
//
//        List<String> result = super.getAllAdmins();
//        if (result != null && !result.isEmpty()) {
//            cache.put(cacheKey, listToString(result).getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    @Override
//    public String getLogs() {
//        String cacheKey = "getLogs";
//        CacheManager.CachedResponse cached = cache.get(cacheKey);
//        if (cached != null) {
//            return cached.getText();
//        }
//
//        String result = super.getLogs();
//        if (result != null && !result.isEmpty()) {
//            cache.put(cacheKey, result.getBytes(), "text/plain");
//        }
//        return result;
//    }
//
//    // ==================== متدهای کمکی برای پارس کردن ====================
//
//    private List<String> parseStringList(String text) {
//        List<String> list = new ArrayList<>();
//        String[] lines = text.split("\n");
//        for (String line : lines) {
//            if (!line.trim().isEmpty()) {
//                list.add(line.trim());
//            }
//        }
//        return list;
//    }
//
//    private String listToString(List<String> list) {
//        StringBuilder sb = new StringBuilder();
//        for (String item : list) {
//            sb.append(item).append("\n");
//        }
//        return sb.toString();
//    }
//
//    private List<BackgroundImage> parseBackgroundImages(String text) {
//        List<BackgroundImage> backgrounds = new ArrayList<>();
//        String[] lines = text.split("\n");
//        for (String line : lines) {
//            if (line.trim().isEmpty()) continue;
//            String[] parts = line.split(";");
//            if (parts.length >= 2) {
//                BackgroundImage bg = new BackgroundImage();
//                bg.id = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
//                bg.filename = parts[1];
//                bg.isDefault = parts.length > 2 && parts[2].equals("1");
//                backgrounds.add(bg);
//            }
//        }
//        return backgrounds;
//    }
//
//    private List<ButtonItem> parseButtonItems(String text) {
//        List<ButtonItem> buttons = new ArrayList<>();
//        String[] lines = text.split("\n");
//        for (String line : lines) {
//            if (line.trim().isEmpty()) continue;
//            String[] parts = line.split(";");
//            if (parts.length >= 9) {
//                buttons.add(new ButtonItem(
//                        Integer.parseInt(parts[0]), parts[1], parts[2], parts[3],
//                        Double.parseDouble(parts[4]), Integer.parseInt(parts[5]),
//                        Integer.parseInt(parts[6]), Integer.parseInt(parts[7]),
//                        Integer.parseInt(parts[8])
//                ));
//            }
//        }
//        return buttons;
//    }
//
//    private List<ManageUsersPage.UserModel> parseUsers(String text) {
//        List<ManageUsersPage.UserModel> users = new ArrayList<>();
//        String[] lines = text.split("\n");
//        for (String line : lines) {
//            if (line.trim().isEmpty()) continue;
//            String[] parts = line.split(";");
//            if (parts.length >= 14) {
//                users.add(new ManageUsersPage.UserModel(
//                        parts[0], parts[1], parts[2], parts[3], parts[4],
//                        parts[5], parts[6], parts[7], parts[8], parts[9],
//                        parts[10], parts[11], parts[12], parts[13]
//                ));
//            }
//        }
//        return users;
//    }
//
//    private List<ManageProductsPage.ButtonModel> parseAllButtons(String text) {
//        List<ManageProductsPage.ButtonModel> buttons = new ArrayList<>();
//        String[] lines = text.split("\n");
//        for (String line : lines) {
//            if (line.trim().isEmpty()) continue;
//            String[] parts = line.split(";");
//            if (parts.length >= 11) {
//                buttons.add(new ManageProductsPage.ButtonModel(
//                        Integer.parseInt(parts[0]), parts[1], parts[2], parts[3],
//                        Double.parseDouble(parts[4]), Integer.parseInt(parts[5]),
//                        Integer.parseInt(parts[6]), Integer.parseInt(parts[7]),
//                        Integer.parseInt(parts[8]), Integer.parseInt(parts[9]), parts[10]
//                ));
//            }
//        }
//        return buttons;
//    }
//
//    private List<UserPanel.ButtonModel> parseUserButtons(String text) {
//        List<UserPanel.ButtonModel> buttons = new ArrayList<>();
//        String[] lines = text.split("\n");
//        for (String line : lines) {
//            if (line.trim().isEmpty()) continue;
//            String[] parts = line.split(";");
//            if (parts.length >= 9) {
//                buttons.add(new UserPanel.ButtonModel(
//                        Integer.parseInt(parts[0]), parts[1], parts[2], parts[3],
//                        Double.parseDouble(parts[4]), Integer.parseInt(parts[5]),
//                        Integer.parseInt(parts[6]), Integer.parseInt(parts[7]),
//                        Integer.parseInt(parts[8])
//                ));
//            }
//        }
//        return buttons;
//    }
//
//    private List<UserLoginLog> parseUserLoginLogs(String text) {
//        List<UserLoginLog> logs = new ArrayList<>();
//        String[] lines = text.split("\n");
//        for (String line : lines) {
//            if (line.trim().isEmpty()) continue;
//            String[] parts = line.split("\\|");
//            if (parts.length >= 11) {
//                logs.add(new UserLoginLog(
//                        Integer.parseInt(parts[0]),
//                        Integer.parseInt(parts[1]),
//                        parts[2], parts[3], parts[4], parts[5],
//                        parts[6], parts[7], parts[8],
//                        parts.length > 9 ? parts[9] : "", parts[10]
//                ));
//            }
//        }
//        return logs;
//    }
//
//    private List<SalesLog> parseSalesLogs(String text) {
//        List<SalesLog> logs = new ArrayList<>();
//        String[] lines = text.split("\n");
//        for (String line : lines) {
//            if (line.trim().isEmpty()) continue;
//            String[] parts = line.split("\\|");
//            if (parts.length >= 16) {
//                logs.add(new SalesLog(
//                        Integer.parseInt(parts[0]),
//                        Integer.parseInt(parts[1]),
//                        parts[2],
//                        parts[3],
//                        Integer.parseInt(parts[4]),
//                        parts[5],
//                        parts[6],
//                        Integer.parseInt(parts[7]),
//                        Double.parseDouble(parts[8]),
//                        Double.parseDouble(parts[9]),
//                        Integer.parseInt(parts[10]),
//                        Integer.parseInt(parts[11]),
//                        Integer.parseInt(parts[12]),
//                        parts[13],
//                        parts[14],
//                        parts[15]
//                ));
//            }
//        }
//        return logs;
//    }
//}