package org.shoushitsu.waveprint.exceptions.sampleextractor;

import java.io.IOException;

/**
* @author Yanus Poluektovich (ypoluektovich@gmail.com)
*/
public class ConvertedFileReadException extends SampleExtractionException {
	public ConvertedFileReadException(final IOException e) {
		super("IO exception while reading data", e);
	}
}
