package cre

class Exceptions {

	public class FileTooLargeException extends Exception {
		
		int numberOfCRs;

		public FileTooLargeException(int numberOfCRs) {
			super();
			this.numberOfCRs = numberOfCRs;
		}
		
		
	}
	
	public class AbortedException extends Exception { }
	
	public class UnsupportedFileFormatException extends Exception { }
	
	
	
	
}
