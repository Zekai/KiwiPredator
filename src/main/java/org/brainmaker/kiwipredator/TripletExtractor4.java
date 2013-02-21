package org.brainmaker.kiwipredator;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.LabeledWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class TripletExtractor4 {
	
	private static boolean sPrintTree = true;

	
	public static void main(String[] args) {

		if (args.length == 1) {
			TripletExtractor4 te = new TripletExtractor4();
			String path = args[0];
			te.Parser(path);

		} else {
			System.out.println("Run it like this:");
			System.out.println("./wsj-raw/00/wsj_0001");

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
			if(sPrintTree) sen.pennPrint();
			extractFromTree(sen);
		}
	}
	
	public void extractFromTree(Tree tree) {

		// Create a reusable pattern object
		TregexPattern patternMW = TregexPattern.compile("S < VP=vp & < NP=np");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(tree);
		// Iterate over all of the subtrees that matched
		while (matcher.findNextMatchingNode()) {
			List<LabeledWord> subWords = new ArrayList<LabeledWord>();
			List<LabeledWord> preWords = new ArrayList<LabeledWord>();
			List<LabeledWord> objWords = new ArrayList<LabeledWord>();
			Tree match = matcher.getMatch();
			// do what we want to with the subtree
			// System.out.println(matcher.getVariableString("NP"));

			Tree subNode = matcher.getNode("np");
			if (subNode != null) {
				System.out.println("subject groups:");
				if(sPrintTree) subNode.pennPrint();
				extractSubject(subNode, subWords);
			}

			Tree vpNode = matcher.getNode("vp");
			if (vpNode != null) {
				//vpNode.pennPrint();
				splitPredObjGroup(vpNode, preWords, objWords);
			}
			//System.out.println(subWords);
			//System.out.println(preWords);
			//System.out.println(objWords);
			print(subWords, preWords, objWords);
		}

		System.out.println("\n\n");

	}
	
	private void print(List<LabeledWord> subWords,List<LabeledWord> predWords,List<LabeledWord> objWords) {
		for(LabeledWord sub:subWords){
			for(int i=0;i<predWords.size();i++){
				LabeledWord pred = predWords.get(i);
				LabeledWord obj = objWords.get(i);
				System.out.println(sub.word()+" "+pred.word()+" "+obj.word());
			}
			
		}
	}
	
	public void splitPredObjGroup(Tree tree,List<LabeledWord> predRes,List<LabeledWord> objRes) {
		System.out.println("==================splitPredObjGroup");
		// Create a reusable pattern object
		 
		TregexPattern patternMW = TregexPattern.compile("VP < (/^VB/=verb $++ (PP|NP|ADJP=obj))");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(tree);
		// Iterate over all of the subtrees that matched
		while (matcher.findNextMatchingNode()) {
			Tree match = matcher.getMatch();
			//match.pennPrint();
			 Tree vbNode = matcher.getNode("verb");
			 
			  if(vbNode!=null){
				  System.out.println("Verb groups:");
				  if(sPrintTree) vbNode.pennPrint();
				  extractPred(vbNode,predRes);
			  }
			  
			  Tree objNode = matcher.getNode("obj");
				 
			  if(objNode!=null){
				  System.out.println("Object groups");
				  if(sPrintTree) objNode.pennPrint();
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
		LabeledWord l = getLeaf(node);
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
