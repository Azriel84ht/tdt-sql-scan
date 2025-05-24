package query;

public class SQLOrderItem {

    private final SQLExpression expression;
    private final boolean ascending;
    private final Boolean nullsFirst;

    public SQLOrderItem(String itemStr) {
        String s = itemStr.trim();
        Boolean nf = null;

        String upper = s.toUpperCase();
        if (upper.endsWith("NULLS FIRST")) {
            nf = true;
            s = s.substring(0, s.length() - "NULLS FIRST".length()).trim();
        } else if (upper.endsWith("NULLS LAST")) {
            nf = false;
            s = s.substring(0, s.length() - "NULLS LAST".length()).trim();
        }

        boolean asc = true;
        upper = s.toUpperCase();
        if (upper.endsWith(" DESC")) {
            asc = false;
            s = s.substring(0, s.length() - "DESC".length()).trim();
        } else if (upper.endsWith(" ASC")) {
            asc = true;
            s = s.substring(0, s.length() - "ASC".length()).trim();
        }

        this.nullsFirst = nf;
        this.ascending = asc;
        this.expression = new SQLExpression(s);
    }

    public SQLExpression getExpression() {
        return expression;
    }

    public boolean isAscending() {
        return ascending;
    }

    public Boolean getNullsFirst() {
        return nullsFirst;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression.toString());
        sb.append(ascending ? " ASC" : " DESC");
        if (nullsFirst != null) {
            sb.append(nullsFirst ? " NULLS FIRST" : " NULLS LAST");
        }
        return sb.toString();
    }
}
