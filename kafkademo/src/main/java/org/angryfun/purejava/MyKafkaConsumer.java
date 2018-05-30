package org.angryfun.purejava;

import org.apache.kafka.clients.consumer.*;

import java.util.*;

public class MyKafkaConsumer
{
    private final KafkaConsumer<String,String> consumer;
    private final Properties props = new Properties();
    public MyKafkaConsumer()
    {
        props.put("bootstrap.servers", KafkaProperties.kafkaServer);
        props.put("group.id", KafkaProperties.groupId);
        props.put("auto.offset.reset", "earliest");//latest
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("topic1","topic2"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records)
                System.out.printf("topic=%s, offset = %d, key = %s, value = %s%n",record.topic(), record.offset(), record.key(), record.value());
        }
    }
    public static void main(String[] args)
    {
        new MyKafkaConsumer();
    }
}