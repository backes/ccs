package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;


public class HistoryPushbackReader extends PushbackReader {

    private final int[] history;
    private final int histSize;
    private int histPos;

    public HistoryPushbackReader(Reader in, int size, int histSize) {
        super(in, size);
        this.histSize = histSize;
        history = new int[histSize + size];
        for (int i = 0; i < histSize + size; ++i)
            history[i] = -1;
        this.histPos = 0;
    }

    public HistoryPushbackReader(Reader in, int histSize) {
        this(in, 1, histSize);
    }

    @Override
    public int read() throws IOException {
        final int read = super.read();
        histPos = histPos == history.length-1 ? 0 : histPos+1;
        history[histPos] = read;
        return read;
    }

    @Override
    public void unread(int c) throws IOException {
        super.unread(c);
        histPos = histPos == 0 ? history.length - 1 : histPos-1;
    }

    public String getHistory(int maxSize) {
        maxSize = Math.min(maxSize, histSize);
        final char[] chars = new char[maxSize];
        int offset = maxSize;
        for (int i = 0; i < maxSize; ++i) {
            int c = history[(histPos+history.length-i)%history.length];
            if (c == '\n' || c == '\r')
                c = ' ';
            else if (c == -1)
                break;
            chars[--offset] = (char)c;
        }
        final String str = new String(chars, offset, maxSize - offset);
        return str;
    }

}
