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
	private static List<String> sSentenceCompatible = Arrays.asList("S");
	private static List<String> sSubCompatible = Arrays.asList("NP");
	private static List<String> sPredCompatible = Arrays.asList("VP");
	private static List<String> sObjCompatible = Arrays.asList("NP", "NNP", "PRP", "ADJP", "PP");

	
	
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
				getAllGroup(groups,false, partType.SUB, n);

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
				 getAllGroup(groups,false,partType.PRED,n);
				 
				 for(Tree g:groups){
					 LabeledWord w = extractPredicate(g);
					 if(w!=null) res1.add(w);
					 List<Tree> groups2 = new ArrayList<Tree>();
					 getAllGroup(groups2,true,partType.OBJ,g);
					 
					 for(Tree obj:groups2)
					 {
						 LabeledWord  w2 = extractOBJ(obj.value(),obj);
						if (w2 != null) {
							res2.add(w2);
							emit(res,w,w2);
						} else {
							res2.add(new LabeledWord());
							emit(res,w,new LabeledWord());
						}
					 }
				 } 
				 
			 }			 
		 }
	}
	
	private void emit(List<LabeledWord> subs,LabeledWord pred,LabeledWord obj){
		for(LabeledWord sub:subs){
			System.out.println(sub.word() +" "+ pred.word() + " "+obj.word());
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
	
	private void getAllGroup(List<Tree> res,boolean findroot, partType type, Tree n){
		Tree firstNodeOfThisType = null;
		if(findroot){			
			
			 for(Tree child:n.children())
				{
					if(checkTypeCompatible(type,child.value()))
					{
						firstNodeOfThisType = child;
						break;
					}
				}
		}
		else{
			firstNodeOfThisType = n;
		}
		if(firstNodeOfThisType==null) return;
		boolean temp = false;
		for (Tree c : firstNodeOfThisType.children()) {
			
			if(checkTypeCompatible(type,c.value()))
			{
				temp = true;
				getAllGroup(res,false,type,c);
			} 
		}
		if(temp==false){
			if(checkTypeCompatible(type,firstNodeOfThisType.value()))
				res.add(firstNodeOfThisType);
		}
		 
	}
	
	private boolean checkTypeCompatible(partType type, String subType) {
		switch (type) {
		case S:
			return sSentenceCompatible.contains(subType);
		case SUB:
			return sSubCompatible.contains(subType);
		case PRED:
			return sPredCompatible.contains(subType);
		case OBJ:
			return sObjCompatible.contains(subType);
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
		      //sen.pennPrint();	
		      List<Tree> sens = new ArrayList<Tree>();
		    		  getAllGroup(sens,true, partType.S, sen);
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
