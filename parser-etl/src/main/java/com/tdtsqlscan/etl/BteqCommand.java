package com.tdtsqlscan.etl;

/**
 * Represents a command within a BTEQ script. This can be either a BTEQ control command
 * (e.g., .LOGON) or an SQL statement.
 */
public interface BteqCommand {
    String getRawText();
}
