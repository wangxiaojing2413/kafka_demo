package org.angryfun.purejava;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liufang on 17/5/11.
 */
public class KafkaStream {
    public KafkaStream(){
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-stream-processing-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaProperties.kafkaServer);
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        StreamsConfig config = new StreamsConfig(props);

        KStreamBuilder builder = new KStreamBuilder();
        builder.stream("topic1").mapValues(value -> value.toString()+" from topic1").to("topic2");

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();
    }
    public static void main(String[] args){
       new KafkaStream();
    }
}
