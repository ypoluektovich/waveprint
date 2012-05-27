package org.shoushitsu.waveprint.wavelet;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public abstract class WaveletTransformTest {

	protected final WaveletTransform waveletTransform;

	protected WaveletTransformTest(final WaveletTransform waveletTransform) {
		this.waveletTransform = waveletTransform;
	}

	@Test
	public void testRow() {
		final double[][] image = {{9.0, 7.0, 3.0, 5.0}};
		waveletTransform.transform(image, 0, 2);
		final double[][] expected = {{6.0, 2.0, 1.0, -1.0}};
		Assert.assertArrayEquals(expected, image);
	}

	@Test
	public void testColumn() {
		final double[][] image = {{9.0}, {7.0}, {3.0}, {5.0}};
		waveletTransform.transform(image, 2, 0);
		final double[][] expected = {{6.0}, {2.0}, {1.0}, {-1.0}};
		Assert.assertArrayEquals(expected, image);
	}
}
