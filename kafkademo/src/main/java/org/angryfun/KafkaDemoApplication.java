package org.angryfun;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class KafkaDemoApplication  implements CommandLineRunner {

	@Autowired
	private KafkaTemplate<String, String> template;

	public static void main(String[] args) {
		SpringApplication.run(KafkaDemoApplication.class, args);
	}


	private final CountDownLatch latch = new CountDownLatch(5);

	@Override
	public void run(String... args) throws Exception {
		this.template.send("myTopic", "foo1");
		this.template.send("myTopic", "foo2");
		this.template.send("myTopic", "foo3");
		this.template.send("myTopic", "hi", "foo4");
		this.template.send("myTopic2", "2", "foo5");
		latch.await(60, TimeUnit.SECONDS);
		log.info("All received");
	}

	@KafkaListener(topics = {"myTopic","myTopic2"})
	public void listen(ConsumerRecord<?, ?> cr) throws Exception {
		log.info(cr.toString());
		latch.countDown();
	}
}
