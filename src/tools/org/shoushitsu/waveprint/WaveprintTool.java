package org.shoushitsu.waveprint;

import msyu.util.collect.Pair;
import org.shoushitsu.waveprint.db.DataAccessException;
import org.shoushitsu.waveprint.db.NoDataException;
import org.shoushitsu.waveprint.db.hsqldb.FileBasedDatabase;
import org.shoushitsu.waveprint.exceptions.sampleextractor.SampleExtractionException;
import org.shoushitsu.waveprint.spectrogram.LogarithmicFrequencySplitter;
import org.shoushitsu.waveprint.spectrogram.SimpleFftSpectrogramBuilder;
import org.shoushitsu.waveprint.wavelet.HVStandardWaveletTransform;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class WaveprintTool {

	public static final String ADD = "add";
	private static final String ADD_USAGE = String.format(
			"\t%s <db location> <tree to add>...",
			ADD
	);

	public static final String FIND = "find";
	private static final String FIND_USAGE = String.format(
			"\t%s <db location> <file to search for>...",
			FIND
	);

	public static void main(final String[] args) throws IOException {
		if (args.length < 3) {
			System.out.println("Usage:");
			System.out.println(ADD_USAGE);
			System.out.println(FIND_USAGE);
		}

		final String dbLocation = args[1];
		switch (args[0]) {
			case ADD:
//				final ExecutorService executor = Executors.newFixedThreadPool(1);
				try (final FileBasedDatabase db = new FileBasedDatabase(dbLocation)) {
					for (int i = 2; i < args.length; i++) {
						final String root = args[i];
						Files.walkFileTree(
								Paths.get(root),
								new SimpleFileVisitor<Path>() {
									private final AtomicInteger counter = new AtomicInteger();
									@Override
									public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
//										executor.submit(new Runnable() {
//											@Override
//											public void run() {
												try {
													System.out.format("%4d Adding: %s\n", counter.incrementAndGet(), file);
													addToDatabase(db, file);
												} catch (DataAccessException | NoDataException | SampleExtractionException e) {
													e.printStackTrace();
												}
//											}
//										});
										return FileVisitResult.CONTINUE;
									}
								}
						);
					}
				} catch (NoDataException | DataAccessException e) {
					e.printStackTrace();
				} finally {
//					executor.shutdown();
				}
				break;
			case FIND:
				try (final FileBasedDatabase db = new FileBasedDatabase(dbLocation)) {
					for (int i = 2; i < args.length; i++) {
						final Path root = Paths.get(args[i]);
						Files.walkFileTree(
								root,
								new SimpleFileVisitor<Path>() {
									@Override
									public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
										if (!Files.isRegularFile(file)) {
											System.out.println("Not a file: " + file);
										} else {
											System.out.println("Searching for: " + file);
											try {
												System.out.println(find(db, file));
											} catch (SampleExtractionException e) {
												e.printStackTrace();
											} catch (DataAccessException | NoDataException e) {
												e.printStackTrace();
												return FileVisitResult.TERMINATE;
											}
										}
										return FileVisitResult.CONTINUE;
									}
								}
						);
					}
				} catch (NoDataException | DataAccessException e) {
					e.printStackTrace();
				}
				break;
			default:
				System.out.println("Unknown command: " + args[0]);
				break;
		}
	}

	public static Pair<Long, String> find(final FileBasedDatabase db, final Path file)
			throws DataAccessException, NoDataException, SampleExtractionException {
		final WaveprintConfig cfg = new WaveprintConfig(db.readSettings());
		final SampleExtractor sampleExtractor = getSampleExtractor(cfg);
		final Waveprint waveprint = getWaveprint(db, cfg);

		long startTime = System.currentTimeMillis();
		final int[] samples = sampleExtractor.extract(file, 60).getFirst();
		System.out.format("Extracted samples in %d ms%n", (System.currentTimeMillis() - startTime));

		startTime = System.currentTimeMillis();
		final List<Long> bestMatches = waveprint.findBestMatches(samples, db, 5);
		final Long trackId = (bestMatches.size() > 0) ? bestMatches.get(0) : null;
		System.out.format("Found matches in %d ms%n", System.currentTimeMillis() - startTime);

		return Pair.of(trackId, "(not queried yet)");
	}

	public static void addToDatabase(final FileBasedDatabase db, final Path file)
			throws DataAccessException, NoDataException, SampleExtractionException {
		final WaveprintConfig cfg = new WaveprintConfig(db.readSettings());
		final SampleExtractor sampleExtractor = getSampleExtractor(cfg);
		final Waveprint waveprint = getWaveprint(db, cfg);

		final int[] samples = sampleExtractor.extract(file).getFirst();
		final List<int[]> fingerprint = waveprint.fingerprint(samples);

		db.addTrack(file.toString(), fingerprint);
	}

	private static Waveprint getWaveprint(
			final FileBasedDatabase db,
			final WaveprintConfig cfg
	) throws DataAccessException, NoDataException {
		final List<Permutation> permutations = db.readPermutations();

		return new Waveprint(
				new SimpleFftSpectrogramBuilder(
						cfg.getSpectrogramLength(),
						cfg.getSpectrogramFrameLengthLog2(),
						cfg.getSpectrogramFrameStep(),
						new LogarithmicFrequencySplitter.Factory(
								cfg.getSampleRate(),
								cfg.getSpectrogramWidth(),
								cfg.getSpectrogramLowestFrequency(),
								cfg.getSpectrogramHighestFrequency()
						)
				),
				new HVStandardWaveletTransform(),
				new TopWaveletSelector(cfg.getTopWavelets()),
				new MinHasher(permutations),
				cfg.getDbFingerprintStep(),
				cfg.getProbeFingerprintStep()
		);
	}

	private static SampleExtractor getSampleExtractor(final WaveprintConfig cfg) {
		return new SampleExtractor(cfg.getSampleRate());
	}
}
