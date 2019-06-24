package test;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class streamtest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		Builder<String> streamBuilder = Stream.<String>builder(); 
		
		streamBuilder.
		
		
//		.add("a").add("b").add("c").build();
		
	}
	
	
	private static Builder<String> addElememnts (Builder<String> streamBuilder) {
		streamBuilder.add("a");
		streamBuilder.add("b");
		streamBuilder.add("c");
		return streamBuilder;
	}

}
