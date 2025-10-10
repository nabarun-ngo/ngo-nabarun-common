package ngo.nabarun.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.IllegalRegexRule;
import org.passay.PasswordData;
import org.passay.PasswordGenerator;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;

/**
 * Utility class for generating and validating passwords.
 *
 * <p>This class provides helper methods to generate strong passwords, random passwords with
 * configurable character sets, numeric strings, and to validate passwords against a regular
 * expression. It is a thin convenience wrapper around the Passay library.
 *
 * <p>Example usage:
 * <pre>
 * String pwd = PasswordUtil.generateStrongPassword(12);
 * boolean ok = PasswordUtil.isPasswordValid(pwd, "^.*$");
 * </pre>
 */
public class PasswordUtil {
	private final static PasswordGenerator passwordGenerator = new PasswordGenerator();

	/**
	 * Generate a strong password of the requested length that includes at least one uppercase
	 * letter, one lowercase letter, one digit, one special character and several alphabetical
	 * characters.
	 *
	 * @param length the desired length of the password; callers should ensure length is large
	 *               enough to satisfy the required character counts
	 * @return a generated password string
	 */
	public static String generateStrongPassword(int length) {

		List<CharacterRule> rules = Arrays.asList(
				new CharacterRule(EnglishCharacterData.UpperCase, 1),
				new CharacterRule(EnglishCharacterData.LowerCase, 1), 
				new CharacterRule(EnglishCharacterData.Digit, 1),
				new CharacterRule(EnglishCharacterData.Special, 1),
				new CharacterRule(EnglishCharacterData.Alphabetical, 3)

				);

		PasswordGenerator generator = new PasswordGenerator();
		String password = generator.generatePassword(length, rules);
		return password;
	}
	
	/**
	 * Validate a password against an illegal-regex rule. This method returns the raw Passay
	 * RuleResult which contains details about whether the password passed and any messages.
	 *
	 * @param password the password to validate
	 * @param regex a regex considered illegal (if the password matches this regex it will fail)
	 * @return Passay RuleResult containing validation outcome and messages
	 */
	public static RuleResult validatePassword(String password,String regex) {
		Rule rule=new IllegalRegexRule(regex);
		PasswordValidator generator = new PasswordValidator(List.of(rule));
		RuleResult result = generator.validate(new PasswordData(password));
		return result;
	}
	
	/**
	 * Generate a random password with configurable character classes.
	 *
	 * @param length desired password length
	 * @param allowDigits whether digits are allowed
	 * @param allowSpecialChar whether a small set of special characters are allowed
	 * @param allowUppercase whether uppercase letters are allowed
	 * @param allowLowercase whether lowercase letters are allowed
	 * @return generated password string
	 */
	public static String generateRandomPassword(int length, boolean allowDigits, boolean allowSpecialChar, boolean allowUppercase, boolean allowLowercase) {
        List<CharacterRule> ruleList = new ArrayList<>();
        ruleList.add(new CharacterRule(EnglishCharacterData.Alphabetical));
        if(allowDigits) {
            ruleList.add(new CharacterRule(EnglishCharacterData.Digit));
        }
        if(allowSpecialChar) {
        	CharacterRule specialCharacterRule = new CharacterRule(new CharacterData() {
        	    @Override
        	    public String getErrorCode() {
        	        return "INVALID_SPECIAL_CHARACTER";
        	    }

        	    @Override
        	    public String getCharacters() {
        	        return "@#$%^&*?";
        	    }
        	});
            ruleList.add(specialCharacterRule);
        }
        if(allowUppercase) {
            ruleList.add(new CharacterRule(EnglishCharacterData.UpperCase));
        }
        if(allowLowercase) {
            ruleList.add(new CharacterRule(EnglishCharacterData.LowerCase));
        }
		return passwordGenerator.generatePassword(length, ruleList);

    }
	
	/**
	 * Generate a random alphabetic or alphanumeric string.
	 *
	 * @param length desired length
	 * @param alphaNumeric if true include digits as well as letters
	 * @return generated string
	 */
	public static String generateRandomString(int length, boolean alphaNumeric) {
        List<CharacterRule> ruleList = new ArrayList<>();
        ruleList.add(new CharacterRule(EnglishCharacterData.Alphabetical));
        if(alphaNumeric) {
            ruleList.add(new CharacterRule(EnglishCharacterData.Digit));
        }
		return passwordGenerator.generatePassword(length, ruleList);

    }
	
	/**
	 * Generate a random numeric string containing the requested number of digits.
	 *
	 * @param digits number of digits to generate
	 * @return numeric string
	 */
	public static String generateRandomNumber(int digits) {
        return passwordGenerator.generatePassword(digits, List.of(new CharacterRule(EnglishCharacterData.Digit)));

    }
	
	/**
	 * Convenience to check if a password is valid according to the provided illegal-regex rule.
	 *
	 * @param password password to check
	 * @param regex illegal regex rule
	 * @return true if the password does not match the illegal regex (i.e., considered valid)
	 */
	public static boolean isPasswordValid(String password,String regex) {
        return validatePassword(password, regex).isValid();

    }
	
}