package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Disease;
import com.yunnongzhizhen.entity.enums.DiseaseLevel;
import com.yunnongzhizhen.mapper.DiseaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 病害资料：农户与认证农技员可见同一套常见病虫害资料，不再按 BASIC/PRO 区分。
 */
@Service
@RequiredArgsConstructor
public class DiseaseService {

    private final DiseaseMapper diseaseMapper;

    /**
     * 用例：病害列表（全量，不按角色过滤层级）。
     */
    public Page<Disease> listByRole(Long cropId, long page, long size) {
        LambdaQueryWrapper<Disease> qw = new LambdaQueryWrapper<Disease>()
                .orderByAsc(Disease::getName);
        if (cropId != null) {
            qw.eq(Disease::getCropId, cropId);
        }
        return diseaseMapper.selectPage(new Page<>(page, size), qw);
    }

    /**
     * 用例：病害详情。
     */
    public Disease getById(Long id) {
        Disease disease = diseaseMapper.selectById(id);
        if (disease == null) {
            throw new BusinessException("病害资料不存在");
        }
        return disease;
    }

    /**
     * 管理员：全部病害分页。
     */
    public Page<Disease> adminList(Long cropId, long page, long size) {
        LambdaQueryWrapper<Disease> qw = new LambdaQueryWrapper<Disease>()
                .orderByDesc(Disease::getUpdateTime);
        if (cropId != null) {
            qw.eq(Disease::getCropId, cropId);
        }
        return diseaseMapper.selectPage(new Page<>(page, size), qw);
    }

    /**
     * 管理员：新增或更新病害。
     */
    public Disease save(Disease disease) {
        if (!StringUtils.hasText(disease.getName()) || disease.getCropId() == null) {
            throw new BusinessException("作物与病害名称不能为空");
        }
        if (disease.getLevel() == null) {
            disease.setLevel(DiseaseLevel.BASIC);
        }
        LocalDateTime now = LocalDateTime.now();
        if (disease.getId() == null) {
            disease.setCreateTime(now);
            disease.setUpdateTime(now);
            diseaseMapper.insert(disease);
        } else {
            disease.setUpdateTime(now);
            diseaseMapper.updateById(disease);
        }
        return disease;
    }

    /**
     * 管理员：删除病害。
     */
    public void delete(Long id) {
        if (diseaseMapper.selectById(id) == null) {
            throw new BusinessException("病害资料不存在");
        }
        diseaseMapper.deleteById(id);
    }
}
