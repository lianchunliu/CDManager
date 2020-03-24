package simon.music.cdmanager;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class MusicSearcher {

	private String indexDir;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	
	
	public MusicSearcher(String indexDir) throws IOException {
		this.indexDir = indexDir;
		
		 IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
		  searcher = new IndexSearcher(reader);
		  analyzer = new StandardAnalyzer(StopWords.stopWords);
		 
		 
		 
	}

	public void search(String words) throws IOException, ParseException {

		// String field = "contents";
		String field = "pathText";

		QueryParser parser = new QueryParser(field, analyzer);

		Query query = parser.parse(words);

		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

		queryBuilder.add(query, Occur.SHOULD);

		field = "contents";

		parser = new QueryParser(field, analyzer);

		query = parser.parse(words);

		queryBuilder.add(query, Occur.SHOULD);

		BooleanQuery finalQuery = queryBuilder.build();

		System.out.println("Searching for: " + finalQuery.toString());

		TopDocs topDocs = searcher.search(finalQuery, 100);
		ScoreDoc[] hits = topDocs.scoreDocs;

		for (ScoreDoc hit : hits) {
			Document doc = searcher.doc(hit.doc);
			String path = doc.get("path");
			System.out.println("find doc: " + path);

		}

	}

}
