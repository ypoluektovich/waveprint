package org.shoushitsu.waveprint.spectrogram;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public abstract class AbstractComplexFFTTest {
	private static final double EPS = 1e-15;

	@Test
	public void flatLine() {
		final double[] re = {1, 1, 1, 1};
		final double[] im = {0, 0, 0, 0};
		getFftForFlatLine().transform(re, im);
		assertArrayEquals(new double[]{1, 0, 0, 0}, re, EPS);
		assertArrayEquals(new double[]{0, 0, 0, 0}, im, EPS);
	}

	protected abstract ComplexFFT getFftForFlatLine();

	@Test
	public void simplePeriod() {
		final double[] re = {1, 0, -1,  0};
		final double[] im = {0, 1,  0, -1};
		getFftForSimplePeriod().transform(re, im);
		assertArrayEquals(new double[]{0, 1, 0, 0}, re, EPS);
		assertArrayEquals(new double[]{0, 0, 0, 0}, im, EPS);
	}

	protected abstract ComplexFFT getFftForSimplePeriod();

	@Test
	public void shiftedSimplePeriod() {
		// same as simplePeriod, but lagging behind with a phase of -Pi/2
		final double[] re = { 0, 1, 0, -1};
		final double[] im = {-1, 0, 1,  0};
		getFftForShiftedSimplePeriod().transform(re, im);
		assertArrayEquals(new double[]{0,  0, 0, 0}, re, EPS);
		assertArrayEquals(new double[]{0, -1, 0, 0}, im, EPS);
	}

	protected abstract ComplexFFT getFftForShiftedSimplePeriod();

	@Test
	public void simplePlusShiftedSimple() {
		final double[] re = { 1, 1, -1, -1};
		final double[] im = {-1, 1,  1, -1};
		getFftForSimplePlusShiftedSimple().transform(re, im);
		assertArrayEquals(new double[]{0,  1, 0, 0}, re, EPS);
		assertArrayEquals(new double[]{0, -1, 0, 0}, im, EPS);
	}

	protected abstract ComplexFFT getFftForSimplePlusShiftedSimple();

	@Test
	public void test1000() {
		final double[] re = {1, 0, 0, 0};
		final double[] im = {0, 0, 0, 0};
		getFftFor1000().transform(re, im);
		assertArrayEquals(new double[]{0.25, 0.25, 0.25, 0.25}, re, EPS);
		assertArrayEquals(new double[]{0,    0,    0,    0   }, im, EPS);
	}

	protected abstract ComplexFFT getFftFor1000();

	@Test
	public void test10000000() {
		final double[] re = {1, 0, 0, 0, 0, 0, 0, 0};
		final double[] im = {0, 0, 0, 0, 0, 0, 0, 0};
		getFftFor10000000().transform(re, im);
		final double x = 0.125;
		assertArrayEquals(new double[]{x, x, x, x, x, x, x, x}, re, EPS);
		assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0}, im, EPS);
	}

	protected abstract ComplexFFT getFftFor10000000();
}
