package org.shoushitsu.waveprint.db;

import msyu.util.functional.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static msyu.util.functional.ConversionFunctions.integerFromString;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public enum WaveprintParameters implements WaveprintParameter {
	SAMPLE_RATE("sample-rate", integerFromString()),
	FINGERPRINT_STEP_DB("fingerprint.step.db", integerFromString()),
	FINGERPRINT_STEP_PROBE("fingerprint.step.probe", integerFromString()),
	SPECTROGRAM_LENGTH("spectrogram.length", integerFromString()),
	SPECTROGRAM_WIDTH("spectrogram.width", integerFromString()),
	SPECTROGRAM_FRAME_LENGTH_L2("spectrogram.frame.length-l2", integerFromString()),
	SPECTROGRAM_FRAME_STEP("spectrogram.frame.step", integerFromString()),
	SPECTROGRAM_FREQ_LOW("spectrogram.frequency.lowest", integerFromString()),
	SPECTROGRAM_FREQ_HIGH("spectrogram.frequency.highest", integerFromString()),
	TOP_WAVELET_COUNT("wavelets.top", integerFromString()),
	MINHASH_FINGERPRINT_LENGTH("minhash.length", integerFromString()),
	LSH_BIN_COUNT("lsh.bin.count", integerFromString()),
	LSH_VOTE_THRESHOLD("lsh.vote.threshold", integerFromString());

	private final String myStringKey;
	private final Function<String, ?> myValueFromString;

	WaveprintParameters(
			@Nonnull final String stringKey,
			@Nullable final Function<String, ?> valueFromString
	) {
		myStringKey = stringKey;
		myValueFromString = valueFromString;
	}

	@Override
	public String getStringKey() {
		return myStringKey;
	}

	@Override
	public Object valueFromString(@Nonnull final String string) {
		return (myValueFromString == null) ?
				string :
				myValueFromString.apply(string);
	}


	public static WaveprintParameter forStringKey(@Nonnull final String key) {
		for (final WaveprintParameters wp : WaveprintParameters.values()) {
			if (key.equals(wp.myStringKey)) {
				return wp;
			}
		}
		return null;
	}
}
