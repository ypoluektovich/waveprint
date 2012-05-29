package org.shoushitsu.waveprint.spectrogram;

import msyu.util.collect.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.copyOf;

/**
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

	@Override
	public List<Pair<double[], double[]>> transform(
			@Nonnull final double[] re,
			@Nonnull final double[] im
	) {
		checkArguments(re, im);

		final List<Pair<double[], double[]>> result =
				new ArrayList<>(myFrameCount);

		final double[] reX = copyOf(re, myFrameLength);
		final double[] imX = copyOf(im, myFrameLength);
		myInitialFft.transform(reX, imX);
		result.add(Pair.of(
				copyOf(reX, myFrameLength),
				copyOf(imX, myFrameLength)
		));

		final double[] reDiff = new double[myFrameLength];
		final double[] imDiff = new double[myFrameLength];

		final double[] reY = new double[myFrameLength];
		final double[] imY = new double[myFrameLength];

		for (int frame = 1; frame < myFrameCount; ++frame) {
			final int startX = (frame - 1) * myStep;

			for (int n = 0; n < myFrameLength; ++n) {
				reDiff[n] = re[startX + n] - re[star(startX, n)];
				imDiff[n] = im[startX + n] - im[star(startX, n)];
			}
			mySubsequentFft.transform(reDiff, imDiff);

			for (int n = 0; n < myFrameLength; ++n) {
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

			System.arraycopy(reY, 0, reX, 0, myFrameLength);
			System.arraycopy(imY, 0, imX, 0, myFrameLength);
		}
		return result;
	}

	private void checkArguments(final double[] re, final double[] im) {
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

	private int star(final int startX, final int n) {
		return (n < myStep) ? (startX + myFrameLength + n) : (startX + n);
	}
}
