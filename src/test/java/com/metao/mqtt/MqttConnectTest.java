//package com.metao.mqtt;
//
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import static org.junit.Assert.assertTrue;
//
///**
// * @author Mehrdad A.Karami at 3/8/19
// **/
//
//@RunWith(SpringRunner.class)
//@PropertySource("classpath:application-test.properties")
//@SpringBootTest
//public class MqttConnectTest{
//
//    protected static Logger log = LoggerFactory.getLogger(MqttConnectTest.class);
//
//    @Value("${mqtt.url}")
//    public String mqttUrl;
//
//    @Value("${mqtt.port}")
//    public Integer mqttPort;
//
//    @Value("${mqtt.username}")
//    public String mqttUsername;
//
//	@Value("${mqtt.password}")
//	public String mqttPassword;
//
//    //Timeout for the Future object until it get completed
//    protected final long timeout = 5000;
//
//    @Test
//    public void connectToMqttServerAndSendMessageTest() throws MqttException {
//        String topic = "MQTT Examples";
//        String content = "Message from MqttPublishSample";
//        int qos = 2;
//        String broker = mqttUrl;
//        String clientId = "mqttClient";
//        MemoryPersistence persistence = new MemoryPersistence();
//        MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
//        MqttConnectOptions connOpts = new MqttConnectOptions();
//        connOpts.setCleanSession(true);
//        connOpts.setUserName(mqttUsername);
//        connOpts.setPassword(mqttPassword.toCharArray());
//        System.out.println("Connecting to broker: " + broker);
//        sampleClient.connect(connOpts);
//		assertTrue(sampleClient.isConnected());
//        System.out.println("Connected");
//        System.out.println("Publishing message: " + content);
//        MqttMessage message = new MqttMessage(content.getBytes());
//        message.setQos(qos);
//        sampleClient.publish(topic, message);
//        System.out.println("Message published");
//        sampleClient.disconnect();
//        System.out.println("Disconnect");
//    }
//
//}
