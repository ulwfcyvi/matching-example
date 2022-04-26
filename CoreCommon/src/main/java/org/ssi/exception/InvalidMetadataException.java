package org.ssi.exception;

public class InvalidMetadataException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2377763494469469763L;

	public InvalidMetadataException(String errorMessage) {
        super(errorMessage);
    }
}
