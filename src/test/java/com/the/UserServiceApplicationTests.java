package com.the;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

@SpringBootTest
@Profile("test")
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
