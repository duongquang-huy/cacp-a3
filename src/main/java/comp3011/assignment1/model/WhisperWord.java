package comp3011.assignment1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Each Word with timing (seconds)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhisperWord {
	public String word;
	public double start;
	public double end;
}
