package com.lox;

/*
    This is an auto generated class from the class tool/GenerateAst.
    This auto generator class implements the visitor pattern as this helps
    each of the class to have different visitor methods without changing the
    actual implementation of the classes itself.
    Any calling class should only implement the Visitor Interface and define how the
    visitor interface is going to work for them.
*/

import java.util.List;

abstract class Stmt {
 interface Visitor<R> {
    R visitIfStmt(If stmt);
    R visitWhileStmt(While stmt);
    R visitBlockStmt(Block stmt);
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitPrintStmt(Print stmt);
    R visitReturnStmt(Return stmt);
    R visitVarStmt(Var stmt);
    R visitReassignStmt(Reassign stmt);
 }
static class If extends Stmt {
  If(Expr condition, List<Stmt> thenBranches, List<Stmt> elseBranches) {
    this.condition = condition;
    this.thenBranches = thenBranches;
    this.elseBranches = elseBranches;
  }

 @Override
 <R> R accept(Visitor<R> visitor) {
    return visitor.visitIfStmt(this);
  }

  final Expr condition;
  final List<Stmt> thenBranches;
  final List<Stmt> elseBranches;
}
static class While extends Stmt {
  While(Expr condition, List<Stmt> body, Stmt initStmt) {
    this.condition = condition;
    this.body = body;
    this.initStmt = initStmt;
  }

 @Override
 <R> R accept(Visitor<R> visitor) {
    return visitor.visitWhileStmt(this);
  }

  final Expr condition;
  final List<Stmt> body;
  final Stmt initStmt;
}
static class Block extends Stmt {
  Block(List<Stmt> statements) {
    this.statements = statements;
  }

 @Override
 <R> R accept(Visitor<R> visitor) {
    return visitor.visitBlockStmt(this);
  }

  final List<Stmt> statements;
}
static class Expression extends Stmt {
  Expression(Expr expression) {
    this.expression = expression;
  }

 @Override
 <R> R accept(Visitor<R> visitor) {
    return visitor.visitExpressionStmt(this);
  }

  final Expr expression;
}
static class Function extends Stmt {
  Function(Token name, List<Token> params, List<Stmt> body) {
    this.name = name;
    this.params = params;
    this.body = body;
  }

 @Override
 <R> R accept(Visitor<R> visitor) {
    return visitor.visitFunctionStmt(this);
  }

  final Token name;
  final List<Token> params;
  final List<Stmt> body;
}
static class Print extends Stmt {
  Print(Expr expression) {
    this.expression = expression;
  }

 @Override
 <R> R accept(Visitor<R> visitor) {
    return visitor.visitPrintStmt(this);
  }

  final Expr expression;
}
static class Return extends Stmt {
  Return(Token keyword, Expr value) {
    this.keyword = keyword;
    this.value = value;
  }

 @Override
 <R> R accept(Visitor<R> visitor) {
    return visitor.visitReturnStmt(this);
  }

  final Token keyword;
  final Expr value;
}
static class Var extends Stmt {
  Var(Token name, Expr initializer) {
    this.name = name;
    this.initializer = initializer;
  }

 @Override
 <R> R accept(Visitor<R> visitor) {
    return visitor.visitVarStmt(this);
  }

  final Token name;
  final Expr initializer;
}
static class Reassign extends Stmt {
  Reassign(Token name, Expr reInitializer) {
    this.name = name;
    this.reInitializer = reInitializer;
  }

 @Override
 <R> R accept(Visitor<R> visitor) {
    return visitor.visitReassignStmt(this);
  }

  final Token name;
  final Expr reInitializer;
}

 abstract <R> R accept(Visitor<R> visitor);
}
