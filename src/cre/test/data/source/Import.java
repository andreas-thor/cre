package cre.test.data.source;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRTable;

@FunctionalInterface
public interface Import {
   void apply(List<File> files, CRTable crTab, int maxCR, int maxPub, int[] yearRange, boolean random) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError, IOException;
}
