package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Assignment;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Choice;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Comma;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Dot;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.IntegerToken;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBrace;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Parallel;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RBrace;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Restrict;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Semicolon;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Stop;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;


public class CCSLexer extends AbstractLexer {

    public List<Token> lex(Reader input) throws LexException {
        final List<Token> tokens = new ArrayList<Token>();

        final PushbackReader pr = new PushbackReader(input);

        try {
            lex0(pr, tokens, 0);
        } catch (final IOException e) {
            throw new LexException("Error reading input stream", e);
        }

        assert !tokens.contains(null);

        return tokens;
    }

    private void lex0(PushbackReader input, List<Token> tokens, int position) throws IOException, LexException {
        int nextChar;

        while ((nextChar = input.read()) != -1) {
            assert nextChar >= 0 && nextChar < 1<<16;

            switch (nextChar) {
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                break;

            case '0':
                tokens.add(new Stop(position));
                break;

            case '.':
                tokens.add(new Dot(position));
                break;

            case '+':
                tokens.add(new Choice(position));
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

            case '{':
                tokens.add(new LBrace(position));
                break;

            case '}':
                tokens.add(new RBrace(position));
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

            case '1': case '2': case '3': case '4': case '5':
            case '6': case '7': case '8': case '9':
                final IntegerToken intToken = readInteger(nextChar, input, tokens, position);
                tokens.add(intToken);
                position += intToken.getEndPosition() - intToken.getStartPosition();
                break;

            default:
                final Identifier id = readIdentifier(nextChar, input, tokens, position);
                if (id == null)
                    throw new LexException("Syntaxerror on position " + position, readNext(input, 15));

                tokens.add(id);
                position += id.getName().length()-1;
                break;
            }
            ++position;
        }
    }

    private String readNext(PushbackReader input, int nr) {
        final StringBuilder sb = new StringBuilder(nr);
        int c;
        try {
            while (nr-- > 0 && (c = input.read()) != -1) {
                if (c != '\n' && c != '\r')
                    sb.append((char)c);
            }
            if ((c = input.read()) != -1) {
                input.unread(c);
                sb.append("...");
            }
        } catch (final IOException e) {
            // hm, ok, then lets just abort here
        }

        return sb.toString();
    }

    private Identifier readIdentifier(int nextChar, PushbackReader input, List<Token> tokens, int position) throws IOException {
        boolean first = true;
        final StringBuilder name = new StringBuilder();
        while ((nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z')
                || (!first && nextChar >= '0' && nextChar <= '9')
                || (!first && (nextChar == '?' || nextChar == '!'))
                || nextChar == '_') {
            assert 0 <= nextChar && nextChar < 1<<16;
            name.append((char)nextChar);
            nextChar = input.read();
            first = false;
        }
        if (nextChar != -1)
            input.unread(nextChar);

        if (name.length() == 0)
            return null;
        // using String.intern() method to save memory if there are a lot of equal identifiers
        return new Identifier(position, position+name.length()-1, name.toString().intern());
    }

    private IntegerToken readInteger(int nextChar, PushbackReader input, List<Token> tokens, int position) throws IOException {
        assert '0' <= nextChar && nextChar <= '9';

        int endPosition = position - 1;
        int value = 0;
        while (nextChar >= '0' && nextChar <= '9') {
            value = 10*value + nextChar - '0';
            nextChar = input.read();
            ++endPosition;
        }
        if (nextChar != -1)
            input.unread(nextChar);

        return new IntegerToken(position, endPosition, value);
    }

}
