package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;


public class HistoryPushbackReader extends PushbackReader {

    private int[] history;
    private int histSize;
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
        int read = super.read();
        history[histPos = (histPos+1)%history.length] = read;
        return read;
    }
    
    @Override
    public void unread(int c) throws IOException {
        super.unread(c);
        histPos = (histPos-1)%history.length;
    }
    
    public String getHistory(int maxSize) {
        char[] chars = new char[history.length];
        int offset = history.length;
        maxSize = Math.min(maxSize, histSize);
        for (int i = 0; i < maxSize; ++i) {
            int c = history[(histPos-i)%history.length];
            if (c == -1)
                continue;
            chars[--offset] = (char)c;
        }
        String str = new String(chars, offset, history.length - offset);
        return str;
    }

}
