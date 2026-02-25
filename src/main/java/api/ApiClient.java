package api;

import View.Admin;
import View.ManageProductsPage;
import View.ManageUsersPage;
import View.UserPanel;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {

    private static final String BASE_URL = "https://menschwoodworks.ir/API/";

    public String login(String username, String password, boolean isAdmin) {
        try {
            String endpoint = isAdmin ? "admin_login.php" : "login.php";
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "username=" + username + "&password=" + password;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes());
                os.flush();
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
                return responseBuilder.toString().trim();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "CONNECTION_FAILED";
        }
    }

    // --- اضافه کردن ادمین جدید ---
    public boolean addAdmin(String fullname, String username, String password,
                            String email, String phone, String level) {
        try {
            URL url = new URL(BASE_URL + "addAdmin.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "fullname=" + URLEncoder.encode(fullname, "UTF-8") +
                    "&username=" + URLEncoder.encode(username, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8") +
                    "&email=" + URLEncoder.encode(email, "UTF-8") +
                    "&phone=" + URLEncoder.encode(phone, "UTF-8") +
                    "&level=" + URLEncoder.encode(level, "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public ArrayList<String> getAdminList() {
        ArrayList<String> list = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getAllAdmins.php");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            while ((line = br.readLine()) != null) {
                list.add(line.trim());
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- حذف ادمین ---
    public boolean deleteAdmin(String username) {
        try {
            URL url = new URL(BASE_URL + "deleteAdmin.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "username=" + URLEncoder.encode(username, "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- گرفتن لیست ادمین‌ها ---
    public List<String> getAllAdmins() {
        List<String> list = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getAllAdmins.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line.trim()); // فرض: هر خط یک username
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- گرفتن اطلاعات کامل یک ادمین ---
    public Admin getAdminDetails(String username) {
        try {
            URL url = new URL(BASE_URL + "getAdminDetails.php?username=" + URLEncoder.encode(username, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine();
            br.close();

            // پاسخ را با ; جدا شده فرض می‌کنیم: username;fullname;email;phone;level
            if (response != null && !response.isEmpty()) {
                String[] parts = response.split(";");
                if (parts.length == 5) {
                    return new Admin(parts[0], parts[1], parts[2], parts[3], parts[4]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- بروزرسانی ادمین ---
    public boolean updateAdmin(String username, String fullname, String email,
                               String phone, String level, String password) {
        try {
            URL url = new URL(BASE_URL + "updateAdmin.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data =
                    "username=" + URLEncoder.encode(username, "UTF-8") +
                            "&fullname=" + URLEncoder.encode(fullname, "UTF-8") +
                            "&email=" + URLEncoder.encode(email, "UTF-8") +
                            "&phone=" + URLEncoder.encode(phone, "UTF-8") +
                            "&level=" + URLEncoder.encode(level, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getLogs() {
        try {
            URL url = new URL(BASE_URL + "getLogs.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            in.close();
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // --- اضافه کردن کاربر ---
    public boolean addUser(String fullname, String username, String password,
                           String phone, String email, String address,
                           String companyName,
                           String ceoName, String ceoPhone, String ceoEmail,
                           String deviceLocation, String createdByAdmin) {

        try {
            // 🟢 اصلاح شده: set_user.php -> addUser.php
            URL url = new URL(BASE_URL + "addUser.php");
            System.out.println("Calling URL: " + url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            String data =
                    "fullname=" + URLEncoder.encode(fullname, "UTF-8") +
                            "&username=" + URLEncoder.encode(username, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8") +
                            "&phone=" + URLEncoder.encode(phone, "UTF-8") +
                            "&email=" + URLEncoder.encode(email, "UTF-8") +
                            "&address=" + URLEncoder.encode(address, "UTF-8") +
                            "&company_name=" + URLEncoder.encode(companyName, "UTF-8") +
                            "&ceo_name=" + URLEncoder.encode(ceoName, "UTF-8") +
                            "&ceo_phone=" + URLEncoder.encode(ceoPhone, "UTF-8") +
                            "&ceo_email=" + URLEncoder.encode(ceoEmail, "UTF-8") +
                            "&device_location=" + URLEncoder.encode(deviceLocation, "UTF-8") +
                            "&created_by_admin=" + URLEncoder.encode(createdByAdmin, "UTF-8");

            System.out.println("Sending data: " + data);

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            }

            String response = br.readLine().trim();
            System.out.println("Server Response: " + response);
            br.close();

            return response != null && response.equalsIgnoreCase("OK");

        } catch (Exception e) {
            System.err.println("Exception in addUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // در کلاس ApiClient این متدها را اضافه کنید:

    // --- دریافت لیست دکمه‌های موجود (برای کپی کردن) ---
    public List<String> getAvailableButtons() {
        List<String> buttons = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getButtons.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                buttons.add(line.trim());
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buttons;
    }

    // --- اضافه کردن دکمه برای کاربر ---
    public boolean addUserButton(int userId, int buttonId) {
        try {
            URL url = new URL(BASE_URL + "addUserButton.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "user_id=" + userId +
                    "&button_id=" + buttonId;

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- دریافت آخرین کاربر اضافه شده (برای گرفتن ID) ---
    public int getLastUserId() {
        try {
            URL url = new URL(BASE_URL + "getLastUserId.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine().trim();
            br.close();

            if (response != null && !response.isEmpty()) {
                return Integer.parseInt(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // --- بررسی تکراری نبودن نام کاربری ---
    public boolean checkUsernameAvailability(String username) {
        try {
            URL url = new URL(BASE_URL + "checkUsername.php?username=" + URLEncoder.encode(username, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("AVAILABLE");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- دریافت دکمه‌های پیش‌فرض (برای انتخاب) ---
    public List<ButtonItem> getDefaultButtons() {
        List<ButtonItem> buttons = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getDefaultButtons.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 9) {
                    ButtonItem button = new ButtonItem(
                            Integer.parseInt(parts[0]),
                            parts[1], parts[2], parts[3],
                            Double.parseDouble(parts[4]),
                            Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6]),
                            Integer.parseInt(parts[7]),
                            Integer.parseInt(parts[8])
                    );
                    buttons.add(button);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buttons;
    }

    // --- دریافت عکس‌های پس‌زمینه موجود ---
    public List<String> getAvailableBackgrounds() {
        List<String> backgrounds = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getBackgrounds.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                backgrounds.add(line.trim());
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backgrounds;
    }

    // --- اضافه کردن دکمه به کاربر ---
    public boolean assignButtonToUser(int userId, int buttonId) {
        try {
            URL url = new URL(BASE_URL + "assignButtonToUser.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "user_id=" + userId +
                    "&button_id=" + buttonId;

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- تنظیم عکس پس‌زمینه برای کاربر ---
    public boolean setUserBackground(int userId, String backgroundImage) {
        try {
            URL url = new URL(BASE_URL + "setUserBackground.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "user_id=" + userId +
                    "&background_image=" + URLEncoder.encode(backgroundImage, "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // کلاس ButtonItem برای نگهداری اطلاعات دکمه
    public static class ButtonItem {
        private int id;
        private String title;
        private String caption;
        private String image;
        private double price;
        private int sweetness;
        private int caffeine;
        private int temperature;
        private int stock;

        public ButtonItem(int id, String title, String caption, String image,
                          double price, int sweetness, int caffeine,
                          int temperature, int stock) {
            this.id = id;
            this.title = title;
            this.caption = caption;
            this.image = image;
            this.price = price;
            this.sweetness = sweetness;
            this.caffeine = caffeine;
            this.temperature = temperature;
            this.stock = stock;
        }

        // Getters
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getCaption() { return caption; }
        public String getImage() { return image; }
        public double getPrice() { return price; }
        public int getSweetness() { return sweetness; }
        public int getCaffeine() { return caffeine; }
        public int getTemperature() { return temperature; }
        public int getStock() { return stock; }
    }

    // در کلاس ApiClient این متدها را اضافه کنید:

    // --- آپلود عکس پس‌زمینه جدید ---
    public String uploadBackgroundImage(File imageFile) {
        try {
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            URL url = new URL(BASE_URL + "uploadBackground.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

                // Add file
                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"background_image\"; filename=\"" + imageFile.getName() + "\"").append("\r\n");
                writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(imageFile.getName())).append("\r\n");
                writer.append("\r\n").flush();

                Files.copy(imageFile.toPath(), os);
                os.flush();

                writer.append("\r\n").flush();
                writer.append("--" + boundary + "--").append("\r\n").flush();
            }

            // Read response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                // Response format: "SUCCESS:filename.jpg" or "ERROR:message"
                String result = response.toString().trim();
                if (result.startsWith("SUCCESS:")) {
                    return result.substring(8); // Return filename
                } else {
                    return null;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- دریافت لیست عکس‌های پس‌زمینه ---
    public List<BackgroundImage> getBackgroundImages() {
        List<BackgroundImage> backgrounds = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getBackgrounds.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                // Format: id;filename;is_default
                String[] parts = line.split(";");
                if (parts.length >= 2) {
                    BackgroundImage bg = new BackgroundImage();
                    bg.id = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
                    bg.filename = parts[1];
                    bg.isDefault = parts.length > 2 && parts[2].equals("1");
                    backgrounds.add(bg);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backgrounds;
    }

    // کلاس BackgroundImage
    public static class BackgroundImage {
        public int id;
        public String filename;
        public boolean isDefault;

        @Override
        public String toString() {
            return filename;
        }
    }

    // --- دریافت لیست کامل کاربران ---
    public List<ManageUsersPage.UserModel> getAllUsers() {
        List<ManageUsersPage.UserModel> users = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getAllUsers.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 14) {
                    ManageUsersPage.UserModel user = new ManageUsersPage.UserModel(
                            parts[0], parts[1], parts[2], parts[3], parts[4],
                            parts[5], parts[6], parts[7], parts[8], parts[9],
                            parts[10], parts[11], parts[12], parts[13]
                    );
                    users.add(user);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    // --- بروزرسانی اطلاعات کاربر (بدون رمز) ---
    public boolean updateUser(int userId, String fullname, String phone, String email,
                              String address, String companyName, String ceoName,
                              String ceoPhone, String ceoEmail, String deviceLocation,
                              String backgroundImage) {
        try {
            URL url = new URL(BASE_URL + "updateUser.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "user_id=" + userId +
                    "&fullname=" + URLEncoder.encode(fullname != null ? fullname : "", "UTF-8") +
                    "&phone=" + URLEncoder.encode(phone != null ? phone : "", "UTF-8") +
                    "&email=" + URLEncoder.encode(email != null ? email : "", "UTF-8") +
                    "&address=" + URLEncoder.encode(address != null ? address : "", "UTF-8") +
                    "&company_name=" + URLEncoder.encode(companyName != null ? companyName : "", "UTF-8") +
                    "&ceo_name=" + URLEncoder.encode(ceoName != null ? ceoName : "", "UTF-8") +
                    "&ceo_phone=" + URLEncoder.encode(ceoPhone != null ? ceoPhone : "", "UTF-8") +
                    "&ceo_email=" + URLEncoder.encode(ceoEmail != null ? ceoEmail : "", "UTF-8") +
                    "&device_location=" + URLEncoder.encode(deviceLocation != null ? deviceLocation : "", "UTF-8") +
                    "&background_image=" + URLEncoder.encode(backgroundImage != null ? backgroundImage : "default_bg.jpg", "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Update User Response Code: " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String response = br.readLine().trim();
            System.out.println("Update User Response: " + response);
            br.close();

            return response != null && response.equalsIgnoreCase("OK");

        } catch (Exception e) {
            System.err.println("Exception in updateUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- بروزرسانی رمز عبور کاربر ---
    public boolean updateUserPassword(int userId, String newPassword) {
        try {
            URL url = new URL(BASE_URL + "updateUserPassword.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // ✅ اصلاح شده - رمز را مستقیماً ارسال می‌کنیم، هش کردن در سرور انجام می‌شود
            String data = "user_id=" + userId +
                    "&password=" + URLEncoder.encode(newPassword, "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Update Password Response Code: " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String response = br.readLine().trim();
            System.out.println("Update Password Response: " + response);
            br.close();

            return response != null && response.equalsIgnoreCase("OK");

        } catch (Exception e) {
            System.err.println("Exception in updateUserPassword: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- حذف کاربر ---
    public boolean deleteUser(int userId) {
        try {
            URL url = new URL(BASE_URL + "deleteUser.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "user_id=" + userId;

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");

        } catch (Exception e) {
            System.err.println("Exception in deleteUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- متد کمکی برای هش کردن رمز (در سمت کلاینت این کار را نمی‌کنیم، در سرور انجام می‌شود) ---
    private String password_hash(String password, String algorithm) {
        // این متد فقط برای انتقال است، هش کردن باید در سرور انجام شود
        return password;
    }

    // --- دریافت لیست تمام دکمه‌ها ---
    public List<ManageProductsPage.ButtonModel> getAllButtons() {
        List<ManageProductsPage.ButtonModel> buttons = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getAllButtons.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 11) {
                    ManageProductsPage.ButtonModel button = new ManageProductsPage.ButtonModel(
                            Integer.parseInt(parts[0]), parts[1], parts[2], parts[3],
                            Double.parseDouble(parts[4]), Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6]), Integer.parseInt(parts[7]),
                            Integer.parseInt(parts[8]), Integer.parseInt(parts[9]), parts[10]
                    );
                    buttons.add(button);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buttons;
    }

    // --- افزودن دکمه جدید ---
    public boolean addButton(String title, String caption, String image, double price,
                             int sweetness, int caffeine, int temperature, int stock, int userId) {
        try {
            URL url = new URL(BASE_URL + "addButton.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "title=" + URLEncoder.encode(title, "UTF-8") +
                    "&caption=" + URLEncoder.encode(caption, "UTF-8") +
                    "&image=" + URLEncoder.encode(image, "UTF-8") +
                    "&price=" + price +
                    "&sweetness=" + sweetness +
                    "&caffeine=" + caffeine +
                    "&temperature=" + temperature +
                    "&stock=" + stock +
                    "&user_id=" + userId;

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- ویرایش دکمه ---
    public boolean updateButton(int buttonId, String title, String caption, String image, double price,
                                int sweetness, int caffeine, int temperature, int stock) {
        try {
            URL url = new URL(BASE_URL + "updateButton.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "button_id=" + buttonId +
                    "&title=" + URLEncoder.encode(title, "UTF-8") +
                    "&caption=" + URLEncoder.encode(caption, "UTF-8") +
                    "&image=" + URLEncoder.encode(image, "UTF-8") +
                    "&price=" + price +
                    "&sweetness=" + sweetness +
                    "&caffeine=" + caffeine +
                    "&temperature=" + temperature +
                    "&stock=" + stock;

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- حذف دکمه ---
    public boolean deleteButton(int buttonId) {
        try {
            URL url = new URL(BASE_URL + "deleteButton.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "button_id=" + buttonId;

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- آپلود عکس دکمه ---
    public String uploadButtonImage(File imageFile) {
        try {
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            URL url = new URL(BASE_URL + "uploadButtonImage.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"button_image\"; filename=\"" + imageFile.getName() + "\"").append("\r\n");
                writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(imageFile.getName())).append("\r\n");
                writer.append("\r\n").flush();

                Files.copy(imageFile.toPath(), os);
                os.flush();

                writer.append("\r\n").flush();
                writer.append("--" + boundary + "--").append("\r\n").flush();
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                String result = response.toString().trim();
                if (result.startsWith("SUCCESS:")) {
                    return result.substring(8);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- ورود کاربر عادی ---
    public UserPanel.User loginUser(String username, String password) {
        try {
            System.out.println("=== ApiClient.loginUser ===");
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);

            URL url = new URL(BASE_URL + "user_login.php");
            System.out.println("URL: " + url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            String data = "username=" + URLEncoder.encode(username, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8");

            System.out.println("Sending data: " + data);

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            }

            String response = br.readLine();
            System.out.println("Raw response: " + response);

            // ادامه خطوط پاسخ را هم بخوان
            StringBuilder fullResponse = new StringBuilder(response != null ? response : "");
            String line;
            while ((line = br.readLine()) != null) {
                fullResponse.append("\n").append(line);
                System.out.println("Additional line: " + line);
            }
            br.close();
            conn.disconnect();

            response = fullResponse.toString().trim();
            System.out.println("Full response: " + response);

            if (response != null && response.startsWith("OK")) {
                String[] parts = response.split(";");
                int id = 0;
                String user = "", fullname = "", bg = "default_bg.jpg", location = "";

                for (String part : parts) {
                    if (part.startsWith("id=")) {
                        id = Integer.parseInt(part.substring(3));
                    } else if (part.startsWith("username=")) {
                        user = part.substring(9);
                    } else if (part.startsWith("fullname=")) {
                        fullname = part.substring(9);
                    } else if (part.startsWith("background=")) {
                        bg = part.substring(11);
                    } else if (part.startsWith("location=")) {
                        location = part.substring(9);
                    }
                }

                System.out.println("Parsed user - ID: " + id + ", Username: " + user + ", Name: " + fullname);

                if (id > 0 && !user.isEmpty()) {
                    return new UserPanel.User(id, user, fullname, bg, location);
                }
            }

            System.out.println("Login failed, response: " + response);

        } catch (Exception e) {
            System.err.println("Exception in loginUser: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // --- دریافت دکمه‌های کاربر ---
    public List<UserPanel.ButtonModel> getUserButtons(int userId) {
        List<UserPanel.ButtonModel> buttons = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getUserButtons.php?user_id=" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 9) {
                    UserPanel.ButtonModel button = new UserPanel.ButtonModel(
                            Integer.parseInt(parts[0]), parts[1], parts[2], parts[3],
                            Double.parseDouble(parts[4]), Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6]), Integer.parseInt(parts[7]),
                            Integer.parseInt(parts[8])
                    );
                    buttons.add(button);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buttons;
    }

    // --- لاگ خروج کاربر ---
    public boolean logUserLogout(int userId, String username, String fullname) {
        try {
            URL url = new URL(BASE_URL + "user_logout.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "user_id=" + userId +
                    "&username=" + URLEncoder.encode(username, "UTF-8") +
                    "&fullname=" + URLEncoder.encode(fullname, "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String response = br.readLine().trim();
            br.close();

            return response.equalsIgnoreCase("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- دریافت لاگ‌های ورود کاربران ---
    public List<UserLoginLog> getUserLoginLogs(int userId, String action, int limit) {
        List<UserLoginLog> logs = new ArrayList<>();
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "getUserLoginLogs.php?");
            if (userId > 0) {
                urlBuilder.append("user_id=").append(userId).append("&");
            }
            if (action != null && !action.isEmpty() && !action.equals("همه") && !action.equals("all")) {
                urlBuilder.append("action=").append(URLEncoder.encode(action, "UTF-8")).append("&");
            }
            urlBuilder.append("limit=").append(limit);

            URL url = new URL(urlBuilder.toString());
            System.out.println("Fetching user logs from: " + url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            }

            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("Raw log line: " + line);

                if (line.equals("NO_LOGS") || line.startsWith("ERROR")) {
                    System.out.println("No logs or error: " + line);
                    continue;
                }

                String[] parts = line.split("\\|");
                System.out.println("Split length: " + parts.length);

                if (parts.length >= 11) {
                    try {
                        UserLoginLog log = new UserLoginLog(
                                Integer.parseInt(parts[0]), // id
                                Integer.parseInt(parts[1]), // user_id
                                parts[2],                   // username
                                parts[3],                   // fullname
                                parts[4],                   // action
                                parts[5],                   // ip_address
                                parts[6],                   // user_agent
                                parts[7],                   // device_info
                                parts[8],                   // status
                                parts.length > 9 ? parts[9] : "",  // error_message
                                parts[10]                   // created_at
                        );
                        logs.add(log);
                        System.out.println("Added log: " + log.getUsername() + " - " + log.getAction());
                    } catch (Exception e) {
                        System.err.println("Error parsing log line: " + line);
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Invalid log format, parts length: " + parts.length);
                }
            }
            br.close();
            conn.disconnect();

            System.out.println("Total logs loaded: " + logs.size());

        } catch (Exception e) {
            System.err.println("Exception in getUserLoginLogs: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }

    // کلاس لاگ ورود کاربران
    public static class UserLoginLog {
        private int id;
        private int userId;
        private String username;
        private String fullname;
        private String action;
        private String ipAddress;
        private String userAgent;
        private String deviceInfo;
        private String status;
        private String errorMessage;
        private String createdAt;

        public UserLoginLog(int id, int userId, String username, String fullname, String action,
                            String ipAddress, String userAgent, String deviceInfo, String status,
                            String errorMessage, String createdAt) {
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.fullname = fullname;
            this.action = action;
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
            this.deviceInfo = deviceInfo;
            this.status = status;
            this.errorMessage = errorMessage;
            this.createdAt = createdAt;
        }

        // Getters
        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getFullname() { return fullname; }
        public String getAction() { return action; }
        public String getIpAddress() { return ipAddress; }
        public String getUserAgent() { return userAgent; }
        public String getDeviceInfo() { return deviceInfo; }
        public String getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public String getCreatedAt() { return createdAt; }
    }

    // کلاس داخلی برای لاگ فروش
    public static class SalesLog {
        private int id;
        private int userId;
        private String username;
        private String fullname;
        private int buttonId;
        private String buttonTitle;
        private String buttonImage;
        private int quantity;
        private double pricePerUnit;
        private double totalPrice;
        private int sweetnessLevel;
        private int caffeineLevel;
        private int temperatureLevel;
        private String paymentMethod;
        private String status;
        private String createdAt;

        public SalesLog(int id, int userId, String username, String fullname,
                        int buttonId, String buttonTitle, String buttonImage,
                        int quantity, double pricePerUnit, double totalPrice,
                        int sweetnessLevel, int caffeineLevel, int temperatureLevel,
                        String paymentMethod, String status, String createdAt) {
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.fullname = fullname;
            this.buttonId = buttonId;
            this.buttonTitle = buttonTitle;
            this.buttonImage = buttonImage;
            this.quantity = quantity;
            this.pricePerUnit = pricePerUnit;
            this.totalPrice = totalPrice;
            this.sweetnessLevel = sweetnessLevel;
            this.caffeineLevel = caffeineLevel;
            this.temperatureLevel = temperatureLevel;
            this.paymentMethod = paymentMethod;
            this.status = status;
            this.createdAt = createdAt;
        }

        // Getters
        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getFullname() { return fullname; }
        public int getButtonId() { return buttonId; }
        public String getButtonTitle() { return buttonTitle; }
        public String getButtonImage() { return buttonImage; }
        public int getQuantity() { return quantity; }
        public double getPricePerUnit() { return pricePerUnit; }
        public double getTotalPrice() { return totalPrice; }
        public int getSweetnessLevel() { return sweetnessLevel; }
        public int getCaffeineLevel() { return caffeineLevel; }
        public int getTemperatureLevel() { return temperatureLevel; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }

        public String getTotalPriceFormatted() {
            return String.format("%,d", (int) totalPrice) + " تومان";
        }

        public String getPricePerUnitFormatted() {
            return String.format("%,d", (int) pricePerUnit) + " تومان";
        }
    }

    // --- ثبت فروش جدید ---
    public int addSale(int userId, String username, String fullname,
                       int buttonId, String buttonTitle, String buttonImage,
                       int quantity, double pricePerUnit, int sweetnessLevel,
                       int caffeineLevel, int temperatureLevel, String paymentMethod) {
        try {
            System.out.println("=== ApiClient.addSale ===");
            System.out.println("User ID: " + userId);
            System.out.println("Username: " + username);
            System.out.println("Button ID: " + buttonId);
            System.out.println("Price: " + pricePerUnit);

            URL url = new URL(BASE_URL + "addSale.php");
            System.out.println("URL: " + url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            double totalPrice = pricePerUnit * quantity;

            String data = "user_id=" + userId +
                    "&username=" + URLEncoder.encode(username != null ? username : "", "UTF-8") +
                    "&fullname=" + URLEncoder.encode(fullname != null ? fullname : "", "UTF-8") +
                    "&button_id=" + buttonId +
                    "&button_title=" + URLEncoder.encode(buttonTitle != null ? buttonTitle : "", "UTF-8") +
                    "&button_image=" + URLEncoder.encode(buttonImage != null ? buttonImage : "", "UTF-8") +
                    "&quantity=" + quantity +
                    "&price_per_unit=" + pricePerUnit +
                    "&total_price=" + totalPrice +
                    "&sweetness_level=" + sweetnessLevel +
                    "&caffeine_level=" + caffeineLevel +
                    "&temperature_level=" + temperatureLevel +
                    "&payment_method=" + URLEncoder.encode(paymentMethod != null ? paymentMethod : "CARD", "UTF-8") +
                    "&status=COMPLETED";

            System.out.println("Sending data: " + data);

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            }

            String response = br.readLine();
            System.out.println("Response: " + response);

            // خواندن خطوط اضافی
            String line;
            StringBuilder fullResponse = new StringBuilder(response != null ? response : "");
            while ((line = br.readLine()) != null) {
                fullResponse.append("\n").append(line);
                System.out.println("Additional line: " + line);
            }
            br.close();
            conn.disconnect();

            response = fullResponse.toString().trim();
            System.out.println("Full response: " + response);

            if (response != null && response.startsWith("OK:")) {
                int id = Integer.parseInt(response.substring(3));
                System.out.println("✅ Sale recorded with ID: " + id);
                return id;
            } else {
                System.out.println("❌ Sale recording failed: " + response);
            }

        } catch (Exception e) {
            System.err.println("Exception in addSale: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // --- دریافت لاگ‌های فروش ---
    // --- دریافت لاگ‌های فروش ---
    public List<SalesLog> getSalesLogs(int userId, String username, int buttonId,
                                       String status, String fromDate, String toDate, int limit) {
        List<SalesLog> logs = new ArrayList<>();
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "getSalesLogs.php?");
            if (userId > 0) urlBuilder.append("user_id=").append(userId).append("&");
            if (username != null && !username.isEmpty()) {
                urlBuilder.append("username=").append(URLEncoder.encode(username, "UTF-8")).append("&");
            }
            if (buttonId > 0) urlBuilder.append("button_id=").append(buttonId).append("&");
            if (status != null && !status.isEmpty() && !status.equals("همه")) {
                urlBuilder.append("status=").append(URLEncoder.encode(status, "UTF-8")).append("&");
            }
            if (fromDate != null && !fromDate.isEmpty()) {
                urlBuilder.append("from_date=").append(fromDate).append("&");
            }
            if (toDate != null && !toDate.isEmpty()) {
                urlBuilder.append("to_date=").append(toDate).append("&");
            }
            urlBuilder.append("limit=").append(limit);

            URL url = new URL(urlBuilder.toString());
            System.out.println("=== getSalesLogs ===");
            System.out.println("URL: " + url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                int lineCount = 0;

                while ((line = br.readLine()) != null) {
                    lineCount++;
                    System.out.println("Line " + lineCount + ": " + line);

                    if (line.equals("NO_LOGS")) {
                        System.out.println("No logs found");
                        break;
                    }

                    if (line.startsWith("ERROR")) {
                        System.out.println("Error from server: " + line);
                        break;
                    }

                    if (line.startsWith("COUNT=")) {
                        System.out.println("Total records: " + line.substring(6));
                        continue;
                    }

                    String[] parts = line.split("\\|");
                    System.out.println("Split length: " + parts.length);

                    if (parts.length >= 16) {
                        try {
                            SalesLog log = new SalesLog(
                                    Integer.parseInt(parts[0]),  // id
                                    Integer.parseInt(parts[1]),  // user_id
                                    parts[2],                     // username
                                    parts[3],                     // fullname
                                    Integer.parseInt(parts[4]),   // button_id
                                    parts[5],                     // button_title
                                    parts[6],                     // button_image
                                    Integer.parseInt(parts[7]),   // quantity
                                    Double.parseDouble(parts[8]), // price_per_unit
                                    Double.parseDouble(parts[9]), // total_price
                                    Integer.parseInt(parts[10]),  // sweetness_level
                                    Integer.parseInt(parts[11]),  // caffeine_level
                                    Integer.parseInt(parts[12]),  // temperature_level
                                    parts[13],                    // payment_method
                                    parts[14],                    // status
                                    parts[15]                     // created_at
                            );
                            logs.add(log);
                            System.out.println("Added log: " + log.getId() + " - " + log.getButtonTitle());
                        } catch (Exception e) {
                            System.err.println("Error parsing line: " + line);
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("Invalid line format, expected 16 parts but got " + parts.length);
                    }
                }
                br.close();
                System.out.println("Total logs parsed: " + logs.size());
            } else {
                System.err.println("HTTP Error: " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("Exception in getSalesLogs: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }

    // --- دریافت لیست تصاویر تبلیغاتی ---
    public List<String> getAdImages() {
        List<String> ads = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "getAdImages.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    ads.add(line.trim());
                }
            }
            br.close();
            conn.disconnect();

            System.out.println("Loaded " + ads.size() + " ad images");
        } catch (Exception e) {
            System.err.println("Exception in getAdImages: " + e.getMessage());
            e.printStackTrace();
        }
        return ads;
    }
}
