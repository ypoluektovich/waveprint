package org.shoushitsu.waveprint.exceptions.sampleextractor;

import java.io.IOException;

/**
* @author Yanus Poluektovich (ypoluektovich@gmail.com)
*/
public class AudioConversionStartException extends SampleExtractionException {
	public AudioConversionStartException(final IOException e) {
		super("Failed to start converter child process", e);
	}
}
