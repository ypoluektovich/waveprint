package org.shoushitsu.waveprint.spectrogram;

import javax.annotation.Nonnull;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class OverlappingFftSpectrogramBuilder implements SpectrogramBuilder {

	private final int myLength;
	private final int myFrameLength;
	private final int myStep;
	private final RealFFT myFFT;
	private final FrequencySplitter myFrequencySplitter;

	public OverlappingFftSpectrogramBuilder(
			final int length,
			final int frameLengthLog2,
			final int step,
			final FrequencySplitterFactory frequencySplitterFactory
	) {
		myLength = length;
		myFrameLength = 1 << frameLengthLog2;
		myStep = step;

		myFFT = new RealFFT(frameLengthLog2);
		myFrequencySplitter =
				frequencySplitterFactory.newSplitter(myFrameLength >> 1);
	}

	@Override
	public double[][] getSpectrogram(
			@Nonnull final int[] samples,
			final int start
	) {
		final double[][] spectrogram = new double[myLength][];

		// the buffer for real Fourier Transform
		final double[] re = new double[myFrameLength];

		for (int sgTime = 0; sgTime < myLength; sgTime++) {
			// copy samples into FFT buffer
			// System.arraycopy is inapplicable as the array types are different
			final int frameStart = start + sgTime * myStep;
			for (int i = 0; i < myFrameLength; ++i) {
				re[i] = samples[frameStart + i];
			}

			myFFT.transform(re);

			// compute amplitudes
			final int frameLengthDiv2 = myFrameLength >> 1;
			final double[] amplitudes = new double[frameLengthDiv2];
			for (int i = 0; i < amplitudes.length; i++) {
				final double reAmp = re[i];
				final double imAmp = re[frameLengthDiv2 + i];
				amplitudes[i] = Math.sqrt(reAmp * reAmp + imAmp * imAmp);
			}

			spectrogram[sgTime] = myFrequencySplitter.apply(amplitudes);
		}
		return spectrogram;
	}

	@Override
	public int getSpectrogramLengthInSamples() {
		return myStep * (myLength - 1) + myFrameLength;
	}

}
