package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

// TODO
public class CCSDoubleClickStrategy implements ITextDoubleClickStrategy {
    protected ITextViewer fText;

    public void doubleClicked(ITextViewer part) {
        final int pos = part.getSelectedRange().x;

        if (pos < 0)
            return;

        fText = part;

        if (!selectComment(pos)) {
            selectWord(pos);
        }
    }

    protected boolean selectComment(int caretPos) {
        final IDocument doc = fText.getDocument();
        int startPos, endPos;

        try {
            int pos = caretPos;
            char c = ' ';

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (c == '\\') {
                    pos -= 2;
                    continue;
                }
                if (c == Character.LINE_SEPARATOR || c == '\"')
                    break;
                --pos;
            }

            if (c != '\"')
                return false;

            startPos = pos;

            pos = caretPos;
            final int length = doc.getLength();
            c = ' ';

            while (pos < length) {
                c = doc.getChar(pos);
                if (c == Character.LINE_SEPARATOR || c == '\"')
                    break;
                ++pos;
            }
            if (c != '\"')
                return false;

            endPos = pos;

            final int offset = startPos + 1;
            final int len = endPos - offset;
            fText.setSelectedRange(offset, len);
            return true;
        } catch (final BadLocationException x) {
            // ignore
        }

        return false;
    }
    protected boolean selectWord(int caretPos) {

        final IDocument doc = fText.getDocument();
        int startPos, endPos;

        try {

            int pos = caretPos;
            char c;

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c))
                    break;
                --pos;
            }

            startPos = pos;

            pos = caretPos;
            final int length = doc.getLength();

            while (pos < length) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c))
                    break;
                ++pos;
            }

            endPos = pos;
            selectRange(startPos, endPos);
            return true;

        } catch (final BadLocationException x) {
            // ignore
        }

        return false;
    }

    private void selectRange(int startPos, int stopPos) {
        final int offset = startPos + 1;
        final int length = stopPos - offset;
        fText.setSelectedRange(offset, length);
    }

}
