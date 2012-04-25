package org.shoushitsu.waveprint.exceptions.fft;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class LengthMismatchException extends RuntimeException {

	public LengthMismatchException(final boolean real, final int arrayLength, final int parameterLength) {
		super(String.format(
				"%s sample array's length is invalid: %d, specified %d",
				real ? "RE" : "IM",
				arrayLength,
				parameterLength
		));
	}
}
