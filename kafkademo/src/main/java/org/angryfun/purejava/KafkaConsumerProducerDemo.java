package org.angryfun.purejava;

public class KafkaConsumerProducerDemo
{
    public static void main(String[] args)
    {
        MyKafkaProducer producerThread = new MyKafkaProducer(KafkaProperties.topic);
        producerThread.start();
        new MyKafkaConsumer();
        //new MyAsyncKafkaConsumer(KafkaProperties.topic);
        new KafkaStream();//将topic1的消息转发给topic2
    }
}