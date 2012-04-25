package org.shoushitsu.waveprint.db;

import javax.annotation.Nonnull;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface WaveprintParameter {
	String getStringKey();
	Object valueFromString(@Nonnull final String string);
}
