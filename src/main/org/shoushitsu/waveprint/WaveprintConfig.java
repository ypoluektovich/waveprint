package org.shoushitsu.waveprint;

import org.shoushitsu.waveprint.db.WaveprintParameter;
import org.shoushitsu.waveprint.db.WaveprintParameters;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class WaveprintConfig {
	private final Map<String, Object> myParams = new HashMap<>();

	public WaveprintConfig(final Properties props) {
		for (final String key : props.stringPropertyNames()) {
			final WaveprintParameter wp = WaveprintParameters.forStringKey(key);
			if (wp == null) {
				myParams.put(key, props.getProperty(key));
			} else {
				myParams.put(key, wp.valueFromString(props.getProperty(key)));
			}
		}
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return myParams.entrySet();
	}

	public int getSampleRate() {
		return (Integer) myParams.get(WaveprintParameters.SAMPLE_RATE.getStringKey());
	}

	public int getDbFingerprintStep() {
		return (Integer) myParams.get(WaveprintParameters.FINGERPRINT_STEP_DB.getStringKey());
	}

	public int getProbeFingerprintStep() {
		return (Integer) myParams.get(WaveprintParameters.FINGERPRINT_STEP_PROBE.getStringKey());
	}


	public int getSpectrogramLength() {
		return (Integer) myParams.get(WaveprintParameters.SPECTROGRAM_LENGTH.getStringKey());
	}

	public int getSpectrogramFrameLengthLog2() {
		return (Integer) myParams.get(WaveprintParameters.SPECTROGRAM_FRAME_LENGTH_L2.getStringKey());
	}

	public int getSpectrogramFrameStep() {
		return (Integer) myParams.get(WaveprintParameters.SPECTROGRAM_FRAME_STEP.getStringKey());
	}

	public int getSpectrogramWidth() {
		return (Integer) myParams.get(WaveprintParameters.SPECTROGRAM_WIDTH.getStringKey());
	}

	public int getSpectrogramLowestFrequency() {
		return (Integer) myParams.get(WaveprintParameters.SPECTROGRAM_FREQ_LOW.getStringKey());
	}

	public int getSpectrogramHighestFrequency() {
		return (Integer) myParams.get(WaveprintParameters.SPECTROGRAM_FREQ_HIGH.getStringKey());
	}


	public int getTopWavelets() {
		return (Integer) myParams.get(WaveprintParameters.TOP_WAVELET_COUNT.getStringKey());
	}

	public int getMinHashLength() {
		return (Integer) myParams.get(WaveprintParameters.MINHASH_FINGERPRINT_LENGTH.getStringKey());
	}

	public int getLshBinCount() {
		return (Integer) myParams.get(WaveprintParameters.LSH_BIN_COUNT.getStringKey());
	}
}
