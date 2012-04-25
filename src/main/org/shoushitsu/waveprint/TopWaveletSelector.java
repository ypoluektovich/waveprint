package org.shoushitsu.waveprint;

import msyu.util.collect.IntArrayBuilder;
import msyu.util.collect.Pair;
import msyu.util.functional.Function;
import msyu.util.java.Comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class TopWaveletSelector implements Function<double[][], int[]> {

	public final int topWaveletCount;

	public TopWaveletSelector(final int topWaveletCount) {
		this.topWaveletCount = topWaveletCount;
	}

	@Override
	public int[] apply(final double[][] waveletDecomposition) {
		final int rowLength = waveletDecomposition[0].length;
		final List<Pair<Double, Integer>> waveletCoefficients =
				new ArrayList<>(waveletDecomposition.length * rowLength);
		for (int i = 0; i < waveletDecomposition.length; i++) {
			final double[] freqRow = waveletDecomposition[i];
			for (int j = 0; j < rowLength; j++) {
				final double coeff = freqRow[j];
				waveletCoefficients.add(Pair.of(Math.abs(coeff), i * rowLength + j));
			}
		}

		Collections.sort(
				waveletCoefficients,
				Pair.lexComparator(
                        Collections.reverseOrder(Comparators.naturalFor(Double.class)),
						Comparators.naturalFor(Integer.class)
				)
		);
		final IntArrayBuilder builder = new IntArrayBuilder(topWaveletCount);
		for (int i = 0; i < topWaveletCount; i++) {
			builder.append(waveletCoefficients.get(i).getSecond());
		}
		return builder.toIntArray();
	}
}
