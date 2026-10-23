package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Crop;
import com.yunnongzhizhen.mapper.CropMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作物列表与维护。
 */
@Service
@RequiredArgsConstructor
public class CropService {

    private final CropMapper cropMapper;

    /**
     * 用例：作物列表 GET /api/user/crop/list。
     */
    public List<Crop> listAll() {
        return cropMapper.selectList(new LambdaQueryWrapper<Crop>().orderByAsc(Crop::getName));
    }

    /**
     * 管理员维护作物（病害库依赖）。
     */
    public Crop save(Crop crop) {
        if (!StringUtils.hasText(crop.getName())) {
            throw new BusinessException("作物名称不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        if (crop.getId() == null) {
            crop.setCreateTime(now);
            crop.setUpdateTime(now);
            cropMapper.insert(crop);
        } else {
            crop.setUpdateTime(now);
            cropMapper.updateById(crop);
        }
        return crop;
    }
}
