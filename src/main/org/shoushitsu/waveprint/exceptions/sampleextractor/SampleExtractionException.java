package org.shoushitsu.waveprint.exceptions.sampleextractor;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public abstract class SampleExtractionException extends Exception {
	public SampleExtractionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public SampleExtractionException(final String message) {
		super(message);
	}
}
