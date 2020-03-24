package simon.music.cdmanager;


public class Main {

	public static void main(String[] args) throws Exception {
		
		String docDir = "/Volumes/Music/古典音乐/";
		String indexDir = "/Users/simon/eclipse-workspace/data";
		
	//	IndexBuilder builder = new IndexBuilder(docDir, indexDir);
	//	builder.buildIndex();
		
		
		MusicSearcher searcher = new MusicSearcher(indexDir);
		
		searcher.search("Tchaikovsky");
		
		
		System.out.println("done!");
		

	}

}
