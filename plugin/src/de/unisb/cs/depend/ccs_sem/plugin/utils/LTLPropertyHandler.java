package de.unisb.cs.depend.ccs_sem.plugin.utils;

import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;

import java.util.HashMap;
import java.util.LinkedList;

import ltlcheck.Counterexample;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.widgets.List;

public class LTLPropertyHandler {

	private final QualifiedName LTL_NR = new QualifiedName("LTL","ltlNumber");
	
	private IFile file;
	private int anzahl;
	private String lastLoaded = null;
	
	public LTLPropertyHandler(IFile file) {
		this.file = file;
	}
	
	public void loadAll(List list, HashMap<String,Counterexample> checked) {
		try {
			String str = file.getPersistentProperty(LTL_NR);
			anzahl = new Integer( str==null ? "0" : str );
			for(int i=0;i<anzahl;i++) {
				initFormula(i,list,checked);
			}
		} catch (CoreException e) {} // ignore
	}
	
	private void initFormula(int nr,List list,HashMap<String,Counterexample> checked) throws CoreException {
		Node dummyNode = new Node(new Graph());
		
		String formula = file.getPersistentProperty(
				new QualifiedName("LTL","formula"+nr));
		if( formula == null ) return;
		if( list != null ) list.add(formula);
		lastLoaded = formula; // cache for other methods
		
		String temp = file.getPersistentProperty(
				new QualifiedName("LTL","formula"+nr+"tested"));
		boolean tested = new Boolean(temp == null ? "false" : temp);
		
		if(tested) {
			Counterexample ce = null;
			temp = file.getPersistentProperty(
					new QualifiedName("LTL","formula"+nr+"testPassed") );
			if( temp == null )
				return; // inconclusive -> neither passed nor failed
			boolean passedTest = new Boolean(temp);
			
			if(!passedTest) {
				// get counterexample
				String cePrefix = file.getPersistentProperty(
						new QualifiedName("LTL","formula"+nr+"cePrefix" )
					);
				String ceCycle = file.getPersistentProperty(
						new QualifiedName("LTL","formula"+nr+"ceCycle" )
					);
				LinkedList<Edge> pref = new LinkedList<Edge> (),
					cycle = new LinkedList<Edge> ();
				
				for( String str : cePrefix.split(",") ) {
					pref.add(new Edge(dummyNode,dummyNode,str));
				}
				for( String str : ceCycle.split(",") ) {
					cycle.add(new Edge(dummyNode,dummyNode,str));
				}
				ce = new Counterexample(pref, cycle);
			}
			
			checked.put(formula, ce);
		}
	}
	
	public void remove(int formulanr) {
		for(int i=formulanr; i<anzahl-1; i++) {
			swap(i,i+1);
		}
		try {
			anzahl--;
			file.setPersistentProperty(LTL_NR, ""+anzahl);
		} catch (CoreException e) {}
	}
	
	private void swap(int i1, int i2) {
		String formula1, formula2;
		Counterexample ce1=null, ce2=null;
		boolean tested1, tested2;
		HashMap<String,Counterexample> getter = new HashMap<String,Counterexample> ();
		try {
			// load booth formula
			initFormula(i1, null, getter);
			formula1 = lastLoaded;
			tested1 = getter.containsKey(formula1);
			if( tested1 ) {
				ce1 = getter.get(formula1);
			}
			getter.clear();
			
			initFormula(i2, null, getter);
			formula2 = lastLoaded;
			tested2 = getter.containsKey(formula2);
			if( tested2 ) {
				ce2 = getter.get(formula2);
			}
			getter.clear();
			
			// swap strings
			file.setPersistentProperty(
					new QualifiedName("LTL", "formula"+i1), formula2);
			file.setPersistentProperty(
					new QualifiedName("LTL", "formula"+i2), formula1);
			
			// save ce's
			if( tested1 ) {
				save(i2,ce1);
			} else {
				removeResult(i2);
			}
			if( tested2 ) {
				save(i1, ce2);
			} else {
				removeResult(i1);
			}
		} catch (CoreException e) {}
	}
	
	public void removeAllResults() {
		try {
			for(int i=0; i<anzahl; i++) {
				removeResult(i);
			}
		} catch (CoreException e) {}
	}
	
	private void removeResult(int index) throws CoreException {
		file.setPersistentProperty(
				new QualifiedName("LTL","formula"+index+"tested"),
				"false");
	}
	
	public void add(String formula) {
		if(formula==null) return;
		try{
			file.setPersistentProperty(LTL_NR, ""+(anzahl+1));
			file.setPersistentProperty(new QualifiedName("LTL","formula"+anzahl),
				formula);
		} catch (CoreException e) {}
		anzahl++;
	}
	
	public void save(int index, Counterexample ce) {
		try {
			file.setPersistentProperty(
				new QualifiedName("LTL","formula"+index+"tested"),
				"true");
		
			if( ce == null ) {
				file.setPersistentProperty(
					new QualifiedName("LTL","formula"+index+"testPassed"),
					"true");
			} else {
				file.setPersistentProperty(
					new QualifiedName("LTL","formula"+index+"testPassed"),
					"false");
				StringBuilder strB = new StringBuilder();
				java.util.List<Edge> edges = ce.getPrefix();
				for(int i=0;i<edges.size(); i++) {
					strB.append(
						edges.get(i).getGuard() +
						(i==edges.size()-1 ? "" : ",")
						);
				}
				file.setPersistentProperty(
					new QualifiedName("LTL","formula"+index+"cePrefix"), 
					strB.toString());
				edges = ce.getCycle();
				for(int i=0;i<edges.size(); i++) {
					strB.append(
						edges.get(i).getGuard() +
						(i==edges.size()-1 ? "" : ",")
						);
				}
				file.setPersistentProperty(
					new QualifiedName("LTL","formula"+index+"ceCycle"), 
					strB.toString());
			}
		} catch (CoreException e) {}
	}
}
