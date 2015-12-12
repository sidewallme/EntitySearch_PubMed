package espubmed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

public class mining {
	static String disease = "/Users/erichsu/Documents/research/espubmed/disease2pubtator";
	static String gene = "/Users/erichsu/Documents/research/espubmed/gene2pubtator";
	static String mutation = "/Users/erichsu/Documents/research/espubmed/mutation2pubtator";
	static String species = "/Users/erichsu/Documents/research/espubmed/species2pubtator";
	static String chemical = "/Users/erichsu/Documents/research/espubmed/chemical2pubtator";
	static HashMap<Integer, String[]> hub = new HashMap<>();
	static HashMap<Integer, String> digit = new HashMap<>();
	static InputStream is ;
	static TreeMap<Integer, String> hm;
	static POSModel model ;
	static POSTaggerME tagger;
	static ChunkerModel cModel ;
	static ChunkerME chunkerME;
	static PrintWriter wr;
	static PrintWriter wr2;
	static PrintWriter wr3;
	static PrintWriter wr4;

	public static void init() throws InvalidFormatException, IOException{
		is = new FileInputStream("en-chunker.bin");
		hm = new TreeMap<>();
		model = new POSModelLoader()
			.load(new File("en-pos-maxent.bin"));
		tagger = new POSTaggerME(model);
		cModel = new ChunkerModel(is);
		chunkerME = new ChunkerME(cModel);
		wr = new PrintWriter(new File("rawOutput.txt"));
		wr2 = new PrintWriter(new File("processed.txt"));
		wr3 = new PrintWriter(new File("relationphrases.txt"));
		//wr4 = new PrintWriter(new File("geneids.txt"));
		digit.put(0, "D");
		digit.put(1, "G");
		digit.put(2, "M");
		digit.put(3, "S");
		digit.put(4, "C");
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		init();
		
		//String filename = "/Volumes/backup/CSDATA/data/allTitleAbs_1_to_1052";
		// String filename =  "/Users/erichsu/Documents/research/espubmed/sample";
		 // String filename =  "/Users/erichsu/Documents/research/espubmed/file_ar";
		//String filename = "processed.txt";

		// get ids	
		
		//tagging(filename);
		
		
		
		// Step1: Get Raw file by chunk tagging
		
		// Step2: Process Raw file, Output to (processed.txt).
		String rawfile = "/Users/erichsu/Documents/research/espubmed/tagged.txt";
		process(rawfile);
		
		// Step3: Make dict and then release patterns, Output to (pattern.txt)
		getDocumentIDs("processed.txt");
		hashMesh();
		mining("processed.txt");
//		String tar = "10047442";
//		BufferedReader br = new BufferedReader(new FileReader(gene));
//		String line = br.readLine();
//		line = br.readLine();
//		while(line!=null){
//			if(line.split("\\t").length>0){
//				int id = Integer.parseInt(line.split("\\t")[0]);
//				if(hub.containsKey(id)){
//					wr4.println(line);
//				}
//			}
//			
//			line = br.readLine();
//		}
//		wr4.close();
	}
	
	public static void mining(String inputFile) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line = br.readLine();
		String id = null;
		String tags = null;
		String chunks = null;
		int count = 0;
		while(line!=null){
			
			if(count%3 ==0){
				//document ID
				if(isNumeric(line))
					id = line;
			}
			else if (count%3 ==1){
				if(line.length()>4)
					tags = line;
			}
			else if (count%3 ==2){
				if(line.length()>4)
					chunks = line;
				if(chunks!=null && id!=null && tags !=null){
					miningHelper(id, tags,chunks);
					chunks = null;
					id = null;
					tags = null;
				}
			}
			
			if(count%10000 == 0){
				print("mining "+count);
			}
			count+=1;
			line = br.readLine();
		}
		wr3.close();
	}
	
	public static void miningHelper(String id, String tags, String chunks){
		String[] tagSeg = tags.replace(" @ ", "@").split("@");
		String[] chunkSeg = chunks.replace(" @ ", "@").split("@");
		
		String[] entities = hub.get(Integer.parseInt(id));
		
		
		for(int i =0;i<chunkSeg.length;i++){
			tagSeg[i] += "|";
			for (int index = 0;index<5;index++){		
				if(checkEntity(chunkSeg[i], entities, index)){
					tagSeg[i] +=digit.get(index);
				}
			}
		}
		
		for(int i =0;i<chunkSeg.length;i++){
			tagSeg[i] += "|";
				if(tagSeg[i].substring(0,2).equals("VP")){
					tagSeg[i] += chunkSeg[i];
				}
			
		}
		
		wr3.println(id);
		StringBuilder sb = new StringBuilder();
		for(String tag: tagSeg){
			sb.append(tag);
			sb.append("\t");
		}
		wr3.println(sb.toString().trim());
		
	}
	
	public static Boolean checkEntity(String raw, String[] entities, int index){
		
		if(entities[index]==null){
			return false;
		}
		String[] detail = entities[index].split("\\|");
		for(String entity: detail){

			if(raw.contains(entity)){

					return true;

			}
		}
		return false;
	}
	
	
	public static void process(String rawfile) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(rawfile));
		String line = br.readLine();
		String id = null;
		String pos = null;
		String chunk = null;
		int count = 0;
		while(line!=null){
			
			if(count%3 ==0){
				//document ID
				if(isNumeric(line))
					id = line;
			}
			else if (count%3 ==1){
				if(line.length()>4)
					pos = line;
			}
			else if (count%3 ==2){
				if(line.length()>4)
					chunk = line;
			}
			if(count%10000 == 0){
				print("peocessing "+count);
			}
			count+=1;
			if(id != null & pos !=null && chunk!=null){
				// print(id);
				processHelper(id, pos, chunk);
				id = null;
				pos = null;
				chunk = null;
			}
			line = br.readLine();
		}
		wr2.close();
	}
	
	public static void processHelper(String id, String pos, String chunk){
		String[] chunSeq = chunk.split("\\t");
		String[] posSeq = pos.split(" ");
		
		// FIRST LINE
		wr2.println(id);
		
		ArrayList<String> chunks = new ArrayList<>();
		
		StringBuilder sbLine = new StringBuilder();
		StringBuilder cTags = new StringBuilder();
		
		for (String chunkUnit : chunSeq){
			String tag = getChunkTag(chunkUnit);
			cTags.append(tag);
			cTags.append(" @ ");
			int[] locs= getLoc(chunkUnit);
			StringBuilder sb = new StringBuilder();
			for (int i = locs[0]; i<locs[1]; i++){
				sb.append(getCont(posSeq[i]));
				sb.append(" ");
			}
			String curChunk = sb.toString().trim();
			sbLine.append(curChunk);
			sbLine.append(" @ ");
		}
		// SECOND LINE
		wr2.println(cTags.toString());
		// THIRD LINE
		wr2.println(sbLine);
	}
	
	public static int[] getLoc(String unit){
		String[] segs = unit.split("\\.");
		//print("unit "+unit);
		//print("ok "+segs[2]);
		String start = segs[0].substring(1, segs[0].length());
		//print(start);
		
		String end = segs[2].split("\\)")[0];
		return new int[]{Integer.parseInt(start), Integer.parseInt(end)};
	}
	
	public static String getPosTag(String unit){
		String[] tmp = unit.split("_");
		return tmp[tmp.length-1];
	}
	
	public static String getChunkTag(String unit){
		String[] tmp = unit.split(" ");
		return tmp[tmp.length-1];
	}
	
	public static String getCont(String unit){
		return unit.substring(0, unit.lastIndexOf("_"));
	}
	
	public static void tagging(String filename) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		String id = null;
		
		int count = 0;
		while(line!=null){
			count ++;
			if(count%5000==0){
				print(count);
			}
			if(line.length()<13){
				// this is id
				id = line;
				line = br.readLine();
				continue;
			}else{
				String[] sentences = line.replace(". ", "@").split("@");
				for (String sen : sentences){
					tagSentence(sen+". ", id);
				}
			}
			line = br.readLine();
		}
		wr.close();
	}
	
	public static void tagSentence(String sentence, String id) throws Exception{
		
		wr.println(id);
		ObjectStream<String> lineStream = new PlainTextByLineStream(
				new StringReader(sentence));
	 
		String line;
		String whitespaceTokenizerLine[] = null;
	 
		String[] tags = null;
		while ((line = lineStream.read()) != null) {
			whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
					.tokenize(line);
			tags = tagger.tag(whitespaceTokenizerLine);
	 
			POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
			wr.println(sample.toString());
		}
		
		Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
		for (Span s : span){
			String stemp = s.toString();
			wr.print(stemp+"\t");
		}
		wr.println();
	}
	
	
	public static void hashMesh() throws Exception{
		hashMeshHelper(disease, 0);
		hashMeshHelper(gene, 1);
		hashMeshHelper(mutation, 2);
		hashMeshHelper(species, 3);
		hashMeshHelper(chemical, 4);
		
	}
	public static void hashMeshHelper(String entityTypeFile, int index) throws Exception{
		print(entityTypeFile);
		BufferedReader br = new BufferedReader(new FileReader(entityTypeFile));
		String line = br.readLine();
		String id = null;
		int numID = 0;
		String pre = null;
		String entity = null;
		String[] segs = null;
		
		int count = 0;
		while(line!=null){
			segs = line.split("\\t");
			count+=1;
			if(count%3000000 ==0){
				print(count);
				//print(line);
			}
			if(segs.length==0){
				line = br.readLine();
				continue;
			}
				
			id = segs[0];
			
			if (!isNumeric(id)){
				line = br.readLine();
				continue;
			}	
			numID = Integer.parseInt(id);
			//print(numID);
			if(index==1)
				entity = segs[1];
			else
				entity = segs[2];
			
			if(hub.containsKey(numID)){
				String[] tmp = hub.get(numID);
				
				if(tmp[index] == null){
					tmp[index] =  entity;
				}
				else{
					String unit = "|"+entity;
					tmp[index]+= unit;
				}
				
				hub.put(numID, tmp);
				// print("found thi "+numID +" -"+ index);
			}
						
			line = br.readLine();
		}
		br.close();
	}
	
	public static void getDocumentIDs(String filename) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		ArrayList<String> ret = new ArrayList<>();
		int c2 =0;
		while(line != null){
			c2+=1;
			if(c2%1000000==0){
				print("getting id "+c2);
			}
			if(line.length() < 13 && isNumeric(line)){
				hub.put(Integer.parseInt(line), new String[5]);
				// print(line);
			}
			line = br.readLine();
		}
		br.close();
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
	public static void print(Object cont){
		System.out.println(cont.toString());
	}
}
