package org.shoushitsu.waveprint.exceptions.sampleextractor;

/**
* @author Yanus Poluektovich (ypoluektovich@gmail.com)
*/
public class AudioConversionWaitException extends SampleExtractionException {
	public AudioConversionWaitException(final InterruptedException e) {
		super("Interrupted while converting audio", e);
	}
}
