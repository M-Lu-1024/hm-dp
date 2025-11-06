package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryById(Long id) {
        //用string去存储shop
        String key = CACHE_SHOP_KEY + id;

        String jsonShop = stringRedisTemplate.opsForValue().get(key);
        Shop shop = null;

        //解决缓存击穿，是指热点key失效，但是重建时间较长，所以希望只有一个线程去重建
        if (jsonShop == null || "".equals(jsonShop)){
            try{
                //没有内容，那可能是热点key失效了
                boolean tryLock = tryLock(key);
                if (!tryLock){  //表示获取互斥锁失败
                    Thread.sleep(100);
                    queryById(id);
                }
                shop = getById(id);
                if (shop == null){
                    //缓存空值避免缓存穿透，但如果用户使用不同的id去频繁的访问还是会被穿透
                    stringRedisTemplate.opsForValue().set(key,"");
                    return Result.fail("店铺不存在");
                }
                //把shop存到缓存里面并设置过期时间
                stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),30L,TimeUnit.MINUTES);
            }catch (Exception e){
                log.error("ShopServiceImpl中的:queryById()发生了异常");
            }finally {
                unLock(key);
            }
        }else {
            shop = JSONUtil.toBean(jsonShop, Shop.class);
        }
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result updateShopById(Shop shop) {
        Long id = shop.getId();
        if (id == null){
            log.error("店铺Id为空-->{}",shop);
            return Result.fail("店铺Id为空");
        }
        updateById(shop);
        stringRedisTemplate.delete(CACHE_SHOP_KEY +shop.getId());
        return Result.ok();
    }

    public boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("Lock:"+key  , "1", 2L, TimeUnit.MINUTES);
        return BooleanUtil.isTrue(flag);
    }
    public boolean unLock(String key){
        Boolean flag = stringRedisTemplate.delete("Lock:"+key);
        return BooleanUtil.isTrue(flag);
    }

}
