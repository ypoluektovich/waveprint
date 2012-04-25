package org.shoushitsu.waveprint.exceptions.sampleextractor;

import java.io.IOException;

/**
* @author Yanus Poluektovich (ypoluektovich@gmail.com)
*/
public class ConvertedFileOpenException extends SampleExtractionException {
	public ConvertedFileOpenException(final IOException e) {
		super("Failed to open converted audio file", e);
	}
}
