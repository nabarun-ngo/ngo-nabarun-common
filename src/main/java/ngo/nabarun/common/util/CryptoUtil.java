package ngo.nabarun.common.util;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public final class CryptoUtil {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12;       // bytes
    private static final int SALT_LENGTH = 16;     // bytes
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;     // bits

    private CryptoUtil() {}

    public static String encrypt(String plainText, String password) {
        try {
            byte[] salt = generateRandomBytes(SALT_LENGTH);
            byte[] iv = generateRandomBytes(IV_LENGTH);
            SecretKey key = deriveKey(password, salt);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine salt + iv + ciphertext
            byte[] encryptedData = new byte[salt.length + iv.length + cipherText.length];
            System.arraycopy(salt, 0, encryptedData, 0, salt.length);
            System.arraycopy(iv, 0, encryptedData, salt.length, iv.length);
            System.arraycopy(cipherText, 0, encryptedData, salt.length + iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encryptedData);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    public static String decrypt(String encryptedText, String password) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherText = new byte[decoded.length - SALT_LENGTH - IV_LENGTH];

            System.arraycopy(decoded, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(decoded, SALT_LENGTH, iv, 0, IV_LENGTH);
            System.arraycopy(decoded, SALT_LENGTH + IV_LENGTH, cipherText, 0, cipherText.length);

            SecretKey key = deriveKey(password, salt);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);

        } catch (AEADBadTagException e) {
            throw new SecurityException("Decryption failed: invalid key or corrupted data", e);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }

    private static SecretKey deriveKey(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, AES);
    }

    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return bytes;
    }
}
