package org.shoushitsu.waveprint;

import msyu.util.collect.IntArrayBuilder;
import msyu.util.collect.ShuffleIntArray;
import msyu.util.functional.Function;
import msyu.util.functional.SequenceAwareFunction;

import javax.annotation.Nonnull;

/**
 * For permuting "1" bits in a sparse array.
 * <p/>
 * A <tt>Permutation</tt> accepts an array of "1" bit positions and returns an
 * array of the same length with new (permuted) positions of these bits.
 *
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class Permutation implements Function<int[], int[]> {

	public static Permutation newRandom(final int length) {
		final IntArrayBuilder arrayBuilder = new IntArrayBuilder(length);
		for (int i = 0; i < length; ++i) {
			arrayBuilder.append(i);
		}
		final int[] ints = ShuffleIntArray.inPlace(arrayBuilder.toIntArray());
		return new Permutation(ints);
	}

	public static Permutation fromString(@Nonnull final String line) {
		final String[] strings = line.split(",");
		final IntArrayBuilder builder = new IntArrayBuilder(strings.length);
		for (final String string : strings) {
			builder.append(Integer.valueOf(string));
		}
		return new Permutation(builder.toIntArray());
	}

	public static Permutation fromIntArray(@Nonnull final int[] array) {
		return new Permutation(array);
	}

	private final int[] permutation;

	private Permutation(@Nonnull final int[] permutation) {
		if (permutation.length == 0) {
			throw new IllegalArgumentException("Permutations of zero length are not allowed");
		}
		this.permutation = permutation.clone();
	}

	@Override
	public int[] apply(final int[] arg) {
		final IntArrayBuilder builder = new IntArrayBuilder(arg.length);
		for (final int bitPos : arg) {
			builder.append(permutation[bitPos]);
		}
		return builder.toIntArray();
	}

	public void forEachElement(final SequenceAwareFunction<Integer, Void> function) {
		for (
				int i = 0, permutationLength = permutation.length;
				i < permutationLength;
				++i
		) {
			final int element = permutation[i];
			function.apply(element, i);
		}
	}

	public String dumpAsString() {
		final StringBuilder sb = new StringBuilder(String.valueOf(permutation[0]));
		for (int i = 1; i < permutation.length; i++) {
			sb.append(',').append(permutation[i]);
		}
		return sb.toString();
	}

	public static final Function<Permutation,String> DUMP_AS_STRING = new Function<Permutation, String>() {
		@Override
		public String apply(final Permutation arg) {
			return arg.dumpAsString();
		}
	};
}
