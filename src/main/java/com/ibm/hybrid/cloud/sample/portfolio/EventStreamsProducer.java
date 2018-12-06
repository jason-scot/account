/*
       Copyright 2017 IBM Corp All Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ibm.hybrid.cloud.sample.stocktrader.portfolio;

import java.net.ConnectException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.BasicConfigurator;


/** Producer class generated by the Event Streams sample producer generator.  I've left as-is, except for
 *  adding the Apache license, renaming the class, and renaming the two environment variables to conform
 *  to my naming conventions.
 */
public class EventStreamsProducer {

    private final String topic;
    private final String USERNAME = System.getenv("KAFKA_USER");
    private final String API_KEY = System.getenv("KAFKA_API_KEY");

    private KafkaProducer<String, String> kafkaProducer;
    
    private Logger logger = Logger.getLogger(EventStreamsProducer.class.getName());

    public EventStreamsProducer(String bootstrapServerAddress, String topic) throws InstantiationException {
        BasicConfigurator.configure();
        this.topic = topic;
        if (topic == null) {
            throw new InstantiationException("Missing required topic name.");
        } else if (bootstrapServerAddress == null) {
            throw new InstantiationException("Missing required bootstrap server address.");
        }
        try {
            kafkaProducer = createProducer(bootstrapServerAddress);
        } catch (KafkaException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    private KafkaProducer<String, String> createProducer(String brokerList) {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        properties.put(CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 4000);
        properties.put(ProducerConfig.RETRIES_CONFIG, 0);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.2");
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "resources/security/certs.jks");
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");
        properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        String saslJaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
            + USERNAME + "\" password=" + API_KEY + ";";
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
        
        KafkaProducer<String, String> kafkaProducer = null;
        
        try {
            kafkaProducer = new KafkaProducer<>(properties);
        } catch (KafkaException kafkaError ) {
            logger.warning("Error while creating producer: "+kafkaError.getMessage());
            throw kafkaError;
        }
        return kafkaProducer;
    }

    public RecordMetadata produce(String message) throws InterruptedException, ExecutionException, ConnectException {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, message);
        RecordMetadata recordMetadata = kafkaProducer.send(record).get();
        return recordMetadata;
    }

    public void shutdown() {
        kafkaProducer.flush();
        kafkaProducer.close();
    }
}
