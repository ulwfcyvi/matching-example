package org.ssi.exception;

public class UnfinishedStateException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2377763494469469763L;

	public UnfinishedStateException(String errorMessage) {
        super(errorMessage);
    }
}
