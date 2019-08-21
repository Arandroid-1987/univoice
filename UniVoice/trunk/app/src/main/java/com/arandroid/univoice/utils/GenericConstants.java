package com.arandroid.univoice.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public interface GenericConstants {
    String FIREBASE_TABLE_USERS = "users";
    String FIREBASE_TABLE_MESSAGES = "messages";
    String FIREBASE_TABLE_RECEIVER = "receiver";
    String FIREBASE_TABLE_SENDER = "sender";

    String FIREBASE_DATE_FORMAT = "yyyyMMdd HH:mm:ss";
    SimpleDateFormat FIREBASE_DATE_FORMATTER = new SimpleDateFormat(FIREBASE_DATE_FORMAT, Locale.ITALIAN);
    String CACHED_CONTACTS = "cached_contacts";
}
