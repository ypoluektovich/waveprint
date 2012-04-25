/*V-
m	int	int
m	Int Int
d
m	int	byte
m	Int	Byte
f	msyu/util/misc/collect/Shuffle%Int%Array.java
*/
package msyu.util.collect;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utilities for shuffling contents of an array of type <code>int[]</code>. //V- s/int/%int%/
 *
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class ShuffleIntArray { //V- s/Int/%Int%/

	public static int[] inPlace(@Nonnull final int[] array) { //V- s/int/%int%/g
		return inPlace(array, ThreadLocalRandom.current());
	}

	public static int[] inPlace( //V- s/int/%int%/
			@Nonnull final int[] array, //V- s/int/%int%/
			@Nonnull final Random random
	) {
		for (int i = array.length; i > 1; i--) {
			swap(array, i - 1, random.nextInt(i));
		}
		return array;
	}

	private static void swap(@Nonnull final int[] arr, final int i, final int j) {  //V- s/int/%int%/
		final int tmp = arr[i]; //V- s/int/%int%/
		arr[i] = arr[j];
		arr[j] = tmp;
	}

}
