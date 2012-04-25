package org.shoushitsu.waveprint.wavelet;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public abstract class AbstractWaveletTransform implements WaveletTransform {
	protected void decomposeCol(final double[][] image, final int rowsLog2, final int col) {
		final int halfRows = 1 << (rowsLog2 - 1);
		final double[] details = new double[halfRows];
		for (int i = 0; i < halfRows; i++) {
			image[i][col] = (image[i * 2][col] + image[i * 2 + 1][col]) * 0.5;
			details[i] = image[i][col] - image[i * 2 + 1][col];
		}
		// can't use System.arraycopy here
		for (int i = 0; i < halfRows; i++) {
			image[halfRows + i][col] = details[i];
		}
	}

	protected void decomposeRow(final double[][] image, final int row, final int colsLog2) {
		final int halfCols = 1 << (colsLog2 - 1);
		final double[] details = new double[halfCols];
		for (int i = 0; i < halfCols; i++) {
			image[row][i] = (image[row][i * 2] + image[row][i * 2 + 1]) * 0.5;
			details[i] = image[row][i] - image[row][i * 2 + 1];
		}
		System.arraycopy(details, 0, image[row], halfCols, halfCols);
	}
}
