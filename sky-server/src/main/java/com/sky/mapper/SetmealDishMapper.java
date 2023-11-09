package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {



    /**
     * 根据菜品id查询套餐id
     * @param dishIds
     * @return
     */
    public List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量插入对应关系
     * @param setMealDishes
     */
    void insertBatch(List<SetmealDish> setMealDishes);

    /**
     * 根据套餐id删除对应关系
     * @param setMealIds
     */
    void deleteBatch(List<Long> setMealIds);

    /**
     * 根据套餐id查询套餐相应菜品
     * @param setMealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setMealId};")
    List<SetmealDish> getBySetmealId(Long setMealId);
}
