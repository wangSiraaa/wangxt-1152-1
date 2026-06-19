package nc.action.forest;

import java.io.Serializable;

public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Object data;
    private int code;

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_ERROR = 500;

    public Result() {
    }

    public Result(boolean success, String message, Object data, int code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    public static Result success() {
        return new Result(true, "操作成功", null, CODE_SUCCESS);
    }

    public static Result success(Object data) {
        return new Result(true, "操作成功", data, CODE_SUCCESS);
    }

    public static Result success(String message, Object data) {
        return new Result(true, message, data, CODE_SUCCESS);
    }

    public static Result error(String message) {
        return new Result(false, message, null, CODE_ERROR);
    }

    public static Result error(String message, int code) {
        return new Result(false, message, null, code);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
