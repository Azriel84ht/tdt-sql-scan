grammar Bteq;

// Top-level rule for a BTEQ script
script
    : (statement)* EOF
    ;

statement
    : bteq_command
    | sql_statement
    ;

// A statement can be a BTEQ command or a SQL statement
bteq_command
    : (logon_command
    | logoff_command
    | export_reset_command
    | export_file_command
    | set_width_command
    | if_errorcode_goto_command) SEMICOLON
    ;

// Differentiates between high-level SQL statement types
sql_statement
    : (dml_statement | ddl_statement) SEMICOLON
    ;

// --- BTEQ Command Rules ---

logon_command
    : LOGON text_up_to_semicolon
    ;

logoff_command
    : LOGOFF
    ;

export_reset_command
    : EXPORT RESET
    ;

export_file_command
    : EXPORT FILE '=' path=STRING
    ;

set_width_command
    : SET WIDTH width=NUMBER
    ;

if_errorcode_goto_command
    : IF ERRORCODE operator=OPERATOR code=NUMBER THEN GOTO label=IDENTIFIER
    ;


// --- SQL Statement Rules ---

dml_statement
    : (SELECT | INSERT | UPDATE | DELETE) text_up_to_semicolon
    ;

ddl_statement
    : (CREATE | DROP) TABLE text_up_to_semicolon
    ;


// --- Generic Helper Rules ---

// This rule will consume any tokens until it sees a semicolon.
text_up_to_semicolon
    : (~SEMICOLON)*
    ;


// --- Lexer Rules ---

// BTEQ Keywords
LOGON: '.' [Ll][Oo][Gg][Oo][Nn];
LOGOFF: '.' [Ll][Oo][Gg][Oo][Ff][Ff];
EXPORT: '.' [Ee][Xx][Pp][Oo][Rr][Tt];
SET: '.' [Ss][Ee][Tt];
IF: '.' [Ii][Ff];
GOTO: '.' [Gg][Oo][Tt][Oo];

// BTEQ Parameters & Keywords
RESET: [Rr][Ee][Ss][Ee][Tt];
FILE: [Ff][Ii][Ll][Ee];
WIDTH: [Ww][Ii][Dd][Tt][Hh];
ERRORCODE: [Ee][Rr][Rr][Oo][Rr][Cc][Oo][Dd][Ee];
THEN: [Tt][Hh][Ee][Nn];

// SQL Keywords
SELECT: [Ss][Ee][Ll][Ee][Cc][Tt];
INSERT: [Ii][Nn][Ss][Ee][Rr][Tt];
UPDATE: [Uu][Pp][Dd][Aa][Tt][Ee];
DELETE: [Dd][Ee][Ll][Ee][Tt][Ee];
CREATE: [Cc][Rr][Ee][Aa][Tt][Ee];
DROP: [Dd][Rr][Oo][Pp];
TABLE: [Tt][Aa][Bb][Ll][Ee];

// Basic Tokens
SEMICOLON: ';';
EQUALS: '=';
OPERATOR: '<>' | '!=' | '>' | '<' | '>=' | '<=';
NUMBER: [0-9]+;
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;
STRING: '\'' (~'\'')* '\'' | '"' (~'"')* '"'; // Handles single or double quoted strings

// Any other text is intentionally not captured by a catch-all rule.
// The text_up_to_semicolon parser rule will consume tokens until it hits a semicolon.

// Whitespace is skipped
WS: [ \t\r\n]+ -> skip;
