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

    PageResponse<?> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, User actor, String... search);

    User getByUsername(String userName);

    List<String> getAllRolesByUserId(long userId);

    User getUserByEmail(String email);
}
