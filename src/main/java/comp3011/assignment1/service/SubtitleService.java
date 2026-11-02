package comp3011.assignment1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import comp3011.assignment1.model.WhisperResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import java.util.ArrayList;
import java.util.List;
import comp3011.assignment1.model.WhisperSegment;

// Send audio to whisper model and get back sub
@Service
public class SubtitleService {
	private static final Logger logger = LoggerFactory.getLogger(SubtitleService.class); // Log to track service's work
	private final  RestClient restClient;
	private final String apiKey;
	private static final double NO_SPEECH_LIMIT = 0.6;
	private static final double LOGPROB_LIMIT = -1;
	
	public SubtitleService() {
		this.apiKey = System.getenv("OPENAI_API_KEY"); // Get Key from OS
		this.restClient = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
	}
	public WhisperResponse getSubtitles(byte[] audioBytes, String filename) {
		logger.info("Transcribe for file: {}", filename);
		if (apiKey == null) {
			logger.error("API_KEY not set");
			throw new IllegalStateException("API key is null");
		}
		// Build multi request
		ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
			@Override
			public String getFilename() {return filename;}
		};
		// Request body
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", audioResource);
		body.add("model", "whisper-1");
		body.add("response_format","verbose_json");
		body.add("timestamp_granularities[]", "word");
		body.add("timestamp_granularities[]", "segment");
		
		// Call OpenAI API, turning JSON to WhisperResponse
		WhisperResponse response =  restClient.post()
				.uri("/audio/transcriptions")
				.header("Authorization", "Bearer " + apiKey)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(body)
				.retrieve().body(WhisperResponse.class);
		// Filter only real speech
		List<WhisperSegment> keep = new ArrayList<>();
		for (WhisperSegment seg : response.segments) {
			boolean isFake = seg.no_speech_prob > NO_SPEECH_LIMIT && seg.avg_logprob < LOGPROB_LIMIT;
			if (isFake) {
				logger.info("Skip segment: {}", seg.text);
			}else {
				keep.add(seg);
			}
		}
		response.segments = keep;
		return response;
	}
	
}
