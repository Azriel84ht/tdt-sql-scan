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

// --- Detailed SELECT statement parsing ---

// This is the new entry point for parsing a single SELECT statement.
select_statement
    : SELECT select_list
      FROM from_clause
      (WHERE where_clause)?
      (GROUP BY group_by_clause)?
      (SEMICOLON)? EOF
    ;

select_list
    : column_element (',' column_element)*
    ;

column_element
    : expression (AS? alias)?
    ;

from_clause
    : table_reference (join_clause)*
    ;

join_clause
    : join_operator? JOIN table_reference (ON condition=expression)?
    ;

join_operator
    : (LEFT | RIGHT | FULL) (OUTER)?
    | INNER
    ;

where_clause
    : expression
    ;

group_by_clause
    : expression (',' expression)*
    ;

table_reference
    : table_name (AS? alias)?
    ;

expression
    : qualified_column_name
    | literal_value
    // This is a simplification. A real expression parser would be much more complex.
    // For now, we'll just capture identifiers and literals.
    // We will capture the full text for complex expressions in the listener.
    ;

qualified_column_name
    : (table_name '.')? column_name
    ;

table_name
    : IDENTIFIER
    ;

column_name
    : IDENTIFIER
    ;

alias
    : IDENTIFIER
    ;

literal_value
    : STRING | NUMBER
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
FROM: [Ff][Rr][Oo][Mm];
WHERE: [Ww][Hh][Ee][Rr][Ee];
JOIN: [Jj][Oo][Ii][Nn];
ON: [Oo][Nn];
AS: [Aa][Ss];
GROUP: [Gg][Rr][Oo][Uu][Pp];
BY: [Bb][Yy];
LEFT: [Ll][Ee][Ff][Tt];
RIGHT: [Rr][Ii][Gg][Hh][Tt];
FULL: [Ff][Uu][Ll][Ll];
INNER: [Ii][Nn][Nn][Ee][Rr];
OUTER: [Oo][Uu][Tt][Ee][Rr];

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
COMMA: ',';
DOT: '.';
ASTERISK: '*';
LPAREN: '(';
RPAREN: ')';

NUMBER: [0-9]+;
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;
STRING: '\'' (~'\'')* '\'' | '"' (~'"')* '"'; // Handles single or double quoted strings

// Any other text is intentionally not captured by a catch-all rule.
// The text_up_to_semicolon parser rule will consume tokens until it hits a semicolon.

// Whitespace is skipped
WS: [ \t\r\n]+ -> skip;
