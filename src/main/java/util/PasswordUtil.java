package util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final int BCRYPT_ROUNDS = 10;

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public static boolean verify(String plain, String hashed) {
        if (plain == null || hashed == null) return false;
        try {
            return BCrypt.checkpw(plain, hashed);
        } catch (Exception e) {
            return false;
        }
    }
}
