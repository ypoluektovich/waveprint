package org.shoushitsu.waveprint.spectrogram;

import msyu.util.functional.Function;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface FrequencySplitter extends Function<double[], double[]> {
	@Override
	double[] apply(final double[] amplitudes);
}
