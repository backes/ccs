package de.unisb.cs.depend.ccs_sem.semantics.expressions.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import de.unisb.cs.depend.ccs_sem.commandline.Main;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


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
public class MinimisingTransitionsExpression extends Expression {

    private final int stateNr;
    private List<Transition> transitions;

    private MinimisingTransitionsExpression(int stateNr) {
        super();
        this.stateNr = stateNr;
        this.transitions = new ArrayList<Transition>();
    }

    public static MinimisingTransitionsExpression create(Expression myExpr) {
        final Queue<Partition> partitions = new PriorityQueue<Partition>(128,
            new Comparator<Partition>() {
                public int compare(Partition o1, Partition o2) {
                    if (o1.isNew && !o2.isNew)
                        return -1;
                    if (o2.isNew && !o1.isNew)
                        return 1;
                    return o2.expressions.size() - o1.expressions.size();
                }
            });

        // first, fill the partitions list
        {
            final Map<Expression, ExprWrapper> exprMap = new HashMap<Expression, ExprWrapper>();

            final Set<Expression> seen = new HashSet<Expression>();
            seen.add(myExpr);

            final Queue<Expression> queue = new LinkedList<Expression>();
            queue.add(myExpr);

            while (!queue.isEmpty()) {
                final Expression expr = queue.poll();
                exprMap.put(expr, new ExprWrapper(expr, null));

                assert expr.isEvaluated();
                for (final Transition trans: expr.getTransitions()) {
                    final Expression succ = trans.getTarget();
                    if (seen.add(succ))
                        queue.add(succ);
                }
            }

            // now, add all transitions to the expression wrappers
            final Map<Transition, TransWrapper> transMap = new HashMap<Transition, TransWrapper>();
            for (final Entry<Expression, ExprWrapper> entry: exprMap.entrySet()) {
                final List<TransWrapper> newTransitions = new ArrayList<TransWrapper>();
                for (final Transition trans: entry.getKey().getTransitions()) {
                    TransWrapper tw = transMap.get(trans);
                    if (tw == null)
                        transMap.put(trans, tw = new TransWrapper(trans.getAction(),
                            exprMap.get(trans.getTarget())));
                    newTransitions.add(tw);
                }
                entry.getValue().transitions = newTransitions;
            }

            final Partition partition = new Partition(new ArrayList<ExprWrapper>(exprMap.values()));
            partitions.add(partition);
        }


        // now, divide the partitions into new partitions
        final Queue<Partition> readyPartitions = new LinkedList<Partition>();
        final Queue<Partition> unChangedPartitions = new LinkedList<Partition>();
        Partition partition;
        int changed = 0;
        int i = 0;
        while ((partition = partitions.poll()) != null) {
            if (++i % 10000 == 0)
                Main.log(i + ": Ready: " + readyPartitions.size() + "; unchanged: " + unChangedPartitions.size() + "; changed: " + changed + "; queue: " + partitions.size());
            if (partition.expressions.size() < 2) {
                readyPartitions.add(partition);
            } else if (partition.divide(partitions)) {
                if (++changed * 10 >= unChangedPartitions.size()) {
                    changed = 0;
                    partitions.addAll(unChangedPartitions);
                    unChangedPartitions.clear();
                }
            } else {
                unChangedPartitions.add(partition);
            }
        }
        readyPartitions.addAll(unChangedPartitions);
        readyPartitions.addAll(partitions);


        // create the new Expressions
        int nextStateNr = 0;
        MinimisingTransitionsExpression startingExpression = null;
        final Map<Partition, MinimisingTransitionsExpression> newExpressions =
            new HashMap<Partition, MinimisingTransitionsExpression>();
        for (final Partition part: readyPartitions) {
            final MinimisingTransitionsExpression newExpr =
                new MinimisingTransitionsExpression(nextStateNr++);
            newExpressions.put(part, newExpr);
        }
        for (final Partition part: readyPartitions) {
            final Set<Transition> transitions = new HashSet<Transition>();
            MinimisingTransitionsExpression myGroup = null;
            for (final ExprWrapper e: part.getExprWrappers()) {
                if (myGroup == null)
                    myGroup = newExpressions.get(e.part);
                if (e.expr.equals(myExpr))
                    startingExpression = myGroup;
                for (final TransWrapper trans: e.transitions) {
                    final Expression target = newExpressions.get(trans.target.part);
                    if (trans.act instanceof TauAction && myGroup.equals(target))
                        continue;
                    transitions.add(new Transition(trans.act, target));
                }
            }
            assert myGroup != null;
            myGroup.setTransitions(new ArrayList<Transition>(transitions));
        }

        return startingExpression;
    }

    @Override
    protected List<Transition> evaluate0() {
        return transitions;
    }

    public void setTransitions(ArrayList<Transition> transitions) {
        this.transitions = transitions;
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
    public Expression replaceRecursion(List<Declaration> declarations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.valueOf(stateNr);
    }

    @Override
    protected int hashCode0() {
        return stateNr;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MinimisingTransitionsExpression other = (MinimisingTransitionsExpression) obj;
        if (stateNr != other.stateNr)
            return false;
        if (!transitions.equals(other.transitions))
            return false;
        return true;
    }

    private static class Partition {
        final List<ExprWrapper> expressions;
        // TODO remove lastSearchPos
        Iterator<TransWrapper> lastSearchPos = null;
        boolean isNew = true;
        Set<TransWrapper> transitions;

        public Partition(List<ExprWrapper> expressions) {
            this.expressions = expressions;
            for (final ExprWrapper ew: expressions)
                ew.part = this;
            this.transitions = null; //computeTransitions();
        }

        private Set<TransWrapper> computeTransitions() {
            final Set<TransWrapper> transitions = new HashSet<TransWrapper>();
            for (final ExprWrapper ew: expressions)
                transitions.addAll(ew.transitions);
            return transitions;
        }

        public List<ExprWrapper> getExprWrappers() {
            return expressions;
        }

        public boolean divide(Queue<Partition> partitions) {
            isNew = false;

            boolean began = false;
            if (lastSearchPos == null || !lastSearchPos.hasNext()) {
                began = true;
                transitions = computeTransitions();
                lastSearchPos = transitions.iterator();
            }
            while (true) {
                while (lastSearchPos.hasNext()) {
                    final TransWrapper trans = lastSearchPos.next();
                    List<ExprWrapper> fulfills = null;
                    List<ExprWrapper> fulfillsNot = null;
                    for (final ExprWrapper otherExpr: expressions) {
                        if (fulfills(otherExpr, trans)) {
                            if (fulfills != null)
                                fulfills.add(otherExpr);
                        } else {
                            if (fulfills == null) {
                                fulfills = new ArrayList<ExprWrapper>();
                                fulfillsNot = new ArrayList<ExprWrapper>();
                                for (final ExprWrapper e2: expressions) {
                                    if (e2.equals(otherExpr))
                                        break;
                                    fulfills.add(e2);
                                }
                            }
                            fulfillsNot.add(otherExpr);
                        }
                    }
                    if (fulfills != null) {
                        final Partition part1 = new Partition(fulfills);
                        final Partition part2 = new Partition(fulfillsNot);
                        partitions.add(part1);
                        partitions.add(part2);
                        return true;
                    }
                }
                if (began)
                    break;
                lastSearchPos = transitions.iterator();
                began = true;
            }
            return false;
        }

        private static boolean fulfills(ExprWrapper otherExpr, TransWrapper trans) {
            if (trans.act instanceof TauAction) {
                final Queue<ExprWrapper> tauReachable = new LinkedList<ExprWrapper>();
                tauReachable.add(otherExpr);
                final Set<ExprWrapper> seen = new HashSet<ExprWrapper>();
                seen.add(otherExpr);
                boolean ok = false;
                ExprWrapper e;
                while ((e = tauReachable.poll()) != null) {
                    if (e.part.equals(trans.target.part)) {
                        ok = true;
                        break;
                    }
                    for (final TransWrapper t: e.transitions)
                        if (t.act instanceof TauAction && seen.add(t.target))
                            tauReachable.add(t.target);
                }
                return ok;
            } else {
                final Queue<ExprWrapper> tauReachable = new LinkedList<ExprWrapper>();
                tauReachable.add(otherExpr);
                final Set<ExprWrapper> seen = new HashSet<ExprWrapper>();
                seen.add(otherExpr);
                boolean ok = false;
                ExprWrapper e;
                while ((e = tauReachable.poll()) != null) {
                    for (final TransWrapper t: e.transitions) {
                        if (t.act instanceof TauAction) {
                            if (seen.add(t.target))
                                tauReachable.add(t.target);
                        } else {
                            if (t.act.equals(trans.act)) {
                                // now check all tau-reachable successor states for a match
                                final Queue<ExprWrapper> reachableAfter = new LinkedList<ExprWrapper>();
                                reachableAfter.add(t.target);
                                final Set<ExprWrapper> seenAfter = new HashSet<ExprWrapper>();
                                seenAfter.add(otherExpr);
                                ExprWrapper e2;
                                while ((e2 = reachableAfter.poll()) != null) {
                                    if (e2.part.equals(trans.target.part)) {
                                        ok = true;
                                        break;
                                    }
                                    for (final TransWrapper t2: e.transitions) {
                                        if (t2.act instanceof TauAction) {
                                            if (seenAfter.add(t2.target))
                                                reachableAfter.add(t2.target);
                                        }
                                    }
                                }
                                if (ok)
                                    break;
                            }
                        }
                    }
                }
                return ok;
            }
        }

    }

    private static class ExprWrapper {
        public Expression expr;
        public Partition part;
        public List<TransWrapper> transitions;

        public ExprWrapper(Expression e, Partition partition) {
            this.expr = e;
            this.part = partition;
            this.transitions = null;
        }
        
        // no need for hashCode or equals because these objects are unique
        // for an expression

    }

    private static class TransWrapper {
        public Action act;
        public ExprWrapper target;

        public TransWrapper(Action act, ExprWrapper target) {
            this.act = act;
            this.target = target;
        }

        // no need for hashCode or equals because these objects are unique
        // for any combination of action and expression

    }

}


