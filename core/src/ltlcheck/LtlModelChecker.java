package ltlcheck;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.trans.LTL2Buchi;
import gov.nasa.ltl.trans.ParseErrorException;

public class LtlModelChecker
{
	public static Counterexample check(Graph ts, String formula,
			IModelCheckingMonitor monitor) throws ParseErrorException
	{
		assert monitor != null;
		
		// Generate Did/Can Expanded Graph
		monitor.subTask("Adding did/can attributes...");
		final Graph dcts = DidCanTranslator.translate(ts);
				
		// Remove deadlock
		monitor.subTask("Massaging deadlock states...");
		GraphTransformations.removeDeadlock(dcts);
		
		// Generate Buchi Automata for negated LTL formula
		monitor.subTask("Generate Buchi automaton...");
		Graph ba = null;
		ba = LTL2Buchi.translate("! (" + formula + ")");
		
		monitor.subTask("Parsing transition labels...");
		GraphActionParser.parseTransitions(ba);

		// Generate Product Automata of Did/Can Expanded Graph and Buchi Automata of LTL formula
		monitor.subTask("Generate product automaton...");
		final GeneralGraph pa = ProductTranslator.translate(dcts, ba);

		// Check Property via reachable cycle detection
		monitor.subTask("Checking property...");
		final PersistenceChecker pc = new PersistenceChecker(pa);
		pc.run();

		return pc.getCounterexample();
	}
}
