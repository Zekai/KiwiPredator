package org.brainmaker.kiwipredator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

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
public class TripletExtractor {
	
	private Integer sencounter;
	private Integer resCounter;
	private Integer fullCounter;
	
	public TripletExtractor()
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
		/*System.out.println(t.depth());
		if(t.depth()>10)
			continue;*/
		
		 Tree[] nodes = t.firstChild().children();
		 
		 
		 LabeledWord res = null;
		 LabeledWord res1 = null;
		 LabeledWord res2 = null;
		 for(Tree n:nodes)
		 {
			 //System.out.println(n.value());
			 if(n.value().contains("NP"))
			 {
				 //System.out.println("SBJ");//block for subject
				 //if the current node contain only a leaf, regardless of the type
				 //just choose it as the subject
				 if(n.depth()==2&&n.numChildren()==1)
				 {
					 res = getLeaf(n.firstChild());
				 }
				 else
				 {
					 Tree SBJnode = getDeepestGroup("NP",n);
					 
					 if(SBJnode!=null)
						 res = extractSubject(SBJnode);
					 else
						 res = extractSubject(n);
				 }
				
				 
				 
				 //if(res==null)
					 //err(trainfile,n);
				 //displayLabeledWord(res);
			 }
			 else if(n.value().equals("VP"))
			 {
				 //System.out.println("PRD");
				 Tree PRDnode = getDeepestGroup("VP",n);
				 
				 
				 if(PRDnode!=null)
					 res1 = extractPredicate(PRDnode);
				 else
					 res1 = extractPredicate(n);
				 
				//if(res1==null)
				 //	err(trainfile,n);
				 //displayLabeledWord(res1);
				 
				 
				 //System.out.println("OBJ");
				 
				 
				 if(PRDnode!=null)
				 {
					 if(PRDnode.numChildren()==2&&PRDnode.getChild(1).value().equals("S"))
					 {
						 res2 = extractObject(escapeClause(PRDnode));
					 }
					 else
						 res2 = extractObject(PRDnode);
				 }
				 else
					 res2 = extractObject(n);
				 
				//if(res2==null)
				 //	err(trainfile,n);
				 //displayLabeledWord(res2);
			 }			 
		 }
		 displayres(res,res1,res2);
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
	
	
	private void displayres(LabeledWord res,LabeledWord res1,LabeledWord res2)
	{
		if(res!=null&&res1!=null)
		{
			resCounter++;
			System.out.print(res.word()+" "+res1.word());
			if(res2!=null)
			{
				System.out.println(" "+res2.word());
				fullCounter++;
			}
			else
				System.out.println();
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
	}
	
	private LabeledWord extractSubject(Tree node)
	{
		//the last NN in the first NP of current node
		
		
			Tree lastNN = null;
			String type = node.firstChild().value();
			boolean sameType = true;
			String NNchain = "";
			for(Tree n:node.children())
			{
				if(n.value().startsWith("NN")||n.value().startsWith("PRP"))
					lastNN = n;
				
				if(sameType&&n.value().equals(type)&&lastNN!=null)
				{
					NNchain = NNchain + "_" + getLeaf(n).word();
				}
				else
					sameType = false;
					
			}
			if(sameType)
			{
				LabeledWord lbw = new LabeledWord(NNchain);
				return lbw;
			}
			if(lastNN!=null)
				return getLeaf(lastNN);
			else
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
	
	private LabeledWord extractObject(Tree node)
	{
		 LabeledWord res = null;
		 Tree SBJnode = getDeepestGroup("NP",node);
		 if(SBJnode!=null)
		 {
			 res = extractOBJ("NP",SBJnode);
		 }
		 else 
		 {
			 SBJnode = getDeepestGroup("PP",node);
			 if(SBJnode!=null)
			 {
				 
				 res = extractOBJ("PP",SBJnode); 
			 }
			 else
			 {
				 SBJnode = getDeepestGroup("ADJP",node);
				 if(SBJnode!=null)
				 {
					 
					 res = extractOBJ("ADJP",SBJnode); 
				 }
			 }
		 }
		 
		 
		 
		 return res;
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
		      Tree parse = lp.apply(sentence);
		      //parse.pennPrint();		      
		      extractorFromTree(parse);
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
			

		
		
			TripletExtractor te =  new TripletExtractor();
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
