package com.tdtsqlscan.web;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class FileMetadata {
    private int transactions;
    private final Set<String> inputTables = new HashSet<>();
    private final Set<String> outputTables = new HashSet<>();

    public int getTransactions() {
        return transactions;
    }

    public void setTransactions(int transactions) {
        this.transactions = transactions;
    }

    public void addInputTable(String table) {
        if (table != null && !table.isEmpty()) {
        this.inputTables.add(table);
        }
    }

    public void addOutputTable(String table) {
        if (table != null && !table.isEmpty()) {
        this.outputTables.add(table);
        }
    }

    public List<String> getInputTables() {
        return inputTables.stream().sorted().collect(Collectors.toList());
    }

    public List<String> getOutputTables() {
        return outputTables.stream().sorted().collect(Collectors.toList());
    }
}
