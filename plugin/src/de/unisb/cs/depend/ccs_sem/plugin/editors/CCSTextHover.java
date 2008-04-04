package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.False;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.IntegerToken;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.True;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.UnknownRecursiveExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstBooleanValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstIntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstString;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.TauChannel;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


public class CCSTextHover implements ITextHover {


    public class MyRegion implements IRegion {

        public Token token;
        public ParseStatus parseStatus;
        public List<ParsingProblem> problems;
        private int offset;
        private final int length;

        public MyRegion(Token token, ParseStatus parseStatus) {
            this.token = token;
            this.parseStatus = parseStatus;
            offset = token.getStartPosition();
            length = token.getLength();
        }

        public MyRegion(List<ParsingProblem> foundProblems) {
            this.problems = foundProblems;
            assert problems.size() > 0;
            int offset = 0;
            int minEnd = Integer.MAX_VALUE;
            for (final ParsingProblem prob: problems) {
                if (prob.getStartPosition() > offset)
                    offset = prob.getStartPosition();
                if (prob.getEndPosition() < minEnd)
                    minEnd = prob.getEndPosition();
            }
            length = minEnd - offset + 1;
        }

        public int getLength() {
            return length;
        }

        public int getOffset() {
            return offset;
        }

    }

    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        if (hoverRegion instanceof MyRegion) {
            final MyRegion myRegion = (MyRegion)hoverRegion;
            final ParseStatus parseStatus = myRegion.parseStatus;
            final ParsingResult result = parseStatus == null ? null : parseStatus.getParsingResult();

            if (myRegion.problems != null) {
                if (myRegion.problems.size() == 1)
                    return myRegion.problems.get(0).getMessage();
                final StringBuilder sb = new StringBuilder();
                for (final ParsingProblem prob: myRegion.problems) {
                    if (sb.length() > 0)
                        sb.append(Globals.getNewline());
                    sb.append("- ").append(prob.getMessage());
                }
                return sb.toString();
            }

            if (myRegion.token instanceof Identifier && result != null) {
                final Object o = result.identifiers.get(myRegion.token);
                if (o instanceof Parameter) {
                    final Parameter param = (Parameter) o;
                    return "Parameter \"" + param.getName() + "\": " + param.getType();
                } else if (o instanceof ParameterReference) {
                    final ParameterReference param = (ParameterReference) o;
                    return "Reference of Parameter \"" + param.getParam().getName()
                        + "\": " + param.getParam().getType();
                } else if (o instanceof Channel) {
                    return o instanceof TauChannel ? "tau Channel" : "Channel: " + o;
                } else if (o instanceof ConstIntegerValue) {
                    return "Constant integer value \"" + ((ConstantValue)o).getStringValue() + "\"";
                } else if (o instanceof ConstBooleanValue) {
                    return "Constant boolean value \"" + ((ConstantValue)o).getStringValue() + "\"";
                } else if (o instanceof ConstString) {
                    return "Constant string \"" + ((ConstantValue)o).getStringValue() + "\"";
                } else if (o instanceof ConstantValue) {
                    return "Constant value \"" + ((ConstantValue)o).getStringValue() + "\"";
                } else if (o instanceof Range) {
                    return "Range: " + o;
                } else if (o instanceof UnknownRecursiveExpression) {
                    return "Process Variable: " + o;
                } else if (o instanceof ProcessVariable) {
                    return "Definition of process variable " + o;
                } else {
                    // we should not get here
                    assert false;
                }
            } else if (myRegion.token instanceof IntegerToken) {
                return "Integer constant: " + ((IntegerToken)myRegion.token).getValue();
            } else if (myRegion.token instanceof True) {
                return "Boolean constant: true";
            } else if (myRegion.token instanceof False) {
                return "Boolean constant: false";
            }
        }
        return null;
    }

    public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
        final IDocument doc = textViewer.getDocument();
        if (doc instanceof CCSDocument) {
            final ParseStatus lastResult = ((CCSDocument)doc).getLastParseResult();
            if (lastResult != null) {
                final ParsingResult parsingResult = lastResult.getParsingResult();
                final List<ParsingProblem> foundProblems = parsingResult == null ? null : searchProblemsOnOffset(parsingResult.parsingProblems, offset);
                if (foundProblems != null && foundProblems.size() > 0)
                    return new MyRegion(foundProblems);
                final List<Token> tokens = parsingResult == null ? null : parsingResult.tokens;
                if (tokens != null && tokens.size() > 0) {
                    final Token hoveredToken = binarySearchTokenOnOffset(tokens, offset);
                    if (hoveredToken != null)
                        return new MyRegion(hoveredToken, lastResult);
                }
            }
        }
        return new Region(offset, 1);
    }

    private List<ParsingProblem> searchProblemsOnOffset(
            List<ParsingProblem> problems, int offset) {
        final List<ParsingProblem> foundProblems = new ArrayList<ParsingProblem>(1);
        for (final ParsingProblem problem: problems) {
            // the problems are ordered by their startPosition:
            if (problem.getStartPosition() > offset)
                break;
            else if (problem.getEndPosition() >= offset)
                foundProblems.add(problem);
        }
        return foundProblems;
    }

    private Token binarySearchTokenOnOffset(List<Token> tokens, int offset) {
        int left = 0;
        int right = tokens.size();
        while (left < right) {
            final int index = (left + right)/2;
            final Token cur = tokens.get(index);
            if (cur.getStartPosition() > offset) {
                right = index;
            } else if (cur.getEndPosition() < offset) {
                left = index+1;
            } else {
                return cur;
            }
        }
        return null;
    }

}
