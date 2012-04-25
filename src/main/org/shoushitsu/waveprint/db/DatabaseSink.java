package org.shoushitsu.waveprint.db;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface DatabaseSink extends AutoCloseableDatabase {
	void addTrack(
			@Nonnull final String trackName,
			@Nonnull final List<int[]> fingerprints
	) throws DataAccessException;
}
