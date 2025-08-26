package com.tdtsqlscan.etl;

import java.util.List;
import java.util.stream.Collectors;

public class BteqConfigurationCommand implements BteqCommand {

    private final List<BteqCommand> commands;

    public BteqConfigurationCommand(List<BteqCommand> commands) {
        this.commands = commands;
    }

    public List<BteqCommand> getCommands() {
        return commands;
    }

    @Override
    public String getRawText() {
        return commands.stream()
                .map(BteqCommand::getRawText)
                .collect(Collectors.joining("\n"));
    }
}
