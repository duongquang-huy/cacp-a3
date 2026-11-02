package comp3011.assignment1.model;

// Erorr Response for all API error cases
public class ErrorResponse {
	private String timestamp; // UTC timestamp at which the error 
	private int status; // HTTP status code
	private String error; 	// Short HTTP reason phrase or error
	private String message; // Human-readable description
	private String path;  // Request path that produced the error
	
	public ErrorResponse(String timestamp,String error, String message, int status, String path) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}
	public String getTimestamp() {return timestamp;}
	public int getStatus() {return status;}
	public String getError() {return error;}
	public String getMessage() {return message;}
	public String getPath() {return path;}
}
