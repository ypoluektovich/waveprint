package org.shoushitsu.waveprint.spectrogram;

import msyu.util.collect.Pair;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The interface for complex Discrete-time
 * Short-Time Fourier Transform (STFT) computation.
 *
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface ComplexSTFT {

	/**
	 * Compute the complex STFT.
	 *
	 * @param re the array for real parts.
	 * @param im the array for imaginary parts.
	 *
	 * @return a list of {@code (Re, Im)} pairs, where {@code Re} stands for
	 * the real-parts array, and {@code Im} stands for the imaginary-parts
	 * array. Each pair is the result of a Discrete-time Fourier Transform of
	 * the frame of the source data with the corresponding index.
	 *
	 * @throws IllegalArgumentException if one of the arrays is too short.
	 */
	List<Pair<double[], double[]>> transform(
			@Nonnull double[] re,
			@Nonnull double[] im
	) throws IllegalArgumentException;

}
