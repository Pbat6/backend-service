package com.the.service;

import com.the.model.User;
import com.the.repository.RoleRepository;
import com.the.repository.SearchRepository;
import com.the.repository.UserHasRoleRepository;
import com.the.repository.UserRepository;
import com.the.service.impl.UserServiceImpl;
import com.the.util.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserService userService;

    private static User admin;
    private static User manager;


    private @Mock UserRepository userRepository;
    private @Mock SearchRepository searchRepository;
    private @Mock KafkaTemplate<String, Object> kafkaTemplate;
    private @Mock RoleRepository roleRepository;
    private @Mock UserHasRoleRepository userHasRoleRepository;
    private @Mock JwtService jwtService;
    private @Mock RedisTokenService redisTokenService;

    @BeforeAll
    static void beforeAll(){
        admin = User.builder()
                .id(1L)
                .firstName("admin")
                .lastName("admin")
                .email("admin@abc.com")
                .phone("1234456787")
                .status(UserStatus.ACTIVE)
                .password("a")
                .dateOfBirth(new Date("10/10/2020"))
                .build();

        manager = User.builder()
                .id(2L)
                .firstName("manager")
                .lastName("manager")
                .email("manager@abc.com")
                .phone("1234456787")
                .status(UserStatus.ACTIVE)
                .password("a")
                .dateOfBirth(new Date("11/11/2020"))
                .build();
    }

    @BeforeEach
    void setUp() {
        // Khoi tao buoc trien khai la UserService
        userService = new UserServiceImpl(userRepository, searchRepository, kafkaTemplate, roleRepository, userHasRoleRepository, jwtService, redisTokenService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void userDetailsService() {
    }

    @Test
    void saveUser() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void getUser() {
    }

    @Test
    void getAllUsersWithSortBy_Success() {
        Page<User> users = new PageImpl<>(Arrays.asList(admin, manager));
//        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);
    }

    @Test
    void advanceSearchWithCriteria() {
    }

    @Test
    void getByUsername() {
    }

    @Test
    void getAllRolesByUserId() {
    }

    @Test
    void getUserByEmail() {
    }
}