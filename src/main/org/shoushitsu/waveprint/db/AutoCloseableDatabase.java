package org.shoushitsu.waveprint.db;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface AutoCloseableDatabase extends AutoCloseable {
	@Override
	void close() throws DataAccessException;
}
