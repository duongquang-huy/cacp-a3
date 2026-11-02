package comp3011.assignment1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
// Service to handle audio transcription via OpenAI

@Service
public class TranscriptionService {
	private final StatsService statsService;
	private final RestClient restClient;
	private final String apiKey;
	// Log to track service's work
	private static final Logger logger = LoggerFactory.getLogger(TranscriptionService.class);
	
	public TranscriptionService(StatsService statsService) {
		this.statsService =  statsService;
		this.apiKey = System.getenv("OPENAI_API_KEY"); // Get key from OS
		this.restClient = RestClient.builder()
				.baseUrl("https://api.openai.com/v1").build();
	}
	
	/**
	@param audioBytes
	@param filename
	@return
	*/
	public String transcribe(byte[] audioBytes, String filename) throws Exception {
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
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", audioResource);
		body.add("model","gpt-4o-mini-transcribe");
		
		// Call API
		String resJson = restClient.post()
				.uri("/audio/transcriptions")
				.header("Authorization", "Bearer " + apiKey)
				.contentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA)
				.body(body)
				.retrieve().body(String.class);
		
		// Parse JSON to text 
		int start = resJson.indexOf("\"text\":\"") + 8;
		int end = resJson.indexOf("\"",start);
		String text = resJson.substring(start,end);
		
		// Parse  (IN-OUT) Tokens
		int inputS = resJson.indexOf("\"input_tokens\":") + 15;
		int inputE = resJson.indexOf(",",inputS);
		long inputTokens = Long.parseLong(resJson.substring(inputS, inputE).trim());
		
		int outputS = resJson.indexOf("\"output_tokens\":") + 16;
		int outputE = resJson.indexOf("}",outputS);
		long outputTokens = Long.parseLong(resJson.substring(outputS, outputE).trim());		
		statsService.addTokens(inputTokens, outputTokens);
		logger.info("Transcription completed - Input Tokens: {}, OutputTokens: {}", inputTokens, outputTokens);
		return text;
	}
}
