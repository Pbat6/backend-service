package com.the.service.impl;

import com.the.configuration.Translator;
import com.the.dto.request.ResetPasswordEvent;
import com.the.dto.request.UpdateUserDTO;
import com.the.exception.InvalidDataException;
import com.the.model.RedisToken;
import com.the.model.Role;
import com.the.model.UserHasRole;
import com.the.repository.RoleRepository;
import com.the.repository.UserHasRoleRepository;
import com.the.service.JwtService;
import com.the.service.RedisTokenService;
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
import com.the.dto.request.SignUpDTO;
import com.the.dto.response.PageResponse;
import com.the.dto.response.UserDetailResponse;
import com.the.exception.ResourceNotFoundException;
import com.the.model.User;
import com.the.repository.SearchRepository;
import com.the.repository.UserRepository;
import com.the.service.UserService;
import com.the.util.UserStatus;
import com.the.util.UserType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.the.util.SearchConst.SORT_BY;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RoleRepository roleRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsernameWithAuthorities(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveUser(SignUpDTO request, User actor) {
        boolean isActorManager = checkRole(actor, UserType.MANAGER);

        if (isActorManager && (request.getRole().equals(UserType.MANAGER.name()) || request.getRole().equals(UserType.ADMIN.name()))) {
            throw new IllegalArgumentException("Manager khong the tao manager hoac admin");
        }

        // Check if username or email exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
             throw new InvalidDataException("Username already exists.");
        }

        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
             throw new InvalidDataException("Email already exists.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(null)
                .status(UserStatus.INACTIVE)
                .createBy(actor.getId())
                .build();

        userRepository.save(user);

        // Assign role
        Role role = roleRepository.findByNameIgnoreCase(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        UserHasRole userHasRole = UserHasRole.builder().user(user).role(role).build();
        userHasRoleRepository.save(userHasRole);

        // Generate setup token and send email via Kafka
        String resetToken = jwtService.generateResetToken(user);
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).resetToken(resetToken).build());

        ResetPasswordEvent event = new ResetPasswordEvent(
                user.getEmail(), user.getUsername(), resetToken, "setup"
        );
        kafkaTemplate.send("reset-password-topic", event);

        return user.getId();
    }

    @Override
    public void updateUser(long userId, UpdateUserDTO request, User actor) {
        User user = getUserById(userId);

        boolean isSelf = actor.getId().equals(user.getId());
        boolean isActorUser = checkRole(actor, UserType.USER);
        boolean isActorManager = checkRole(actor, UserType.MANAGER);
        boolean isActorAdmin = checkRole(actor, UserType.ADMIN);
        boolean isTargetManager = checkRole(user, UserType.MANAGER);
        boolean isTargetAdmin = checkRole(user, UserType.ADMIN);

        if(!isSelf){
            if(isActorUser){
                throw new InvalidDataException("user chi co the update ban than");
            }else if(isActorManager){
                if(isTargetAdmin || isTargetManager){
                    throw new InvalidDataException("Manager khong the update manager hoac admin khac");
                }else if(!actor.getId().equals(user.getCreateBy())){
                    throw new InvalidDataException("Manager chi co the update nguoi ma minh tao");
                }
            }
        }

        if(isActorAdmin){
            Role role = roleRepository.findByNameIgnoreCase(request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            UserHasRole userHasRole = UserHasRole.builder().user(user).role(role).build();
            userHasRoleRepository.save(userHasRole);
        }

        if(!user.getEmail().equals(request.getEmail())){
            Boolean isEmailExists = userRepository.existsByEmail(request.getEmail());
            if(isEmailExists){
                throw new InvalidDataException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setCity(request.getCity());
        user.setUpdatedBy(actor.getId());
        userRepository.save(user);

        log.info("User updated successfully");
    }


    @Override
    public UserDetailResponse getUser(long userId, User actor) {
        User user = getUserById(userId);

        boolean isSelf = actor.getId().equals(user.getId());
        boolean isActorUser = checkRole(actor, UserType.USER);
        boolean isActorManager = checkRole(actor, UserType.MANAGER);
        boolean isTargetManager = checkRole(user, UserType.MANAGER);
        boolean isTargetAdmin = checkRole(user, UserType.ADMIN);

        if(!isSelf){
            if(isActorUser){
                throw new InvalidDataException("user chi co the lay thong tin ban than");
            }else if(isActorManager && (isTargetAdmin || isTargetManager)){
                throw new InvalidDataException("Manager khong the lay thong tin manager hoac admin khac");
            }
        }

        return UserDetailResponse.builder()
                .id(userId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(uhr -> uhr.getRole().getName()).toList())
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy, User actor) {
            int page = pageNo + 1;

        List<Sort.Order> sorts = new ArrayList<>();

        if (StringUtils.hasLength(sortBy)) {
            // sortBy = firstName:asc|desc
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

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));

        boolean isActorManager = checkRole(actor, UserType.MANAGER);
        Page<User> users;
        try{
            if(isActorManager){
                users = userRepository.findUsersByRoleName(UserType.USER.name(), pageable);
            }else{
                users = userRepository.findAll(pageable);
            }
        }catch (Exception e){
            throw new InvalidDataException("Sai column name");
        }


        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles().stream().map(uhr -> uhr.getRole().getName()).toList())
                .build()).toList();
        return PageResponse.builder()
                .pageNo(page)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .totalItem(users.getTotalElements())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<?> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, User actor, String... search) {
        return searchRepository.searchUserByCriteria(pageNo, pageSize, sortBy, actor, search);
    }

    @Override
    public User getByUsername(String userName) {
        return userRepository.findByUsername(userName).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Delete user by userId
     *
     * @param userId
     */
    @Override
    public void deleteUser(long userId, User actor) {
        boolean isActorManager = checkRole(actor, UserType.MANAGER);
        User user = getUserById(userId);

        boolean isTargetManager = checkRole(user, UserType.MANAGER);
        boolean isTargetAdmin = checkRole(user, UserType.ADMIN);

        if(isActorManager) {
            if (isTargetAdmin || isTargetManager) {
                throw new InvalidDataException("Manager khong the delete manager hoac admin khac");
            } else if (!actor.getId().equals(user.getCreateBy())) {
                throw new InvalidDataException("Manager chi co the delete nguoi ma minh tao");
            }
        }

        userRepository.deleteById(userId);
        redisTokenService.remove(user.getUsername());
        log.info("User has deleted permanent successfully, userId={}", userId);
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

    private boolean checkRole(User user, UserType userType){
        return user.getRoles().stream().anyMatch(uhr -> uhr.getRole().getName().equals(userType.name()));
    }

}
