package org.ssi.model;

import java.io.Serializable;

public class MessageRespone implements Serializable{

    private static final long serialVersionUID = 1L;
    public static final int MESSAGE_ERRCODE_OK = 0;
    public static final int MESSAGE_ERRCODE_FAIL = 1;
    
    private int errorCode;
    private int messageCode;
    private String message;
    private byte eventType;

    
    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }
    
    public void defaultMessage() {
	this.errorCode = MESSAGE_ERRCODE_OK;
    }
    
    public void setFail() {
	this.errorCode = MESSAGE_ERRCODE_FAIL;
    }
    
    public boolean isSuccess() {
	return MESSAGE_ERRCODE_OK == this.errorCode;
    }

    public byte getEventType() {
	return eventType;
    }

    public void setEventType(byte eventType) {
	this.eventType = eventType;
    }

}
