package ngo.nabarun.common.util;

import org.junit.jupiter.api.Test;
import org.passay.RuleResult;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testGenerateStrongPasswordLength() {
        String pwd = PasswordUtil.generateStrongPassword(12);
        assertNotNull(pwd);
        assertEquals(12, pwd.length());
    }

    @Test
    public void testGenerateRandomPasswordFlags() {
        String pwd = PasswordUtil.generateRandomPassword(16, true, true, true, true);
        assertNotNull(pwd);
        assertEquals(16, pwd.length());
    }

    @Test
    public void testGenerateRandomNumber() {
        String num = PasswordUtil.generateRandomNumber(6);
        assertNotNull(num);
        assertEquals(6, num.length());
        assertTrue(num.chars().allMatch(Character::isDigit));
    }

    @Test
    public void testValidatePasswordIllegalRegex() {
        String password = "abc1";
        // Illegal regex that matches any string containing a digit
        String illegalRegex = ".*\\d.*";
        RuleResult result = PasswordUtil.validatePassword(password, illegalRegex);
        assertFalse(result.isValid());
        assertFalse(PasswordUtil.isPasswordValid(password, illegalRegex));
    }

}
