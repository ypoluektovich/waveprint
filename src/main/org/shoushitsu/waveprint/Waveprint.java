package org.shoushitsu.waveprint;

import msyu.util.collect.Pair;
import org.shoushitsu.waveprint.db.DataAccessException;
import org.shoushitsu.waveprint.db.NoDataException;
import org.shoushitsu.waveprint.db.WaveprintParameters;
import org.shoushitsu.waveprint.db.hsqldb.FileBasedDatabase;
import org.shoushitsu.waveprint.spectrogram.SpectrogramBuilder;
import org.shoushitsu.waveprint.wavelet.WaveletTransform;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class Waveprint {

	private final SpectrogramBuilder mySpectrogramBuilder;
	private final WaveletTransform myWaveletTransform;
	private final TopWaveletSelector myTopWaveletSelector;
	private final MinHasher myMinHasher;
	private final int myDbFingerprintStep;
	private final int myProbeFingerprintStep;

	public Waveprint(
			@Nonnull final SpectrogramBuilder spectrogramBuilder,
			@Nonnull final WaveletTransform waveletTransform,
			@Nonnull final TopWaveletSelector topWaveletSelector,
			@Nonnull final MinHasher minHasher,
			final int dbFingerprintStep,
			final int probeFingerprintStep
	) {
		mySpectrogramBuilder = spectrogramBuilder;
		myWaveletTransform = waveletTransform;
		myTopWaveletSelector = topWaveletSelector;
		myMinHasher = minHasher;
		myDbFingerprintStep = dbFingerprintStep;
		myProbeFingerprintStep = probeFingerprintStep;
	}

	public List<int[]> fingerprint(final int[] samples) {
		return fingerprint(samples, myDbFingerprintStep);
	}

	public List<Long> findBestMatches(
			final int[] samples,
			final FileBasedDatabase db,
			final int count
	) throws DataAccessException, NoDataException {
		long startTime = System.currentTimeMillis();
		final List<int[]> fingerprints =
				fingerprint(samples, myProbeFingerprintStep);
		long duration = System.currentTimeMillis() - startTime;
		System.out.printf(
				"Computed %d fingerprints in %d ms, avg %.3f%n",
				fingerprints.size(),
				duration,
				(float) duration / fingerprints.size()
		);

		startTime = System.currentTimeMillis();
		final List<Map<Long, Pair<Long, Integer>>> lshMatchesByFpIx =
				db.getLshMatches(fingerprints);
		duration = System.currentTimeMillis() - startTime;
		System.out.printf("Got LSH matches in %d ms", duration);

		startTime = System.currentTimeMillis();
		final Map<Long, Long> trackSimilarity = new HashMap<>();
		for (
				int i = 0, fingerprintsSize = fingerprints.size();
				i < fingerprintsSize;
				i++
		) {
			final Map<Long, Pair<Long, Integer>> lshMatches = lshMatchesByFpIx.get(i);
			for (final Pair<Long, Integer> trackAndSimilarity : lshMatches.values()) {
				final Long trackId = trackAndSimilarity.getFirst();
				final long newSimilarity;
				if (trackSimilarity.containsKey(trackId)) {
					newSimilarity = trackSimilarity.get(trackId) +
							trackAndSimilarity.getSecond();
				} else {
					newSimilarity = trackAndSimilarity.getSecond();
				}
				trackSimilarity.put(trackId, newSimilarity);
			}
		}
		duration = System.currentTimeMillis() - startTime;
		System.out.printf(", computed similarities in %d ms\n", duration);

		final List<Long> tracks = new ArrayList<>(trackSimilarity.keySet());
		Collections.sort(tracks, new Comparator<Long>() {
			@Override
			public int compare(final Long track1, final Long track2) {
				// reverse order (desc) on track similarity
				return trackSimilarity.get(track2)
						.compareTo(trackSimilarity.get(track1));
			}
		});

		final List<Long> bestTracks =
				tracks.subList(0, Math.min(tracks.size(), count));

		final int maxScore = fingerprints.size() *
				Integer.parseInt(db.readSetting(
						WaveprintParameters.MINHASH_FINGERPRINT_LENGTH
				));
		for (final Long track : bestTracks) {
			final int similarity = trackSimilarity.get(track).intValue();
			System.out.printf(
					"%d -> %d / %d (%.5f)\n",
					track, similarity, maxScore, (double) similarity / maxScore
			);
		}

		return bestTracks;
	}

	private List<int[]> fingerprint(final int[] samples, final int step) {
		final int maxStartPosition = samples.length -
				mySpectrogramBuilder.getSpectrogramLengthInSamples();
		final List<int[]> result = new ArrayList<>();
		for (int start = 0; start <= maxStartPosition; start += step) {
//			final long startTime = System.currentTimeMillis();
			final double[][] spectrogram =
					mySpectrogramBuilder.getSpectrogram(samples, start);
//			final long sgTime = System.currentTimeMillis();
			myWaveletTransform.transform(spectrogram, 7, 5);
//			final long waveletTime = System.currentTimeMillis();
			final int[] topWavelets = myTopWaveletSelector.apply(spectrogram);
//			final long topWaveletsTime = System.currentTimeMillis();
			final int[] hash = myMinHasher.hash(topWavelets);
//			final long minhashTime = System.currentTimeMillis();
//			System.out.printf(
//					"sg %d wt %d tw %d mh %d%n",
//					sgTime - startTime,
//					waveletTime - sgTime,
//					topWaveletsTime - waveletTime,
//					minhashTime - topWaveletsTime
//			);
			result.add(hash);
		}
		return result;
	}
}
