package com.the.controller;

import com.the.dto.request.UpdateUserDTO;
import com.the.dto.response.UserDetailResponse;
import com.the.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.the.configuration.Translator;
import com.the.dto.request.SignUpDTO;
import com.the.dto.response.ResponseData;
import com.the.service.UserService;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
@Tag(name = "User Controller")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(method = "POST", summary = "Add new user", description = "Send a request via this API to create new user")
    @PostMapping(value = "/")
    public ResponseData<Long> addUser(@Valid @RequestBody SignUpDTO user) {
        log.info("Request add user, {} {}", user.getFirstName(), user.getLastName());
        User actor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = userService.saveUser(user, actor);
        return new ResponseData<>(HttpStatus.CREATED.value(), Translator.toLocale("user.add.success"), userId);

    }

    @Operation(summary = "Update user", description = "Send a request via this API to update user")
    @PutMapping("/{userId}")
    public ResponseData<?> updateUser(@PathVariable @Min(1) long userId, @Valid @RequestBody UpdateUserDTO user) {
        log.info("Request update userId={}", userId);
        User actor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updateUser(userId, user, actor);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), Translator.toLocale("user.upd.success"));
    }

    @Operation(summary = "Delete user permanently", description = "Send a request via this API to delete user permanently")
    @DeleteMapping("/{userId}")
    public ResponseData<?> deleteUser(@PathVariable @Min(value = 1, message = "userId must be greater than 0") int userId) {
        log.info("Request delete userId={}", userId);
        User actor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.deleteUser(userId, actor);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), Translator.toLocale("user.del.success"));

    }

    @Operation(summary = "Get user detail", description = "Send a request via this API to get user information")
    @GetMapping("/{userId}")
    public ResponseData<UserDetailResponse> getUser(@PathVariable @Min(1) long userId) {
        log.info("Request get user detail, userId={}", userId);
        User actor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new ResponseData<>(HttpStatus.OK.value(), "Get one user successful", userService.getUser(userId, actor));
    }

    @Operation(summary = "Get list of users per pageNo", description = "Send a request via this API to get user list by pageNo and pageSize")
    @GetMapping("/list")
    public ResponseData<?> getAllUsers(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                       @Min(1) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                       @RequestParam(required = false) String sortBy) {
        log.info("Request get all of users");
        User actor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new ResponseData<>(HttpStatus.OK.value(), "users list", userService.getAllUsersWithSortBy(pageNo, pageSize, sortBy, actor));
    }

//    @Operation(summary = "Get list of users with sort by multiple columns", description = "Send a request via this API to get user list by pageNo, pageSize and sort by multiple column")
//    @GetMapping("/list-with-sort-by-multiple-columns")
//    public ResponseData<?> getAllUsersWithSortByMultipleColumns(@RequestParam(defaultValue = "0", required = false) int pageNo,
//                                                                @RequestParam(defaultValue = "20", required = false) int pageSize,
//                                                                @RequestParam(required = false) String... sorts) {
//        log.info("Request get all of users with sort by multiple columns");
//        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.getAllUsersWithSortByMultipleColumns(pageNo, pageSize, sorts));
//    }
//
//    @Operation(summary = "Get list of users and search with paging and sorting by customize query", description = "Send a request via this API to get user list by pageNo, pageSize and sort by multiple column")
//    @GetMapping("/list-user-and-search-with-paging-and-sorting")
//    public ResponseData<?> getAllUsersAndSearchWithPagingAndSorting(@RequestParam(defaultValue = "0", required = false) int pageNo,
//                                                                    @RequestParam(defaultValue = "20", required = false) int pageSize,
//                                                                    @RequestParam(required = false) String search,
//                                                                    @RequestParam(required = false) String sortBy) {
//        log.info("Request get list of users and search with paging and sorting");
//        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.getAllUsersAndSearchWithPagingAndSorting(pageNo, pageSize, search, sortBy));
//    }
//
//    @Operation(summary = "Advance search query by criteria", description = "Send a request via this API to get user list by pageNo, pageSize and sort by multiple column")
//    @GetMapping("/advance-search-with-criteria")
//    public ResponseData<?> advanceSearchWithCriteria(@RequestParam(defaultValue = "0", required = false) int pageNo,
//                                                     @RequestParam(defaultValue = "20", required = false) int pageSize,
//                                                     @RequestParam(required = false) String sortBy,
//                                                     @RequestParam(required = false) String address,
//                                                     @RequestParam(defaultValue = "") String... search) {
//        log.info("Request advance search query by criteria");
//        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.advanceSearchWithCriteria(pageNo, pageSize, sortBy, address, search));
//    }
}
