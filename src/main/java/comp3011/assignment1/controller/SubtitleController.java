package comp3011.assignment1.controller;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.model.ErrorResponse;
import comp3011.assignment1.model.WhisperResponse;
import comp3011.assignment1.service.SubtitleService;

// Controller for sub endpoint
//POST /api/v1/transcribe
@RestController
@RequestMapping("/api/v1")
public class SubtitleController {
	private final SubtitleService subtitleService;
	
	public SubtitleController(SubtitleService subtitleService) {
		this.subtitleService = subtitleService;
	}
	@PostMapping("/subtitles")
	public ResponseEntity<?> subtitles(@RequestParam("audio") MultipartFile audio){
		try {
			WhisperResponse response = subtitleService.getSubtitles(audio.getBytes(),audio.getOriginalFilename());
			return ResponseEntity.ok(response);
		}catch (Exception e){
			return ResponseEntity.status(500)
					.body(new ErrorResponse(
							Instant.now().toString(),"Server Error",
							e.getMessage(), 500, "/api/v1/subtitles"));
		}
	}
}
