package comp3011;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacv.Frame;

// Draw subtitle boxes onto each frame: white text, black outline
// max 42 char/line, max 2 lines
public class FrameSubtitler extends FrameProcessor {
	private static final int MAX_CHARS = 42;
	private static final int MAX_LINES = 2;
	private static final int OUTLINE = 2; // outline thickness (pixels) 
	private static final int MARGIN = 20; // gap from bottom (pixels)
    private volatile boolean enabled = true; // on/of subtitle switch
    private String lastText = null;
    private BufferedImage lastImage = null;
    // All subs received from the server
    private final List<Subtitle> subtitles = new ArrayList<>();
    private static final long MIN_SHOW_US = 1000000; // show each line at least 1 second
    private static final long MAX_SHOW_US = 7000000; // max: 7 seconds

	
    @Override
    public void process(Frame frame, InfoFrame info) throws Exception{
    	if(!enabled) {
    		return;
    	}
    	if(frame.image == null || frame.image.length == 0 || frame.image[0] == null) {
    		return;
    	}
    	if (!(frame.image[0] instanceof ByteBuffer pixels)) {
    		return;
    	}
    	
    	// Find the sub for now
    	String text = getTextAt(info.timestampUs());
    	if (text == null) {
    		return;
    	}
    	
    	// New pic for new text
    	if(!text.equals(lastText)) {
    		lastImage = makeTextImage(text);
    		lastText = text;
    	}
    	
    	pasteImage(lastImage, pixels);
    }
    
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isEnabled() {
		return enabled;
	}
	
	public synchronized void addSubtitles(List<Subtitle> newSubtitles) {
		for(Subtitle s: newSubtitles) {
			long length = s.endUs - s.startUs;
			if(length < MIN_SHOW_US) {
				s.endUs = s.startUs + MIN_SHOW_US; // make the sub 1 sec if too short
			}
			if (length > MAX_SHOW_US) {
				s.endUs = s.startUs + MAX_SHOW_US; // make the sub shorter if too long
			}
			subtitles.add(s);
		}
	}
	
	private synchronized String getTextAt(long timestampUs) {
		for(Subtitle s:subtitles) {
			if(timestampUs >= s.startUs && timestampUs < s.endUs) {
				return s.text;
			}
		}
		return null; // If no sub
	}
	
	// Split the text to lines of max 42 char/lines, max 2 lines
	private List<String> splitLines(String text){
		List<String> lines = new ArrayList<>();
		String line = "";
		String[] words = text.trim().split(" ");
		for(String word : words) {
			if (line.equals("")) {
				line = word;
			}else if (line.length() + 1 + word.length() <= MAX_CHARS) {
				line = line + " " + word;
			}else {
				lines.add(line);
				line = word;
			}
		}
		lines.add(line);
		
		// Keep only 2 first lines
		while (lines.size() > MAX_LINES) {
			lines.remove(lines.size() - 1);
		}
		return lines;
	}
	
	// Draw white text and black outline
	private BufferedImage makeTextImage(String text) {
		List<String> lines = splitLines(text);
		int width = videoInfo.imageWidth();
		int fontSize = videoInfo.imageHeight()/18;
		int lineHeight = fontSize + 10;
		int height = lineHeight*lines.size() + 10;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setFont(new Font("SansSerif", Font.BOLD, fontSize));
		
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			int textWidth = g.getFontMetrics().stringWidth(line);
			int x = (width - textWidth)/2;
			int y = (i+1)*lineHeight;
			
			// Black outline
			g.setColor(Color.BLACK);
			for (int dx = -OUTLINE; dx <= OUTLINE; dx++) {
				for (int dy = -OUTLINE; dy <= OUTLINE; dy++) {
					g.drawString(line, x + dx, y + dy);
				}
			}
			
			// White text
			g.setColor(Color.WHITE);
			g.drawString(line, x, y);
		}
		g.dispose();
		return image;
	} 
	
	private void pasteImage(BufferedImage image, ByteBuffer pixels) {
		int top = videoInfo.imageHeight() - image.getHeight() - MARGIN;
		if (top<0) {
			return; // video too small
		}
		for (int y = 0; y<image.getHeight(); y++) {
			for (int x = 0; x<image.getWidth(); x++) {
				int color = image.getRGB(x, y);
				if (color == Color.WHITE.getRGB()) {
					setPixel(pixels, x, top +y, 255); //White
				}else if(color == Color.BLACK.getRGB()) {
					setPixel(pixels, x, top+y, 0); // black
				}
			}
		}
	}
	
	// Same as setPixel in FrameNumberer
	private void setPixel(ByteBuffer pixels, int x, int y, int value) {
        int base = y * videoInfo.imageStride() + x * videoInfo.imageChannels();
        byte byteValue = (byte) value;

        for (int channel = 0; channel < videoInfo.imageChannels(); channel++) {
            pixels.put(base + channel, byteValue);
        }
    }
}

