package com.the;

import com.the.controller.AuthenticationController;
import com.the.controller.CommonController;
import com.the.controller.UserController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

@SpringBootTest
@Profile("test")
class BackendServiceApplicationTests {

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private AuthenticationController authenticationController;

    @InjectMocks
    private CommonController commonController;

	@Test
	void contextLoads() {
        Assertions.assertNotNull(userController);
        Assertions.assertNotNull(authenticationController);
        Assertions.assertNotNull(commonController);
	}

}
