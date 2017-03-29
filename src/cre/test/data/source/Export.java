package cre.test.data.source;

import java.io.File;
import java.io.IOException;

import cre.test.data.CRTable;

@FunctionalInterface
public interface Export {
   void apply(File file, CRTable crTab) throws IOException;
}
