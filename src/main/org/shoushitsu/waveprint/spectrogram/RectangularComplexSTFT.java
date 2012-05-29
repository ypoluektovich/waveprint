package org.shoushitsu.waveprint.spectrogram;

import msyu.util.collect.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.copyOf;

/**
 * An implementation of STFT that uses (and is optimized for) rectangular
 * window function.
 * <p/>
 * TODO: add description of the optimization
 *
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class RectangularComplexSTFT implements ComplexSTFT {

	private final int myFrameLength;

	private final int myStep;

	private final int myFrameCount;

	private final double[] myReStarToY;

	private final double[] myImStarToY;

	private final ComplexFFT myInitialFft;

	private final ComplexFFT mySubsequentFft;

	public RectangularComplexSTFT(
			final int frameLengthLog2,
			final int stepLog2,
			final int frameCount
	) {
		myFrameLength = 1 << frameLengthLog2;
		myStep = 1 << stepLog2;
		myFrameCount = frameCount;

		myReStarToY = new double[myFrameLength];
		myImStarToY = new double[myFrameLength];
		precomputeStarToY();

		myInitialFft = new SimpleComplexFFT(frameLengthLog2);
		mySubsequentFft = new ZeroTailComplexFFT(frameLengthLog2, stepLog2);
	}

	private void precomputeStarToY() {
		final double twoPiMOverN = 2.0 * Math.PI / myFrameLength * myStep;
		for (int k = 0; k < myFrameLength; ++k) {
			myReStarToY[k] = Math.cos(twoPiMOverN * k);
			myImStarToY[k] = Math.sin(twoPiMOverN * k);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Pair<double[], double[]>> transform(
			@Nonnull final double[] re,
			@Nonnull final double[] im
	) throws IllegalArgumentException {
		checkArguments(re, im);

		final List<Pair<double[], double[]>> result =
				new ArrayList<>(myFrameCount);

		// the first FFT must be computed without any tricks
		final double[] reX = copyOf(re, myFrameLength);
		final double[] imX = copyOf(im, myFrameLength);
		myInitialFft.transform(reX, imX);
		result.add(Pair.of(
				copyOf(reX, myFrameLength),
				copyOf(imX, myFrameLength)
		));

		// this is for (x - y*) waveform and its Fourier Transform
		final double[] reDiff = new double[myFrameLength];
		final double[] imDiff = new double[myFrameLength];

		// *X contain previous frame's results, these are for current frame
		final double[] reY = new double[myFrameLength];
		final double[] imY = new double[myFrameLength];

		for (int frame = 1; frame < myFrameCount; ++frame) {
			// where the previous frame started
			final int startX = (frame - 1) * myStep;

			// fill in and transform (x - y*)
			for (int n = 0; n < myFrameLength; ++n) {
				reDiff[n] = re[startX + n] - re[star(startX, n)];
				imDiff[n] = im[startX + n] - im[star(startX, n)];
			}
			mySubsequentFft.transform(reDiff, imDiff);

			// F(x - y*) --> F(y)
			for (int n = 0; n < myFrameLength; ++n) {
				// F(y*)
				final double reFYStar = reX[n] - reDiff[n];
				final double imFYStar = imX[n] - imDiff[n];

				// F(y[n]) = F(y*[n]) * starToY[n]
				final double reStarToY = myReStarToY[n];
				final double imStarToY = myImStarToY[n];
				reY[n] = reFYStar * reStarToY - imFYStar * imStarToY;
				imY[n] = imFYStar * reStarToY + reFYStar * imStarToY;
			}

			result.add(Pair.of(
					copyOf(reY, myFrameLength),
					copyOf(imY, myFrameLength)
			));

			// save current results as previous
			System.arraycopy(reY, 0, reX, 0, myFrameLength);
			System.arraycopy(imY, 0, imX, 0, myFrameLength);
		}

		return result;
	}

	/**
	 * Checks {@link #transform(double[], double[])} arguments for validity.
	 * The only check performed is that of the arrays' length.
	 *
	 * @param re    the array with the waveform's real parts.
	 * @param im    the array with the waveform's imaginary parts.
	 *
	 * @throws IllegalArgumentException if one of the arguments has
	 * insufficient length for the STFT computation to work.
	 */
	private void checkArguments(final double[] re, final double[] im)
			throws IllegalArgumentException {
		final int requiredLength = myFrameLength + myStep * (myFrameCount - 1);
		if (re.length < requiredLength) {
			throw new IllegalArgumentException(String.format(
					"Real parts array is too short " +
							"(required at least %d" +
							" for frame length %d, step %d, frame count %d; " +
							"actual %d)",
					requiredLength,
					myFrameLength, myStep, myFrameCount,
					re.length
			));
		}
		if (im.length < requiredLength) {
			throw new IllegalArgumentException(String.format(
					"Imaginary parts array is too short " +
							"(required at least %d" +
							" for frame length %d, step %d, frame count %d; " +
							"actual %d)",
					requiredLength,
					myFrameLength, myStep, myFrameCount,
					im.length
			));
		}
	}

	/**
	 * Get the index of y*[n] in the source array(s).
	 *
	 * @param startX    start of the previous frame in the source arrays.
	 * @param n         index of element in current y* frame.
	 *
	 * @return the index of y*[n] = y[n-m] in the source arrays.
	 */
	private int star(final int startX, final int n) {
		return (n < myStep) ? (startX + myFrameLength + n) : (startX + n);
	}
}
