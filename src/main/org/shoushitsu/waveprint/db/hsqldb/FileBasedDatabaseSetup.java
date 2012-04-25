package org.shoushitsu.waveprint.db.hsqldb;

import msyu.util.functional.SequenceAwareFunction;
import msyu.util.java.WrappedException;
import org.shoushitsu.waveprint.Permutation;
import org.shoushitsu.waveprint.WaveprintConfig;
import org.shoushitsu.waveprint.db.DataAccessException;
import org.shoushitsu.waveprint.db.DatabaseSetup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class FileBasedDatabaseSetup implements DatabaseSetup {

	private final String database;

	public FileBasedDatabaseSetup(final String database) {
		this.database = database;
	}
	
	@Override
	public void setUp(final WaveprintConfig config) throws DataAccessException {
		// todo: add parameter sanity checks
		// todo: add user-specified login and password
		try (final Connection connection = DriverManager.getConnection(
				"jdbc:hsqldb:file:" + database, "SA", ""
		)) {
			// enable transactions just to see if we can
			try {
				connection.setAutoCommit(false);
			} catch (SQLException e) {
				throw new DataAccessException("Failed to enable transactions", e);
			}

			// actual setup
			try (final Statement statement = connection.createStatement()) {
				setupInfo(config, connection, statement);
				setupTrack(statement);
				setupFingerprint(config.getMinHashLength(), statement);
				setupLshBins(config.getLshBinCount(), statement);
				setupPermutations(statement);
			} catch (SQLException e) {
				throw new DataAccessException("Failed to get a statement object", e);
			}

			try {
				connection.commit();
			} catch (SQLException e) {
				throw new DataAccessException("Failed to commit database set-up", e);
			}

			try (final Statement stmt = connection.createStatement()) {
				stmt.execute("shutdown");
			} catch (SQLException e) {
				throw new DataAccessException("Graceful shutdown failed", e);
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to open database connection", e);
		}
	}

	@Override
	public void storePermutations(final List<Permutation> permutations) throws DataAccessException {
		try (final Connection connection = DriverManager.getConnection(
				"jdbc:hsqldb:file:" + database, "SA", ""
		)) {
			// enable transactions just to see if we can
			try {
				connection.setAutoCommit(false);
			} catch (SQLException e) {
				throw new DataAccessException("Failed to enable transactions", e);
			}

			try (final PreparedStatement insert = connection.prepareStatement(
					"insert into permutation (num, index, value) values (?, ?, ?)"
			)) {
				for (
						int i = 0, permutationsSize = permutations.size();
						i < permutationsSize;
						++i
				) {
					final Permutation permutation = permutations.get(i);
					insert.setInt(1, i);
					permutation.forEachElement(new SequenceAwareFunction<Integer, Void>() {
						@Override
						public Void apply(final Integer element, final int index) {
							try {
								insert.setInt(2, index);
								insert.setInt(3, element);
								insert.addBatch();
							} catch (SQLException e) {
								throw new WrappedException(e);
							}
							return null;
						}
					});
				}
				insert.executeBatch();
			} catch (SQLException e) {
				throw new DataAccessException("Failed to store permutations", e);
			} catch (WrappedException e) {
				throw new DataAccessException(
						"Failed to store permutations",
						e.getCause()
				);
			}

			try {
				connection.commit();
			} catch (SQLException e) {
				throw new DataAccessException("Failed to commit database set-up", e);
			}

			try (final Statement stmt = connection.createStatement()) {
				stmt.execute("shutdown");
			} catch (SQLException e) {
				throw new DataAccessException("Graceful shutdown failed", e);
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to open database connection", e);
		}
	}

	private void setupPermutations(final Statement statement) throws DataAccessException {
		try {
			statement.execute(
					"create table permutation (" +
							"num integer not null, " +
							"index integer not null, " +
							"value integer not null" +
					")"
			);
		} catch (SQLException e) {
			throw new DataAccessException("Failed to setup permutations table", e);
		}
	}

	private void setupLshBins(
			final int lshBinCount,
			final Statement statement
	) throws DataAccessException {
		// todo: variable bin length
		// final int lshBinLength = minhashLength / lshBinCount;
		for (int bin = 0; bin < lshBinCount; bin++) {
			try {
				statement.execute(
						"create table lsh_bin_" + bin + " (" +
								"fingerprint_id bigint, " +
								"value bigint" +
						")"
				);
				statement.execute(
						"create index ix_lsh_bin_" + bin +
								" on lsh_bin_" + bin +
								" (value)"
				);
			} catch (SQLException e) {
				throw new DataAccessException("Failed to create LSH bin " + bin, e);
			}
		}
	}

	private void setupFingerprint(
			final int minhashLength,
			final Statement statement
	) throws DataAccessException {
		try {
			statement.execute(
					"create table fingerprint (" +
							"id bigint generated always as identity " +
								"(start with 1) primary key, " +
							"track_id bigint, " +
							"seq integer, " +
							"value varbinary(" + minhashLength + ")" +
					")"
			);
		} catch (SQLException e) {
			throw new DataAccessException("Failed to create fingerprint table", e);
		}
	}

	private void setupTrack(final Statement statement) throws DataAccessException {
		try {
			statement.execute(
					"create table track (" +
							"id bigint generated always as identity " +
								"(start with 1), " +
							"name varchar(1024 characters)" +
					")"
			);
		} catch (SQLException e) {
			throw new DataAccessException("Failed to create track table", e);
		}
	}

	private void setupInfo(
			final WaveprintConfig config,
			final Connection connection,
			final Statement statement
	) throws DataAccessException {
		try {
			// set up and populate info table
			statement.execute(
					"create table info (" +
							"key varchar(128 characters) primary key, " +
							"value varchar(1024 characters)" +
					")"
			);

			try (final PreparedStatement insertIntoInfo = connection.prepareStatement(
					"insert into info (key, value) values (?, ?)"
			)) {
				for (final Map.Entry<String, ?> entry : config.entrySet()) {
					insertIntoInfo.setString(1, entry.getKey());
					insertIntoInfo.setString(2, String.valueOf(entry.getValue()));
					insertIntoInfo.addBatch();
				}
				insertIntoInfo.executeBatch();
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to set up info table", e);
		}
	}
}
