package com.rilchil.mythoughts;

public class ThoughtTable {

    private ThoughtTable() {

    }
    public static final String TABLE_NAME = "THOUGHT_TABLE";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "TITLE";
    public static final String COLUMN_THOUGHT_TEXT = "THOUGHT_TEXT";
    public static final String COLUMN_DATE = "DATE_CREATED";
    public static final String COLUMN_TIME = "TIME_CREATED";

    public static final String[] COLUMNS = {COLUMN_ID, COLUMN_TITLE, COLUMN_THOUGHT_TEXT,
        COLUMN_DATE, COLUMN_TIME};

}
