package com.jhl.mds.util;

public class PipelineCancelException extends RuntimeException {
    
    public PipelineCancelException() {
        super();
    }

    public PipelineCancelException(String message) {
        super(message);
    }

    public PipelineCancelException(String message, Throwable cause) {
        super(message, cause);
    }

    public PipelineCancelException(Throwable cause) {
        super(cause);
    }

    protected PipelineCancelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
