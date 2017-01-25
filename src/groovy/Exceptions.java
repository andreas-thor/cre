package cre;

public class Exceptions {

	public static class FileTooLargeException extends Exception {
		
		private static final long serialVersionUID = 1L;

		int numberOfCRs;

		public FileTooLargeException(int numberOfCRs) {
			super();
			this.numberOfCRs = numberOfCRs;
		}
		
		
	}
	
	public static class AbortedException extends Exception { 
		
		private static final long serialVersionUID = 1L;

		public AbortedException () {
			super();
		}
		
	}
	
	public class UnsupportedFileFormatException extends Exception {

		private static final long serialVersionUID = 1L; 
		
	}
	
}
