package com.tdtsqlscan.web;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class FileMetadata {
    private String fileName;
    private int executionOrder;
    private String fileFormat;
    private String fileSize;
    private int transactions;
    private final Set<String> inputTables = new HashSet<>();
    private final Set<String> outputTables = new HashSet<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(int executionOrder) {
        this.executionOrder = executionOrder;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

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
