package org.shoushitsu.waveprint.spectrogram;

import org.shoushitsu.waveprint.exceptions.fft.LengthMismatchException;

import javax.annotation.Nonnull;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
class SimpleComplexFFT implements ComplexFFT {

	private final int myLengthLog2;
	private final PrecomputedFftData myPrecomputedFftData;

	SimpleComplexFFT(final int lengthLog2) {
		myLengthLog2 = lengthLog2;
		myPrecomputedFftData = new PrecomputedFftData(lengthLog2);
	}

	@Override
	public void transform(
			@Nonnull final double[] re,
			@Nonnull final double[] im
	) {
		final int length = 1 << myLengthLog2;
		if (re.length != length) {
			throw new LengthMismatchException(true, re.length, length);
		}
		if (im.length != length) {
			throw new LengthMismatchException(false, re.length, length);
		}

		bitReverseSort(re, im, length);

		final double[][] cos = myPrecomputedFftData.getCosines();
		final double[][] sin = myPrecomputedFftData.getSines();
		for (int l = 1; l <= myLengthLog2; ++l) { // current level
			final int curLength = 1 << l; // length of window on current level
			final int prevLength = curLength >> 1; // length of window on previous level
			for (int k = 0; k < prevLength; ++k) {
				final double reTk = cos[l - 1][k];
				final double imTk = sin[l - 1][k];
				for (int curK = k; curK < length; curK += curLength) { // number of window on current level
					final int curK2 = curK + prevLength;

					final double reEk = re[curK];
					final double imEk = im[curK];

					final double reOk = re[curK2];
					final double imOk = im[curK2];

					final double reTOk = reTk * reOk - imTk * imOk;
					final double imTOk = imTk * reOk + reTk * imOk;

					re[curK] = reEk + reTOk;
					im[curK] = imEk + imTOk;

					re[curK2] = reEk - reTOk;
					im[curK2] = imEk - imTOk;
				}
			}
		}

		// normalize
		for (int i = 0; i < length; i++) {
			re[i] /= length;
			im[i] /= length;
		}
	}

	private void bitReverseSort(
			final double[] re,
			final double[] im,
			final int length
	) {
		final double[] tRe = new double[length];
		final double[] tIm = new double[length];
		final int[] permutation =
				myPrecomputedFftData.getBitReversalPermutation();
		for (int i = 0; i < length; i++) {
			tRe[i] = re[permutation[i]];
			tIm[i] = im[permutation[i]];
		}
		System.arraycopy(tRe, 0, re, 0, length);
		System.arraycopy(tIm, 0, im, 0, length);
	}

}
