package com.lox;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
    We are making a top-down parser
    It takes in a list of tokens and parses out these tokens
*/

/*
    Parser expression grammar for jlox
    //statement syntax tree
     program     → declaration* EOF ;
     declaration → funDecl | varDecl | varReassign | statement;
     funDecl     → "fun" function ;
     function    → IDENTIFIER "(" parameters ? ")" block ;
     parameters  → IDENTIFIER ( "," IDENTIFIER )*
     varDecl     → "var" IDENTIFIER ( "=" expression) ? ";" ;
     varReassign → IDENTIFIER ( "=" expression);
     statement   → exprStmt | ifStmt | whileStmt | forStmt |  printStmt | blockStmt | returnStmt ;
     ifStmt      → "if" "(" expression ")" statements
                    ( "else" statements)? ;
     whileStmt   → "while" "(" expression ")" statement;
     forStmt     → "for" "(" (varDecl | exprStmt | ;) expression? ";" expression?")" statements;
     blockStmt   → "{" declaration* "}";
     exprStmt    → expression ";" ;
     printStmt   → "print" expression ";" ;
     returnStmt  → "return" expression ? ";" ;

    //expression syntax tree
     expression →  assignment ;
     assignment → IDENTIFIER "=" assignment | logic_or ;
     logic_or   → logic_and ( "or" logic_and)* ;
     logic_and  → equality ( "and" equality )* ;
     equality   → comparison ( ( "!=" | "==" ) comparison )* ;
     comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     term       → factor ( ( "-" | "+" ) factor )* ;
     factor     → unary ( ( "/" | "*" ) unary )* ;
     unary      → ( "!" | "-" ) unary | call ;
     call       → primary ("(" arguments ?")")* ;
     arguments  → expression ( "," expression )* ;
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
            if(match(TokenType.FUN)) return funcDeclaration("function");
            if(match(TokenType.VAR)) return varDeclaration();
            if(match(TokenType.IDENTIFIER)){
                if(check(TokenType.EQUAL)) {
                    return reassignmentDeclaration();
                }
                else {
                    --current;
                }
            }
            return statement();
        } catch (ParseError err){
            synchronize();
            return null;
        }
    }

    private Stmt funcDeclaration(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect" + kind + "name");
        consume(TokenType.LEFT_PAREN, "Expect '(' in the start of new function definition");
        List<Token> params = new ArrayList<>();
        if(!check(TokenType.RIGHT_PAREN)){
            do{
                if(params.size() > 255)
                    error(peek(),"Cannot have more than 255 parameters");
                params.add(consume(TokenType.IDENTIFIER,"Expect parameter name"));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' in the end of function");
        consume(TokenType.LEFT_BRACE, "Expect '{' in the start of function body");
        List<Stmt> body = block();
        return new Stmt.Function(name, params, body);
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

    private Stmt reassignmentDeclaration() {
        Expr reInitializer = null;
        Token var_name = previous();
        if(match(TokenType.EQUAL)) {
            reInitializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after reassignment.");
        return new Stmt.Reassign(var_name, reInitializer);
    }

    private Stmt statement() {
        if(match(TokenType.IF)) return ifStatement();
        if(match(TokenType.FOR)) return forStatement();
        if(match(TokenType.WHILE)) return whileStatement();
        if(match(TokenType.PRINT)) return printStatement();
        if(match(TokenType.RETURN)) return returnStatement();
        if(match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if(!check(TokenType.SEMICOLON)){
            value = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after return statement.");
        return new Stmt.Return(keyword, value);
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
    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for' statement.");
        Stmt initializer;
        if(match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else{
            initializer = expressionStatement();
        }
        Expr condition = null;
        if(!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");
        Stmt increment = null;
        if(!check(TokenType.RIGHT_PAREN)) {
            advance();
            increment = reassignmentDeclaration();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for loop statement.");
        consume(TokenType.LEFT_BRACE, "Expect '{' after fot loop statement.");
        List<Stmt> statements = block();
        if(increment != null) {
            statements.addFirst(increment);
        }
        if(condition == null) {
            condition = new Expr.Literal(true);
        }
        return new Stmt.While(condition, statements, initializer);
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after while statement.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after while statement.");
        consume(TokenType.LEFT_BRACE, "Expect '{' after start of while block.");
        List<Stmt> bodyStmts = block();
        return new Stmt.While(condition, bodyStmts, null);
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
        return call();
    }

    private Expr call() {
        Expr expr = primary();
        while (true){
            if(match(TokenType.LEFT_PAREN)){
                expr = finishCall(expr);
            }
            else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr expr) {
        List<Expr> arguments = new ArrayList<>();
        if(!check(TokenType.RIGHT_PAREN)){
            do{
                if(arguments.size() > 255){
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while(match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after finish call.");
        return new Expr.Call(expr, paren, arguments);
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
