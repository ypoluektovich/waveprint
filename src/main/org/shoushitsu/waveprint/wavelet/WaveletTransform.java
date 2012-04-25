package org.shoushitsu.waveprint.wavelet;

import javax.annotation.Nonnull;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface WaveletTransform {
	void transform(@Nonnull double[][] image, int rowsLog2, int colsLog2);
}
