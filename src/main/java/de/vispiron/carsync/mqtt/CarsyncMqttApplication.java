package de.vispiron.carsync.mqtt;

import de.vispiron.carsync.mqtt.server.TCPServerAcceptorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarsyncMqttApplication implements CommandLineRunner {

	@Autowired
	TCPServerAcceptorHandler acceptor;

	public static void main(String[] args) {
		SpringApplication.run(CarsyncMqttApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		acceptor.initialize();//initialize the mqtt server
	}
}
