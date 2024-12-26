package com.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for(Stmt statement : statements) {
                execute(statement);
            }
        }
        catch (RuntimeError err){
            Lox.runtimeError(err);
        }
    }
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }
    private String stringify(Object obj) {
        if(obj == null) return "nil";

        if(obj instanceof Double) {
            String text = obj.toString();
            if(text.endsWith(".0")) text = text.substring(0, text.length()-2);
            return text;
        }
        return obj.toString();
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if(isTruthy(evaluate(stmt.condition))) {
            executeBlock(stmt.thenBranches, new Environment(environment));
        } else if (stmt.elseBranches != null) {
            executeBlock(stmt.elseBranches, new Environment(environment));
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment((environment)));
        return null;
    }
    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for(Stmt statement : statements) {
                execute(statement);
            }
        }
        finally {
            this.environment = previous;
        }
    }
    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if(stmt.initializer != null)
            value = evaluate(stmt.initializer);
        environment.define(stmt.name, value);
        return null;
    }
    @Override
    public Void visitReassignStmt(Stmt.Reassign stmt) {
        Object value = null;
        if (stmt.reInitializer != null)
            value = evaluate(stmt.reInitializer);
        environment.assign(stmt.name, value);
        return null;
    }
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if(expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        }
        else {
            if(!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type)
        {
            case TokenType.BANG:
                return !isTruthy(right);

            case TokenType.MINUS:
                checkNumberOperand(expr.operator,right);
                return -(double)right;
        }
        //unreachable
        return null;
    }
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type)
        {
            case TokenType.GREATER:
                checkNumberOperand(expr.operator,left, right);
                return (double)left > (double)right;
            case TokenType.LESS:
                checkNumberOperand(expr.operator,left, right);
                return (double)left < (double)right;
            case TokenType.GREATER_EQUAL:
                checkNumberOperand(expr.operator,left, right);
                return (double)left >= (double)right;
            case TokenType.LESS_EQUAL:
                checkNumberOperand(expr.operator,left, right);
                return (double)left <= (double)right;
            case TokenType.EQUAL_EQUAL:
                return isEqual(left, right);
            case TokenType.BANG_EQUAL:
                return !isEqual(left, right);
            case TokenType.PLUS:
                if(left instanceof String && right instanceof String)
                    return (left.toString() + right.toString());
                else if(left instanceof Double && right instanceof Double)
                    return (double)left + (double)right;
                else if(left instanceof String && right instanceof Double) {
                    if(right.toString().endsWith(".0"))
                        right = right.toString().substring(0, right.toString().length()-2);
                    return left + right.toString();
                }
                else if (left instanceof Double && right instanceof String) {
                    if(left.toString().endsWith(".0"))
                        left = left.toString().substring(0, left.toString().length()-2);
                    return left.toString() + right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
            case TokenType.MINUS:
                checkNumberOperand(expr.operator,left, right);
                return (double)left - (double)right;
            case TokenType.SLASH:
                checkNumberOperand(expr.operator,left, right);
                if((double)right == 0)
                    throw new RuntimeError(expr.operator, "cannot divide by zero");
                return (double)left / (double)right;
            case TokenType.STAR:
                checkNumberOperand(expr.operator,left, right);
                return (double)left * (double)right;
        }
        return null;
    }

    private void checkNumberOperand(Token operator, Object... obj)
    {
        if(obj.length == 1) {
            if (obj[0] instanceof Double) return;
            throw new RuntimeError(operator, "Operand must be a number");
        }
        if(obj.length == 2) {
            if (obj[0] instanceof Double && obj[1] instanceof Double) return;
            throw new RuntimeError(operator, "Operands must be a number");
        }
    }
    private boolean isEqual(Object left, Object right) {
       if(left == null && right == null) return true;
       if(left == null) return false;
       return left.equals(right);
    }

    private boolean isTruthy(Object obj)
    {
        if(obj == null) return false;
        if(obj instanceof Boolean) return (boolean)obj;
        return true;
    }
    private Object evaluate(final Expr expr) {
        return expr.accept(this);
    }

}
