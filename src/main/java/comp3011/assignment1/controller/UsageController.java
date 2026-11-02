package comp3011.assignment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.model.UsageResponse;
import comp3011.assignment1.service.UsageService;

//Controller for total Whisper time used, endpoint using 
//GET /api/v1/global/usage
@RestController
@RequestMapping("/api/v1/global")
public class UsageController {
	private final UsageService usageService;
	
	public UsageController(UsageService usageService) {
		this.usageService =  usageService;
	}
	@GetMapping("/usage")
	public UsageResponse getUsage() {
		return new UsageResponse(usageService.getTotalSeconds());
	}
}
