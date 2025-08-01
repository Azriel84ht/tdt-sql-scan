package com.tdtsqlscan.etl;

public class BteqControlCommand implements BteqCommand {

    private final BteqCommandType type;
    private final String commandText;

    public BteqControlCommand(BteqCommandType type, String commandText) {
        this.type = type;
        this.commandText = commandText;
    }

    public BteqCommandType getType() {
        return type;
    }

    @Override
    public String getRawText() {
        return commandText;
    }
}
