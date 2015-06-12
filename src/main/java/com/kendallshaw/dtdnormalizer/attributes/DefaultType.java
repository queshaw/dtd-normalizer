package com.kendallshaw.dtdnormalizer.attributes;

public enum DefaultType {
   IMPLIED("#IMPLIED"),
   REQUIRED("#REQUIRED");
   private String text;
   DefaultType(final String s) {
       text = s;
   }
   public static DefaultType fromString(final String s) {
       for (final DefaultType t : DefaultType.values())
           if (t.toString().equals(s))
               return t;
       throw new IllegalArgumentException("Not enum text: " + s);
   }
   public String toString() {
       return text;
   }
}
