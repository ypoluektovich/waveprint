package org.shoushitsu.waveprint.spectrogram;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface FrequencySplitterFactory {
	FrequencySplitter newSplitter(final int amplitudeCount);
}
