package org.shoushitsu.waveprint.spectrogram;

import org.shoushitsu.waveprint.exceptions.fft.LengthMismatchException;

import javax.annotation.Nonnull;

/**
 * Efficiently computes a Fast Fourier Transform of a periodic wave that has
 * a small head of non-zero elements and a long tail of zeros.
 * <p/>
 * More specifically, if an FFT frame of length 2<sup>n</sup> has non-zero
 * elements only in its first 2<sup>m</sup> positions, this implementation
 * optimizes calculations of recombination steps where it is sure that the
 * answer will be 0.
 *
 * Consider computations at level <code>l</code>, where two FFTs of length
 * 2<sup>l-1</sup> are recombined into an FFT of length 2<sup>l</sup>. If
 * <code>l < n-m</code>, some of the frames are dependent only on zero-filled
 * frames from previous level. Namely, on level <code>l</code>, in each
 * consecutive group of 2<sup>n-m-l</sup> frames, only the first one can contain
 * non-zero elements.
 *
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
class ZeroTailComplexFFT implements ComplexFFT {

	private final int myLengthLog2;
	private final int myNonzeroLog2;
	private final PrecomputedFftData myPrecomputedFftData;

	ZeroTailComplexFFT(final int lengthLog2, final int nonzeroLog2) {
		myLengthLog2 = lengthLog2;
		myNonzeroLog2 = nonzeroLog2;
		myPrecomputedFftData = new PrecomputedFftData(lengthLog2);
	}

	@Override
	public void transform(@Nonnull final double[] re, @Nonnull final double[] im) {
		final int length = 1 << myLengthLog2;
		if (re.length != length) {
			throw new LengthMismatchException(true, re.length, length);
		}
		if (im.length != length) {
			throw new LengthMismatchException(false, re.length, length);
		}

		/*
		 Since we're computing in place,
		 decomposition is implemented as a permutation.
		*/
		bitReverseSort(re, im);

		// Recombination stage. Apply butterflies ad nauseum.
		for (int l = 1; l <= myLengthLog2; ++l) {
			/*
			 On level l we recombine frames of 2^l
			 from pairs of frames of 2^(l-1)
			*/
			recombine(re, im, length, l);
		}

		// Normalize coefficients.
		for (int i = 0; i < length; i++) {
			re[i] /= length;
			im[i] /= length;
		}
	}

	/**
	 * Recombines frames of length 2<sup>l</sup> from pairs of FFTs of length
	 * 2<sup>l-1</sup>.
	 *
	 * @param re        real parts
	 * @param im        imaginary parts
	 * @param length    length of full FFT
	 * @param l         current recombination level
	 */
	private void recombine(
			final double[] re,
			final double[] im,
			final int length,
			final int l
	) {
		/*
		 Only frames with indexes that produce 0 after being &'d with this mask
		 (essentially, first frames in 2^?) can contain non-zeros and therefore
		 have to be fully evaluated.
		*/
		final int dirtyWindowMask;
		if (l < (myLengthLog2 - myNonzeroLog2)) {
			dirtyWindowMask = (1 << (myLengthLog2 - myNonzeroLog2 - l)) - 1;
		} else {
			// at sufficiently high levels there are no non-dirty frames
			dirtyWindowMask = 0;
		}

		// length of FFT frame on previous level
		final int prevLength = 1 << (l - 1);
		// iterate over indexes of elements in a frame
		for (int k = 0; k < prevLength; ++k) {
			// iterate over indexes of frames
			for (int ixK = 0, windowCount = length >> l;
				 ixK < windowCount;
				 ++ixK
			) {
				/*
				 If the condition is false, it means that the frame is
				 recombined from two zero-filled frames. Since everything is
				 computed in place, we don't even have to assign zeros, they
				 are already there after the bit reverse sort.
				 */
				if ((ixK & dirtyWindowMask) == 0) {
					butterfly(re, im, l - 1, k, ixK);
				}
			}
		}
	}

	/**
	 * Applies the butterfly diagram, computing one pair of coefficients.
	 *
	 * @param re           real parts
	 * @param im           imaginary parts
	 * @param lMinusOne    level (and binary logarithm of length) of source FFTs
	 * @param k            index of coefficients in the source FFTs
	 * @param ixK          index of target FFT frame
	 */
	private void butterfly(
			final double[] re,
			final double[] im,
			final int lMinusOne,
			final int k,
			final int ixK
	) {
		final int prevLength = 1 << lMinusOne;
		final int curLength = prevLength << 1;

		final double reTk = myPrecomputedFftData.getCosines()[lMinusOne][k];
		final double imTk = myPrecomputedFftData.getSines()[lMinusOne][k];

		// index of k'th element of frame ixK
		final int curK = k + ixK * curLength;
		// index of (k + half_of_frame)'th element of frame ixK
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

	/**
	 * Applies precomputed bit reverse sort to the arrays of coefficients.
	 *
	 * @param re    real parts
	 * @param im    imaginary parts
	 *
	 * @see PrecomputedFftData#getBitReversalPermutation()
	 */
	private void bitReverseSort(final double[] re, final double[] im) {
		final int length = 1 << myLengthLog2;
		final int nonzeroLength = 1 << myNonzeroLog2;
		final double[] tRe = new double[length];
		final double[] tIm = new double[length];
		final int[] permutation =
				myPrecomputedFftData.getBitReversalPermutation();
		for (int i = 0; i < length; i++) {
			final int ixFrom = permutation[i];
			if (ixFrom < nonzeroLength) {
				tRe[i] = re[ixFrom];
				tIm[i] = im[ixFrom];
			} else {
				tRe[i] = 0;
				tIm[i] = 0;
			}
		}
		System.arraycopy(tRe, 0, re, 0, length);
		System.arraycopy(tIm, 0, im, 0, length);
	}

}
