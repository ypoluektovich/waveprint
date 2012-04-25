package org.shoushitsu.waveprint.spectrogram;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class LogarithmicFrequencySplitter implements FrequencySplitter {

	private final int myBinCount;

	private final int[] myBinRanges;

	/**
	 * Creates a new logarithmic frequency splitter.
	 *
	 * @param sampleRate          sample rate of source audio.
	 * @param amplitudeCount      count of amplitudes coming out of Fourier
	 *                               Transforms.
	 * @param binCount            count of bins to split frequencies into.
	 * @param lowestFrequency     the lowest frequency to take, in Hz.
	 * @param highestFrequency    the highest frequency to take, in Hz.
	 */
	protected LogarithmicFrequencySplitter(
			final int sampleRate,
			final int amplitudeCount,
			final int binCount,
			final int lowestFrequency,
			final int highestFrequency
	) {
		myBinCount = binCount;

		// this is the frequency to which the last FFT amplitude corresponds
		final int fftMaxSampleRate = sampleRate / 2;

		final double maxLog = Math.log(highestFrequency / lowestFrequency);

		myBinRanges = new int[myBinCount * 2];
		for (int i = 0; i < myBinCount; i++) {
			final double lowLog = maxLog * i / myBinCount;
			final double highLog = maxLog * (i + 1) / myBinCount;

			final double lowFreq = lowestFrequency * Math.exp(lowLog);
			final double highFreq = lowestFrequency * Math.exp(highLog);

			// why not round? - it sometimes breaks 'increasingness' of bin size
			// why ceil not floor? - wanted to capture more high freqs
			myBinRanges[i * 2] = (int) Math.ceil(
					amplitudeCount * lowFreq / fftMaxSampleRate);
			myBinRanges[i * 2 + 1] = (int) Math.ceil(
					amplitudeCount * highFreq / fftMaxSampleRate);
		}
	}

	@Override
	public double[] apply(final double[] amplitudes) {
		final double[] bins = new double[myBinCount];
		for (int ixBin = 0; ixBin < myBinCount; ++ixBin) {
			double amplitudeSum = 0;
			final int ixAmpLow = myBinRanges[ixBin * 2];
			final int ixAmpHigh = myBinRanges[ixBin * 2 + 1];
			for (int j = ixAmpLow; j < ixAmpHigh; ++j) {
				amplitudeSum += amplitudes[j];
			}
			bins[ixBin] = amplitudeSum;
		}
		return bins;
	}


	public static class Factory implements FrequencySplitterFactory {

		private final int mySampleRate;
		private final int myBinCount;
		private final int myLowestFrequency;
		private final int myHighestFrequency;

		public Factory(
				final int sampleRate,
				final int binCount,
				final int lowestFrequency,
				final int highestFrequency
		) {
			mySampleRate = sampleRate;
			myBinCount = binCount;
			myLowestFrequency = lowestFrequency;
			myHighestFrequency = highestFrequency;
		}

		@Override
		public FrequencySplitter newSplitter(final int amplitudeCount) {
			return new LogarithmicFrequencySplitter(
					mySampleRate,
					amplitudeCount,
					myBinCount,
					myLowestFrequency,
					myHighestFrequency
			);
		}
	}
}
