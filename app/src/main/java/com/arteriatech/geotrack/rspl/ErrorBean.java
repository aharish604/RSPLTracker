package com.arteriatech.geotrack.rspl;

/**
 * Created by e10526 on 6/23/2017.
 */

public class ErrorBean {
    private int ErrorCode = 0;
    private String ErrorMsg = "";
    private boolean hasNoError = false;
    private boolean isStoreFailed = false;

    public String getErrorMsg() {
        return ErrorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        ErrorMsg = errorMsg;
    }

    public int getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(int errorCode) {
        ErrorCode = errorCode;
    }

    public boolean hasNoError() {
        return hasNoError;
    }

    public void setHasNoError(boolean hasNoError) {
        this.hasNoError = hasNoError;
    }

    public boolean isStoreFailed() {
        return isStoreFailed;
    }

    public void setStoreFailed(boolean storeFailed) {
        isStoreFailed = storeFailed;
    }
}
