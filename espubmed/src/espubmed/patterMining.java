package espubmed;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
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


public class patterMining {
	static InputStream is ;
	static TreeMap<Integer, String> hm;
	static POSModel model ;
	static POSTaggerME tagger;
	static ChunkerModel cModel ;
	static ChunkerME chunkerME;
	static PrintWriter wr;
	
	public static void init() throws InvalidFormatException, IOException{
		is = new FileInputStream("en-chunker.bin");
		hm = new TreeMap<>();
		model = new POSModelLoader()
			.load(new File("en-pos-maxent.bin"));
		tagger = new POSTaggerME(model);
		cModel = new ChunkerModel(is);
		chunkerME = new ChunkerME(cModel);
		wr = new PrintWriter(new File("final_out_with_sen.txt"));
	}
	
	public static void main(String[] args) throws Exception {
		init();
		// TODO Auto-generated method stub
		String diseaseFile = "disease2pubtator";
		BufferedReader br = new BufferedReader(new FileReader(diseaseFile));
		String line = br.readLine();
		line = br.readLine();
		int key = 0;
		int count = 0;
		String[] ph =null;	
		for(int i=0;i<25218994;i++){
			if(i%1000 ==0)
				System.out.println(i);
			hm.put(i, "");
		}
		String holder = "";
		while(line!=null){
			count ++;
			if(count%1000 ==0)
				System.out.println(count);
			
			ph = line.split("\\t");
			key = Integer.parseInt(ph[0]);
			StringBuilder stringBuilder = new StringBuilder();
			holder = hm.get(key);
			stringBuilder.append(holder);
			
			if(holder!="")
				stringBuilder.append(" @ ");
			
			stringBuilder.append( ph[2] );
			hm.put(key, stringBuilder.toString());
			line = br.readLine();
		}
		
		br.close();
		readLineParse();
		wr.close();
	}

	public static void readLineParse() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("/Volumes/backup/CSDATA/data/file_bh"));
		//BufferedReader br = new BufferedReader(new FileReader("hsinsanlin.txt"));

		// hsinsanlin.txt
		String line = br.readLine();
		int id = -1;
		int count = 0;
		
		
		while(line!=null){
			count ++;
			if(count%1000 ==0)
				System.out.println(count);
			if(line.length()<10 && isNumeric(line)){
				id = Integer.parseInt(line);
			}
			else{
				String[] sentences = line.replace(". ", "@").split("@");
				for (String sen : sentences){
					extractPattern(id,sen, hm.get(id));
				}
			}
			line = br.readLine();
			if(count==100000){
				break;
			}
		}
		br.close();
	}
	
	public static void extractPattern(int id, String sentence, String entities) throws IOException{
		String[] ph = entities.replace(" @ ", "@").split("@");
		
		int count = 0;
		HashMap<String, Integer> enti = new HashMap<>();
		StringBuilder stt = new StringBuilder();
		for(String temp: ph){
			if (sentence.contains(temp)){
				count++;
				stt.append(temp);
				stt.append("\t");
				enti.put(temp, 1);
			}
		}
		if(count>=2){
			wr.println(sentence);
			wr.println(stt.toString().trim());
			POSSample sample = null;
			
			ObjectStream<String> lineStream = new PlainTextByLineStream(
					new StringReader(sentence));
		 
			String line;
			String whitespaceTokenizerLine[] = null;
		 
			String[] tags = null;
			while ((line = lineStream.read()) != null) {
				whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
						.tokenize(line);
				tags = tagger.tag(whitespaceTokenizerLine);
		 
				sample = new POSSample(whitespaceTokenizerLine, tags);
			}
			String[] taggers = sample.toString().split(" ");
		
						 

			Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
			StringBuilder sb = new StringBuilder();
			String res= "";
			String chunk = "";
			for (Span s : span){
				String stemp = s.toString();
				chunk = combine(stemp, taggers);
				res = detectIt(chunk, enti);
				if(res.length()!=0){
					sb.append("["+res+"]");
				}
				else{
					sb.append(stemp.split(" ")[1]);
				}
				sb.append("\t");
			}
			wr.println(sb.toString().trim());
		}
		
	}
	public static String detectIt(String input, HashMap<String, Integer> hmp){
		for(String key:hmp.keySet()){
			if(input.contains(key)){
				return key;
			}
		}
		return "";
	}

	
	
	public static int[] parseLoc(String input){
        
        int[] ret = new int[2];
        String loc = input.split(" ")[0];
        String holder = loc.substring(1, loc.length()-1);

        ret[0] = Integer.parseInt(holder.split("\\.")[0]);
        ret[1] = Integer.parseInt(holder.split("\\.")[2]);
        
        return ret;
    }
	
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    int d = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	public static String combine(String seg, String[] res){
		int[] locs = parseLoc(seg);
		StringBuilder sb = new StringBuilder();
		for (int i = locs[0];i<locs[1];i++){
			sb.append(res[i].split("_")[0]);
			sb.append(" ");
		}
		return sb.toString().trim();
	}
	
	public static String extract(String input){
		return input.split("_")[0];
	}

}
