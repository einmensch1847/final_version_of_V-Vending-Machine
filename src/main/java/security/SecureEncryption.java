package security;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SecureEncryption {

    // الگوریتم‌ها
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final String RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String HMAC_ALGORITHM = "HmacSHA512";
    private static final int AES_KEY_SIZE = 256;
    private static final int RSA_KEY_SIZE = 2048;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int SALT_LENGTH = 32;
    private static final int IV_LENGTH = 12;

    // کلیدهای جلسه
    private SecretKey aesKey;
    private KeyPair rsaKeyPair;
    private PublicKey serverPublicKey;
    private String sessionId;
    private Map<String, SecretKey> messageKeys;

    public SecureEncryption() {
        try {
            initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initialize() throws Exception {
        // تولید کلید AES برای جلسه
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        aesKey = keyGen.generateKey();

        // تولید کلید RSA
        KeyPairGenerator rsaKeyGen = KeyPairGenerator.getInstance("RSA");
        rsaKeyGen.initialize(RSA_KEY_SIZE);
        rsaKeyPair = rsaKeyGen.generateKeyPair();

        // تولید شناسه جلسه
        sessionId = generateSessionId();

        // ذخیره کلیدهای پیام
        messageKeys = new HashMap<>();

        System.out.println("🔐 سیستم رمزنگاری با موفقیت راه‌اندازی شد");
        System.out.println("📱 Session ID: " + sessionId);
    }

    /**
     * رمزنگاری داده با الگوریتم ترکیبی
     */
    public String encrypt(String plainText) throws Exception {
        // مرحله ۱: تولید کلید یکبارمصرف برای این پیام
        SecretKey messageKey = generateMessageKey();
        String messageKeyId = generateKeyId();
        messageKeys.put(messageKeyId, messageKey);

        // مرحله ۲: رمزنگاری متن با AES-GCM
        byte[] iv = generateIV();
        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, messageKey, gcmSpec);

        byte[] encryptedData = aesCipher.doFinal(plainText.getBytes("UTF-8"));

        // مرحله ۳: رمزنگاری کلید پیام با RSA
        byte[] encryptedMessageKey = encryptWithRSA(messageKey.getEncoded());

        // مرحله ۴: ایجاد HMAC برای احراز اصالت
        String hmac = generateHMAC(encryptedData, messageKey);

        // مرحله ۵: ترکیب همه اجزا
        SecurePacket packet = new SecurePacket(
                sessionId,
                messageKeyId,
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(encryptedData),
                Base64.getEncoder().encodeToString(encryptedMessageKey),
                hmac,
                System.currentTimeMillis()
        );

        return packet.toEncryptedString();
    }

    /**
     * رمزگشایی داده
     */
    public String decrypt(String encryptedString) throws Exception {
        // استخراج پکت
        SecurePacket packet = SecurePacket.fromEncryptedString(encryptedString);

        // مرحله ۱: بررسی HMAC
        if (!verifyHMAC(
                Base64.getDecoder().decode(packet.encryptedData),
                packet.hmac,
                messageKeys.get(packet.messageKeyId))) {
            throw new SecurityException("HMAC verification failed - Message may be tampered");
        }

        // مرحله ۲: رمزگشایی کلید پیام
        byte[] decryptedMessageKey = decryptWithRSA(
                Base64.getDecoder().decode(packet.encryptedMessageKey));

        SecretKeySpec messageKey = new SecretKeySpec(decryptedMessageKey, "AES");

        // مرحله ۳: رمزگشایی داده اصلی
        byte[] iv = Base64.getDecoder().decode(packet.iv);
        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.DECRYPT_MODE, messageKey, gcmSpec);

        byte[] decryptedData = aesCipher.doFinal(
                Base64.getDecoder().decode(packet.encryptedData));

        return new String(decryptedData, "UTF-8");
    }

    /**
     * رمزنگاری داده برای ذخیره سازی (با کلید مشتق شده از رمز عبور)
     */
    public String encryptForStorage(String plainText, String password) throws Exception {
        // تولید کلید از رمز عبور با PBKDF2
        byte[] salt = generateSalt();
        SecretKey key = deriveKeyFromPassword(password, salt);

        // رمزنگاری با AES
        byte[] iv = generateIV();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

        // ترکیب salt + iv + encrypted
        byte[] combined = new byte[salt.length + iv.length + encrypted.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encrypted, 0, combined, salt.length + iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * رمزگشایی داده ذخیره شده
     */
    public String decryptFromStorage(String encryptedString, String password) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedString);

        // استخراج اجزا
        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[16]; // IV برای CBC
        byte[] encrypted = new byte[combined.length - SALT_LENGTH - 16];

        System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
        System.arraycopy(combined, SALT_LENGTH, iv, 0, 16);
        System.arraycopy(combined, SALT_LENGTH + 16, encrypted, 0, encrypted.length);

        // تولید کلید از رمز عبور
        SecretKey key = deriveKeyFromPassword(password, salt);

        // رمزگشایی
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8");
    }

    /**
     * ایجاد امضای دیجیتال برای داده
     */
    public String signData(String data) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(rsaKeyPair.getPrivate());
        signature.update(data.getBytes("UTF-8"));

        byte[] digitalSignature = signature.sign();
        return Base64.getEncoder().encodeToString(digitalSignature);
    }

    /**
     * تأیید امضای دیجیتال
     */
    public boolean verifySignature(String data, String signatureBase64, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes("UTF-8"));

        byte[] digitalSignature = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(digitalSignature);
    }

    /**
     * هش کردن با الگوریتم Argon2 (شبیه‌سازی)
     */
    public String hashPassword(String password) throws Exception {
        // تولید salt
        byte[] salt = generateSalt();

        // ترکیب password + salt
        String combined = password + Base64.getEncoder().encodeToString(salt);

        // چندین بار هش کردن
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hash = digest.digest(combined.getBytes("UTF-8"));

        for (int i = 0; i < 10000; i++) {
            hash = digest.digest(hash);
        }

        // بازگشت salt + hash
        return Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);
    }

    /**
     * تأیید رمز عبور
     */
    public boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            String combined = password + Base64.getEncoder().encodeToString(salt);
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(combined.getBytes("UTF-8"));

            for (int i = 0; i < 10000; i++) {
                hash = digest.digest(hash);
            }

            return MessageDigest.isEqual(hash, expectedHash);
        } catch (Exception e) {
            return false;
        }
    }

    // --- متدهای کمکی ---

    private SecretKey generateMessageKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    private String generateKeyId() {
        return "key_" + System.currentTimeMillis() + "_" +
                Math.abs(new SecureRandom().nextInt());
    }

    private byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private String generateSessionId() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                100000,  // iterations
                256      // key length
        );

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] encryptWithRSA(byte[] data) throws Exception {
        if (serverPublicKey == null) {
            throw new IllegalStateException("Server public key not set");
        }

        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        return cipher.doFinal(data);
    }

    private byte[] decryptWithRSA(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
        return cipher.doFinal(data);
    }

    private String generateHMAC(byte[] data, SecretKey key) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(key);
        byte[] hmacBytes = mac.doFinal(data);
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    private boolean verifyHMAC(byte[] data, String hmacBase64, SecretKey key) throws Exception {
        if (key == null) return false;

        String calculatedHMAC = generateHMAC(data, key);
        return calculatedHMAC.equals(hmacBase64);
    }

    // --- متدهای Getter و Setter ---

    public String getSessionId() {
        return sessionId;
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
    }

    public void setServerPublicKey(String publicKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        serverPublicKey = keyFactory.generatePublic(spec);
        System.out.println("✅ کلید عمومی سرور تنظیم شد");
    }

    public String exportPrivateKey() throws Exception {
        byte[] keyBytes = rsaKeyPair.getPrivate().getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public void importPrivateKey(String privateKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(spec);

        // ایجاد KeyPair جدید با کلید خصوصی وارد شده
        rsaKeyPair = new KeyPair(rsaKeyPair.getPublic(), privateKey);
    }

    // --- کلاس داخلی برای پکت امن ---

    private static class SecurePacket {
        String sessionId;
        String messageKeyId;
        String iv;
        String encryptedData;
        String encryptedMessageKey;
        String hmac;
        long timestamp;

        SecurePacket(String sessionId, String messageKeyId, String iv,
                     String encryptedData, String encryptedMessageKey,
                     String hmac, long timestamp) {
            this.sessionId = sessionId;
            this.messageKeyId = messageKeyId;
            this.iv = iv;
            this.encryptedData = encryptedData;
            this.encryptedMessageKey = encryptedMessageKey;
            this.hmac = hmac;
            this.timestamp = timestamp;
        }

        String toEncryptedString() {
            // فرمت: sessionId|messageKeyId|iv|encryptedData|encryptedMessageKey|hmac|timestamp
            return sessionId + "|" + messageKeyId + "|" + iv + "|" +
                    encryptedData + "|" + encryptedMessageKey + "|" +
                    hmac + "|" + timestamp;
        }

        static SecurePacket fromEncryptedString(String encryptedString) {
            String[] parts = encryptedString.split("\\|");
            if (parts.length != 7) {
                throw new IllegalArgumentException("Invalid packet format");
            }

            return new SecurePacket(
                    parts[0],  // sessionId
                    parts[1],  // messageKeyId
                    parts[2],  // iv
                    parts[3],  // encryptedData
                    parts[4],  // encryptedMessageKey
                    parts[5],  // hmac
                    Long.parseLong(parts[6])  // timestamp
            );
        }
    }
}