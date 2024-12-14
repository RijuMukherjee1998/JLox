package com.lox;
import java.util.List;


/*
    We are making a top-down parser
    It takes in a list of tokens and parses out these tokens
*/

/*
    Parser expression grammar for jlox
     expression →  equality ;
     equality   → comparison ( ( "!=" | "==" ) comparison )* ;
     comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     term       → factor ( ( "-" | "+" ) factor )* ;
     factor     → unary ( ( "/" | "*" ) unary )* ;
     unary      → ( "!" | "-" ) unary | primary ;
     primary    → NUMBER | STRING | "true" | "false" | "nil"
                   |"(" expression ")" ;
*/

public class Parser {
    private static class ParseError extends RuntimeException { }
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // initial method to call to parse the entire code
    Expr parse() {
        try {
            return expression();
        }
        catch (ParseError e) {
            return null;
        }
    }
    // expression -> equality
    private Expr expression()
    {
        return equality();
    }

    // equality -> comparison (("!=" | "==") comparison)*
    private Expr equality()
    {
        Expr expr = comparison();

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL))
        {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term)*
    private Expr comparison() {
        Expr expr = term();
        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL))
        {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    // term -> factor( ("-" | "+") factor) *
    private Expr term() {
        Expr expr = factor();
        while(match(TokenType.MINUS, TokenType.PLUS))
        {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);

        }
        return expr;
    }
    // factor -> unary (("/" | "*") unary) *
    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR))
        {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    // unary -> ("!" | "-") unary | primary
    private Expr unary() {
        if(match(TokenType.BANG, TokenType.MINUS))
        {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    // primary ->  NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    private Expr primary() {
        if(match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.NIL)) return new Expr.Literal(null);

        if(match(TokenType.NUMBER , TokenType.STRING)) return new Expr.Literal(previous().literal);

        if(match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType tokenType, String err_message) {
        if(check(tokenType)) {
            return advance();
        }
        throw error(peek(), err_message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while(!isAtEnd()) {
            if(previous().type == TokenType.SEMICOLON) return;
            switch (peek().type) {
                case TokenType.CLASS:
                case TokenType.FUN:
                case TokenType.VAR:
                case TokenType.FOR:
                case TokenType.IF:
                case TokenType.WHILE:
                case TokenType.PRINT:
                case TokenType.RETURN:
                    return;
            }
            advance();
        }
    }
    private boolean match(TokenType... types)
    {
        for(TokenType type : types)
        {
            if(check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }
    private Token advance()
    {
        if(!isAtEnd()) current++;
        return previous();
    }
    private boolean isAtEnd()
    {
        return peek().type == TokenType.EOF;
    }
    private Token peek() {
        return tokens.get(current);
    }
    private Token previous() {
        return tokens.get(current-1);
    }
}
