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


public class entityMarker {
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
		wr = new PrintWriter(new File("fila_aa_out.txt"));
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
		//System.out.println(hm.get(18650484));
		readLineParse();
		wr.close();
	}

	public static void readLineParse() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("/Users/erichsu/Documents/research/file_aa"));
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
					findEntities(id,sen, hm.get(id));
				}
			}
			line = br.readLine();
		}
		br.close();
	}
	
	/*
	 * If there are two plus entities, then 
	 */
	public static void findEntities(int id, String sentence, String entities) throws IOException{
		String[] ph = entities.replace(" @ ", "@").split("@");
		int count =0;
		for(String temp: ph){
			if (sentence.contains(temp)){
				count++;
				if(count>=2)
					break;
					
			}
		}
		
		if (count>=2){
			for(int i=0;i<ph.length;i++){
				for(int j=i;j<ph.length;j++){
					if (i!=j){
						find_pair(id, sentence, ph[i], ph[j]);
					}
				}
			}
		}
	}
	
	public static void find_pair(int id, String sentence, String e1, String e2) throws IOException{
		@SuppressWarnings("deprecation")
		ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(sentence));
		String line;
		String whitespaceTokenizerLine[] = null;
		String[] tags = null;
		
		int i1=0;
		int i2=0;
		
		while ((line = lineStream.read()) != null) {
			whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
					.tokenize(line);
			tags = tagger.tag(whitespaceTokenizerLine);
		}
		
		for(int i=0;i<whitespaceTokenizerLine.length;i++){
            if(whitespaceTokenizerLine[i].toLowerCase().contains(e1.toLowerCase()))
                i1 = i;
            if(whitespaceTokenizerLine[i].toLowerCase().contains(e2.toLowerCase()))
                i2 = i; 
        }
		
		Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);

		for(Span s : span){
			String stemp = s.toString();
			if (stemp.contains(" VP")){
				int[] locs = parseLoc(stemp);
				if(i2>=locs[1]&&i1<=locs[0]){
					StringBuilder sb = new StringBuilder();
					sb.append(id);
					sb.append("\t");
					sb.append(e1);
					sb.append("\t");
					sb.append(e2);
					sb.append("\t");
					sb.append(makeVP(whitespaceTokenizerLine, locs));
					wr.println(sb.toString());
	            }
				else if (i2<=locs[0]&&i1<=locs[1]){
					StringBuilder sb = new StringBuilder();
					sb.append(id);
					sb.append("\t");
					sb.append(e1);
					sb.append("\t");
					sb.append(e2);
					sb.append("\t");
					sb.append(makeVP(whitespaceTokenizerLine, locs));
					wr.println(sb.toString());
	            }
			}
            
		}
		

	}
	
	public void init_dict(){

	}
	
	public static String makeVP(String[] strlist, int[] seg){
		StringBuilder sb = new StringBuilder();
		for(int i= seg[0];i<seg[1];i++){
			sb.append(strlist[i]);
			sb.append(" ");
		}
		String ret = sb.toString();
		return ret.trim();
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

}
