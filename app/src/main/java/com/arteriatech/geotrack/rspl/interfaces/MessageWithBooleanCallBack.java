package com.arteriatech.geotrack.rspl.interfaces;


import com.arteriatech.geotrack.rspl.ErrorBean;

/**
 * Created by e10526 on 6/27/2017.
 */

public interface MessageWithBooleanCallBack {
    void clickedStatus(boolean clickedStatus, String errorMsg, ErrorBean errorBean);
}
