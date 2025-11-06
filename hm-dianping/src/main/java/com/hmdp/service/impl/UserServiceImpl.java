package com.hmdp.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static com.hmdp.utils.RedisConstants.*;
/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("输入的手机号错误");
        }
        //TODO 生成对应的验证码，返回给前端，验证码放在redis中
        String code = RandomUtil.randomNumbers(6);
        log.info("用户生成的验证码为：{}",code);

        //使用string结构去存储
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone,code);
        stringRedisTemplate.expire(LOGIN_CODE_KEY + phone,LOGIN_USER_TTL,TimeUnit.SECONDS);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();

        //判断用户传入的内容是否合法
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("输入的手机号错误");
        }
        if ("".equals(phone) || "".equals(code)){
            return Result.fail("输入的数据为空");
        }

        //从redis中取出code做校验
        String redisCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if ("".equals(redisCode) ||!code.equals(redisCode) ){
            return Result.fail("验证码错误或未生成验证码");
        }

        //判断该用户是否存在，如果不存在就创建用户
        User user = lambdaQuery().eq(User::getPhone,phone).one();
        if (user == null){
            user = new User();
            user.setCreateTime(LocalDateTime.now());
            user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(6));
            BeanUtil.copyProperties(loginForm,user);
            saveOrUpdate(user);
        }

        UserDTO userDTO = new UserDTO();
        BeanUtil.copyProperties(user,userDTO);
        //把当前用户信息存放到redis中，设置有效期，生成token
        String token = UUID.randomUUID().toString();

        HashOperations hashOperations = stringRedisTemplate.opsForHash();
        hashOperations.putAll(LOGIN_USER+token,BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((FieldName,FieldValue)-> FieldValue.toString())));
        stringRedisTemplate.expire(LOGIN_USER + token,LOGIN_USER_TTL, TimeUnit.SECONDS);

        return Result.ok(token);
    }
}
