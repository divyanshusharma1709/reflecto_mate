package com.sgih.dialogactivity;

public class Message {
    private String mUMessage;
    private String mCmessage;
    Message(String message, String uMessage)
    {
        mUMessage = message;
        mCmessage = uMessage;
    }

    public String getUMessage() {
        return mUMessage;
    }
    public String getCmessage(){
        return mCmessage;
    }
}
