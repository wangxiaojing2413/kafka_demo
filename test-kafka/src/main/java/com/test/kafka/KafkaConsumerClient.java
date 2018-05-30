package com.test.kafka;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.Message;
import kafka.message.MessageAndMetadata;

/**
 * User: guanqing-liu 
 */
public class KafkaConsumerClient {

	private String groupid; //can be setting by spring
	private String zkConnect;//can be setting by spring
	private String location = "kafka-consumer.properties";//配置文件位置
	private String topic;
	private int partitionsNum;
	private MessageExecutor executor; //message listener
	private ExecutorService threadPool;
	
	private ConsumerConnector connector;
	
	private Charset charset = Charset.forName("utf8");

	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}

	public void setZkConnect(String zkConnect) {
		this.zkConnect = zkConnect;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void setPartitionsNum(int partitionsNum) {
		this.partitionsNum = partitionsNum;
	}

	public void setExecutor(MessageExecutor executor) {
		this.executor = executor;
	}

	public KafkaConsumerClient() {}

	//init consumer,and start connection and listener
	public void init() throws Exception {
		if(executor == null){
			throw new RuntimeException("KafkaConsumer,exectuor cant be null!");
		}
		Properties properties = new Properties();
		properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(location));
		
		if(groupid != null){
			properties.put("groupid", groupid);
		}
		if(zkConnect != null){
			properties.put("zookeeper.connect", zkConnect);
		}
		ConsumerConfig config = new ConsumerConfig(properties);

		connector = Consumer.createJavaConsumerConnector(config);
		Map<String, Integer> topics = new HashMap<String, Integer>();
		topics.put(topic, partitionsNum);
		Map<String, List<KafkaStream<byte[], byte[]>>> streams = connector.createMessageStreams(topics);
		List<KafkaStream<byte[], byte[]>> partitions = streams.get(topic);
		threadPool = Executors.newFixedThreadPool(partitionsNum * 2);
		
		//start
		for (KafkaStream<byte[], byte[]> partition : partitions) {
			threadPool.execute(new MessageRunner(partition));
		}
	}

	public void close() {
		try {
			threadPool.shutdownNow();
		} catch (Exception e) {
			//
		} finally {
			connector.shutdown();
		}

	}

	class MessageRunner implements Runnable {
		private KafkaStream<byte[], byte[]> partition;

		MessageRunner(KafkaStream<byte[], byte[]> partition) {
			this.partition = partition;
		}

		public void run() {
			ConsumerIterator<byte[], byte[]> it = partition.iterator();
			while (it.hasNext()) {
				// connector.commitOffsets();手动提交offset,当autocommit.enable=false时使用
				MessageAndMetadata<byte[], byte[]> item = it.next();
				try{
					executor.execute(new String(item.message(),charset));// UTF-8,注意异常
				}catch(Exception e){
					//
				}
			}
		}
		
		public String getContent(Message message){
            ByteBuffer buffer = message.payload();
            if (buffer.remaining() == 0) {
                return null;
            }
            CharBuffer charBuffer = charset.decode(buffer);
            return charBuffer.toString();
		}
	}

	interface MessageExecutor {

		public void execute(String message);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		KafkaConsumerClient consumer = null;
		try {
			MessageExecutor executor = new MessageExecutor() {

				public void execute(String message) {
					System.out.println(message);
				}
			};
			consumer = new KafkaConsumerClient();
			
			consumer.setTopic("test-topic");
			consumer.setPartitionsNum(2);
			consumer.setExecutor(executor);
			consumer.init();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			 if(consumer != null){
				 consumer.close();
			 }
		}

	}

}
