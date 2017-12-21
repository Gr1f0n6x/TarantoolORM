package org.tarantool.orm.exception;

/**
 * Created by GrIfOn on 20.12.2017.
 */
public class TarantoolORMException extends Exception {
    public TarantoolORMException() {
    }

    public TarantoolORMException(String message) {
        super(message);
    }

    public TarantoolORMException(String message, Throwable cause) {
        super(message, cause);
    }

    public TarantoolORMException(Throwable cause) {
        super(cause);
    }

    public TarantoolORMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
