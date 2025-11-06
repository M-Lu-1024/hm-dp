package com.hmdp.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result getType() throws JsonProcessingException {

        String json = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);
        List<ShopType> typeList = null;
        if ("".equals(json) || json == null){
            typeList = query().orderByAsc("sort").list();
            json = objectMapper.writeValueAsString(typeList);
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY,json);
        }else {
            typeList = objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ShopType.class));
        }
        return Result.ok(typeList);
    }
}
