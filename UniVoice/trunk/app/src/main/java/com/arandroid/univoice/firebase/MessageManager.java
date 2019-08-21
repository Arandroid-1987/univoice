package com.arandroid.univoice.firebase;

import com.arandroid.univoice.model.Message;
import com.arandroid.univoice.utils.GenericConstants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MessageManager {
    private static MessageManager instance;
    private FirebaseFirestore firestore;

    private MessageManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public static MessageManager getInstance() {
        if (instance == null) {
            instance = new MessageManager();
        }
        return instance;
    }

    public static String getSenderCollection(String senderUid, String receiverUid) {
        return GenericConstants.FIREBASE_TABLE_USERS + "/" + senderUid + "/"
                + GenericConstants.FIREBASE_TABLE_RECEIVER + "/" + receiverUid + "/"
                + GenericConstants.FIREBASE_TABLE_MESSAGES;
    }

    public static String getReceiverCollection(String senderUid, String receiverUid) {
        return GenericConstants.FIREBASE_TABLE_USERS + "/" + receiverUid + "/"
                + GenericConstants.FIREBASE_TABLE_RECEIVER + "/" + senderUid + "/"
                + GenericConstants.FIREBASE_TABLE_MESSAGES;
    }

    public void writeMessage(Message msg) {
        DocumentReference ref = firestore.collection(getSenderCollection(msg.getSenderUid(), msg.getReceiverUid())).document();
        ref.set(msg);
        firestore.collection(getReceiverCollection(msg.getSenderUid(), msg.getReceiverUid())).document(ref.getId()).set(msg);
    }
}
