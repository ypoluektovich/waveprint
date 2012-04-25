package org.shoushitsu.waveprint.db.hsqldb;

import msyu.util.collect.IntArrayBuilder;
import msyu.util.collect.Pair;
import msyu.util.string.StringUtils;
import org.shoushitsu.waveprint.Permutation;
import org.shoushitsu.waveprint.db.DataAccessException;
import org.shoushitsu.waveprint.db.DatabaseSink;
import org.shoushitsu.waveprint.db.DatabaseSource;
import org.shoushitsu.waveprint.db.NoDataException;
import org.shoushitsu.waveprint.db.WaveprintParameter;
import org.shoushitsu.waveprint.db.WaveprintParameters;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import static org.shoushitsu.waveprint.db.WaveprintParameters.LSH_VOTE_THRESHOLD;
import static org.shoushitsu.waveprint.db.WaveprintParameters.MINHASH_FINGERPRINT_LENGTH;
import static org.shoushitsu.waveprint.db.WaveprintParameters.SPECTROGRAM_LENGTH;
import static org.shoushitsu.waveprint.db.WaveprintParameters.SPECTROGRAM_WIDTH;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
// todo: better rollback exception handling
public class FileBasedDatabase implements DatabaseSource, DatabaseSink {

	private final Object myMonitor = new Object();

	private final Connection myConnection;

	private final PreparedStatement myReadInfoStmt;
	private final PreparedStatement myReadAllInfoStmt;

	private final PreparedStatement myReadPermutationsStmt;

	private final PreparedStatement myQueryFingerprintStmt;

	private final PreparedStatement myInsertTrackStmt;
	private final PreparedStatement myInsertFingerprintStmt;
	private final PreparedStatement[] myInsertLshBinStmts;

	public FileBasedDatabase(final String database) throws DataAccessException, NoDataException {
		try {
			// todo: user-specified login & password
			myConnection = DriverManager.getConnection(
					"jdbc:hsqldb:file:" + database,
					"SA",
					""
			);
			myConnection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DataAccessException("Failed to open database connection", e);
		}
		try {
			try {
				myReadInfoStmt = myConnection.prepareStatement("select value from info where key = ?");
				myReadAllInfoStmt = myConnection.prepareStatement("select key, value from info");

				myReadPermutationsStmt = myConnection.prepareStatement(
						"select num, index, value " +
								"from permutation " +
								"where num <= ? and index <= ? " +
								"order by num, index"
				);

				myInsertTrackStmt = myConnection.prepareStatement(
						"insert into track (id, name) values (default, ?)",
						new int[] {1}
				);

				myInsertFingerprintStmt = myConnection.prepareStatement(
						"insert into fingerprint (id, track_id, seq, value) " +
								"values (default, ?, ?, ?)",
						new int[] {1}
				);
				myQueryFingerprintStmt = myConnection.prepareStatement(
						"select value from fingerprint where id = ?"
				);

				final Integer lshBinCount = Integer.valueOf(
						readSetting(WaveprintParameters.LSH_BIN_COUNT)
				);
				myInsertLshBinStmts = new PreparedStatement[lshBinCount];
				for (int i = 0; i < lshBinCount; ++i) {
					myInsertLshBinStmts[i] = myConnection.prepareStatement(
							"insert into lsh_bin_" + i +
									" (fingerprint_id, value)" +
									" values (?, ?)"
					);
				}
			} catch (SQLException e) {
				throw new DataAccessException("Failed to prepare database access tools", e);
			}
		} catch (DataAccessException e) {
			try {
				myConnection.close();
			} catch (SQLException e1) {
				e.addSuppressed(e1);
			}
			throw e;
		}
	}

	@Override
	public String readSetting(final WaveprintParameter key) throws NoDataException, DataAccessException {
		synchronized (myMonitor) {
			try {
				myReadInfoStmt.setString(1, key.getStringKey());
				try (final ResultSet rs = myReadInfoStmt.executeQuery()) {
					if (rs.next()) {
						return rs.getString(1);
					} else {
						throw new NoDataException("No info record for key: " + key);
					}
				}
			} catch (SQLException e) {
				throw new DataAccessException("Failure while reading setting " + key, e);
			}
		}
	}

	@Override
	public Properties readSettings() throws DataAccessException {
		synchronized (myMonitor) {
			try (final ResultSet rs = myReadAllInfoStmt.executeQuery()) {
				final Properties props = new Properties();
				while (rs.next()) {
					props.setProperty(
							rs.getString("key"),
							rs.getString("value")
					);
				}
				return props;
			} catch (SQLException e) {
				throw new DataAccessException("Failure while reading settings", e);
			}
		}
	}

	@Override
	public List<Permutation> readPermutations() throws DataAccessException, NoDataException {
		synchronized (myMonitor) {
			final int count =
					Integer.valueOf(readSetting(MINHASH_FINGERPRINT_LENGTH));
			final Integer length =
					Integer.valueOf(readSetting(SPECTROGRAM_LENGTH));
			final Integer width =
					Integer.valueOf(readSetting(SPECTROGRAM_WIDTH));

			try {
				final List<Permutation> result = new ArrayList<>();
				final int permLength = length * width;
				final IntArrayBuilder iab = new IntArrayBuilder();
				myReadPermutationsStmt.setInt(1, count);
				myReadPermutationsStmt.setInt(2, permLength);
				try (final ResultSet rs = myReadPermutationsStmt.executeQuery()) {
					while (rs.next()) {
						iab.append(rs.getInt("value"));
						if (rs.getInt("index") == permLength - 1) {
							result.add(Permutation.fromIntArray(iab.toIntArray()));
							iab.clear();
						}
					}
				}
				// todo: verification of array and list sizes
				return result;
			} catch (SQLException e) {
				throw new DataAccessException(
						"Failure while reading MinHash permutation data", e);
			}
		}
	}

	@Override
	public List<Map<Long, Pair<Long, Integer>>> getLshMatches(final List<int[]> probeFingerprints)
			throws DataAccessException, NoDataException {
		synchronized (myMonitor) {
			final int binCount = myInsertLshBinStmts.length;
			final List<Map<Long, List<Integer>>> fpIxsByValueByBin = new ArrayList<>(binCount);
			final List<String> queries = new ArrayList<>(binCount);
			final int probeFpCount = probeFingerprints.size();
			for (int ixBin = 0; ixBin < binCount; ++ixBin) {
				final int lshValueStart = ixBin << 2;
				final Map<Long, List<Integer>> fpIxsByValue = new HashMap<>();
				final Set<Long> lshValues = new HashSet<>();
				for (int ixFp = 0; ixFp < probeFpCount; ++ixFp) {
					final int[] probeFp = probeFingerprints.get(ixFp);
					final long lshValue = getLshValue(probeFp, lshValueStart);
					if (!fpIxsByValue.containsKey(lshValue)) {
						fpIxsByValue.put(lshValue, new ArrayList<Integer>());
					}
					fpIxsByValue.get(lshValue).add(ixFp);
					lshValues.add(lshValue);
				}
				fpIxsByValueByBin.add(fpIxsByValue);

				final StringBuilder sb = new StringBuilder(
						"select lb.value, fp.id, fp.track_id " +
								"from lsh_bin_" + ixBin + " lb " +
								"join fingerprint fp " +
								"on lb.fingerprint_id = fp.id" +
								" where value in ("
				);
				sb.append(StringUtils.join(lshValues, ", "));
				sb.append(')');
				queries.add(sb.toString());
			}

			final List<Map<Long, Long>> votes = new ArrayList<>(probeFpCount);
			for (int i = 0; i < probeFpCount; ++i) {
				votes.add(new HashMap<Long, Long>());
			}
			final Map<Long, Long> trackIdByFpId = new HashMap<>();
			long startTime;
			startTime = System.currentTimeMillis();
			for (int ixBin = 0; ixBin < binCount; ixBin++) {
				final Map<Long, List<Integer>> fpIxsByValue =
						fpIxsByValueByBin.get(ixBin);
				try (final Statement queryStmt = myConnection.createStatement()) {
					try (final ResultSet rs =
								 queryStmt.executeQuery(queries.get(ixBin))) {
						while (rs.next()) {
							final long lshValue = rs.getLong("value");
							for (final Integer fpIx : fpIxsByValue.get(lshValue)) {
								final Map<Long, Long> fpVotes = votes.get(fpIx);
								final long fpId = rs.getLong("id");
								final long newVoteCount = 1 + (
										fpVotes.containsKey(fpId) ?
												fpVotes.get(fpId) :
												0
								);
								fpVotes.put(fpId, newVoteCount);

								if (!trackIdByFpId.containsKey(fpId)) {
									trackIdByFpId.put(fpId, rs.getLong("track_id"));
								}
							}
						}
					}
				} catch (SQLException e) {
					throw new DataAccessException(
							"Error while querying LSH bin " + ixBin,
							e
					);
				}
			}
			System.out.printf("Queried bins in %d ms\n", System.currentTimeMillis() - startTime);

			final Integer voteThreshold =
					Integer.valueOf(readSetting(LSH_VOTE_THRESHOLD));
			final List<Map<Long, Pair<Long, Integer>>> result = new ArrayList<>();
			for (int fpIx = 0; fpIx < probeFpCount; fpIx++) {
				final int[] probeFingerprint = probeFingerprints.get(fpIx);
				final Map<Long, Long> fpVotes = votes.get(fpIx);
				final Collection<Long> fpIds = new HashSet<>();
				for (final Entry<Long, Long> vote : fpVotes.entrySet()) {
					if (vote.getValue() >= voteThreshold) {
						fpIds.add(vote.getKey());
					}
				}
				final Map<Long, Pair<Long, Integer>> fpResult = new HashMap<>();
				for (final Long fpId : fpIds) {
					int fpSimilarity = 0;
					try {
						myQueryFingerprintStmt.setLong(1, fpId);
						fpSimilarity = 0;
						try (final ResultSet rs =
									 myQueryFingerprintStmt.executeQuery()) {
							rs.next();
							final InputStream is = rs.getBinaryStream("value");
							for (final int probeElement : probeFingerprint) {
								final int dbElement = is.read();
								if (dbElement == (probeElement > 255 ? 255 : probeElement)) {
									++fpSimilarity;
								}
							}
						}
					} catch (SQLException | IOException e) {
						throw new DataAccessException(
								"Error while querying fingerprint " + fpId,
								e
						);
					}
					fpResult.put(fpId, Pair.of(trackIdByFpId.get(fpId), fpSimilarity));
				}
				result.add(fpResult);
			}
			return result;
		}
	}

	@Override
	public void addTrack(
			@Nonnull final String trackName,
			@Nonnull final List<int[]> fingerprints
	) throws DataAccessException {
		synchronized (myMonitor) {
			final long trackId = insertTrackRecord(trackName);

			// batch-insert fingerprints
			try {
				myInsertFingerprintStmt.setLong(1, trackId);
				for (int i = 0, count = fingerprints.size(); i < count; i++) {
					final int[] fingerprint = fingerprints.get(i);
					myInsertFingerprintStmt.setInt(2, i);
					myInsertFingerprintStmt.setBinaryStream(
							3,
							new FingerprintInputStream(fingerprint)
					);
					myInsertFingerprintStmt.addBatch();
				}
				myInsertFingerprintStmt.executeBatch();
				try (final ResultSet rs =
							 myInsertFingerprintStmt.getGeneratedKeys()) {
					int i = 0;
					while (rs.next()) {
						final int[] fingerprint = fingerprints.get(i);
						final long fingerprintId = rs.getLong("id");
						for (int j = 0; j < myInsertLshBinStmts.length; ++j) {
							final long value = getLshValue(fingerprint, j << 2);
							final PreparedStatement stmt = myInsertLshBinStmts[j];
							stmt.setLong(1, fingerprintId);
							stmt.setLong(2, value);
							stmt.addBatch();
						}
						++i;
					}
				}
				for (final PreparedStatement stmt : myInsertLshBinStmts) {
					stmt.executeBatch();
				}
				myConnection.commit();
			} catch (SQLException e) {
				try {
					myConnection.rollback();
				} catch (SQLException e1) {
					e.addSuppressed(e1);
				}
				throw new DataAccessException("Error while adding fingerprints", e);
			}
		}
	}

	private long getLshValue(final int[] fingerprint, final int lshValueStart) {
		return (fingerprint[lshValueStart] << 24) +
				(fingerprint[lshValueStart + 1] << 16) +
				(fingerprint[lshValueStart + 2] << 8) +
				(fingerprint[lshValueStart + 3]);
	}

	private long insertTrackRecord(@Nonnull final String trackName) throws DataAccessException {
		try {
			myInsertTrackStmt.setString(1, trackName);
			myInsertTrackStmt.executeUpdate();
			final ResultSet generatedKeys = myInsertTrackStmt.getGeneratedKeys();
			generatedKeys.next();
			return generatedKeys.getLong(1);
		} catch (SQLException e) {
			try {
				myConnection.rollback();
			} catch (SQLException e1) {
				e.addSuppressed(e1);
			}
			throw new DataAccessException("Error while adding new track record (named " + trackName + ")", e);
		}
	}

	@Override
	public void close() throws DataAccessException {
		try (final Statement stmt = myConnection.createStatement()) {
			stmt.execute("shutdown");
			myConnection.close();
		} catch (SQLException e) {
			throw new DataAccessException("Graceful shutdown failed", e);
		}
	}

	private static class FingerprintInputStream extends InputStream {

		private final int[] fingerprint;
		private int pointer;

		private FingerprintInputStream(final int[] fingerprint) {
			this.fingerprint = fingerprint;
		}

		@Override
		public int read() throws IOException {
			if (pointer == fingerprint.length) {
				return -1;
			}
			final int value = fingerprint[pointer];
			pointer += 1;
			return (value > 255) ? 255 : value;
		}
	}
}
