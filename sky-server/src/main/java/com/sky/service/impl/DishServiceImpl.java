package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMappper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMappper dishFlavorMappper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
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

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        long total = page.getTotal();
        List<DishVO> records = page.getResult();
        return new PageResult(total,records);
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //判断当前菜品是否能够删除--是否存在起售的菜品？？
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否能够删除--是否被关联了？？
        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIdsByDishIds != null && setmealIdsByDishIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品表中的数据
//        for(Long id:ids){
//            dishMapper.deleteById(id);
//            //删除菜品关联的口味数据
//            dishFlavorMappper.deleteByDishId(id);
//        }
        //删除菜品表中的数据
        dishMapper.deleteByIds(ids);
        //删除菜品关联的口味数据
        dishFlavorMappper.deleteByDishIds(ids);


    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {

        Dish dish = dishMapper.getById(id);
        //List<DishFlavor> dishFlavors = dishFlavorMappper.getByDishId(id);
        DishVO dishVO =new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavorMappper.getByDishId(id));
        //dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据id修改菜品的基本信息和口味信息
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表基本信息
        dishMapper.update(dish);
        //删除重新插入
        dishFlavorMappper.deleteByDishId(dishDTO.getId());
        //dishFlavorMappper.insertBatch(dishDTO.getFlavors());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors.size() > 0 && flavors != null){
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishDTO.getId());
            });
            //向口味表插入n条数据
            dishFlavorMappper.insertBatch(flavors);
        }
    }

    /**
     * 启售禁售菜品
     * @param status
     * @param id
     */
    @Transactional
    public void startOrStop(Integer status, long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        if(status == StatusConstant.DISABLE){
            //如果是停售操作，需要将当前菜品的套餐也停售
            List<Long> DishIds = new ArrayList<>();
            DishIds.add(id);
            List<Long> SetMealIds = setmealDishMapper.getSetmealIdsByDishIds(DishIds);
            if(SetMealIds.size()>0 && SetMealIds != null){
                for (Long setMealId : SetMealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setMealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
        dishMapper.update(dish);
    }

    /**
     * 根据分类id显示菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }
}
