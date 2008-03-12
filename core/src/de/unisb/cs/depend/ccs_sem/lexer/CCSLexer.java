package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.*;


public class CCSLexer extends AbstractLexer {

    private static final int ENVIRONMENT_SIZE = 15;

    private int position;

    public List<Token> lex(Reader input) throws LexException {
        final List<Token> tokens = new ArrayList<Token>();

        final HistoryPushbackReader pr = new HistoryPushbackReader(input, 1, ENVIRONMENT_SIZE);

        try {
            lex0(pr, tokens);
        } catch (final IOException e) {
            throw new LexException("Error reading input stream", e);
        }

        assert !tokens.contains(null);

        return tokens;
    }

    private synchronized void lex0(HistoryPushbackReader input, List<Token> tokens) throws IOException, LexException {
        int nextChar;

        // temporaryly needed variables
        String str;

        position = 0;

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
                nextChar = input.read();
                if (nextChar == '.')
                    tokens.add(new IntervalDots(position, ++position));
                else {
                    if (nextChar != -1)
                        input.unread(nextChar);
                    tokens.add(new Dot(position));
                }
                break;

            case '+':
                tokens.add(new Plus(position));
                break;

            case '-':
                tokens.add(new Minus(position));
                break;

            case '*':
                tokens.add(new Multiplication(position));
                break;

            case '/':
                // check for comment
                nextChar = input.read();
                if (nextChar == '/') {
                    ++position;
                    while ((nextChar = input.read()) != -1) {
                        ++position;
                        if (nextChar == '\n' || nextChar == '\r')
                            break;
                    }
                } else {
                    if (nextChar != -1)
                        input.unread(nextChar);
                    tokens.add(new Division(position));
                }
                break;

            case '%':
                tokens.add(new Modulo(position, position));
                break;

            case '|':
                nextChar = input.read();
                if (nextChar == '|')
                    tokens.add(new Or(position, ++position));
                else {
                    if (nextChar != -1)
                        input.unread(nextChar);
                    tokens.add(new Parallel(position));
                }
                break;

            case '&':
                nextChar = input.read();
                if (nextChar == '&')
                    tokens.add(new And(position, ++position));
                else
                    throw new LexException("Syntaxerror: Expected a second '&'", readEnvironment(input, ENVIRONMENT_SIZE));
                break;

            case '\\':
                tokens.add(new Restrict(position));
                break;

            case '(':
                // check for comment
                nextChar = input.read();
                if (nextChar == '*') {
                    ++position;
                    while ((nextChar = input.read()) != -1) {
                        ++position;
                        if (nextChar == '*') {
                            nextChar = input.read();
                            ++position;
                            if (nextChar == ')')
                                break;
                        }
                    }
                    if (nextChar == -1)
                        throw new LexException("Comment is not closed");
                } else {
                    if (nextChar != -1)
                        input.unread(nextChar);
                    tokens.add(new LParenthesis(position));
                }
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
                nextChar = input.read();
                if (nextChar == '=')
                    tokens.add(new Equals(position, ++position));
                else if (nextChar != -1) {
                    input.unread(nextChar);
                    tokens.add(new Assign(position, position));
                }
                break;

            case ';':
                tokens.add(new Semicolon(position));
                break;

            case '?':
                tokens.add(new QuestionMark(position));
                break;

            case '!':
                nextChar = input.read();
                if (nextChar == '=')
                    tokens.add(new Neq(position++));
                else {
                    if (nextChar != -1)
                        input.unread(nextChar);
                    tokens.add(new Exclamation(position));
                }
                break;

            case ':':
                tokens.add(new Colon(position));
                break;

            case '<':
                nextChar = input.read();
                switch (nextChar) {
                case '<':
                    tokens.add(new LeftShift(position++));
                    break;

                case '=':
                    tokens.add(new Leq(position++));
                    break;

                default:
                    if (nextChar != -1)
                        input.unread(nextChar);
                    tokens.add(new Less(position));
                    break;
                }
                break;

            case '>':
                nextChar = input.read();
                switch (nextChar) {
                case '>':
                    tokens.add(new RightShift(position++));
                    break;

                case '=':
                    tokens.add(new Geq(position++));
                    break;

                default:
                    if (nextChar != -1)
                        input.unread(nextChar);
                    tokens.add(new Greater(position));
                    break;
                }
                break;

            case '"':
                // read until the next DoubleQuotes
                final int startPosition = position;
                str = readQuotedString(input);
                tokens.add(new Identifier(startPosition, position, str, true));
                break;

            case '1': case '2': case '3': case '4': case '5':
            case '6': case '7': case '8': case '9':
                final IntegerToken intToken = readInteger(nextChar, input, tokens);
                tokens.add(intToken);
                break;

            default:
                str = readString(nextChar, input);
                if (str.length() == 0)
                    throw new LexException("Syntaxerror on position " + position, readEnvironment(input, ENVIRONMENT_SIZE));
                if ("when".equals(str) || "if".equals(str))
                    tokens.add(new When(position, position += str.length() - 1));
                else if ("then".equals(str))
                    tokens.add(new Then(position, position += str.length() - 1));
                else if ("else".equals(str))
                    tokens.add(new Else(position, position += str.length() - 1));
                else if ("mod".equals(str))
                    tokens.add(new Modulo(position, position += str.length() - 1));
                else if ("true".equals(str))
                    tokens.add(new True(position, position += str.length() - 1));
                else if ("false".equals(str))
                    tokens.add(new False(position, position += str.length() - 1));
                else if ("and".equals(str))
                    tokens.add(new And(position, position += str.length() - 1));
                else if ("or".equals(str))
                    tokens.add(new Or(position, position += str.length() - 1));
                else if ("range".equals(str))
                    tokens.add(new RangeToken(position, position += str.length() - 1));
                else if ("const".equals(str))
                    tokens.add(new ConstToken(position, position += str.length() - 1));
                else if ("ERROR".equals(str))
                    tokens.add(new ErrorToken(position, position += str.length() - 1));
                else
                    tokens.add(new Identifier(position, position += str.length() - 1, str, false));

                break;
            }
            ++position;
        }
    }

    private String readEnvironment(HistoryPushbackReader input, int nr) {
        final StringBuilder sb = new StringBuilder(nr);
        // append the last "nr" read chars
        sb.append(input.getHistory(nr));
        // append the next "nr" chars
        int c;
        try {
            while (nr-- > 0 && (c = input.read()) != -1) {
                if (c == '\n' || c == '\r')
                    sb.append(' ');
                else
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

    private String readString(int nextChar, HistoryPushbackReader input) throws IOException {
        boolean first = true;
        final StringBuilder name = new StringBuilder();
        while ((nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z')
                || (!first && nextChar >= '0' && nextChar <= '9')
                //|| (!first && (nextChar == '?' || nextChar == '!'))
                || nextChar == '_') {
            assert 0 <= nextChar && nextChar < 1<<16;
            name.append((char)nextChar);
            nextChar = input.read();
            first = false;
        }
        if (nextChar != -1)
            input.unread(nextChar);

        return name.toString();
    }

    private String readQuotedString(HistoryPushbackReader input) throws LexException, IOException {
        int nextChar;
        final StringBuilder sb = new StringBuilder();
        while ((nextChar = input.read()) != -1) {
            ++position;
            if (nextChar == '\\') {
                nextChar = input.read();
                if (nextChar == '"') {
                    sb.append((char)nextChar);
                } else {
                    sb.append('\\').append((char)nextChar);
                }
            } else if (nextChar == '"') {
                return sb.toString();
            } else
                sb.append((char)nextChar);
        }

        throw new LexException("Expected '\"'", readEnvironment(input, ENVIRONMENT_SIZE));
    }

    private IntegerToken readInteger(int nextChar, HistoryPushbackReader input, List<Token> tokens) throws IOException {
        assert '0' <= nextChar && nextChar <= '9';

        final int startPosition = position--;
        int value = 0;
        while (nextChar >= '0' && nextChar <= '9') {
            value = 10*value + nextChar - '0';
            nextChar = input.read();
            ++position;
        }
        if (nextChar != -1)
            input.unread(nextChar);

        return new IntegerToken(startPosition, position, value);
    }

}
