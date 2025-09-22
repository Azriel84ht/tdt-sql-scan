package com.tdtsqlscan.parser;

import com.tdtsqlscan.parser.antlr.BteqLexer;
import com.tdtsqlscan.parser.antlr.BteqParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AntlrBteqParserService {

    public List<String> parse(String scriptContent) {
        // Create a CharStream from the script content
        BteqLexer lexer = new BteqLexer(CharStreams.fromString(scriptContent));

        // Create a token stream from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Create a parser from the token stream
        BteqParser parser = new BteqParser(tokens);

        // Start parsing from the 'script' rule
        ParseTree tree = parser.script();

        // Create a listener to walk the parse tree
        BteqScriptListener listener = new BteqScriptListener();

        // Walk the tree with the listener
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return the extracted commands
        return listener.getCommands();
    }
}
