package win.panyong.utils;

import com.alibaba.fastjson2.JSONObject;

public class AppException extends RuntimeException {
    public static final String service = "demo-server";
    private Integer errorCode = 0;

    public AppException(String message) {
        super(message);
    }

    public AppException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppException(Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return new JSONObject()
                .fluentPut("service", service)
                .fluentPut("errorCode", errorCode)
                .fluentPut("message", this.getMessage())
                .toJSONString();
    }
}
