package comp3011.assignment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.model.GlobalStatsResponse;
import comp3011.assignment1.service.StatsService;

// GET Total token usage since server start
// via http://localhost:8080/api/v1/global/stats
@RestController
@RequestMapping("/api/v1/global")
public class StatsController {
	private final StatsService statsService;
	
	public StatsController(StatsService statService) {
		this.statsService = statService;
	}
	
	@GetMapping("/stats")
	public GlobalStatsResponse getStats() {
		return new GlobalStatsResponse(statsService.getInputTokens(), statsService.getOutputTokens());
		
	}
}
