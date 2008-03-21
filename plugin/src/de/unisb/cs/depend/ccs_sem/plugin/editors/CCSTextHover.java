package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.UnknownRecursiveExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.TauChannel;


public class CCSTextHover implements ITextHover {


    public class MyRegion implements IRegion {

        public Token token;
        public ParseStatus parseStatus;
        public ParsingProblem problem;

        public MyRegion(Token token, ParseStatus parseStatus) {
            this.token = token;
            this.parseStatus = parseStatus;
        }

        public MyRegion(ParsingProblem problem) {
            this.problem = problem;
        }

        public int getLength() {
            if (problem != null)
                return problem.getEndPosition() - problem.getStartPosition() + 1;
            return token.getLength();
        }

        public int getOffset() {
            return problem != null ? problem.getStartPosition() : token.getStartPosition();
        }

    }

    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        if (hoverRegion instanceof MyRegion) {
            final MyRegion myRegion = (MyRegion)hoverRegion;
            final ParseStatus parseStatus = myRegion.parseStatus;
            final ParsingResult result = parseStatus == null ? null : parseStatus.getParsingResult();

            if (myRegion.problem != null) {
                return myRegion.problem.getMessage();
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
                final ParsingProblem problem = parsingResult == null ? null : binarySearchProblemOnOffset(parsingResult.parsingProblems, offset);
                if (problem != null)
                    return new MyRegion(problem);
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

    private ParsingProblem binarySearchProblemOnOffset(
            List<ParsingProblem> problems, int offset) {
        int left = 0;
        int right = problems.size();
        while (left < right) {
            final int index = (left + right)/2;
            final ParsingProblem cur = problems.get(index);
            if (cur.getStartPosition() > offset) {
                right = index;
            } else if (cur.getEndPosition() < offset) {
                left = index+1;
            } else {
                // TODO handle multiple overlapping problems
                return cur;
            }
        }
        return null;
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
