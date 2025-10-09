package com.the.service.impl;

import com.the.configuration.Translator;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.the.dto.request.UserRequestDTO;
import com.the.dto.response.PageResponse;
import com.the.dto.response.UserDetailResponse;
import com.the.exception.ResourceNotFoundException;
import com.the.model.User;
import com.the.repository.SearchRepository;
import com.the.repository.UserRepository;
import com.the.service.MailService;
import com.the.service.UserService;
import com.the.util.UserStatus;
import com.the.util.UserType;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.the.util.AppConst.SORT_BY;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
    private final MailService mailService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long saveUser(UserRequestDTO request) throws MessagingException, UnsupportedEncodingException {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .status(request.getStatus())
                .build();

        User result = userRepository.save(user);

        log.info("User has saved!");

//        if (result != null) {
//            // mailService.sendConfirmLink(user.getEmail(), user.getId(), "code@123");
//            kafkaTemplate.send("confirm-account-topic", String.format("email=%s,id=%s,code=%s", user.getEmail(), user.getId(), "code@123"));
//        }

        return user.getId();
    }

    @Override
    public void updateUser(long userId, UserRequestDTO request) {
        User user = getUserById(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        if (!request.getEmail().equals(user.getEmail())) {
            // check email from database if not exist then allow update email otherwise throw exception
            user.setEmail(request.getEmail());
        }
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setStatus(request.getStatus());
        userRepository.save(user);

        log.info("User updated successfully");
    }


    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .id(userId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();

        if (StringUtils.hasLength(sortBy)) {
            // firstName:asc|desc
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        Page<User> users = userRepository.findAll(pageable);
        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> orders = new ArrayList<>();

        if (sorts != null) {
            for (String sortBy : sorts) {
                log.info("sortBy: {}", sortBy);
                // firstName:asc|desc
                Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
                Matcher matcher = pattern.matcher(sortBy);
                if (matcher.find()) {
                    if (matcher.group(3).equalsIgnoreCase("asc")) {
                        orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                    } else {
                        orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                    }
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(orders));

        Page<User> users = userRepository.findAll(pageable);
        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String search, String sort) {
        return searchRepository.searchUser(pageNo, pageSize, search, sort);
    }

    @Override
    public PageResponse<?> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, String address, String... search) {
//        return searchRepository.searchUserByCriteria(pageNo, pageSize, sortBy, address, search);
        return null;
    }

    @Override
    public User getByUsername(String userName) {
        return userRepository.findByUsername(userName).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public long saveUser(User user) {
        userRepository.save(user);
        return user.getId();
    }

    /**
     * Delete user by userId
     *
     * @param userId
     */
    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
        log.info("User has deleted permanent successfully, userId={}", userId);
    }

    /**
     * Get all user per pageNo and pageSize
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public PageResponse<?> getAllUsers(int pageNo, int pageSize) {
        Page<User> page = userRepository.findAll(PageRequest.of(pageNo, pageSize));

        List<UserDetailResponse> list = page.stream().map(user -> UserDetailResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .build())
                .toList();

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(list)
                .build();
    }

    @Override
    public List<String> getAllRolesByUserId(long userId) {
        return userRepository.findAllRolesByUserId(userId);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("Email not found"));
    }

    /**
     * Get user by userId
     *
     * @param userId
     * @return User
     */
    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(Translator.toLocale("user.not.found")));
    }

}
