package is.hi.hbv501.bokamarkadur.bokamarkadur.Wrappers;

import java.util.List;

public class GenericResponse {
    private String message;
    private List<?> errors;

    GenericResponse() {
    }

    GenericResponse(String message, List<?> errors) {
        this.message = message;
        this.errors = errors;
    }

    public String getMsg() {
        return message;
    }

    public void setMsg(String message) {
        this.message = message;
    }

    public List<?> getErrors() {
        return errors;
    }

    public void setErrors(List<?> errors) {
        this.errors = errors;
    }

    public boolean isOk() {
        return (errors == null || errors.size() == 0);
    }

    public void setOk(boolean ok) {
    }
}
