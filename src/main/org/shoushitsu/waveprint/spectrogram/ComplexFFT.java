package org.shoushitsu.waveprint.spectrogram;

import javax.annotation.Nonnull;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface ComplexFFT {
	void transform(@Nonnull double[] re, @Nonnull double[] im);
}
