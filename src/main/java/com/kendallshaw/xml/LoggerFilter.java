package com.kendallshaw.xml;

public interface LoggerFilter {

    void message(String msg);

    boolean accept(String msg);
}
