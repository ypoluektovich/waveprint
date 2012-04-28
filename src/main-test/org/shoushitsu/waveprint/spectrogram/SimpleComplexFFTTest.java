package org.shoushitsu.waveprint.spectrogram;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class SimpleComplexFFTTest extends AbstractComplexFFTTest {

	@Override
	protected ComplexFFT getFftForFlatLine() {
		return new SimpleComplexFFT(2);
	}

	@Override
	protected ComplexFFT getFftForSimplePeriod() {
		return new SimpleComplexFFT(2);
	}

	@Override
	protected ComplexFFT getFftForShiftedSimplePeriod() {
		return new SimpleComplexFFT(2);
	}

	@Override
	protected ComplexFFT getFftForSimplePlusShiftedSimple() {
		return new SimpleComplexFFT(2);
	}

	@Override
	protected ComplexFFT getFftFor1000() {
		return new SimpleComplexFFT(2);
	}

	@Override
	protected ComplexFFT getFftFor10000000() {
		return new SimpleComplexFFT(3);
	}
}
