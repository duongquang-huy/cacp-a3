package comp3011.assignment1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//Time(seconds) of audio OpenAI charged for
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhisperUsage {
	public String type;
	public double seconds;
}
