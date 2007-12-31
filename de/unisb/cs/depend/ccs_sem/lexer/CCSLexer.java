package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Assignment;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Comma;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Dot;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Parallel;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Plus;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Restrict;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Semicolon;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Stop;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;


public class CCSLexer extends AbstractLexer {

    public List<Token> lex(Reader input) throws LexException {
        List<Token> tokens = new ArrayList<Token>();
        
        PushbackReader pr = new PushbackReader(input);
        
        try {
            lex0(pr, tokens, 0);
        } catch (IOException e) {
            throw new LexException("Error reading input stream", e);
        }
        
        assert !tokens.contains(null);
        
        return tokens;
    }
    
    private void lex0(PushbackReader input, List<Token> tokens, int position) throws IOException {
        int nextChar = input.read();
        if (nextChar == -1)
            return;
        assert nextChar >= 0 && nextChar < 1<<16;
        
        switch (nextChar) {
        case ' ':
        case '\t':
        case '\n':
            break;

        case '0':
            tokens.add(new Stop(position));
            break;
            
        case '.':
            tokens.add(new Dot(position));
            break;
            
        case '+':
            tokens.add(new Plus(position));
            break;
            
        case '|':
            tokens.add(new Parallel(position));
            break;
            
        case '\\':
            tokens.add(new Restrict(position));
            break;
            
        case '(':
            tokens.add(new LParenthesis(position));
            break;
            
        case ')':
            tokens.add(new RParenthesis(position));
            break;
            
        case '[':
            tokens.add(new LBracket(position));
            break;
            
        case ']':
            tokens.add(new RBracket(position));
            break;
            
        case ',':
            tokens.add(new Comma(position));
            break;
            
        case '=':
            tokens.add(new Assignment(position));
            break;
            
        case ';':
            tokens.add(new Semicolon(position));
            break;
            
        default:
            Identifier id = readIdentifier(nextChar, input, tokens, position);
            if (id == null) {
                // TODO Exception
            }
            tokens.add(id);
            position += id.getName().length()-1;
            break;
        }
        
        lex0(input, tokens, position+1);
    }

    private Identifier readIdentifier(int nextChar, PushbackReader input, List<Token> tokens, int position) throws IOException {
        boolean first = true;
        StringBuilder name = new StringBuilder();
        while ((nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z') || (!first && nextChar >= '0' && nextChar <= '9')) {
            assert (char) nextChar == nextChar;
            name.append((char)nextChar);
            nextChar = input.read();
        }
        if (nextChar != -1)
            input.unread(nextChar);

        if (name.length() == 0)
            return null;
        return new Identifier(position, position+name.length()-1, name.toString());
    }

}
