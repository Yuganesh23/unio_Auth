	package util;
	import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
	import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
	import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
	import com.google.api.client.json.jackson2.JacksonFactory;

	import java.util.Collections;

	public class GoogleTokenVerifierUtil {

	    private static final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
	    private static GoogleIdTokenVerifier verifier;

	    static {
	        try {
	            verifier = new GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport(), jacksonFactory)
	                    // Specify the CLIENT_ID of the app that accesses the backend:
	                    .setAudience(Collections.singletonList("YOUR_GOOGLE_CLIENT_ID"))
	                    .build();
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    }

	    public static GoogleIdToken.Payload verifyToken(String idTokenString) throws Exception {
	        GoogleIdToken idToken = verifier.verify(idTokenString);
	        if (idToken != null) {
	            return idToken.getPayload();
	        } else {
	            throw new Exception("Invalid ID token.");
	        }
	    }
	}
