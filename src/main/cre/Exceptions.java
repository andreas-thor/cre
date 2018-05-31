package main.cre;

public class Exceptions {

	public static class BadResponseCodeException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public int code;
		
		public BadResponseCodeException (int code) {
			this.code = code;
		}

	}
	
	public static class FileTooLargeException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public long numberOfCRs;
		public long numberOfPubs;

		public FileTooLargeException(long numberOfCRs, long numberOfPubs) {
			super();
			this.numberOfCRs = numberOfCRs;
			this.numberOfPubs = numberOfPubs;
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
