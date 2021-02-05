package com.arteriatech.geotrack.rspl.interfaces;

import android.view.View;
import android.widget.EditText;

/**
 * Created by e10769 on 03-04-2017.
 */

public interface FocusOnTextChangeInterface  {
    void onTextChange(View v, boolean hasFocus, int poss, EditText editText);
    void setOnTouch(View v, int poss);
}
