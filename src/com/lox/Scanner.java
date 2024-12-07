package com.lox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner
{
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords = new HashMap<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    static
    {
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("fun", TokenType.FUN);
        keywords.put("class", TokenType.CLASS);
        keywords.put("var", TokenType.VAR);
        keywords.put("this", TokenType.THIS);
        keywords.put("super", TokenType.SUPER);
        keywords.put("print", TokenType.PRINT);
        keywords.put("nil", TokenType.NIL);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
    }
    Scanner(String source)
    {
        this.source = source;
    }

    List<Token> scanTokens()
    {
        tokens.clear();
        while (!isAtEnd())
        {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd()
    {
        return current >= source.length();
    }
    private void scanToken()
    {
        char c = advance();
        switch (c)
        {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if(match('/'))
                {
                    while(peek() != '\n' && !isAtEnd())
                        advance();
                }
                else if(match('*'))
                {
                    while(peek() != '*' && !isAtEnd())
                        advance();
                    if(!isAtEnd() && peekNext() == '/')
                        advance();
                    if(!isAtEnd())
                        advance();
                }
                else
                {
                    addToken(TokenType.SLASH);
                }
                break;
            //ignore white spaces
            case ' ':
            case '\r':
            case '\t':
                break;
            // ignore empty line but increase line count
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if(isDigit(c))
                {
                    number();
                }
                else if (isAlpha(c))
                {
                    identifier();
                } else
                {
                    Lox.error(line, "Unexpected character");
                }
                break;
        }
    }

    private void identifier()
    {
        while(isAlphaNumeric(peek()))
            advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c)
    {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
    }

    private void number() {
        while(isDigit(peek()))
            advance();
        // look for . to represent float ops
        if(peek() == '.' && isDigit(peekNext()))
        {
            advance();
            while(isDigit(peek()))
                advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext()
    {
        if(current+1 >= source.length())
            return '\0';
        return source.charAt(current+1);
    }

    private boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    private void string()
    {
        while  (peek() != '"' && !isAtEnd())
        {
            if(peek() == '\n')
                line++;
            advance();
        }
        if(isAtEnd())
        {
            Lox.error(line, "Unexpected end of string");
            return;
        }
        advance();
        // trim the quotes
        String value = source.substring(start+1, current-1);
        addToken(TokenType.STRING, value);
    }
    private char peek()
    {
        if(isAtEnd())
            return '\0';
        return source.charAt(current);
    }
    private boolean match(char expected)
    {
        if(isAtEnd())
            return false;
        if(source.charAt(current) != expected)
            return false;
        current++;
        return true;
    }
    private char advance()
    {
        current = current + 1;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type)
    {
        addToken(type,null);
    }

    private void addToken(TokenType type, Object literal)
    {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
