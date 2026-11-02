package comp3011.assignment1.model;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//Full transcription response from Whisper
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhisperResponse {
	public String text;
	public double duration;
	public List<WhisperSegment> segments = new ArrayList<>();
	public List<WhisperWord> words = new ArrayList<>();
	public WhisperUsage usage;
}
