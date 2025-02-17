package com.capstone.toolScheduler.dto.ack;

public interface Acknowledgement<T> {
    String getAcknowledgementId();
    T getPayload();
}
