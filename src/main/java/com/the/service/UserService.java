package com.the.service;

import com.the.model.User;
import jakarta.mail.MessagingException;
import com.the.dto.request.UserRequestDTO;
import com.the.dto.response.PageResponse;
import com.the.dto.response.UserDetailResponse;
import com.the.util.UserStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public interface UserService {

    UserDetailsService userDetailsService();

    long saveUser(UserRequestDTO request) throws MessagingException, UnsupportedEncodingException;

    void updateUser(long userId, UserRequestDTO request);

    void changeStatus(long userId, UserStatus status);

    String confirmUser(int userId, String verifyCode);

    void deleteUser(long userId);

    UserDetailResponse getUser(long userId);

    PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy);

    PageResponse<?> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts);

    PageResponse<?> getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String search, String sortBy);

    PageResponse<?> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, String address, String... search);

    User getByUsername(String userName);

    long saveUser(User user);

    PageResponse<?> getAllUsers(int pageNo, int pageSize);

    List<String> getAllRolesByUserId(long userId);

    User getUserByEmail(String email);
}
