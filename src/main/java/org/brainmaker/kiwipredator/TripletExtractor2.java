package org.brainmaker.kiwipredator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.LabeledWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

/*
 * This class is to convert the standard penn treebank into the format we want. 
 * Not for the extraction task
 */
public class TripletExtractor2 {
	
	private Integer sencounter;
	private Integer resCounter;
	private Integer fullCounter;
	
	private enum partType {S,SUB,PRED,OBJ}
	private static Map<String,Integer> sSentenceCompatible;
	private static Map<String,Integer> sSubCompatible;
	private static Map<String,Integer> sPredCompatible;
	private static Map<String,Integer> sObjCompatible;
	private static final Integer DEFAULT_SATIS_LEVEL =  10;
	
	static {
		sSentenceCompatible = new Hashtable<String,Integer>();
		sSubCompatible = new Hashtable<String,Integer>();
		sPredCompatible = new Hashtable<String,Integer>();
		sObjCompatible = new Hashtable<String,Integer>();
		
		// we use integer to indicate the satisfaction level, 1 is the best.
		// when iterating, if we've already have a group with level x, we will not accept other groups higher than that level.
		// make sure that all levels are smaller than DEFAULT_SATIS_LEVEL, otherwise they won't be accepted at all
		sSentenceCompatible.put("S", 1);
		sSubCompatible.put("NP", 1);
		sPredCompatible.put("VP", 1);
		sObjCompatible.put("NP", 1);
		sObjCompatible.put("NNP", 2);
		sObjCompatible.put("PRP", 3);
		sObjCompatible.put("ADJP", 3);
		sObjCompatible.put("PP", 3);
	}
	
	private static List<String> sPredAux = Arrays.asList("VBP","VBZ");
	private static List<String> sObjAux = Arrays.asList("IN");

	
	
	public TripletExtractor2()
	{
		sencounter = 0;
		resCounter = 0;
		fullCounter = 0;
	}
	
	
	public void walk( String path ) {

        File root = new File( path );
        File[] list = root.listFiles();

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath() );
                System.out.println( "Dir:" + f.getAbsoluteFile() );
            }
            else if(!f.isHidden()) {
                System.out.println( "File:" + f.getAbsoluteFile() );
            	extractor(f.getAbsolutePath());
            }
        }
    }
	
	public void extractorFromTree(Tree t)
	{
		
		 Tree[] nodes = t.children();
		 
		 List<LabeledWord> res = new ArrayList<LabeledWord> ();
		 List<LabeledWord> res1 = new ArrayList<LabeledWord> ();
		 List<LabeledWord> res2 = new ArrayList<LabeledWord> ();
		 for(Tree n:nodes)
		 {
			 //System.out.println(n.value());
			if (n.value().contains("NP")) {
				List<Tree> groups = new ArrayList<Tree>();
				LabeledWord aux = new LabeledWord();
				getAllGroup(groups,false, partType.SUB, n, aux );

				for (Tree g : groups) {
					LabeledWord w = extractSubject(g);
					if (w != null)
						res.add(w);
				}

			}
			 else if(n.value().equals("VP"))
			 {
				 //System.out.println("PRD");
				 List<Tree> groups = new ArrayList<Tree>();
				 LabeledWord aux = new LabeledWord();
				 getAllGroup(groups,false,partType.PRED,n,aux);
				 
				 for(Tree g:groups){
					 LabeledWord w = extractPredicate(g);
					 if(w!=null) res1.add(w);
					 List<Tree> groups2 = new ArrayList<Tree>();
					 LabeledWord aux2 = new LabeledWord();
					 getAllGroup(groups2,true,partType.OBJ,g,aux2);
					 
					 for(Tree obj:groups2)
					 {
						 LabeledWord  w2 = extractOBJ(obj.value(),obj);
						if (w2 != null) {
							res2.add(w2);
							emit(res,aux,w,aux2,w2);
						} else {
							res2.add(new LabeledWord());
							emit(res,aux,w,aux2,new LabeledWord());
						}
					 }
				 } 
				 
			 }			 
		 }
	}
	
	private void emit(List<LabeledWord> subs,LabeledWord predaux,LabeledWord pred,LabeledWord ObjAux,LabeledWord obj){
		for(LabeledWord sub:subs){
			System.out.println(sub.word() +" "+(predaux.word()==null?"":predaux.word()+"_")+ pred.word() +(ObjAux.word()==null?"":"_"+ObjAux.word())+ " "+obj.word());
		}
		
	}
	
	public void extractor(String trainfile)
	{
		try {
			BufferedReader bf = new BufferedReader(new FileReader(trainfile));
			PennTreeReader pt = new PennTreeReader(bf);
			Tree t = null;
			while ((t=pt.readTree())!=null)
			{
				
				sencounter ++;
				extractorFromTree(t);
			
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	
	private void displayres(List<LabeledWord>  res,List<LabeledWord> res1,List<LabeledWord> res2)
	{
		
		for(LabeledWord sub:res){
			for(int i=0;i<res1.size();i++){
				    String verb = res1.get(i).word();
				    String obj = res2.get(i).word();
					System.out.println(sub.word() +" "+ verb + " "+obj);
				}
			
		}
	}
	
	public void displayCount()
	{
		System.out.println("Total Sentence:"+sencounter);
		System.out.println("Result found:"+resCounter);
		System.out.println("Triplet found:"+fullCounter);
	}
	
	private Tree DeeperSameGroup(String type, Tree node)
	{
		for(Tree current:node.children())
		{
			//current.printLocalTree();
			if(current.value().equals(type))
			{
				return DeeperSameGroup(type, current);
			}
		}
		return node;
	}
	
	private void err(String filename,Tree node)
	{
		System.err.println(filename);
		node.pennPrint();
	}
	/*
	private Tree getDeepestGroup(String type, Tree n)
	{
		Tree firstNodeOfThisType = null;
		
		
		 for(Tree child:n.children())
			{
				if(child.value().startsWith(type))
				{
					firstNodeOfThisType = child;
					break;
				}
			}
		 Tree deepestNodeOfThisType = null;
		 if(firstNodeOfThisType!=null)
			 deepestNodeOfThisType = DeeperSameGroup(type,firstNodeOfThisType);
		 
		 return deepestNodeOfThisType;
	}*/
	
	private void getAllGroup(List<Tree> res,boolean findroot, partType type, Tree n,LabeledWord aux){
		Tree firstNodeOfThisType = null;
		if (findroot) {

			for (Tree child : n.children()) {
				if (checkTypeCompatible(type, child.value())<DEFAULT_SATIS_LEVEL) {
					firstNodeOfThisType = child;
					break;
				}
			}
		}
		else{
			firstNodeOfThisType = n;
		}
		if(firstNodeOfThisType==null) return;
		
		boolean hasPart = false;
		LabeledWord l = new LabeledWord();
		Integer acceptLevel = DEFAULT_SATIS_LEVEL;
		for (Tree c : firstNodeOfThisType.children()) {
			int r = checkTypeCompatible(type, c.value());
			if (r<=acceptLevel) {
				acceptLevel = r;
				hasPart = true;
				if(l.word()!=null){//we only do side effect of aux when there is embedded group
					aux.setWord(l.word());
					aux.setTag(l.tag());
					aux.setValue(l.value());
				}
				getAllGroup(res, false, type, c, aux);
				//if(r==1) break;// if we got the first choice group, stop iterating other compatible group
			} else {
				if (checkTypeAux(type, c.value())) {
					l = getLeaf(c);
				}
			}
		}
		
		if(hasPart==false){
			if(checkTypeCompatible(type,firstNodeOfThisType.value())<DEFAULT_SATIS_LEVEL)
			{
				res.add(firstNodeOfThisType);
			}
		}
	}
	
	private Integer checkTypeCompatible(partType type, String subType) {
		Integer result = null;
		switch (type) {
		case S:
			result =   sSentenceCompatible.get(subType);
			break;
		case SUB:
			result =  sSubCompatible.get(subType);
			break;
		case PRED:
			result = sPredCompatible.get(subType);
			break;
		case OBJ:
			result =  sObjCompatible.get(subType);
			break;
		default:
			break;
		}
		if(result==null) return Integer.MAX_VALUE;
		else
			return result;
	}
	
	private boolean checkTypeAux(partType type,String subType){
		switch (type) {
		case S:
			return false;
		case SUB:
			return false;
		case PRED:
			return sPredAux.contains(subType);
		case OBJ:
			return sObjAux.contains(subType);
		default:
				return false;
		}
	}
	
	private LabeledWord extractSubject(Tree node) {
		// the last NN in the first NP of current node
		if (node.depth() == 2 && node.numChildren() == 1) {
			return getLeaf(node.firstChild());
		}

		Tree lastNN = null;

		boolean sameType = true;
		String NNchain = "";
		if (node.depth() > 1) {
			String type = node.firstChild().value();

			for (Tree n : node.children()) {
				if (n.value().startsWith("NN") || n.value().startsWith("PRP"))
					lastNN = n;

				if (sameType && n.value().equals(type) && lastNN != null) {
					NNchain = NNchain + "_" + getLeaf(n).word();
				} else
					sameType = false;

			}
			if (sameType) {
				LabeledWord lbw = new LabeledWord(NNchain);
				return lbw;
			}
		} else {
			if (node.value().startsWith("NN") || node.value().startsWith("PRP"))
				lastNN = node;
		}

		if (lastNN != null)
			return getLeaf(lastNN);
		else
			return null;
	}
	
	private LabeledWord extractPredicate(Tree node)
	{
		
		boolean foundNP = false;
		for(Tree child:node)
		{
			if(child.value().startsWith("VB"))
			{
			 	return getLeaf(child);
			}
		}
		return null;
		
	}
	
	private LabeledWord extractOBJ(String type,Tree node)
	{
		//the last NN in the first NP of current node
		
			Tree lastNN = null;
			for(Tree n:node.children())
			{
				if(type.startsWith("NP"))
				{
					if(n.value().startsWith("NN")||n.value().startsWith("PRP"))
						lastNN = n;
				}
				else if(type.startsWith("PP"))
				{
					if(n.value().startsWith("NN"))
						lastNN = n;
				}
				else if(type.startsWith("ADJP"))
				{
					if(n.value().startsWith("JJ"))
						lastNN = n;
				}
			}
	
			if(lastNN!=null)
				return getLeaf(lastNN);
			else
				return null;
	}
	
	
	private Tree escapeClause(Tree node)
	{
		for(Tree n:node)
		{
			if(n.value().startsWith("NP"))
			{
				if(!n.firstChild().value().equals("-NONE-"))
				{
					return n;
				}
			}
		}
		return node;
	}
	
	
	private LabeledWord getLeaf(Tree node)
	{
			List<LabeledWord> lbs = node.labeledYield();
			return lbs.get(0);
	}
	
	private void displayLabeledWord(LabeledWord lb)
	{
		if(lb!=null)
			System.out.println(lb.tag().value()+" "+lb.word());
		else
			System.err.println("Empty result");
	}
	
	
	
	
	  public  void Parser(String filename) {
		    // This option shows loading and sentence-segment and tokenizing
		    // a file using DocumentPreprocessor
		  
		  LexicalizedParser lp = LexicalizedParser.loadModel();
		    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		    // You could also create a tokenier here (as below) and pass it
		    // to DocumentPreprocessor
		    for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
		    	if(sentence.size()>50)
		    		continue;
		      System.out.println(sentence.toString());
		      Tree sen = lp.apply(sentence);
		      sen.pennPrint();	
		      List<Tree> sens = new ArrayList<Tree>();
		      LabeledWord aux = new LabeledWord();
		      getAllGroup(sens,true, partType.S, sen,aux);
		      for(Tree s:sens){
		    	  extractorFromTree(s);
		      }
		      //extractorFromTree(sen);
		    }
		  }
	
     
	public static void main (String[] args) {
		
		// -f  folder
		// -d  document
		// -r raw
		// -t tree
		String type = args[0];
		String filetype = args[1];
		String path = args[2];
		
		if(args.length==3)
		{
			

		
		
			TripletExtractor2 te =  new TripletExtractor2();
			if(type.equals("-r"))
			{
				te.Parser(path);
			}
			else if (type.equals("-t"))
			{
				if(filetype.equals("-f"))
					te.walk(path);
				else if(filetype.equals("-d"))
					te.extractor(path);
			}
			
			
			//
			te.displayCount();
		//
		
		}
		else
		{
			
			System.out.println("-r: indicating it is raw file");
			System.out.println("-t: indicating it is tree file");
			System.out.println("-f  path_to_folder");
			System.out.println("-d  Path_to_fingle_document");
			System.out.println("Run it like this:");
			System.out.println("-r -d ./wsj-raw/00/wsj_0001");
			System.out.println("-t -f ./Penn-trees/00");
			System.out.println("-t -d ./Penn-trees/00/wsj_0001.mrg");
			
		}
		
	}

}
