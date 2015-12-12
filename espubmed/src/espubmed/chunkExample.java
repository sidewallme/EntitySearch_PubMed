package espubmed;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.TreeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;


public class chunkExample {
	public static void main(String[] args)  throws Exception{
		chunk();
		System.out.println("y\\tc");
	}
	public static void chunk() throws IOException {
		POSModel model = new POSModelLoader()
				.load(new File("en-pos-maxent.bin"));
		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
		POSTaggerME tagger = new POSTaggerME(model);
	 
		//String input = "A significant positive correlation was seen between endogenous thrombin potential (ETP) (p = 0.012), time to peak (TTP) (p = 0.033) start tail (p = 0.007) and carotid IMT in the group of healthy volunteers younger than 45 years";
		String input = "At one day, i ate an apple";
		ObjectStream<String> lineStream = new PlainTextByLineStream(
				new StringReader(input));
	 
		perfMon.start();
		String line;
		String whitespaceTokenizerLine[] = null;
	 
		String[] tags = null;
		while ((line = lineStream.read()) != null) {
			whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
					.tokenize(line);
			tags = tagger.tag(whitespaceTokenizerLine);
	 
			POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
			System.out.println(sample.toString()+"----");
				perfMon.incrementCounter();
		}
		perfMon.stopAndPrintFinalResult();
	 
		// chunker
		InputStream is = new FileInputStream("en-chunker.bin");
		ChunkerModel cModel = new ChunkerModel(is);
	 
		ChunkerME chunkerME = new ChunkerME(cModel);
		String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);
	 
		for (String s : result)
			System.out.println(s);
	 
		Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
		for (Span s : span){
			String stemp = s.toString();
			System.out.println(stemp+"sda");
		}
			
	}
	public static int[] parseLoc(String input){
        
        int[] ret = new int[2];
        String loc = input.split(" ")[0];
        String holder = loc.substring(1, loc.length()-1);

        ret[0] = Integer.parseInt(holder.split("\\.")[0]);
        ret[1] = Integer.parseInt(holder.split("\\.")[2]);
        
        return ret;
    }
	
	
}
