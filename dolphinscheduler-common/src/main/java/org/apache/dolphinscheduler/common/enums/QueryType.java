package org.apache.dolphinscheduler.common.enums;

public enum QueryType {

    FORM,
    SQL;

    public static QueryType getEnum(int value){
        for (QueryType e:QueryType.values()) {
            if(e.ordinal() == value) {
                return e;
            }
        }
        //For values out of enum scope
        return null;
    }
}
