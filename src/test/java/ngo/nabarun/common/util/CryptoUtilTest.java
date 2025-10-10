package ngo.nabarun.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoUtilTest {

    @Test
    public void testEncryptDecryptRoundtrip() {
        String password = "s3cr3tP@ss";
        String plain = "Hello World!";

        String encrypted = CryptoUtil.encrypt(plain, password);
        assertNotNull(encrypted);

        String decrypted = CryptoUtil.decrypt(encrypted, password);
        assertEquals(plain, decrypted);
    }

    @Test
    public void testDecryptWithWrongPasswordThrows() {
        String password = "correct";
        String wrong = "wrong";
        String plain = "Some secret text";

        String encrypted = CryptoUtil.encrypt(plain, password);
        assertNotNull(encrypted);

        assertThrows(SecurityException.class, () -> {
            CryptoUtil.decrypt(encrypted, wrong);
        });
    }
}
