package simon.music.cdmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexBuilder {

	private String indexDir;
	private String docDir;
	
	public IndexBuilder(String docDir, String indexDir) {
		this.indexDir = indexDir;
		this.docDir = docDir;
	}

	public void buildIndex() {
		
		try {
			final Path docPath = Paths.get(this.docDir);
			Directory dir = FSDirectory.open(Paths.get(this.indexDir));
			Analyzer analyzer = new StandardAnalyzer(StopWords.stopWords);
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			
			IndexWriter writer = new IndexWriter(dir, iwc);
			
			indexDocs(writer, docPath);
			
			
			writer.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	static void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}

	
	static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {

		
		System.out.println("index File: " + file.toString());

		InputStream stream = null;
		String lowName = file.toString().toLowerCase();
		if (lowName.endsWith(".cue")
				|| lowName.endsWith(".txt")
				|| lowName.endsWith(".log")) {
			stream = Files.newInputStream(file);
			
		}


		
		// make a new, empty document
		Document doc = new Document();

		// Add the path of the file as a field named "path". Use a
		// field that is indexed (i.e. searchable), but don't tokenize
		// the field into separate words and don't index term frequency
		// or positional information:
		Field pathField = new StringField("path", file.toString(), Field.Store.YES);
		doc.add(pathField);

		// Add the last modified date of the file a field named "modified".
		// Use a LongPoint that is indexed (i.e. efficiently filterable with
		// PointRangeQuery). This indexes to milli-second resolution, which
		// is often too fine. You could instead create a number based on
		// year/month/day/hour/minutes/seconds, down the resolution you require.
		// For example the long value 2011021714 would mean
		// February 17, 2011, 2-3 PM.
		doc.add(new LongPoint("modified", lastModified));

		if (stream != null)
			doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
		doc.add(new TextField("pathText", new BufferedReader(new StringReader(file.toString()))));

		if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
			// New index, so we just add the document (no old document can be there):
			System.out.println("adding " + file);
			writer.addDocument(doc);
		} else {
			// Existing index (an old copy of this document may have been indexed) so
			// we use updateDocument instead to replace the old one matching the exact
			// path, if present:
			System.out.println("updating " + file);
			writer.updateDocument(new Term("path", file.toString()), doc);
		}

	
	}
	
	

}
