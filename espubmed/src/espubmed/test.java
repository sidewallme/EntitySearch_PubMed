package espubmed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.NotFound;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;

public class test {

	static UserAgent userAgentHub = new UserAgent();  
	public static HashMap<Integer, String> wordPair = new HashMap<>();
	static HashMap<String, ArrayList<Integer>> relationPhrases = new HashMap<>();
	static PrintWriter wr;
	static int fail = 0;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		wr = new PrintWriter(new File("d-g.txt"));
		//getURL("diabetes", "insulin");
		getEntityPair();
		wr.close();
	}
	
	public static void getEntityPair() throws IOException, NotFound, ResponseException{
		//chemical2pubtator.sample
		
		String e1, e2;
		BufferedReader br1 = new BufferedReader(new FileReader("chemical2pubtator.sample"));
		String line1 = br1.readLine();
		line1 = br1.readLine();
		while(line1 != null){
			e1 = getEntity(line1);
			if(e1 !=null){
				for(String ee1: e1.split("\\|")){
					BufferedReader br2 = new BufferedReader(new FileReader("disease2pubtator.sample"));
					String line2 = br2.readLine();
					line2 = br2.readLine();
					
					while(line2!=null){
						e2 = getEntity(line2);
						if(e2!=null)
							for(String ee2: e2.split("\\|")){
								wr.print(ee1+" | "+ee2 +" , ");
								getURL(ee1, ee2);
							}
						
						line2 = br2.readLine();
					}
					br2.close();
				}
			}
			
			
			line1 = br1.readLine();
		}
		br1.close();
		
	}
	
	public static String getEntity(String line){
		if(line.contains("MESH"))
			return line.split("\\t")[2];
		return null;
	}
	
	public ArrayList<String> getRelationSet(ArrayList<String> pair){
		String e1 = pair.get(0);
		String e2 = pair.get(1);
		
		return null;
	}
	
	public static void getURL(String e1, String e2) throws ResponseException, NotFound {
		String url = formatUrl(e1, e2, 0);
		Boolean ifMorePage = true;
		try {
			userAgentHub.visit(url);
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			fail++;
			System.out.println(url);
			System.out.println(fail);
		}
		
		Element ele = userAgentHub.doc.findFirst("<div id=\"stats\">");
		String result = ele.innerText();
		int answer = Integer.parseInt(result.split(" ")[0]);
		int page = answer/20;
		int rem = answer%20;
		
		if(page == 0)
			return;	//nothing here
		else {
			if (rem >0)
				page ++;
			System.out.println(e1+" | "+e2);
			
			//something here
			String tuple  = makeTuple(e1, e2);
			int curIndex = wordPair.size();
			wordPair.put(curIndex, tuple);
			
			for(int i=0;i<page;i++){
				url = formatUrl(e1, e2, i);
				userAgentHub.visit(url);
				Elements elements = userAgentHub.doc.findEvery("<span class=\"title-string\">");      //find every element who's tagname is div or span.
				int e =0;
				for (Element element :elements){
					if(e%2==1){
						String rel = element.innerText().trim();
						wr.println(rel);
						// to add
						if(relationPhrases.containsKey(rel)){
							relationPhrases.get(rel).add(curIndex);
						}else{
							relationPhrases.put(rel, new ArrayList<>());
							relationPhrases.get(rel).add(curIndex);
						}
					}
					e++;
				}
			}
		}
		
	}
	
	public static String formatUrl(String e1, String e2, int page){
		String s = String.format("http://openie.allenai.org/search/?arg1=%s&arg2=%s&page=%d&log=false",e1,e2,page);
		
		return s.replaceAll(" ", "+").replaceAll("\"", "");
	}
	
	public static String makeTuple(String e1, String e2){
		return e1+" | " +e2;
	}
	

}
