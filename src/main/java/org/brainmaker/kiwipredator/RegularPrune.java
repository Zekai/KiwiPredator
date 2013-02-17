package org.brainmaker.kiwipredator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.trees.Tree;

public class RegularPrune {
	
	
	//rule format
	//String parentOfPOS, String... childOfPOS, enum childrenType con/dis
	//S, NP, con, VP, con : parent is S, has NP, VP as children.
	//X,X, dis,CC, con,X, dis: children has same type with parents, and a CC
	
	public Tree prune(final Tree tree,RegularFilter rf){
		Tree result = tree.deepCopy();
		if(result.value().equals(rf.getRootPOS())||rf.getRootPOS()==null)
		{
			Integer num = rf.getDepth1Num();
			Integer numC = result.numChildren();
			
			
			boolean allLeaf = true;
			for(Tree node:result.children()){
				if(node.depth()!=1){
					allLeaf = false;
					break;
					
				}
			}
			if(num>0&&num==numC&&allLeaf){
				return result;//no prune
			}
			
			
			List<String> rules = rf.getChildPOS();
			Map<String,Boolean> bookkeeping = new Hashtable<String,Boolean>();
			for(String r:rules){
				bookkeeping.put(r, false);
			}
			
			
			
			for(int i=numC-1;i>=0;i--){
				Tree node = result.getChild(i);
				String pos = node.value();
				if(!rules.contains(pos)){
					result.removeChild(i);	
				}
				else{
					bookkeeping.put(pos, true);
				}
			}
			
			for(Boolean b:bookkeeping.values()){
				if(!b)
					return null;
			}
			
			
			
			return result;
		}
		else
		{
			return null;
		}
	}
	
	
	
}
