package comp3011;
/* 
 * Authors:
 *  Duong Quang Huy a2950659
 */

// Sub line and show time(milisecond)
public class Subtitle {
	public String text;
	public long startUs;
	public long endUs;
	
	public Subtitle(String text, long startUs, long endUs) {
		this.text = text;
		this.startUs = startUs;
 		this.endUs = endUs;
	}
}
