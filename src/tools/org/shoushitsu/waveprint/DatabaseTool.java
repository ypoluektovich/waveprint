package org.shoushitsu.waveprint;

import msyu.util.collect.Pair;
import org.shoushitsu.waveprint.db.DataAccessException;
import org.shoushitsu.waveprint.db.DatabaseSource;
import org.shoushitsu.waveprint.db.NoDataException;
import org.shoushitsu.waveprint.db.WaveprintParameters;
import org.shoushitsu.waveprint.db.hsqldb.FileBasedDatabase;
import org.shoushitsu.waveprint.db.hsqldb.FileBasedDatabaseSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class DatabaseTool {

	public static final String DBSETUP = "dbsetup";
	private static final String DBSETUP_USAGE = String.format(
			"\t%s <database location> <property file location>",
			DBSETUP
	);

	public static void setup(final String path, final WaveprintConfig config) {
		try {
			new FileBasedDatabaseSetup(path).setUp(config);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}

	public static Pair<Integer, Integer> getMinhashParams(final String path) throws DataAccessException, NoDataException {
		try (final DatabaseSource ds = new FileBasedDatabase(path)) {
			final Integer minhashLength = Integer.valueOf(
					ds.readSetting(WaveprintParameters.MINHASH_FINGERPRINT_LENGTH)
			);
			final Integer spectrogramLength = Integer.valueOf(
					ds.readSetting(WaveprintParameters.SPECTROGRAM_LENGTH)
			);
			final Integer spectrogramWidth = Integer.valueOf(
					ds.readSetting(WaveprintParameters.SPECTROGRAM_WIDTH)
			);
			return Pair.of(minhashLength, spectrogramLength * spectrogramWidth);
		}
	}

	public static void storePermutations(
			final String path,
			final List<Permutation> permutations
	) {
		try {
			new FileBasedDatabaseSetup(path).storePermutations(permutations);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		if (args.length == 0) {
			System.out.println("Usage:");
			System.out.println(DBSETUP_USAGE);
			return;
		}
		switch (args[0]) {
			case DBSETUP:
				if (args.length != 3) {
					System.out.format(
							"Wrong number of arguments: expected 3, got %d%n" ,
							args.length
					);
					System.out.println("Usage:\n" + DBSETUP_USAGE);
				} else {
					final Properties props = new Properties();
					try {
						props.load(Files.newBufferedReader(
								Paths.get(args[2]),
								Charset.forName("UTF-8")
						));
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
					setup(args[1], new WaveprintConfig(props));
				}
				break;
			default:
				System.out.println("Unknown command: " + args[0]);
				break;
		}
	}
}
