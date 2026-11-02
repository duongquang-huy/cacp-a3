package comp3011.assignment1.service;

import org.springframework.stereotype.Service;

// Service to track token usage since server start
@Service
public class StatsService {
	private long inputTokens = 0;
	private long outputTokens = 0;	
	
	public synchronized void addTokens(long input, long output) {
		inputTokens += input;
		outputTokens += output;
	}
	
	// Called whenever an API call to add token
	public synchronized long getInputTokens() { return inputTokens; }
	public synchronized long getOutputTokens() { return outputTokens; }
}
