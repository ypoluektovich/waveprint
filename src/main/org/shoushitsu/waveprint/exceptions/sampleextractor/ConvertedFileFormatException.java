package org.shoushitsu.waveprint.exceptions.sampleextractor;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
* @author Yanus Poluektovich (ypoluektovich@gmail.com)
*/
public class ConvertedFileFormatException extends SampleExtractionException {
	public ConvertedFileFormatException(final UnsupportedAudioFileException e) {
		super("Audio format of converted source is not supported", e);
	}
}
