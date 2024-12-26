package com.tool;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.out.println("Usage: generate_ast <output_directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign : Token name, Expr value",
                "Binary : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal : Object value",
                "Logical : Expr left, Token operator, Expr right",
                "Unary : Token operator, Expr right",
                "Variable : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "If : Expr condition, List<Stmt> thenBranches, List<Stmt> elseBranches",
                "Block : List<Stmt> statements",
                "Expression : Expr expression",
                "Print  : Expr expression",
                "Var : Token name, Expr initializer",
                "Reassign : Token name, Expr reInitializer"
        ));
    }
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);
        writer.println("package com.lox;");
        writer.println();
        writer.println("""
                /*
                    This is an auto generated class from the class tool/GenerateAst.
                    This auto generator class implements the visitor pattern as this helps
                    each of the class to have different visitor methods without changing the
                    actual implementation of the classes itself.
                    Any calling class should only implement the Visitor Interface and define how the
                    visitor interface is going to work for them.
                */
                """);
        writer.println("abstract class " + baseName + " { ");
        defineVisitor(writer, baseName, types);
        //All the AST Sub Classes
        for(String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }
        writer.println();
        writer.println(" abstract <R> R accept(Visitor<R> visitor);");
        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(" interface Visitor<R> {");
        for(String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName+baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println(" }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) throws IOException {
        writer.println("static class " + className + " extends "+ baseName + " {");
        //Constructor
        writer.println("  " + className+ "(" + fields + ") " +  "{");
        String[] fieldList = fields.split(", ");
        for(String field : fieldList) {
            String name = field.split(" ")[1].trim();
            writer.println("    this." + name + " = " + name + ";");
        }
        writer.println("  }");

        writer.println();
        writer.println(" @Override");
        writer.println(" <R> R accept(Visitor<R> visitor) {");
        writer.println("    return visitor.visit" + className + baseName + "(this);");
        writer.println("  }");
        //Fields
        writer.println();
        for(String field : fieldList) {
            writer.println("  final " + field + ";");
        }
        writer.println("}");
    }
}
