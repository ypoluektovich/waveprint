package org.shoushitsu.waveprint.spectrogram;

import msyu.util.collect.Pair;

/**
 * Encapsulates data needed for computation of FFT, that can be precomputed.
 * <p/>
 * The items contained within are:
 * <ul>
 *     <li>{@link #getBitReversalPermutation() bit reversal permutation};</li>
 *     <li>{@link #getSines() some values} of the
 *     {@link Math#sin(double) sine} function;</li>
 *     <li>{@link #getCosines() some values} of the
 *     {@link Math#cos(double) cosine} function.</li>
 * </ul>
 *
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
class PrecomputedFftData {
	private final int myLengthLog2;
	private final int[] myBitReversalPermutation;
	private final double[][] mySines;
	private final double[][] myCosines;

	/**
	 * Computes data needed for FFT with the specified base-2 logarithm of
	 * frame length, and stores it in a new object.
	 *
	 * @param lengthLog2    base-2 logarithm of the FFT frame.
	 *                         Must not be negative.
	 */
	protected PrecomputedFftData(final int lengthLog2) {
		if (lengthLog2 < 0) {
			throw new IllegalArgumentException(
					"Base-2 logarithm of FFT window length must be positive, " +
							"but " + lengthLog2 + " was specified"
			);
		}
		myLengthLog2 = lengthLog2;
		myBitReversalPermutation = precomputeBitReversal(lengthLog2);
		final Pair<double[][], double[][]> sinesAndCosines = precomputeSines(
				lengthLog2
		);
		mySines = sinesAndCosines.getFirst();
		myCosines = sinesAndCosines.getSecond();
	}

	/**
	 * Get the base-2 logarithm of FFT frame length for which this object's
	 * contents were computed.
	 *
	 * @return the base-2 logarithm value.
	 */
	protected final int getLengthLog2() {
		return myLengthLog2;
	}

	/**
	 * Get the precomputed bit reversal permutation.
	 *
	 * @return an array of which <code>i</code>'th element is the index of
	 * element in the source sequence which must be placed into
	 * <code>i</code>'th position of the permuted sequence:
	 * <pre>permuted[i] := source[result[i]]</pre>
	 */
	protected final int[] getBitReversalPermutation() {
		return myBitReversalPermutation;
	}

	/**
	 * Get the precomputed sines for FFT butterfly recombination.
	 *
	 * @return a 2-dimensional array of precomputed sine values,
	 * indexed first by (recombination level - 1), then (index of elements in
	 * frames on the previous recombination level used by a single butterfly).
	 *
	 * @see #getCosines()
	 */
	protected final double[][] getSines() {
		return mySines;
	}

	/**
	 * Get the precomputed cosines for FFT butterfly recombination.
	 *
	 * @return a 2-dimensional array of precomputed cosine values,
	 * indexed first by (recombination level - 1), then (index of elements in
	 * frames on the previous recombination level used by a single butterfly).
	 *
	 * @see #getSines()
	 */
	protected final double[][] getCosines() {
		return myCosines;
	}

	/**
	 * Computes the bit reversal permutation.
	 *
	 * @param lengthLog2    binary logarithm of length of intended FFT frame
	 * @return an array containing the permutation indexes.
	 *
	 * @see #getBitReversalPermutation()
	 */
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

	/**
	 * Computes sines and cosines of arguments that will be needed in
	 * (full) FFT of given length.
	 *
	 * @param lengthLog2    binary logarithm of length of full FFT frame.
	 *
	 * @return a pair of arrays (for sines and cosines, in that order).
	 *
	 * @see #getSines()
	 * @see #getCosines()
	 */
	private static Pair<double[][], double[][]> precomputeSines(
			final int lengthLog2
	) {
		final double[][] sin = new double[lengthLog2][];
		final double[][] cos = new double[lengthLog2][];
		for (int l = 1; l <= lengthLog2; ++l) {
			final int curLength = 1 << l;
			final int prevLength = curLength >> 1;
			sin[l - 1] = new double[prevLength];
			cos[l - 1] = new double[prevLength];
			for (int k = 0; k < prevLength; ++k) {
				final double argTk = -2.0 * Math.PI * k / curLength;
				sin[l - 1][k] = Math.sin(argTk);
				cos[l - 1][k] = Math.cos(argTk);
			}
		}
		return Pair.of(sin, cos);
	}


	/**
	 * Checks if another object is equal to this one.
	 * <p/>
	 * This method does not accept subclasses. Past that, the only
	 * comparison this method does is that of {@link #myLengthLog2} fields,
	 * as everything else here is computed from that value.
	 *
	 * @param o    another object.
	 * @return {@code true} if {@code o} is a precomputed FFT data object that
	 * contains the same data as this one; {@code false} otherwise.
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PrecomputedFftData other = (PrecomputedFftData) o;
		return myLengthLog2 == other.myLengthLog2;
	}

	/**
	 * Returns a hash code value for the object.
	 * <p/>
	 * The value of ({@link #myLengthLog2} + 1) is used. The increment is to
	 * differentiate from {@code null} values, which are often hashed as
	 * {@code 0}).
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode() {
		return myLengthLog2;
	}

}
