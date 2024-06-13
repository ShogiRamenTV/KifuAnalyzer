package lib;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// -------------------------------------------------------------------------
// ----------------------- << Sound >> -------------------------------------
// -------------------------------------------------------------------------
public class KomaSound {
	String soundFilePath = "./sound/Koma Oto.wav";
	Clip soundKoma;
	public KomaSound() {
		
	}
	public void initializeSoundSetting() {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(soundFilePath));
			soundKoma = AudioSystem.getClip();
			soundKoma.open(ais);
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	public void soundKoma() {
		soundKoma.stop();
		soundKoma.setFramePosition(0);
		soundKoma.start();
	}
}