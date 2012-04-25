package msyu.util.string;

import msyu.util.collect.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class StringUtils {
	private StringUtils() {}

	public static StringBuilder join(@Nullable final Collection<?> elements, @Nonnull final Object separator) {
		return join(elements, separator, new StringBuilder());
	}

	public static StringBuilder join(
			@Nullable final Collection<?> elements,
			@Nonnull final Object separator,
			final StringBuilder builder
	) {
		if (CollectionUtils.isEmpty(elements)) {
			return builder;
		}
		// elements != null here
		final Iterator<?> iterator = elements.iterator();
		builder.append(iterator.next());
		while (iterator.hasNext()) {
			builder.append(separator).append(iterator.next());
		}
		return builder;
	}
}
