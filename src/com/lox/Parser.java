package com.lox;
import java.util.ArrayList;
import java.util.List;


/*
    We are making a top-down parser
    It takes in a list of tokens and parses out these tokens
*/

/*
    Parser expression grammar for jlox
    //statement syntax tree
     program    → declaration* EOF ;
     declaration → varDecl | reassignmentDecl | statement;
     varDecl    → "var" IDENTIFIER ( "=" expression) ? ";" ;
     statement  → exprStmt | printStmt | blockStmt | ifStmt;
     ifStmt     → "if" "(" expression ")" statement
                    ( "else" statement)? ;
     blockStmt  → "{" declaration* "}";
     exprStmt   → expression ";" ;
     printStmt  → "print" expression ";" ;\

    //expression syntax tree
     expression →  assignment ;
     assignment → IDENTIFIER "=" assignment | logic_or ;
     logic_or   → logic_and ( "or" logic_and)* ;
     logic_and  → equality ( "and" equality )* ;
     equality   → comparison ( ( "!=" | "==" ) comparison )* ;
     comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     term       → factor ( ( "-" | "+" ) factor )* ;
     factor     → unary ( ( "/" | "*" ) unary )* ;
     unary      → ( "!" | "-" ) unary | primary ;
     primary    → NUMBER | STRING | "true" | "false" | "nil"
                   |"(" expression ")" | IDENTIFIER ;
*/

public class Parser {
    private static class ParseError extends RuntimeException { }
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // initial method to call to parse the entire code
    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(declarations());
        }
        return statements;
    }

    private Stmt declarations() {
        try{
            if(match(TokenType.VAR)) return varDeclaration();
            if(match(TokenType.IDENTIFIER)) return reassignmentDeclaration();
            return statement();
        } catch (ParseError perr){
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if(match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if(match(TokenType.IF)) return ifStatement();
        if(match(TokenType.PRINT)) return printStatement();
        if(match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after if statement.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if statement.");
        consume(TokenType.LEFT_BRACE, "Expect '{' after if start of if block.");
        List<Stmt> thenBranchStmts = block();
        List<Stmt> elseBranchStmts = null;
        if(match(TokenType.ELSE)) {
            consume(TokenType.LEFT_BRACE, "Expect '{' after start of else block.");
            elseBranchStmts = block();
        }
        return new Stmt.If(condition, thenBranchStmts, elseBranchStmts);
    }

    private Stmt reassignmentDeclaration() {
        Expr reIntializer = null;
        Token var_name = previous();
        if(match(TokenType.EQUAL)) {
            reIntializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after reassignment.");
        return new Stmt.Reassign(var_name, reIntializer);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declarations());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Expression(value);
    }

    // expression -> equality
    private Expr expression()
    {
        return assignment();
    }

    private Expr assignment()
    {
        Expr expr = or();

        if(match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, expr);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        if(match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        if(match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
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

        if (match(TokenType.IDENTIFIER)){
            return new Expr.Variable(previous());
        }
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
