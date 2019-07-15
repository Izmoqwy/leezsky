package lz.izmoqwy.core.api.database.exceptions;

public class SQLActionImpossibleException extends Exception {

	private static final long serialVersionUID = 5998422527673311910L;
	private ImpossibleExceptionType extype;
	
	public SQLActionImpossibleException(String message, ImpossibleExceptionType extype) {
		
		super(message);
		this.extype = extype;
		
	}
	
	public ImpossibleExceptionType getExType() {
		return this.extype;
	}
	
	public enum ImpossibleExceptionType {
		
		SQLERROR, KEYISNULL;
		
	}

}
