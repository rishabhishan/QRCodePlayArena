package com.semidigit.qrcodeplayarena.Utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilityMethods{

    private Context context;
    public UtilityMethods(Context context) {
        this.context=context;
    }

    public String displayDate(Date d){
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT);
        return formatter.format(d);
    }

    public String getValueOrDefaultString(Object value, String defaultValue) {
        return value.equals(null) ? defaultValue : (String)value;
    }

    public int getValueOrDefaultInt(Object value, int defaultValue) {
        return value.equals(null) ? defaultValue : (int)value;
    }
}


