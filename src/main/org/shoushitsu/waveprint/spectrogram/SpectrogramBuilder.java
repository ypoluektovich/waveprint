package org.shoushitsu.waveprint.spectrogram;

import javax.annotation.Nonnull;

/**
 * Builds spectrograms from samples.
 *
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface SpectrogramBuilder {
	/**
	 * Computes a spectrogram from the given samples.
	 *
	 * @param samples    the array with sound samples.
	 * @param start      where to start getting samples for the spectrogram.
	 *
	 * @return an array with the spectrogram. First index is time (measured in
	 * samples, second index is frequency bin.
	 */
	double[][] getSpectrogram(@Nonnull int[] samples, int start);

	/**
	 * Get the amount of samples that one spectrogram uses.
	 *
	 * @return the amount of samples.
	 */
	int getSpectrogramLengthInSamples();
}
