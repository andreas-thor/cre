package cre

class Exceptions {

	static class FileTooLargeException extends Exception {
		
		int numberOfCRs

		public FileTooLargeException(int numberOfCRs) {
			super()
			this.numberOfCRs = numberOfCRs
		}
		
		
	}
	
	static class AbortedException extends Exception { 
		
		public AbortedException () {
			super();
		}
		
	}
	
	public class UnsupportedFileFormatException extends Exception { }
	
	
	
	
}
