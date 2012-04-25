package msyu.util.functional;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class ConversionFunctions {
	private ConversionFunctions() {}
	
	public static Function<String, Integer> integerFromString() {
		return new Function<String, Integer>() {
			@Override
			public Integer apply(final String arg) {
				return Integer.valueOf(arg);
			}
		};
	}
}
