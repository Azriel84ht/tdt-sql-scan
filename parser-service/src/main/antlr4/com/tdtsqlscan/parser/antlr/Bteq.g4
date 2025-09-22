grammar Bteq;

// Top-level rule for a BTEQ script
script
    : (bteq_command | sql_statement)* EOF
    ;

// A statement can be a BTEQ command or a SQL statement
bteq_command
    : logon_command
    | logoff_command
    ;

sql_statement
    : content_up_to_semicolon SEMICOLON
    ;

// Specific BTEQ commands
logon_command
    : LOGON content_up_to_semicolon SEMICOLON
    ;

logoff_command
    : LOGOFF SEMICOLON
    ;

// This rule will consume any tokens until it sees a semicolon.
// This is used for the body of the LOGON command and for any SQL statement.
content_up_to_semicolon
    : (~SEMICOLON)*
    ;

// Lexer Rules - these define the tokens
LOGON: '.' [Ll][Oo][Gg][Oo][Nn];
LOGOFF: '.' [Ll][Oo][Gg][Oo][Ff][Ff];

SEMICOLON: ';';

// Any other text is just treated as generic content at the lexer level.
// The parser rules will give it meaning.
CONTENT: ~[; \t\r\n]+;

// Whitespace is skipped
WS: [ \t\r\n]+ -> skip;
