package com.tdtsqlscan.core;

import java.util.ArrayList;
import java.util.List;

public class SQLParserUtils {

    public static String extractBetweenKeywords(String sql, String startKeyword, String endKeyword) {
        int startIndex = sql.toUpperCase().indexOf(startKeyword.toUpperCase());
        if (startIndex == -1) return null;
        startIndex += startKeyword.length();
        int endIndex = endKeyword != null ? sql.toUpperCase().indexOf(endKeyword.toUpperCase(), startIndex) : sql.length();
        if (endIndex == -1) endIndex = sql.length();
        return sql.substring(startIndex, endIndex).trim();
    }

    public static String extractAfterKeyword(String sql, String keyword, String endKeyword) {
        int startIndex = sql.toUpperCase().indexOf(keyword.toUpperCase());
        if (startIndex == -1) return null;
        startIndex += keyword.length();
        int endIndex = endKeyword != null ? sql.toUpperCase().indexOf(endKeyword.toUpperCase(), startIndex) : sql.length();
        if (endIndex == -1) endIndex = sql.length();
        return sql.substring(startIndex, endIndex).trim();
    }

    public static List<String> splitTopLevel(String input, String delimiter) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0 && input.startsWith(delimiter, i)) {
                parts.add(input.substring(start, i).trim());
                i += delimiter.length() - 1;
                start = i + 1;
            }
        }
        parts.add(input.substring(start).trim());
        return parts;
    }

    public static int findTopLevelKeyword(String sql, String keyword, int startIndex) {
        int depth = 0;
        int length = sql.length();
        keyword = keyword.toUpperCase();
        for (int i = startIndex; i < length - keyword.length(); i++) {
            char c = sql.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0 && sql.regionMatches(true, i, keyword, 0, keyword.length())) {
                return i;
            }
        }
        return -1;
    }

    public static String getFirstWord(String s) {
        String[] words = s.trim().split("\\s+");
        if (words.length > 0) {
            String word = words[0];
            // Clean trailing characters that are not part of the name
            while (word.length() > 0 && (word.endsWith(")") || word.endsWith(",") || word.endsWith(";"))) {
                word = word.substring(0, word.length() - 1);
            }
            return word;
        }
        return "";
    }

    public static String extractTableFromExpression(String expression) {
        String table = expression.trim();

        // Split by space to separate table name from alias
        String[] parts = table.split("\\s+");
        if (parts.length == 0) {
            return "";
        }
        String baseName = parts[0];

        // Clean trailing characters that are not part of the name
        while (baseName.length() > 0 && (baseName.endsWith(")") || baseName.endsWith(",") || baseName.endsWith(";"))) {
            baseName = baseName.substring(0, baseName.length() - 1);
        }
        return baseName;
    }

    public static String extractTableName(String sql, String keyword) {
        String afterKeyword = extractAfterKeyword(sql, keyword, null);
        if (afterKeyword != null) {
            return getFirstWord(afterKeyword);
        }
        return null;
    }

    public static String extractBalancedParentheses(String sql) {
        int firstParen = sql.indexOf('(');
        if (firstParen == -1) return null;

        int depth = 1;
        for (int i = firstParen + 1; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return sql.substring(firstParen + 1, i);
                }
            }
        }
        return null; // Unbalanced parentheses
    }
}
