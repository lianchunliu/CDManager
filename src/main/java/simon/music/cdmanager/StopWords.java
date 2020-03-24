package simon.music.cdmanager;

import org.apache.lucene.analysis.CharArraySet;

public class StopWords {

	public final static CharArraySet stopWords;
	
	static {
		
		stopWords = new CharArraySet(10, true);
		stopWords.add(new char[] {'/', ',', '.', '-'});
		
	}

	
}
