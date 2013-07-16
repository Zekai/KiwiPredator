package org.brainmaker.kiwipredator.test;

import java.util.List;

import org.brainmaker.kiwipredator.TripletExtractor4;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import junit.framework.TestCase;

public class TripletExtractorTest extends TestCase {

	public void testSplitTree() {
		String sent = "Their bodies are covered with feathers and they have wings.";
		LexicalizedParser lp = LexicalizedParser.loadModel();
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		Tree sen = lp.apply(sent);
		TripletExtractor4 te = new TripletExtractor4();
		List<Tree> sens = te.splitTree(sen, "S");
		assertEquals(2, sens.size());
	}

}
