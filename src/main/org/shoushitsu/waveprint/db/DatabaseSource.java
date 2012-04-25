package org.shoushitsu.waveprint.db;

import msyu.util.collect.Pair;
import org.shoushitsu.waveprint.Permutation;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface DatabaseSource extends AutoCloseableDatabase {
	String readSetting(final WaveprintParameter key) throws NoDataException, DataAccessException;

	Properties readSettings() throws DataAccessException;

	List<Permutation> readPermutations() throws DataAccessException, NoDataException;

	List<Map<Long, Pair<Long, Integer>>> getLshMatches(final List<int[]> probeFingerprints) throws DataAccessException, NoDataException;
}
