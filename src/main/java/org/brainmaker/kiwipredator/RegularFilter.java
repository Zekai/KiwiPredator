package org.brainmaker.kiwipredator;

import java.util.ArrayList;
import java.util.List;

public class RegularFilter{
	 private String rootPOS;
	 private List<String> children;
	 private Integer numDepth1;
	 
	 public RegularFilter(){
		 children = new ArrayList<String>();
		 numDepth1 = 0;
	 }
	 
	 
	 public void setDepth1Num(Integer num){
		 this.numDepth1 = num;
	 }
	 
	 public Integer getDepth1Num(){
		 return this.numDepth1;
	 }
	 
	 public void setRoot(String pos ){
		 rootPOS = pos;
	 }
	 
	 public void addChildRule(String pos){
		 children.add(pos);
	 }
	 
	 public String getRootPOS(){
		 return rootPOS;
	 }
	 public void reSetRule(){
		 children.clear();
	 }
	 public List<String> getChildPOS(){
		 return children;
	 }
}