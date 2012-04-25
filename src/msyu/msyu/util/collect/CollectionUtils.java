package msyu.util.collect;

import msyu.util.functional.Function;
import msyu.util.functional.Predicate;
import msyu.util.functional.SequenceAwareFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class CollectionUtils {
	private CollectionUtils() {}

	public static boolean isEmpty(@Nullable final Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static <T> int indexOf(
			@Nonnull final Iterable<T> iterable,
			@Nonnull final Predicate<? super T> predicate
	) {
		int i = -1;
		for (final T element : iterable) {
			i++;
			if (predicate.apply(element)) {
				return i;
			}
		}
		return -1;
	}

	public static <A, V> List<V> map(
			@Nonnull final Collection<? extends A> sources,
			@Nonnull final Function<A, V> function
	) {
		final List<V> list = new ArrayList<>(sources.size());
		for (final A arg : sources) {
			list.add(function.apply(arg));
		}
		return list;
	}

	public static <A, V> List<V> map(
			@Nonnull final Iterable<? extends A> sources,
			@Nonnull final SequenceAwareFunction<A, V> function
	) {
		final List<V> list = new ArrayList<>();
		int index = 0;
		for (final A arg : sources) {
			list.add(function.apply(arg, index));
			index++;
		}
		return list;
	}

	public static <A, V> List<V> map(
			final A argument,
			@Nonnull final Collection<? extends Function<A, ? extends V>> functions
	) {
		final List<V> results = new ArrayList<>(functions.size());
		for (final Function<A, ? extends V> function : functions) {
			results.add(function.apply(argument));
		}
		return results;
	}
}
