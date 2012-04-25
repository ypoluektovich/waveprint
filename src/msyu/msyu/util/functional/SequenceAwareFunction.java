package msyu.util.functional;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public interface SequenceAwareFunction<A, V> {
	V apply(A element, int index);
}
