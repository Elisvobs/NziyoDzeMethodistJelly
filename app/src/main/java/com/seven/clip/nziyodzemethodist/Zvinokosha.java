package com.seven.clip.nziyodzemethodist;

import android.content.Context;

import java.util.Calendar;

/*
 * Created by Bennysway on 07.02.17.
 */

public class Zvinokosha {
    private String today="",savedDate="";
    private Data access;

    public Zvinokosha(Context current){
        Calendar c = Calendar.getInstance();
        access = new Data(current.getApplicationContext(),"accflag");
        savedDate = access.get();
        today = String.valueOf(c.get(Calendar.DATE));
    }

    public void set(){
        access.update(today);
        savedDate = today;
    }

    public void clear(){
        access.deleteAll();
    }

    public boolean hasAccess(){
        return today.equals(savedDate);
    }

    String check(){
        String s;
        if(hasAccess())
            s=" and has access";
        else
            s=" and has no access";
        return "Today:" + today + " Saved:" + savedDate + s;
    }
}
