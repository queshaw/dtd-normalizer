package com.kendallshaw.dtdnormalizer;

import java.util.regex.Pattern;

public interface Patterns {

    static String NAME_START_CHAR =
        ":_a-zA-Z"
        + "\u00C0-\u00D6"
        + "\u00D8-\u00F6"
        + "\u00F8-\u02FF"
        + "\u0370-\u037D"
        + "\u037F-\u1FFF"
        + "\u200C-\u200D"
        + "\u2070-\u218F"
        + "\u2C00-\u2FEF"
        + "\u3001-\uD7FF"
        + "\uF900-\uFDCF"
        + "\uFDF0-\uFFFD"
        + new String(Character.toChars(0x10000))
        + "-"
        + new String(Character.toChars(0xEFFFF));

    static String NAME_CHAR =
            "-."
            + "0-9"
            + "\u00B7"
            + "\u0300-\u036F"
            + "\u203F-\u2040"
            + NAME_START_CHAR;

    static Pattern CHAR_REF_REX =
        Pattern.compile("&#([0-9]+|x[0-9a-fA-Z]+);");

    static Pattern GE_REX =
        Pattern.compile(String.format("&([%s][%s]*);",
                                      NAME_START_CHAR, NAME_CHAR));

    static Pattern PE_REX =
        Pattern.compile(String.format("%%([%s][%s]*);",
                                      NAME_START_CHAR, NAME_CHAR));

    static Pattern E_REX =
        Pattern.compile(String.format("(%%|&#?)([%s][%s]*);",
                                      NAME_START_CHAR, NAME_CHAR));
}
