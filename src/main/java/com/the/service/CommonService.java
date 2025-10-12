package com.the.service;

import com.the.dto.request.ResetPasswordEvent;
import com.the.exception.InvalidDataException;
import com.the.model.RedisToken;
import com.the.model.User;
import com.the.util.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void resendLink(String email, User actor){
        User user = userService.getUserByEmail(email);

        boolean isActorManager = checkRole(actor, UserType.MANAGER);
        boolean isTargetManager = checkRole(user, UserType.MANAGER);
        boolean isTargetAdmin = checkRole(user, UserType.ADMIN);

        if(isActorManager){
            if(isTargetAdmin || isTargetManager){
                throw new IllegalArgumentException("Manager khong the send link toi manager hoac admin");
            }
            if(!actor.getId().equals(user.getCreateBy())){
                throw new InvalidDataException("Manager chi co the send link toi nguoi ma minh tao");
            }
        }

        if(!user.isEnabled()){
            // generate reset token
            String resetToken = jwtService.generateResetToken(user);
            redisTokenService.save(RedisToken.builder().id(user.getUsername()).resetToken(resetToken).build());
            ResetPasswordEvent event = new ResetPasswordEvent(user.getEmail(), user.getUsername(), resetToken, "setup");
            kafkaTemplate.send("reset-password-topic", event);
        }

    }

    private boolean checkRole(User user, UserType userType){
        return user.getRoles().stream().anyMatch(uhr -> uhr.getRole().getName().equals(userType.name()));
    }
}
