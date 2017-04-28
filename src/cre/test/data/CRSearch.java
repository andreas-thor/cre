package cre.test.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.RAMDirectory;

import cre.test.ui.CRTableView.CRColumn;

public class CRSearch {

	private static CRSearch crSearch = null;
	
	private RAMDirectory idx;
	
	public static CRSearch get() {
		if (crSearch==null) {
			crSearch = new CRSearch();
		}
		return crSearch;
	}
	
	
	private CRSearch() {
		init();
	}
	
	public void init() {
		idx = null;
	}
	
	public void search (String queryString) throws IOException, ParseException {
		
		if (idx == null) createIndex();
		
		DirectoryReader reader = DirectoryReader.open(idx);
		IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(idx));
		
		QueryParser parser = new QueryParser(CRColumn.CR.id, new StandardAnalyzer());
	    Query query = parser.parse(queryString);
		ScoreDoc[] docs = searcher.search(query, Integer.MAX_VALUE).scoreDocs;
		
		Map<Integer, Double> searchResult = Arrays.stream(docs).collect((Collectors.toMap(
				doc -> { try { return searcher.doc(doc.doc).getField(CRColumn.ID.id).numericValue().intValue(); } catch (Exception e) { return -1; }},
				doc -> (double)doc.score
				)));
		
		
		
		CRTable.get().getCR().forEach(cr -> {
			cr.setSEARCH_SCORE ( searchResult.containsKey(cr.getID()) ? searchResult.get(cr.getID()) : 0 );
		});
		
		
		
//		return Arrays.stream(docs).mapToInt((doc -> { 
//			try { return searcher.doc(doc.doc).getField(CRColumn.ID.id).numericValue().intValue(); } catch (Exception e) { return -1; }
//		})).toArray();
	}
	
	private void createIndex () throws IOException {
		
		 idx = new RAMDirectory();
		 
		 final IndexWriter writer = new IndexWriter(idx, new IndexWriterConfig(new StandardAnalyzer()));
		 
		 CRTable.get().getCR().forEach(cr -> {
			 
			 Document doc = new Document();
			 doc.add(new StoredField(CRColumn.ID.id, cr.getID()));
		     doc.add(new TextField(CRColumn.CR.id, cr.getCR(), Store.NO));
		     
			 try {
				writer.addDocument(doc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
		 });
		 
		 writer.close();
		 
	}
	
	
}
