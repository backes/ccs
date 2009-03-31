package de.unisb.cs.depend.ccs_sem.utils;

import java.util.HashSet;
import java.util.Iterator;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpression;

public class TopLevelParallelismChecker {

	private HashSet<Expression> alreadyChecked;
	
	/**
	 * Checks if an expression is of the form: (P1 | P2 | ... | Pn)\R with
	 * Pi has no more |-operator in it for all i.
	 * @param exp - the expression
	 * @return true, if it has the form; false otherwise
	 */
	public boolean checkSyntax(Expression exp) {
		if(exp instanceof RestrictExpression) {
			Iterator<Expression> it = exp.getChildren().iterator();
			exp = it.next();
		}
		
		if( !(exp instanceof ParallelExpression) ) {
			return checkNoMoreParallelism(exp);
		} else {
			for( Expression e : exp.getChildren() ) {
				if( !checkSyntax(e) )
					return false;
			}
			return true;
		}
	}
	
	private boolean checkNoMoreParallelism(Expression exp) {
		if(alreadyChecked.contains(exp)) return true;
		
		if( exp instanceof ParallelExpression) {
			return false;
		} 
		
		alreadyChecked.add(exp);
		
		for(Expression e : exp.getChildren() ) {
			if( !checkNoMoreParallelism(e) )
				return false;
		}
		
		return true;
	}
	
	private TopLevelParallelismChecker() {
		alreadyChecked = new HashSet<Expression> ();
	}
	
	private static TopLevelParallelismChecker singleton = null;
	
	public static TopLevelParallelismChecker getChecker() {
		if( singleton == null ) {
			singleton = new TopLevelParallelismChecker();
		}
		
		return singleton;
	}
}
