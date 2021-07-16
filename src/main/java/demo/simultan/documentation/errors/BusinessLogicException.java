package demo.simultan.documentation.errors;

import org.springframework.core.NestedRuntimeException;

public class BusinessLogicException extends NestedRuntimeException {

    private String code;

    private String message;

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public BusinessLogicException(String code, String message) {
        super(code);
        this.code = code;
        this.message = message;
    }

    public BusinessLogicException(String code, String message, Throwable cause) {
        super(code, cause);
        this.code = code;
        this.message = message;
    }
}
