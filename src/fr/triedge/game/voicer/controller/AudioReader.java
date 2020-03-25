package fr.triedge.game.voicer.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import fr.triedge.game.voicer.common.Message;
import fr.triedge.game.voicer.common.SoundPacket;
import fr.triedge.game.voicer.utils.Utils;

public class AudioReader implements Runnable{

	private SourceDataLine speaker = null; //speaker
	private ArrayList<Message> queue = new ArrayList<Message>(); //queue of messages to be played
	private int lastSoundPacketLen = SoundPacket.defaultDataLenght;
	//private long lastPacketTime = System.nanoTime();
	private boolean running = true;

	@Override
	public void run() {
		try {
			//open channel to sound card
			AudioFormat af = SoundPacket.defaultFormat;
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
			speaker = (SourceDataLine) AudioSystem.getLine(info);
			speaker.open(af);
			speaker.start();
			while (isRunning()) {
				if (queue.isEmpty()) { //nothing to play, wait
					Utils.sleep(10);
					continue;
				}else {
					//lastPacketTime = System.nanoTime();
					Message in = queue.get(0);
					queue.remove(in);
					if (in.getData() instanceof SoundPacket) { //it's a sound packet, send it to sound card
						SoundPacket m = (SoundPacket) (in.getData());
						if (m.getData() == null) {//sender skipped a packet, play comfort noise
							byte[] noise = new byte[lastSoundPacketLen];
							for (int i = 0; i < noise.length; i++) {
								noise[i] = (byte) ((Math.random() * 3) - 1);
							}
							speaker.write(noise, 0, noise.length);
						}else {
							//decompress data
							GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(m.getData()));
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							for (;;) {
								int b = gis.read();
								if (b == -1) {
									break;
								} else {
									baos.write((byte) b);
								}
							}
							//play decompressed data
							byte[] toPlay=baos.toByteArray();
							speaker.write(toPlay, 0, toPlay.length);
							lastSoundPacketLen = m.getData().length;
						}
					}else { //not a sound packet, trash
						continue; //invalid message
					}
				}
			}
		} catch (LineUnavailableException | IOException e) {
			if (speaker != null) {
				speaker.close();
			}
			stop();
			e.printStackTrace();
		}
	}

	private void stop() {
		setRunning(false);
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
