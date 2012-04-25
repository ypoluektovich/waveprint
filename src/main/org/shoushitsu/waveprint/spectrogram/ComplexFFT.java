package org.shoushitsu.waveprint.spectrogram;

import msyu.util.collect.Pair;
import org.shoushitsu.waveprint.exceptions.fft.LengthMismatchException;

import javax.annotation.Nonnull;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
class ComplexFFT {

	private final int myLengthLog2;
	private final int[] myBitReversalPermutation;
	private final double[][] mySines;
	private final double[][] myCosines;

	ComplexFFT(final int lengthLog2) {
		myLengthLog2 = lengthLog2;

		myBitReversalPermutation = precomputeBitReversal(lengthLog2);

		final Pair<double[][], double[][]> sinAndCos = precomputeSines(myLengthLog2);
		mySines = sinAndCos.getFirst();
		myCosines = sinAndCos.getSecond();
	}

	void transform(@Nonnull final double[] re, @Nonnull final double[] im) {
		final int length = 1 << myLengthLog2;
		if (re.length != length) {
			throw new LengthMismatchException(true, re.length, length);
		}
		if (im.length != length) {
			throw new LengthMismatchException(false, re.length, length);
		}

		bitReverseSort(re, im, length);

		for (int l = 1; l <= myLengthLog2; ++l) { // current level
			final int curLength = 1 << l; // length of window on current level
			final int prevLength = curLength >> 1; // length of window on previous level
			for (int k = 0; k < prevLength; ++k) {
				final double reTk = myCosines[l - 1][k];
				final double imTk = mySines[l - 1][k];
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

	private void bitReverseSort(final double[] re, final double[] im, final int length) {
		final double[] tRe = new double[length];
		final double[] tIm = new double[length];
		for (int i = 0; i < length; i++) {
			tRe[i] = re[myBitReversalPermutation[i]];
			tIm[i] = im[myBitReversalPermutation[i]];
		}
		System.arraycopy(tRe, 0, re, 0, length);
		System.arraycopy(tIm, 0, im, 0, length);

//		final int n2 = n / 2;
//		int j = n2;
//		for (int i = 0; i < n - 2; i++) {
//			if (i < j) {
//				double t = re[i];
//				re[i] = re[j];
//				re[j] = t;
//				t = im[i];
//				im[i] = im[j];
//				im[j] = t;
//			}
//			int k = n2;
//			while (k <= j) {
//				j -= k;
//				k /= 2;
//			}
//			j += k;
//		}
	}

	private static Pair<double[][], double[][]> precomputeSines(final int lengthLog2) {
		final double[][] sin = new double[lengthLog2][1 << (lengthLog2 - 1)];
		final double[][] cos = new double[lengthLog2][1 << (lengthLog2 - 1)];
		for (int l = 1; l <= lengthLog2; ++l) {
			final int curLength = 1 << l;
			final int prevLength = curLength >> 1;
			for (int k = 0; k < prevLength; ++k) {
				final double argTk = -2.0 * Math.PI * k / curLength;
				sin[l - 1][k] = Math.cos(argTk);
				cos[l - 1][k] = Math.sin(argTk);
			}
		}
		return Pair.of(sin, cos);
	}

	private static int[] precomputeBitReversal(final int lengthLog2) {
		final int length = 1 << lengthLog2;
		final int[] result = new int[length];
		for (int i = 0; i < length; i++) {
			int x = i;
			int r = 0;
			for (int j = 0; j < lengthLog2; j++) {
				r = r << 1;
				if ((x & 0x1) == 1) {
					r += 1;
				}
				x = x >> 1;
			}
			result[i] = r;
		}
		return result;
	}
}
