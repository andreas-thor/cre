package cre.test.data.source;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import cre.test.data.type.PubType;

public interface ImportReader extends Iterator<PubType> {

	public void init (List<File> files) throws IOException;
	
	public default Iterable<PubType> getIterable () { 
		return () -> this;
	}
	
}
