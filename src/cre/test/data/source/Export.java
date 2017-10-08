package cre.test.data.source;

import java.io.IOException;

@FunctionalInterface
public interface Export {
   void apply(String file_name) throws IOException, RuntimeException;
}
