package com.lox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;


public class Lox
{
    public static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    public static void main(String[] args) throws IOException
    {
        if(args.length > 1) {
            System.out.println("Usage: jlox <path to lox file>");
            System.exit(64);
        } else if(args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    // start interpreting the whole file/code
    private static void runFile(String path) throws IOException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(hadError) System.exit(65);
        if(hadRuntimeError) System.exit(70);
    }

    // run the code more interactively prompt by prompt
    private static void runPrompt() throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true)
        {
            System.out.print("jlox > ");
            String line = reader.readLine();
            if(Objects.equals(line, "quit"))
                System.exit(0);
            if(line ==  null || line.trim().isEmpty())
                continue;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source)
    {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if(hadError) return;
        //System.out.println(new AstPrinter().print(expression));
        interpreter.interpret(statements);

    }

    static void error(int line, String message)
    {
        report(line, " ", message);
    }
    private static void report(int line, String where, String message)
    {
        System.err.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }
    static void error(Token token, String message)
    {
        if(token.type == TokenType.EOF){
            report(token.line, "at end"+" ", message);
        }
        else {
            report(token.line, "at '" + token.lexeme + "'"+" ", message);
        }
    }

    public static void runtimeError(RuntimeError err) {
        System.err.println("[Line " + err.token.line + "]" + "\n" + err.getMessage() );
        hadRuntimeError = true;
    }
}
