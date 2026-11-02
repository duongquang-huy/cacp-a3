package comp3011.assignment1.model;

//Returns transcribed text from OpenAI STT API
public class TranscriptionResponse {
	private String text;
	
	public TranscriptionResponse(String text) {
		this.text = text;
	}
	public String getText() {return text;}
}
