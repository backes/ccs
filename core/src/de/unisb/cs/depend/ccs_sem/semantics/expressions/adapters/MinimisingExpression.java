package de.unisb.cs.depend.ccs_sem.semantics.expressions.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.Bisimulation;
import de.unisb.cs.depend.ccs_sem.utils.UniqueQueue;
import de.unisb.cs.depend.ccs_sem.utils.Bisimulation.Partition;
import de.unisb.cs.depend.ccs_sem.utils.Bisimulation.TransitionToPartition;


/**
 * This is an adapter for an expression that minimizes all outgoing transitions
 * by building a partition of the states according to weak bisimulation.
 *
 * It uses my very own algorithm to compute the quotient of the LTS w.r.t
 * weak bisimulation (i.e. it computes the smalles weak bisimilar LTS).
 * There are a lot of strange optimisations to get a very fast runtime.
 *
 * @author Clemens Hammacher
 */
public class MinimisingExpression extends Expression {

    private final int stateNo;
    private List<Transition> transitions;

    private MinimisingExpression(int stateNo) {
        super();
        this.stateNo = stateNo;
        this.transitions = new ArrayList<Transition>();
    }

    public static MinimisingExpression create(Expression expr, boolean strong)
            throws InterruptedException {
        final Map<Expression, Partition> partitions = Bisimulation.computePartitions(expr, strong);

        // create the new Expressions (in a BFS manner)
        final Queue<Partition> queue = new UniqueQueue<Partition>();
        queue.add(partitions.get(expr));
        int nextStateNo = 0;
        final Map<Partition, MinimisingExpression> newExpressions =
            new HashMap<Partition, MinimisingExpression>();
        final Map<Partition, Set<TransitionToPartition>> partitionTransitions =
            new HashMap<Partition, Set<TransitionToPartition>>();
        Partition part;
        while ((part = queue.poll()) != null) {
            if (part.isError()) {
                assert part.getAllTransitions().isEmpty();
                newExpressions.put(part, new MinimisingExpression(-1));
            } else
                newExpressions.put(part, new MinimisingExpression(nextStateNo++));
            final Set<TransitionToPartition> partTransitions = part.getAllTransitions();
            partitionTransitions.put(part, partTransitions);
            for (final TransitionToPartition trans: partTransitions)
                queue.add(trans.targetPart);
        }
        // now add the transitions
        for (final Entry<Partition, MinimisingExpression> entry: newExpressions.entrySet()) {
            final Set<TransitionToPartition> transitions = partitionTransitions.get(entry.getKey());
            final ArrayList<Transition> newTransitions = new ArrayList<Transition>(transitions.size());
            for (final TransitionToPartition trans: transitions) {
                final Expression target = newExpressions.get(trans.targetPart);
                if (trans.act instanceof TauAction && entry.getValue().equals(target))
                    continue;
                newTransitions.add(new Transition(trans.act, target));
            }
            newTransitions.trimToSize();
            entry.getValue().transitions = newTransitions;
        }

        return newExpressions.get(partitions.get(expr));
    }

    @Override
    protected List<Transition> evaluate0() {
        return transitions;
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        throw new UnsupportedOperationException("An expression cannot be instantiated after minimization.");
    }

    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isError0() {
        return stateNo == -1;
    }

    @Override
    public String toString() {
        return String.valueOf(stateNo);
    }

    @Override
    protected int hashCode0() {
        // TODO fix
        return System.identityHashCode(this);
    }

}
