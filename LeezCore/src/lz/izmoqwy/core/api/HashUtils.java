package lz.izmoqwy.core.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
	
    public static String computeHash(String password, String salt) {
        try {
			return "$SHA$" + salt + "$" + hash256(hash256(password) + salt);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        return null;
    }

    public static boolean comparePassword(String password, String hashedPassword) {
        String[] line = hashedPassword.split("\\$");
        return line.length == 4 && isEqual(hashedPassword, computeHash(password, line[2]));
    }
    
    public static boolean isEqual(String string1, String string2) {
        return MessageDigest.isEqual(
            string1.getBytes(StandardCharsets.UTF_8), string2.getBytes(StandardCharsets.UTF_8));
    }
    
    public static String hash256(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data.getBytes());
        return bytesToHex(md.digest());
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

}
