package org.shoushitsu.waveprint;

import msyu.util.collect.IntArrayBuilder;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class MinHasher {

	private final List<Permutation> permutations;

	public MinHasher(@Nonnull final List<Permutation> permutations) {
		this.permutations = permutations;
	}

	public int[] hash(@Nonnull final int[] bitPositions) {
		final IntArrayBuilder hashBuilder = new IntArrayBuilder(permutations.size());
		for (final Permutation permutation : permutations) {
			final int[] permutedBitPositions = permutation.apply(bitPositions);
			int min = permutedBitPositions[0];
			for (int i = 1; i < permutedBitPositions.length; i++) {
				final int position = permutedBitPositions[i];
				if (position < min) {
					min = position;
				}
			}
			hashBuilder.append(min);
		}
		return hashBuilder.toIntArray();
	}
}
