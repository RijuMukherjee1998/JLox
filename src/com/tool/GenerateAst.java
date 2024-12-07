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
                "Binary : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal : Object value",
                "Unary : Token operator, Expr right"
        ));
    }
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);
        writer.println("package com.lox;");
        writer.println();
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