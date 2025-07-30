package com.tdtsqlscan.etl;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a BTEQ script, which is a collection of BTEQ commands.
 */
public class BteqScript {

    private final List<BteqCommand> commands;

    public BteqScript() {
        this.commands = new ArrayList<>();
    }

    public void addCommand(BteqCommand command) {
        this.commands.add(command);
    }

    public List<BteqCommand> getCommands() {
        return commands;
    }
}
