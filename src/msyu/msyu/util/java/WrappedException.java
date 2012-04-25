package msyu.util.java;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class WrappedException extends RuntimeException {
	public WrappedException(final Throwable cause) {
		super(cause);
	}
}
