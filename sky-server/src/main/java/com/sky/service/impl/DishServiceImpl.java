package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMappper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMappper dishFlavorMappper;
    /**
     * 新增菜品和对应的口味数据
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //向菜品表提交一条数据
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long id = dish.getId();

        //向口味表提交n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors.size() > 0 && flavors != null){
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(id);
            });
            //向口味表插入n条数据
            dishFlavorMappper.insertBatch(flavors);
        }
    }
}