package comp3011;

import java.nio.ByteBuffer;

import org.bytedeco.javacv.Frame;

// Draw subtitle boxes onto each frame
public class FrameSubtitler extends FrameProcessor {
	// Pixel
	private static final int BOX_HEIGHT = 40; // height of box
	private static final int BOTTOM_MARGIN = 10; // gap between box and bottom
	private String preText = ""; // Previous text, only print when text changes
	private volatile boolean enabled = true; // On/Off switch sub button

	@Override
    public void process(Frame frame, InfoFrame info) throws Exception {
		if (!enabled) {
			return; // sub turned off
		}
        if (frame.image == null || frame.image.length == 0 || frame.image[0] == null) {
            return;
        }

        if (!(frame.image[0] instanceof ByteBuffer pixels)) {
            throw new Exception("FrameSubtitler requires byte-backed image frames");
        }
        // Find the subtitle for the current moment in the video
        String text = getTextAt(info.timestampUs());
        if(text == null) {
        	preText = "";
        	return; // No sub
        }
        if(!text.equals(preText)) {
        	System.out.println("[Subtitle] " +(info.timestampUs() / 1_000_000.0) + "s: " + text );
        	preText = text;
        }
        darkenBox(pixels);
        
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isEnabled() {
		return enabled;
	}
	
	// Fake sub for testing, new/3s
	private String getTextAt(long timestampUs) {
		long second = timestampUs/1_000_000;
		long index = second/3;
		if(index % 4 == 3) {
			return null;
		}
		return "Fake sub: "+index;
	}
	
	private void darkenBox(ByteBuffer pixels) {
		int width = videoInfo.imageWidth();
		int height = videoInfo.imageHeight();
		int channels = videoInfo.imageChannels();
		int stride = videoInfo.imageStride();
		
		int top = height - BOTTOM_MARGIN - BOX_HEIGHT;
		int bottom = height- BOTTOM_MARGIN;
		int left = width/10;
		int right = width - left;
		if (top < 0) {
			return; // vid too small
		}
		for (int y = top; y < bottom; y++) {
			for (int x = left; x < right; x++) {
				int base = y * stride + x * channels;
				for (int c = 0; c < channels; c++) {
					int value = pixels.get(base + c) & 0xFF; 
					pixels.put(base + c, (byte) (value / 3));  
				}
			}
		}
	}
}

