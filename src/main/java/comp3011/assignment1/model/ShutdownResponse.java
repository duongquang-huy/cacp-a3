package comp3011.assignment1.model;
// Shutdown Response for POST /api/v1/admin/shutdown
// that return confirmation message when success
public class ShutdownResponse {
	private String message; // Message from server
	
	public ShutdownResponse(String message) {
		this.message = message;
		
	}
	public String getMessage() {return message;}
}
