package comp3011.assignment1.model;

public class GlobalStatsResponse {
	private long inputTokens; // Total input tokens consumed
	private long outputTokens; 	// Total output tokens produced
	
	public GlobalStatsResponse(long inputTokens,long outputTokens) {
		this.inputTokens = inputTokens;
		this.outputTokens = outputTokens;
	}
	public long getOutputTokens() {return outputTokens;}
	public long getInputTokens() {return inputTokens;}

}
