package comp3011.assignment1.service;

import org.springframework.stereotype.Service;

// Count total seconds used since server start
@Service
public class UsageService {
	private double totalSeconds = 0;
	
	// Call after every request
	public synchronized void addSeconds(double seconds) {
		totalSeconds += seconds;
	}
	public synchronized double getTotalSeconds() {
		return totalSeconds;
	}
}
