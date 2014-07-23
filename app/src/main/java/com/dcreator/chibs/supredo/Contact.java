package com.dcreator.chibs.supredo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chibs on 2014-07-06.
 */
public class Contact {
    private String _ID;
    private String DISPLAY_NAME;
    private List<String> phones;

    public String getDisplayName() {
        return DISPLAY_NAME;
    }
    public void setDisplayName(String name) {
        this.DISPLAY_NAME = name;
    }

    public void setID(String id) {
        this._ID = id;
    }

    public List<String> getPhoneList()
    {
        return this.phones;
    }

    public void addPhoneNumber(String phoneNumber)
    {
        if(phones == null)
        {
            phones = new ArrayList<String>();
        }

        phones.add(phoneNumber);
    }
}
