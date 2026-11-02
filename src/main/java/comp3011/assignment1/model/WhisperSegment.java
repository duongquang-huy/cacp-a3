package comp3011.assignment1.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//Each Sentence with timing (seconds)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhisperSegment {
	public String text;
	public double start;
	public double end;
	public double avg_logprob; // How sure Whisper is about the words (closer 0 = more sure)
	public double no_speech_prob; // probability of no speech text (0-1)
}
