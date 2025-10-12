package com.the.service;

import com.the.dto.request.UpdateUserDTO;
import com.the.model.User;
import com.the.dto.request.SignUpDTO;
import com.the.dto.response.PageResponse;
import com.the.dto.response.UserDetailResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    UserDetailsService userDetailsService();

    Long saveUser(SignUpDTO request, User creator);

    void updateUser(long userId, UpdateUserDTO request, User actor);

    void deleteUser(long userId, User actor);

    UserDetailResponse getUser(long userId, User actor);

    PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy, User actor);

    PageResponse<?> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts);

    PageResponse<?> getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String search, String sortBy);

    PageResponse<?> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, String address, String... search);

    User getByUsername(String userName);

    long saveUser(User user);

    PageResponse<?> getAllUsers(int pageNo, int pageSize);

    List<String> getAllRolesByUserId(long userId);

    User getUserByEmail(String email);
}
