package comp3011.assignment1.model;

//Response model for GET /api/v1/global/usage
public class UsageResponse {
	private double seconds; // total seconds of the vid sent to whisper
	
	public UsageResponse(double seconds) {
		this.seconds = seconds;
	}
	public double getSeconds() {return seconds;}
}

