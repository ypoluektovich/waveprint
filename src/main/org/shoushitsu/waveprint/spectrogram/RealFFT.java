package org.shoushitsu.waveprint.spectrogram;

import javax.annotation.Nonnull;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
class RealFFT {
	private final int lengthDiv2;
	private final ComplexFFT complexFFT;

	RealFFT(final int lengthLog2) {
		lengthDiv2 = 1 << (lengthLog2 - 1);
		complexFFT = new ComplexFFT(lengthLog2);
	}

	void transform(@Nonnull final double[] re) {
		final double[] im = new double[re.length];
		complexFFT.transform(re, im);

		// copy sin coefficients to source array and re-normalize
		re[0] += re[lengthDiv2];
		re[lengthDiv2] = 0;
		for (int i = 1; i < lengthDiv2; i++) {
			re[i] *= 2;
			re[lengthDiv2 + i] = -2.0 * im[i];
		}
	}

}
