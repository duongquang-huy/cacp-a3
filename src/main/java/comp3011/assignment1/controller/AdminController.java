package comp3011.assignment1.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.model.ErrorResponse;
import comp3011.assignment1.model.ShutdownResponse;
import comp3011.assignment1.model.UptimeResponse;

// Controller handles uptime, shutdown, and stats in YAML

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
	// server start time
	private static final Instant SERVER_START = Instant.now();
	private static volatile boolean shuttingDown = false; // volatile <=> ensure threads has newest values, avoid race condition 
	
	// get server start and current time, and uptime
	// By GET http://localhost:8080/api/v1/admin/uptime
	@GetMapping("/uptime")
	public ResponseEntity<UptimeResponse> getUpTime(){
		Instant now = Instant.now();
		double uptimeSeconds = ChronoUnit.SECONDS.between(SERVER_START, now);
		UptimeResponse response = new UptimeResponse(SERVER_START.toString(), now.toString(), uptimeSeconds);
		return ResponseEntity.ok(response);
	}
	
	// Check Shutdown?
	// By POST http://localhost:8080/api/v1/admin/shutdown
	@PostMapping("/shutdown")
	public ResponseEntity<?> shutdown(){
		if(shuttingDown) {
			ErrorResponse error = new ErrorResponse(Instant.now().toString(),
					"Conflict", "Shutdown already in progress", 409,"/api/v1/admin/shutdown" );
			return ResponseEntity.status(409).body(error);
		}
		shuttingDown = true;
		
		Thread.ofVirtual().start(() -> {
		    try {
		        Thread.sleep(100);
		        System.exit(0);
		    } catch (InterruptedException e) {
		        Thread.currentThread().interrupt();
		    }
		});
		return ResponseEntity.status(202).body(new ShutdownResponse("Graceful shutdown requested."));
	}
	
	
	
	
}
