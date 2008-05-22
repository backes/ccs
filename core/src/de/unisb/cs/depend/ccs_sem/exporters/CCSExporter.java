package de.unisb.cs.depend.ccs_sem.exporters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;

import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ChoiceExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ErrorExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ExpressionRepository;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.PrefixExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.StopExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.UnknownRecursiveExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterList;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.utils.Globals;
import de.unisb.cs.depend.ccs_sem.utils.UniqueQueue;

public class CCSExporter implements Exporter {

    private final String procVarPrefix;

    public CCSExporter(String procVarPrefix) {
        super();
        this.procVarPrefix = procVarPrefix;
    }

    public void export(File dotFile, Program program) throws ExportException {
        final Program newProgram = generateNewCCSProgram(
            program.getExpression(), procVarPrefix);

        Writer writer = null;
        try {
            writer = new FileWriter(dotFile);
            writer.write(newProgram.toString());
            writer.write(Globals.getNewline());
        } catch (final IOException e) {
            throw new ExportException("Error writing to .ccs-File: "
                    + e.getMessage(), e);
        } finally {
            // close the ccs file
            try {
                if (writer != null)
                    writer.close();
            } catch (final IOException e) {
                // ignore
            }
        }

    }

    public static Program generateNewCCSProgram(Expression expression,
            String procVarPrefix) throws ExportException {
        // 1. step: analyse how often each expression is used. expressions
        // used more than once get a process variable.
        // an expression sets "FALSE" in this map when it is visited first and
        // "TRUE" when it is visited the second time.
        final Map<Expression, Boolean> needsProcVar = new HashMap<Expression, Boolean>();

        Queue<Expression> expressionQueue = new UniqueQueue<Expression>();
        expressionQueue.add(expression);
        while (!expressionQueue.isEmpty()) {
            final Expression e = expressionQueue.poll();
            final Boolean b = needsProcVar.get(e);
            if (b == null) {
                needsProcVar.put(e, Boolean.FALSE);
                for (final Transition trans: e.getTransitions()) {
                    final Expression target = trans.getTarget();
                    if (!expressionQueue.add(target))
                        // we have seen it the second time: it needs a process variable
                        // BUT: we don't want to add process variables for too simple expressions
                        // (mainly expressions with just one outgoing transition, leading to 0)
                        if (!tooSimpleForProcessVariable(target))
                            needsProcVar.put(target, Boolean.TRUE);
                }
            }
        }
        expressionQueue = null;

        // 2. step: create process variable names for all expressions occuring
        // more than once
        final Map<Expression, String> procVarNames = new HashMap<Expression, String>();
        int nextNum = 0;
        for (final Entry<Expression, Boolean> entry : needsProcVar.entrySet())
            if (entry.getValue())
                procVarNames.put(entry.getKey(), procVarPrefix + nextNum++);

        // 3. step: create the process variables and the necessary expressions
        final Map<Expression, Expression> newExpressions = new HashMap<Expression, Expression>();
        final Map<Expression, ProcessVariable> procVars = new HashMap<Expression, ProcessVariable>();
        for (final Entry<Expression, String> entry : procVarNames.entrySet()) {
            procVars.put(entry.getKey(), new ProcessVariable(entry.getValue(),
                    new ParameterList(0), createExpression(entry.getKey(),
                            procVarNames, newExpressions, true)));
        }

        // 4. step: create all other expressions
        // (we reuse the first map here, as it contains all occuring expressions)
        for (final Expression e: needsProcVar.keySet())
            if (!newExpressions.containsKey(e))
                newExpressions.put(e, createExpression(e, procVarNames, newExpressions, false));

        // 5. step: create the program, consisting of the process variables and
        // then main expression
        final Program program;
        try {
            program = new Program(new ArrayList<ProcessVariable>(procVars
                    .values()), newExpressions.get(expression));
        } catch (final ParseException e) {
            throw new ExportException(
                    "Internal error while creating the new ccs program", e);
        }

        // return the newly created program
        return program;
    }

    private static boolean tooSimpleForProcessVariable(Expression expr) {
        final List<Transition> trans = expr.getTransitions();
        return trans.size() == 0 || (trans.size() == 1
                &&  trans.get(0).getTarget().getTransitions().size() == 0);
    }

    private static Expression createExpression(Expression expr,
            Map<Expression, String> procVarNames,
            Map<Expression, Expression> newExpressions, boolean noReference) {
        assert expr.isEvaluated();
        Expression newExpr = newExpressions.get(expr);
        if (newExpr != null)
            return newExpr;

        if (expr.isError())
            return ErrorExpression.get();

        final String procVarName = procVarNames.get(expr);
        if (procVarName != null && !noReference)
            return new UnknownRecursiveExpression(procVarName);

        for (final Transition trans: expr.getTransitions()) {
            final Expression newChoicePart = ExpressionRepository.getExpression(
                new PrefixExpression(
                    trans.getAction(), createExpression(trans.getTarget(),
                            procVarNames, newExpressions, false)));
            newExpr = newExpr == null ? newChoicePart :
                    ChoiceExpression.create(newExpr, newChoicePart);
        }

        return newExpr == null ? StopExpression.get() : newExpr;
    }

    public String getIdentifier() {
        return "ccs File export";
    }

}
