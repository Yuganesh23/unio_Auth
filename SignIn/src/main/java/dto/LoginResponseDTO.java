package dto;

public class LoginResponseDTO {

    private String message;
    private boolean success;
    private String token;


    public LoginResponseDTO(String message, boolean success, String token) {
        this.message = message;
        this.success = success;
        this.token = token;
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
    
}
