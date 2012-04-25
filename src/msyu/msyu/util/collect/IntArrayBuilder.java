package msyu.util.collect;

import java.util.Arrays;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class IntArrayBuilder {
	private int[] array;
	private int len;

	public IntArrayBuilder() {
		this(32);
	}

	public IntArrayBuilder(final int initialCapacity) {
		if (initialCapacity < 1) {
			throw new IllegalArgumentException("Initial capacity too small: " + initialCapacity);
		}
		array = new int[initialCapacity];
		len = 0;
	}

	public int[] toIntArray() {
		return Arrays.copyOf(array, len);
	}

	public int size() {
		return len;
	}

	public IntArrayBuilder append(final int n) {
		if (len == array.length) {
			array = Arrays.copyOf(array, array.length << 1);
		}
		array[len++] = n;
		return this;
	}

	public IntArrayBuilder clear() {
		Arrays.fill(array, 0);
		len = 0;
		return this;
	}
}
