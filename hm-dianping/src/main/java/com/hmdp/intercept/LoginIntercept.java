package com.hmdp.intercept;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

public class LoginIntercept implements HandlerInterceptor {


    private StringRedisTemplate stringRedisTemplate;

    public LoginIntercept(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authorization = request.getHeader("authorization");
        if ("".equals(authorization) || authorization == null){
            response.setStatus(401);
            return false;
        }
        //解析token，找到用户，存rusession
        String key = LOGIN_USER + authorization;

        Map<Object, Object> objectMap = stringRedisTemplate.opsForHash().entries(key);
        if (objectMap.isEmpty()){
            response.setStatus(401);
            return false;
        }
        UserDTO userDTO = BeanUtil.fillBeanWithMap(objectMap, new UserDTO(), true);
        stringRedisTemplate.expire(key,LOGIN_USER_TTL, TimeUnit.SECONDS);
        UserHolder.saveUser(userDTO);
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
