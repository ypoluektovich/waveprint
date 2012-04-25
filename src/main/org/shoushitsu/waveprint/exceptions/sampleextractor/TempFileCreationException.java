package org.shoushitsu.waveprint.exceptions.sampleextractor;

import java.io.IOException;

/**
* @author Yanus Poluektovich (ypoluektovich@gmail.com)
*/
public class TempFileCreationException extends SampleExtractionException {
	public TempFileCreationException(final IOException e) {
		super("Failed to create file for converted audio", e);
	}
}
