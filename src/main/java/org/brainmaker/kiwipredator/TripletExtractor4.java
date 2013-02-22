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
			System.out.println("You need to specify an input file.");

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
			List<Tree> res = splitTree(sen);
			for(Tree t:res){
				extractFromTree(t);
			}
		}
	}
	
	public List<Tree> splitTree(Tree tree){
		TregexPattern patternMW = TregexPattern.compile("S < S=s1 < CC < S=s2");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(tree);
		// Iterate over all of the subtrees that matched
		List<Tree> results = new ArrayList<Tree>();
		while (matcher.findNextMatchingNode()) {
			Tree tree1 = matcher.getNode("s1");
			if(tree1!=null) results.add(tree1);
			
			Tree tree2 = matcher.getNode("s2");
			if(tree2!=null) results.add(tree2);
			
		}
		return results;
	}
	
	public void extractFromTree(Tree tree) {

		// Create a reusable pattern object
		TregexPattern patternMW = TregexPattern.compile("S < VP=vp & < NP=np");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(tree);
		// Iterate over all of the subtrees that matched
		while (matcher.findNextMatchingNode()) {
			List<String> subWords = new ArrayList<String>();
			List<String> preWords = new ArrayList<String>();
			List<String> objWords = new ArrayList<String>();
			Tree match = matcher.getMatch();
			// do what we want to with the subtree
			// System.out.println(matcher.getVariableString("NP"));

			Tree subNode = matcher.getNode("np");
			if (subNode != null) {
				
				if(sPrintTree){
					System.out.println("subject groups:");
					subNode.pennPrint();
				}
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
	
	private void print(List<String> subWords,List<String> predWords,List<String> objWords) {
		for(String sub:subWords){
			int count = objWords.size();
			for(int i=0;i<predWords.size();i++){
				String pred = predWords.get(i);
				String obj = "";
				if(count>i) obj = objWords.get(i);
				System.out.println(sub+" "+pred+" "+obj);
			}
			
		}
	}
	
	public void splitPredObjGroup(Tree tree,List<String> predRes,List<String> objRes) {
		System.out.println("==================splitPredObjGroup");
		// Create a reusable pattern object
		 
		TregexPattern patternMW = TregexPattern.compile("VP < (/^VB/=verb $+ (NP|PP|ADJP=obj))");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(tree);
		// Iterate over all of the subtrees that matched
		while (matcher.findNextMatchingNode()) {
			Tree match = matcher.getMatch();
			//match.pennPrint();
			 Tree vbNode = matcher.getNode("verb");
			 
			  if(vbNode!=null){
				  
				  if(sPrintTree){
					  System.out.println("Verb groups:");
					  vbNode.pennPrint();
				  }
				  extractPred(vbNode,predRes);
			  }
			  
			  Tree objNode = matcher.getNode("obj");
				 
			  if(objNode!=null){
				  
				  if(sPrintTree){
					  System.out.println("Object groups");
					  objNode.pennPrint();
				  }
				  extractObject(objNode,objRes);
			  }
		}
	}
	
	
	public void extractSubject(Tree node,List<String> res) {
		String l = null;
		if (node.depth() == 2 && node.numChildren() == 1) {
			 l = getLeaf(node.firstChild());
		}
		else {
			 l = getNN(node,true);
		}
		
		if(l!=null) res.add(l);
	}
	
	public void extractPred(Tree node,List<String> res) {
		String l = getLeaf(node);
		if(l!=null) res.add(l);
	}
	
	public void extractObject(Tree node,List<String> res){//PP,NP,ADJP
		String rootType = node.value();
		if("NP".equals(rootType))
		{
			String l = getNN(node,true);
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
					String l = getNN(node,false);
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
					String l = getLeaf(jjNode);
					if(l!=null) res.add(l);
				}
			}
		}
	}
	
	public String getNN(Tree node,boolean excludePP){
		String pattern;
		if(excludePP){
			pattern = "NP !> PP < (/^NN/=nn)";
		}
		else
		{
			pattern = "NP < (/^NN/=nn)";
		}
		TregexPattern patternMW = TregexPattern.compile(pattern);
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(node);
		// Iterate over all of the subtrees that matched
		String res = "";
		while (matcher.findNextMatchingNode()) {
			Tree objNode = matcher.getNode("nn");
			 
			  if(objNode!=null){
				  String l =  getLeaf(objNode);
				  if(l!=null){
					  if(res.equals(""))
						  res = l;
					  else 
						  res += "_"+l;
				  }
			  }
		}
		return res;
	}
	
	public String getVB(Tree node){
		TregexPattern patternMW = TregexPattern.compile("VP < (/^VB/=vb)");
		// Run the pattern on one particular tree
		TregexMatcher matcher = patternMW.matcher(node);
		// Iterate over all of the subtrees that matched
		String res = "";
		if (matcher.findNextMatchingNode()) {
			Tree objNode = matcher.getNode("vb");
			 
			if(objNode!=null){
				  res =  getLeaf(objNode);
			  }
		}
		return res;
	}
	
	private String getLeaf(Tree node)
	{
			List<LabeledWord> lbs = node.labeledYield();
			return lbs.get(0).word();
	}
}
