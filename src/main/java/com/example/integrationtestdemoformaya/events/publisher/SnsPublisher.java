package com.example.integrationtestdemoformaya.events.publisher;

import com.amazonaws.services.sns.AmazonSNS;

public abstract class SnsPublisher<T> {
    private final AmazonSNS amazonSNS;

    protected SnsPublisher(AmazonSNS amazonSNS) {
        this.amazonSNS = amazonSNS;
    }

    public abstract void publish(T t);

    protected void publish(String topicArn, String payload) {
        amazonSNS.publish(topicArn, payload);
    }
}
