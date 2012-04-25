package msyu.util.collect;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class Pair<A, B> {
	protected final A first;
	protected final B second;

	public static <A, B> Pair<A, B> of(final A first, final B second) {
		return new Pair<>(first, second);
	}

	public Pair(final A first, final B second) {
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", first, second);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final Pair pair = (Pair) o;

		if (first != null ? !first.equals(pair.first) : pair.first != null)
			return false;
		if (second != null ? !second.equals(pair.second) : pair.second != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = first != null ? first.hashCode() : 0;
		result = 31 * result + (second != null ? second.hashCode() : 0);
		return result;
	}

	public static <A, B> Comparator<Pair<A, B>> lexComparator(
			@Nonnull final Comparator<A> first,
			@Nonnull final Comparator<B> second
	) {
		 return new Comparator<Pair<A, B>>() {
			 @Override
			 public int compare(final Pair<A, B> o1, final Pair<A, B> o2) {
				 final int c1 = first.compare(o1.getFirst(), o2.getFirst());
				 return (c1 != 0) ? c1 : second.compare(o1.getSecond(), o2.getSecond());
			 }
		 };
	}
}
