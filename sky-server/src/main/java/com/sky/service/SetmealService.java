package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     * 新增套餐和对应的菜品数据
     * @param setmealDTO
     */
    public void saveWithFish(SetmealDTO setmealDTO);


    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteByIds(List<Long> ids);

    /**
     * 根据id查找套餐
     * @param id
     * @return
     */
    SetmealVO getByIdWithDishs(Long id);

    /**
     * 更新套餐以及相关联菜品情况
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 起售停售套餐
     * @param status
     * @param id
     */
    void StartOrStop(Integer status, Long id);
}
