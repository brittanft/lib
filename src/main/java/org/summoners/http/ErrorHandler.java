package org.summoners.http;

/**
 * An error handler to handle any vital issues.
 * @author Brittan Thomas
 *
 * @param <T>
 *            the generic throwable type
 */
@FunctionalInterface
public interface ErrorHandler<T extends Throwable> {

	/**
	 * Handles the error which resulted in the specified throwable.
	 *
	 * @param throwable
	 *            the thrown error
	 * @return the amount of time to retry (-1 to indicate an exit)
	 */
	public int handle(T throwable);
}
