//package util;
//
//import java.security.SecureRandom;
//
//public class OtpUtil {
//
//    private static final SecureRandom random = new SecureRandom();
//
//    public static String generateOtp() {
//        int otp = 1000 + random.nextInt(9000); // Generates a 4-digit OTP (1000 to 9999)
//        return String.valueOf(otp);
//    }
//}
package util;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class OtpUtil {

    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_EXPIRATION_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds

    // This map will store the OTP and its expiration time for each email.
    private static final Map<String, OtpData> otpStore = new HashMap<>();

    public static String generateOtp(String email) {
        int otp = 1000 + random.nextInt(9000); // Generates a 4-digit OTP (1000 to 9999)
        String otpString = String.valueOf(otp);
        
        // Store OTP with its expiration time
        otpStore.put(email, new OtpData(otpString, System.currentTimeMillis() + OTP_EXPIRATION_TIME));
        
        return otpString;
    }

    // Method to check if the OTP is valid and not expired
    public static boolean isOtpValid(String email, String otp) {
        OtpData storedOtpData = otpStore.get(email);
        
        if (storedOtpData == null) {
            return false;
        }

        // Check if OTP is expired
        if (System.currentTimeMillis() > storedOtpData.expirationTime) {
            otpStore.remove(email);  // Remove expired OTP
            return false;
        }

        // Check if OTP matches
        return storedOtpData.otp.equals(otp);
    }

    // Inner class to store OTP and its expiration time
    private static class OtpData {
        String otp;
        long expirationTime;

        OtpData(String otp, long expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }
    }
}
