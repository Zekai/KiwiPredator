package org.brainmaker.kiwipredator;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.LabeledWord;
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
			sen.pennPrint();
			extractFromTree(sen);
		}
	}
	
	public void extractFromTree(Tree tree){
		
		List<LabeledWord> subWords = new ArrayList<LabeledWord> ();
		 List<LabeledWord> preWords = new ArrayList<LabeledWord> ();
		 List<LabeledWord> objWords = new ArrayList<LabeledWord> ();
		
		// Create a reusable pattern object 
		TregexPattern patternMW = TregexPattern.compile("S < VP=vp & < NP=np"); 
		// Run the pattern on one particular tree 
		TregexMatcher matcher = patternMW.matcher(tree); 
		// Iterate over all of the subtrees that matched 
		while (matcher.findNextMatchingNode()) { 
		  Tree match = matcher.getMatch(); 
		  // do what we want to with the subtree 
		  //System.out.println(matcher.getVariableString("NP"));
		  
		  Tree subNode = matcher.getNode("np");
			if (subNode != null) {
				subNode.pennPrint();
				extractSubject(subNode,subWords);
			}
		  
		  Tree vpNode = matcher.getNode("vp");
		  if(vpNode!=null){
			  //vpNode.pennPrint();
			  splitPredObjGroup(vpNode,preWords,objWords);
		  }
		}
		
		System.out.println(subWords.toString());
		System.out.println(preWords.toString());
		System.out.println(objWords.toString());
		
		System.out.println("\n\n");

	}
	
	public void splitPredObjGroup(Tree tree,List<LabeledWord> predRes,List<LabeledWord> objRes) {
		System.out.println("==================splitPredObjGroup");
		// Create a reusable pattern object
		 
		TregexPattern patternMW = TregexPattern.compile("VP < (/^VB/=verb $++ __=obj)");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(tree);
		// Iterate over all of the subtrees that matched
		while (matcher.findNextMatchingNode()) {
			Tree match = matcher.getMatch();
			//match.pennPrint();
			 Tree vbNode = matcher.getNode("verb");
			 
			  if(vbNode!=null){
				  System.out.println("Verb groups:");
				  vbNode.pennPrint();
				  extractPred(tree,predRes);
			  }
			  
			  Tree objNode = matcher.getNode("obj");
				 
			  if(objNode!=null){
				  System.out.println("Object groups");
				  objNode.pennPrint();
				  extractObject(objNode,objRes);
			  }
		}
	}
	
	
	public void extractSubject(Tree node,List<LabeledWord> res) {
		LabeledWord l = null;
		if (node.depth() == 2 && node.numChildren() == 1) {
			 l = getLeaf(node.firstChild());
		}
		else {
			 l = getNN(node);
		}
		
		if(l!=null) res.add(l);
	}
	
	public void extractPred(Tree node,List<LabeledWord> res) {
		LabeledWord l = getVB(node);
		if(l!=null) res.add(l);
	}
	
	public void extractObject(Tree node,List<LabeledWord> res){//PP,NP,ADJP
		String rootType = node.value();
		if("NP".equals(rootType))
		{
			LabeledWord l = getNN(node);
			if(l!=null) res.add(l);
		}
		else if("PP".equals(rootType)){
			TregexPattern patternMW = TregexPattern.compile("PP < IN=in & < NP=np");
			// Run the pattern on one particular tree
			TregexMatcher matcher = patternMW.matcher(node);
			if (matcher.findNextMatchingNode()) {
				Tree npNode = matcher.getNode("np");
				String npType = npNode.value();
				if("NP".equals(npType))
				{
					LabeledWord l = getNN(node);
					if(l!=null) res.add(l);
				}
			}
		}
		else if("ADJP".equals(rootType)){
			TregexPattern patternMW = TregexPattern.compile("ADJP <: JJ=jj");
			// Run the pattern on one particular tree
			TregexMatcher matcher = patternMW.matcher(node);
			if (matcher.findNextMatchingNode()) {
				Tree jjNode = matcher.getNode("jj");
				String npType = jjNode.value();
				if("JJ".equals(npType))
				{
					LabeledWord l = getLeaf(jjNode);
					if(l!=null) res.add(l);
				}
			}
		}
	}
	
	public LabeledWord getNN(Tree node){
		TregexPattern patternMW = TregexPattern.compile("NP <<- (/^NN/=nn)");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(node);
		// Iterate over all of the subtrees that matched
		if (matcher.findNextMatchingNode()) {
			Tree objNode = matcher.getNode("nn");
			 
			  if(objNode!=null){
				  return getLeaf(objNode);
			  }
		}
		return null;
	}
	
	public LabeledWord getVB(Tree node){
		TregexPattern patternMW = TregexPattern.compile("VP < (/^VB/=vb)");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(node);
		// Iterate over all of the subtrees that matched
		if (matcher.findNextMatchingNode()) {
			Tree objNode = matcher.getNode("vb");
			 
			  if(objNode!=null){
				  return getLeaf(objNode);
			  }
		}
		return null;
	}
	
	private LabeledWord getLeaf(Tree node)
	{
			List<LabeledWord> lbs = node.labeledYield();
			return lbs.get(0);
	}
}
