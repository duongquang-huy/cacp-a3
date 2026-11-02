package comp3011.assignment1.model;
// Response model for GET /api/v1/admin/uptime
public class UptimeResponse {
	private String utcServerStart; // UTC time server started
	private String utcNow; 	// Current time
	private double serverUptimeSeconds; // server started and current time gap
	
	public UptimeResponse(String utcServerStart,String utcNow, double serverUptimeSeconds) {
		this.utcServerStart = utcServerStart;
		this.utcNow = utcNow;
		this.serverUptimeSeconds = serverUptimeSeconds;
	}
	public String getUtcServerStart() {return utcServerStart;}
	public String getUtcNow() {return utcNow;}
	public double getServerUptimeSeconds() {return serverUptimeSeconds;}
}
