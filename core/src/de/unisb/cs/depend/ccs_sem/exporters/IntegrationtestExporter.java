package de.unisb.cs.depend.ccs_sem.exporters;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.exporters.helpers.StateNumberComparator;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.utils.Globals;
import de.unisb.cs.depend.ccs_sem.utils.StateNumerator;


public class IntegrationtestExporter implements Exporter {

    private static String extractClassName(File javaFilename) {
        final String filename = javaFilename.getName();
        if (filename.length() == 0)
            return randomClassName();

        final int dotIndex = filename.indexOf('.');
        if (dotIndex != -1) {
            if (dotIndex == 0)
                return randomClassName();
            final char first = Character.toUpperCase(filename.charAt(0));
            return first + removeIllegalCharacters(filename.substring(1, dotIndex));
        }

        return removeIllegalCharacters(filename);
    }

    private static String removeIllegalCharacters(String filename) {
        final char[] chars = filename.toCharArray();
        for (int i = 0; i < chars.length; ++i)
            if (i==0 ? !Character.isJavaIdentifierStart(chars[i])
                     : !Character.isJavaIdentifierPart(chars[i]))
                chars[i] = '_';

        return new String(chars);
    }

    private static String randomClassName() {
        return "Unnamed_" + (new Random().nextInt(1000000));
    }

    public void export(File javaFile, Program program) throws ExportException {
        final PrintWriter javaWriter;
        try {
            javaWriter = new PrintWriter(javaFile);
        } catch (final IOException e) {
            throw new ExportException("Error opening output file: "
                    + e.getMessage(), e);
        }

        final Expression expr = program.getExpression();

        final Map<Expression, Integer> stateNumbers =
                StateNumerator.numerateStates(expr);

        // write header
        final String className = extractClassName(javaFile);
        javaWriter.println("package de.unisb.cs.depend.ccs_sem.junit.integrationtests;");
        javaWriter.println();
        javaWriter.println("import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;");
        javaWriter.println();
        javaWriter.println();
        javaWriter.println("/*");
        javaWriter.println("The CCS program:");
        javaWriter.println();
        javaWriter.println(program.toString(true));
        javaWriter.println("*/");
        javaWriter.println();
        javaWriter.println("public class " + className + " extends IntegrationTest {");
        javaWriter.println();
        javaWriter.println("    @Override");
        javaWriter.println("    protected String getExpressionString() {");
        javaWriter.println("        return " + encode0(program.toString(true)) + ";");
        javaWriter.println("    }");
        javaWriter.println();
        javaWriter.println("    @Override");
        javaWriter.println("    protected boolean isMinimize() {");
        javaWriter.println("        return " + program.isMinimized() + ";");
        javaWriter.println("    }");
        javaWriter.println();
        javaWriter.println("    @Override");
        javaWriter.println("    protected void addStates() {");

        // write the states
        final PriorityQueue<Expression> queue =
                new PriorityQueue<Expression>(11, new StateNumberComparator(
                        stateNumbers));
        queue.add(expr);

        final Set<Expression> written = new HashSet<Expression>(stateNumbers.size()*3/2);
        written.add(expr);

        int stateCnt = 0;
        int methodCnt = 0;
        while (!queue.isEmpty()) {
            final Expression e = queue.poll();
            String stateString = e.toString();
            if (e.isError() && !"ERROR".equals(stateString))
                stateString = "error_" + stateString;
            javaWriter.println("        addState(" + encode0(stateString) + ");");
            if (++stateCnt % 5000 == 0) {
                javaWriter.println("        addStates" + methodCnt + "();");
                javaWriter.println("    }");
                javaWriter.println();
                javaWriter.println("    protected void addStates" + methodCnt++ + "() {");
            }

            for (final Transition trans: e.getTransitions()) {
                final Expression targetExpr = trans.getTarget();
                if (written.add(targetExpr))
                    queue.add(targetExpr);
            }
        }

        javaWriter.println("    }");
        javaWriter.println();
        javaWriter.println("    @Override");
        javaWriter.println("    protected void addTransitions() {");

        // write the transitions
        queue.add(expr);

        written.clear();
        written.add(expr);

        stateCnt = methodCnt = 0;
        while (!queue.isEmpty()) {
            final Expression e = queue.poll();
            final int sourceStateNo = stateNumbers.get(e);

            for (final Transition trans: e.getTransitions()) {
                final Expression targetExpr = trans.getTarget();
                final int targetStateNo = stateNumbers.get(targetExpr);
                javaWriter.println("        addTransition(" + sourceStateNo + ", "
                    + targetStateNo + ", " + encode0(trans.getAction().getLabel()) + ");");
                if (++stateCnt % 5000 == 0) {
                    javaWriter.println("        addTransitions" + methodCnt + "();");
                    javaWriter.println("    }");
                    javaWriter.println();
                    javaWriter.println("    protected void addTransitions" + methodCnt++ + "() {");
                }
                if (written.add(targetExpr))
                    queue.add(targetExpr);
            }
        }

        javaWriter.println("    }");
        javaWriter.println("}");

        // close the file
        javaWriter.close();

    }

    private String encode0(String str) {
        final StringBuilder sb = new StringBuilder(str.length() * 3 / 2);

        boolean needsDecode = false;
        for (final char c: str.toCharArray())
            switch (c) {
            // lists all valid characters (better too little than too many)
            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g':
            case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n':
            case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u':
            case 'v': case 'w': case 'x': case 'y': case 'z': case 'A': case 'B':
            case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I':
            case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P':
            case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W':
            case 'X': case 'Y': case 'Z': case '0': case '1': case '2': case '3':
            case '4': case '5': case '6': case '7': case '8': case '9': case '.':
            case ' ': case ':': case '{': case '}': case '_': case '!': case '?':
            case '=': case ',': case '[': case ']': case '|': case ';': case '+':
            case '-': case '/': case '*': case '(': case ')': case '>': case '<':
                sb.append(c);
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '\r':
                //sb.append("\\r");
                break;
            case '\n':
                sb.append("\\n\"").append(Globals.getNewline()).append("            + \"");
                break;
            default:
                needsDecode = true;
                sb.append('%').append((int)c);
                break;
            }

        if (needsDecode)
            return "decode(\"" + sb.toString() + "\")";
        else
            return '"' + sb.toString() + '"';
    }

    public String getIdentifier() {
        return "Integration test export";
    }

}
