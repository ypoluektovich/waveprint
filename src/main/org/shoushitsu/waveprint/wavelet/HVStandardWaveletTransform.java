package org.shoushitsu.waveprint.wavelet;

import javax.annotation.Nonnull;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class HVStandardWaveletTransform extends AbstractWaveletTransform {

	@Override
	public void transform(@Nonnull final double[][] image, int rowsLog2, int colsLog2) {
		while (colsLog2 > 0) {
			final int rows = 1 << rowsLog2;
			for (int row = 0; row < rows; row++) {
				decomposeRow(image, row, colsLog2);
			}
			colsLog2 -= 1;
		}
		while (rowsLog2 > 0) {
			final int cols = 1 << colsLog2;
			for (int col = 0; col < cols; col++) {
				decomposeCol(image, rowsLog2, col);
			}
			rowsLog2 -= 1;
		}
	}

}
