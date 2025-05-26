package query;

/**
 * Excepción lanzada cuando ocurre un error de parseo en SQL.
 */
public class SQLParseException extends RuntimeException {

    public SQLParseException(String message) {
        super(message);
    }

    public SQLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
