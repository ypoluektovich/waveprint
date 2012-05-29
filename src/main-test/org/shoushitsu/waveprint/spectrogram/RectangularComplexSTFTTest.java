package org.shoushitsu.waveprint.spectrogram;

import msyu.util.collect.Pair;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class RectangularComplexSTFTTest {

	@Test
	public void test1() {
		final double[] re = new double[]{1, 1, 1, 1, 0, 0, 0, 0};
		final double[] im = new double[]{0, 0, 0, 0, 0, 0, 0, 0};

		final ComplexSTFT stft = new RectangularComplexSTFT(2, 1, 3);
		final List<Pair<double[], double[]>> transform = stft.transform(re, im);

		final ComplexFFT fft = new SimpleComplexFFT(2);
		for (int i = 0; i < 3; ++i) {
			final double[] reFrame = Arrays.copyOfRange(re, 2 * i, 2 * i + 4);
			final double[] imFrame = Arrays.copyOfRange(im, 2 * i, 2 * i + 4);
			fft.transform(reFrame, imFrame);
			try {
				assertArrayEquals(reFrame, transform.get(i).getFirst(), 1e-8);
				assertArrayEquals(imFrame, transform.get(i).getSecond(), 1e-8);
			} catch (AssertionError e) {
				System.err.println("Context: i = " + i);
				System.err.println(
						"Expected Re " + Arrays.toString(reFrame)
				);
				System.err.println(
						"Expected Im " + Arrays.toString(imFrame)
				);
				System.err.println(
						"Got      Re " +
								Arrays.toString(transform.get(i).getFirst())
				);
				System.err.println(
						"Got      Im " +
								Arrays.toString(transform.get(i).getSecond())
				);
				throw e;
			}
		}
	}

}
