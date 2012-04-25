package msyu.util.java;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class Comparators {
	private Comparators() {}

	@Nonnull
	public static <C extends Comparable<C>> Comparator<C> naturalFor(@Nonnull final Class<C> clazz) {
		return new Comparator<C>() {
			@Override
			public int compare(final C o1, final C o2) {
				return o1.compareTo(o2);
			}
		};
	}

}
