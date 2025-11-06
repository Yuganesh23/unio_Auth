package dto;

public class SignUpDto {

	private String name;
	private String email;
	private String password;
	private String otp;
	private boolean otpCheck=false;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public boolean isOtpCheck() {
		return otpCheck;
	}
	public void setOtpCheck(boolean otpCheck) {
		this.otpCheck = otpCheck;
	}
	public SignUpDto(String name, String email, String password, String otp, boolean otpCheck) {
		super();
		this.name = name;
		this.email = email;
		this.password = password;
		this.otp = otp;
		this.otpCheck = otpCheck;
	}
	public SignUpDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "SignUpDto [name=" + name + ", email=" + email + ", password=" + password + ", otp=" + otp
				+ ", otpCheck=" + otpCheck + "]";
	}
	
	
}
