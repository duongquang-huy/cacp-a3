package comp3011.assignment1.controller;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.model.ErrorResponse;
import comp3011.assignment1.model.TranscriptionResponse;
import comp3011.assignment1.service.TranscriptionService;

// Controller for audio transcribe endpoint using 
// POST /api/v1/transcribe
@RestController
@RequestMapping("/api/v1")
public class TranscriptionController {
	private final TranscriptionService transcriptionService;
	
	public TranscriptionController(TranscriptionService transcriptionService) {
		this.transcriptionService =  transcriptionService;
	}
	@PostMapping("/transcribe")
	public ResponseEntity<?> transcribe(@RequestParam("audio") MultipartFile audio) {
		try {
			String text = transcriptionService.transcribe(audio.getBytes(),audio.getOriginalFilename());
			return ResponseEntity.ok(new TranscriptionResponse(text));
		}catch (Exception e) {
			return ResponseEntity.status(500)
					.body(new ErrorResponse(
							Instant.now().toString(),"Server Error",
							e.getMessage(), 500, "/api/v1/transcribe"));
		}
	}
}
