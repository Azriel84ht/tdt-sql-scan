package com.tdtsqlscan.etl;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a BTEQ script, which is a collection of BTEQ commands.
 */
public class BteqScript {

    private String scriptName;
    private final List<BteqCommand> commands;

    public BteqScript() {
        this.commands = new ArrayList<>();
    }

    public BteqScript(String scriptName) {
        this.scriptName = scriptName;
        this.commands = new ArrayList<>();
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public void addCommand(BteqCommand command) {
        this.commands.add(command);
    }

    public List<BteqCommand> getCommands() {
        return commands;
    }
}
