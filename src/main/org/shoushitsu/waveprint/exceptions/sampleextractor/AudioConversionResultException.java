package org.shoushitsu.waveprint.exceptions.sampleextractor;

/**
* @author Yanus Poluektovich (ypoluektovich@gmail.com)
*/
public class AudioConversionResultException extends SampleExtractionException {
	public AudioConversionResultException(final int exitCode) {
		super("Converter exited with nonnull code: " + exitCode);
	}
}
