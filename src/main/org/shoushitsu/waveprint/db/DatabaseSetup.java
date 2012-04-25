package org.shoushitsu.waveprint.db;

import org.shoushitsu.waveprint.Permutation;
import org.shoushitsu.waveprint.WaveprintConfig;

import java.util.List;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface DatabaseSetup {
	void setUp(final WaveprintConfig config) throws DataAccessException;

	void storePermutations(final List<Permutation> permutations)
			throws DataAccessException;
}
