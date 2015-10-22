package com.kendallshaw.dtdnormalizer;

import java.util.regex.Matcher;

import org.apache.xerces.xni.XMLString;

public class TextHandler implements Patterns {

    public static String entityText(XMLString text, XMLString rawText) {
        String textString = text.toString();
        String rawString = rawText.toString();
        if ("".equals(textString))
            return rawString;
        Matcher m = PE_REX.matcher(rawString);
        if (m.find())
            return textString;
        return rawString;
    }
}
