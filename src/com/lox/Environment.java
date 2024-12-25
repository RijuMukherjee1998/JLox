package com.lox;

import java.util.HashMap;
import java.util.Map;

// This class maintains variable declarations, values and scopes
public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        this.enclosing = null;
    }
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }
    void define(Token name, Object value)
    {
        if(values.containsKey(name.lexeme))
            throw new RuntimeError(name,"Variable " + "'" + name.lexeme + "'"+" already defined");

        values.put(name.lexeme, value);
    }
    void assign(Token name, Object value)
    {
        if (values.containsKey(name.lexeme))
        {
            values.put(name.lexeme, value);
            return;
        }
        if(enclosing != null)
        {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
    Object get(Token name)
    {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
