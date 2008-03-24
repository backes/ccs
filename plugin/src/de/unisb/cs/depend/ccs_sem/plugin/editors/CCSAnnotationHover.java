package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.utils.Globals;

public class CCSAnnotationHover implements IAnnotationHover {

    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
        final IDocument doc = sourceViewer.getDocument();
        if (!(doc instanceof CCSDocument))
            return null;
        final ParseStatus lastResult = ((CCSDocument)doc).getLastParseResult();
        if (lastResult == null)
            return null;
        final ParsingResult parsingResult = lastResult.getParsingResult();
        if (parsingResult == null || parsingResult.parsingProblems == null)
            return null;
        IRegion lineRegion;
        try {
            lineRegion = doc.getLineInformation(lineNumber);
        } catch (BadLocationException e) {
            return null;
        }
        final List<ParsingProblem> foundProblems = searchProblemsOnLine(parsingResult.parsingProblems, lineRegion);
        if (foundProblems == null || foundProblems.size() == 0)
            return null;
        if (foundProblems.size() == 1)
            return foundProblems.get(0).getMessage();
        StringBuilder sb = new StringBuilder();
        for (ParsingProblem prob: foundProblems) {
            if (sb.length() > 0)
                sb.append(Globals.getNewline());
            sb.append("- ").append(prob.getMessage());
        }
        return sb.toString();
    }

    private List<ParsingProblem> searchProblemsOnLine(
            List<ParsingProblem> problems, IRegion lineRegion) {
        List<ParsingProblem> foundProblems = new ArrayList<ParsingProblem>(1);
        for (ParsingProblem problem: problems) {
            // the problems are ordered by their startPosition:
            if (problem.getStartPosition() > lineRegion.getOffset() + lineRegion.getLength())
                break;
            else if (problem.getEndPosition() >= lineRegion.getOffset())
                foundProblems.add(problem);
        }
        return foundProblems;
    }

}
