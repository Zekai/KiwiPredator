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
public class TripletExtractor3 {
	
	
	
	
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
		      
		      sen = extractorFromTree(sen);
		      
		      if(sen!=null){
		    	  Tree subgroup = ExtractSubGroup(sen.firstChild());
		    	  if(subgroup!=null) subgroup.pennPrint();
		      }
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
			TripletExtractor3 te =  new TripletExtractor3();
			if(type.equals("-r"))
			{
				te.Parser(path);
			}
		
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
	
	
	public Tree extractorFromTree(final Tree tree){
		RegularPrune rp = new RegularPrune();
		RegularFilter rf = new RegularFilter();
		rf.setRoot("S");
		rf.addChildRule("NP");
		rf.addChildRule("VP");
		Tree result = rp.prune(tree.firstChild(), rf);
		return result;
	}
	
	public Tree ExtractSubGroup(final Tree tree){
		RegularPrune rp = new RegularPrune();
		RegularFilter rf = new RegularFilter();
		rf.setRoot("NP");
		rf.addChildRule("NNS");
		Tree result = rp.prune(tree, rf);
		if(result==null)
		{
			rf.reSetRule();
			rf.setDepth1Num(1);
			result = rp.prune(tree, rf);
		}
		return result;
	}

}

