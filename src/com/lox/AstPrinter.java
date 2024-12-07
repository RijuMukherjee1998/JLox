package com.lox;

/*
    This is the AstPrinter class which prints the expression into an Abstract Syntax Tree.
    As you can see it uses the Visitor pattern from the Expr and implements the interface Visitor.
    Now see once the accept() function is called on that object it goes for each of the implementation
    of each of those objects. So literally it saved the classes from bloating due to separate function
    implementations.
*/

class AstPrinter implements Expr.Visitor<String> {

    //Testing our little Abstract Tree Printer with our small Expression
    public static void main(String[] args)
    {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)
                )
        );
        System.out.println(new AstPrinter().print(expression)); // Prints out the expression like this (* (- 123) (group 45.67))
    }
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value == null)
            return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... expressions) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(name);
        for(Expr expr : expressions) {
            sb.append(" ");
            sb.append(expr.accept(this));
        }
        sb.append(")");
        return sb.toString();
    }
}
