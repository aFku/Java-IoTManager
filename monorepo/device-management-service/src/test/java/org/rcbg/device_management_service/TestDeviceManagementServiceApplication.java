package org.rcbg.device_management_service;

import org.springframework.boot.SpringApplication;

public class TestDeviceManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(DeviceManagementServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
