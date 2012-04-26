package org.shoushitsu.waveprint.spectrogram;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class ZeroTailComplexFFTTest {

	private static final double EPS = 1e-15;

	@Test
	public void flatLine() {
		final double[] re = {1, 1, 1, 1};
		final double[] im = {0, 0, 0, 0};
		new ZeroTailComplexFFT(2, 2).transform(re, im);
		assertArrayEquals(new double[]{1, 0, 0, 0}, re, EPS);
		assertArrayEquals(new double[]{0, 0, 0, 0}, im, EPS);
	}

	@Test
	public void simplePeriod() {
		final double[] re = {1, 0, -1,  0};
		final double[] im = {0, 1,  0, -1};
		new ZeroTailComplexFFT(2, 2).transform(re, im);
		assertArrayEquals(new double[]{0, 1, 0, 0}, re, EPS);
		assertArrayEquals(new double[]{0, 0, 0, 0}, im, EPS);
	}

	@Test
	public void shiftedSimplePeriod() {
		// same as simplePeriod, but lagging behind with a phase of -i
		final double[] re = { 0, 1, 0, -1};
		final double[] im = {-1, 0, 1,  0};
		new ZeroTailComplexFFT(2, 2).transform(re, im);
		assertArrayEquals(new double[]{0,  0, 0, 0}, re, EPS);
		assertArrayEquals(new double[]{0, -1, 0, 0}, im, EPS);
	}

	@Test
	public void simplePlusShiftedSimple() {
		final double[] re = { 1, 1, -1, -1};
		final double[] im = {-1, 1,  1, -1};
		new ZeroTailComplexFFT(2, 2).transform(re, im);
		assertArrayEquals(new double[]{0,  1, 0, 0}, re, EPS);
		assertArrayEquals(new double[]{0, -1, 0, 0}, im, EPS);
	}

	@Test
	public void test1000() {
		final double[] re = {1, 0, 0, 0};
		final double[] im = {0, 0, 0, 0};
		new ZeroTailComplexFFT(2, 0).transform(re, im);
		assertArrayEquals(new double[]{0.25, 0.25, 0.25, 0.25}, re, EPS);
		assertArrayEquals(new double[]{0,    0,    0,    0   }, im, EPS);
	}

	@Test
	public void test10000000() {
		final double[] re = {1, 0, 0, 0, 0, 0, 0, 0};
		final double[] im = {0, 0, 0, 0, 0, 0, 0, 0};
		new ZeroTailComplexFFT(3, 0).transform(re, im);
		final double x = 0.125;
		assertArrayEquals(new double[]{x, x, x, x, x, x, x, x}, re, EPS);
		assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0}, im, EPS);
	}
}
