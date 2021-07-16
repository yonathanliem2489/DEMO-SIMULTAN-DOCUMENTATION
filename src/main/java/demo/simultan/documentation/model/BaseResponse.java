package demo.simultan.documentation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class BaseResponse<T> implements Serializable {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("data")
    private T data;

    @JsonCreator
    public BaseResponse(@JsonProperty("code") String code,
            @JsonProperty("message") String message,
            @JsonProperty("errors") List<String> errors,
            @JsonProperty("data") T data) {
        this.code = code;
        this.message = message;
        this.errors = errors;
        this.data = data;
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse("SUCCESS", "success", null, data);
    }

    public static <T> BaseResponse<T> error(String code, String message, List<String> errors) {
        return new BaseResponse(code, message, errors, null);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public T getData() {
        return data;
    }
}
