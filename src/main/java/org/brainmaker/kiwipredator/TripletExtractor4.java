package org.brainmaker.kiwipredator;

import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.AbstractTreebankLanguagePack;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.Function;

public class TripletExtractor4 {

	
	
	
	
	public void extractFromTree(Tree tree){
		
		// Create a reusable pattern object 
		TregexPattern patternMW = TregexPattern.compile("S < VP=vp & < NP=np"); 
		// Run the pattern on one particular tree 
		TregexMatcher matcher = patternMW.matcher(tree); 
		// Iterate over all of the subtrees that matched 
		while (matcher.findNextMatchingNode()) { 
		  Tree match = matcher.getMatch(); 
		  // do what we want to with the subtree 
		  //System.out.println(matcher.getVariableString("NP"));
		  

		  
		  Tree npNode = matcher.getNode("np");
		  //if(npNode!=null) npNode.pennPrint();
		  
		  Tree vpNode = matcher.getNode("vp");
		  if(vpNode!=null){
			  vpNode.pennPrint();
			  splitPredObjGroup(vpNode);
		  }
		}
		
		System.out.println("\n\n");

	}
	
	public void splitPredObjGroup(Tree tree) {
		System.out.println("==================splitPredObjGroup");
		// Create a reusable pattern object
		 
		TregexPattern patternMW = TregexPattern.compile("VP << /^VB/=verb");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(tree);
		// Iterate over all of the subtrees that matched
		while (matcher.findNextMatchingNode()) {
			Tree match = matcher.getMatch();
			//match.pennPrint();
			 Tree vbNode = matcher.getNode("verb");
			  if(vbNode!=null){
				  vbNode.pennPrint();
			  }
		}
	}
	
	public void Parser(String filename) {
		// This option shows loading and sentence-segment and tokenizing
		// a file using DocumentPreprocessor

		LexicalizedParser lp = LexicalizedParser.loadModel();
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		// You could also create a tokenier here (as below) and pass it
		// to DocumentPreprocessor
		for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
			if (sentence.size() > 50)
				continue;
			System.out.println(sentence.toString());
			Tree sen = lp.apply(sentence);
			//sen.pennPrint();
			extractFromTree(sen);
		}
	}

	public static void main(String[] args) {

		// -f folder
		// -d document
		// -r raw
		// -t tree
		String type = args[0];
		String filetype = args[1];
		String path = args[2];

		if (args.length == 3) {
			TripletExtractor4 te = new TripletExtractor4();
			if (type.equals("-r")) {
				te.Parser(path);
			}

		} else {

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
