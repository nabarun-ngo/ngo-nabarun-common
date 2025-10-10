package ngo.nabarun.common.util;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utility class for AES-GCM encryption and decryption using a password-derived key.
 *
 * <p>This class provides simple password-based encryption using PBKDF2 (HmacSHA256) to derive
 * a 256-bit AES key from a password and a random salt. Encryption uses AES/GCM/NoPadding with a
 * 12-byte random IV and a 128-bit authentication tag. The encrypted payload is returned as a
 * Base64-encoded string containing salt + iv + ciphertext.
 *
 * <p>Important security notes:
 * - The password must be strong and kept secret. PBKDF2 parameters (iterations, salt length)
 *   are set as constants in this class. Consider increasing ITERATION_COUNT for higher work factor.
 * - The output format is: base64(salt || iv || ciphertext). The salt and iv are generated randomly
 *   for each encryption operation.
 * - AES-GCM provides authenticated encryption; decryption will fail with AEADBadTagException if the
 *   ciphertext or password is incorrect.
 */
public final class CryptoUtil {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12;       // bytes
    private static final int SALT_LENGTH = 16;     // bytes
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;     // bits

    private CryptoUtil() {}

    /**
     * Encrypt the provided plain text using a password-derived AES-GCM key.
     *
     * <p>The method generates a random salt and IV for each encryption, derives a 256-bit key
     * using PBKDF2WithHmacSHA256, and returns a Base64 string containing salt || iv || ciphertext.
     *
     * @param plainText the text to encrypt (must not be null)
     * @param password the password used to derive the encryption key (must not be null)
     * @return Base64-encoded string containing salt + iv + ciphertext
     * @throws RuntimeException on underlying crypto errors
     */
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

    /**
     * Decrypt a Base64-encoded encrypted string produced by {@link #encrypt(String, String)}.
     *
     * @param encryptedText Base64 string containing salt + iv + ciphertext
     * @param password password used to derive the decryption key
     * @return decrypted plain text
     * @throws SecurityException if authentication fails (invalid key or corrupted data)
     * @throws RuntimeException on other decryption errors
     */
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

    /**
     * Derive a SecretKey from a password and salt using PBKDF2WithHmacSHA256.
     *
     * @param password the password
     * @param salt the salt
     * @return SecretKey suitable for AES
     * @throws NoSuchAlgorithmException if PBKDF2 algorithm is not available
     * @throws InvalidKeySpecException if key specification is invalid
     */
    private static SecretKey deriveKey(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, AES);
    }

    /**
     * Generate secure random bytes of the requested length.
     *
     * @param length number of bytes to generate
     * @return random byte array
     */
    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return bytes;
    }
}