package org.brainmaker.kiwipredator;

import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.Filter;


/**
 * Given a complex sentence, split it into simple sentences with 
 * @author Zekai Huang
 *
 */
public class SentenceRelation {
	public enum RelationType {CONJUNCTION,DISJUNCTION,INFERENCE}

	
	
	public static void main(String[] args) {
		String sentence = "Birds are warm-blooded and lay eggs. ";
		System.out.println(sentence.toString());
		SentenceRelation sr = new SentenceRelation();
		sr.start(sentence);
		
	}
	
	public static LexicalizedParser lp(){
		LexicalizedParser lp = LexicalizedParser.loadModel();
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		return lp;
	}
	
	public void start(String sentence){
		Tree parse = lp().apply(sentence);
		parse.pennPrint();
		for(Tree sub:parse.firstChild().children()){
			if(sub.value().equals("NP")){
				System.out.println("NP=============");
				NPPrune(sub);
			}else if(sub.value().equals("VP")){
				System.out.println("VP=============");
				VPPrune(sub);
			}
		}
	}
	
	Filter<Tree> npfilter =  new Filter<Tree>(){

		@Override
		public boolean accept(Tree node) {
			if(node.isLeaf())
				return true;
			
			if(node.value().startsWith("N")) 
				return true;
			else 
				return false;
		}};
		
		Filter<Tree> predfilter =  new Filter<Tree>(){

			@Override
			public boolean accept(Tree node) {
				if(node.isLeaf())
					return true;
				
				if(node.value().startsWith("V")||node.value().startsWith("ADJP")||node.value().startsWith("JJ")) 
					return true;
				else 
					return false;
			}};
	
	public void NPPrune(Tree subTree){
		
		Tree newTree = subTree.prune(npfilter);
		newTree.pennPrint();
	}
	
	public void VPPrune(Tree subTree){
		Tree newTree = subTree.prune(predfilter);
		newTree.pennPrint();
	}

}