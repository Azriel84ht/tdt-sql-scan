package com.tdtsqlscan.parser;

import com.tdtsqlscan.parser.antlr.BteqLexer;
import com.tdtsqlscan.parser.antlr.BteqParser;
import com.tdtsqlscan.parser.dto.ParseResultDto;
import com.tdtsqlscan.parser.dto.SelectStatementDto;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Service;

@Service
public class AntlrBteqParserService {

    public ParseResultDto parse(String scriptContent) {
        BteqLexer lexer = new BteqLexer(CharStreams.fromString(scriptContent));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BteqParser parser = new BteqParser(tokens);
        ParseTree tree = parser.script();
        BteqScriptListener listener = new BteqScriptListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        return listener.getParseResult();
    }

    public SelectStatementDto parseSelectStatement(String selectQuery) {
        BteqLexer lexer = new BteqLexer(CharStreams.fromString(selectQuery));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BteqParser parser = new BteqParser(tokens);

        // Start parsing from the 'select_statement' rule instead of 'script'
        ParseTree tree = parser.select_statement();

        // Create our new listener
        SelectStatementListener listener = new SelectStatementListener();

        // Walk the tree with the listener
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return the DTO populated by the listener
        return listener.getSelectStatementDto();
    }
}
