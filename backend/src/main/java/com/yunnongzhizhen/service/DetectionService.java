package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Crop;
import com.yunnongzhizhen.entity.DetectionRecord;
import com.yunnongzhizhen.mapper.CropMapper;
import com.yunnongzhizhen.mapper.DetectionRecordMapper;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 病虫害识别业务（与诊断分离）。
 */
@Service
@RequiredArgsConstructor
public class DetectionService {

    private final DetectionRecordMapper detectionRecordMapper;
    private final CropMapper cropMapper;
    private final AiService aiService;

    /**
     * 用例：病虫害识别 detectDisease。顺序图：校验图片→AiService.detectByImage→保存 DetectionRecord
     *
     * @param imagePath 图片相对路径
     * @param cropId    作物ID（可空）
     * @return 识别记录
     */
    public DetectionRecord detectDisease(String imagePath, Long cropId) {
        if (!StringUtils.hasText(imagePath)) {
            throw new BusinessException("图片路径不能为空");
        }
        Long userId = SecurityUtils.requireUserId();
        String cropHint = null;
        if (cropId != null) {
            Crop crop = cropMapper.selectById(cropId);
            if (crop != null) {
                cropHint = crop.getName();
            }
        }
        Map<String, Object> ai = aiService.detectByImage(imagePath, cropHint);
        DetectionRecord record = new DetectionRecord();
        record.setUserId(userId);
        record.setImagePath(imagePath);
        record.setCropId(cropId);
        record.setResult(String.valueOf(ai.getOrDefault("result", "未知")));
        Object conf = ai.get("confidence");
        if (conf instanceof Number n) {
            record.setConfidence(BigDecimal.valueOf(n.doubleValue()));
        }
        record.setDescription(String.valueOf(ai.getOrDefault("description", "")));
        LocalDateTime now = LocalDateTime.now();
        record.setCreateTime(now);
        record.setUpdateTime(now);
        detectionRecordMapper.insert(record);
        return record;
    }

    /**
     * 用例：识别历史。
     *
     * @param page 页码
     * @param size 页大小
     * @return 分页
     */
    public Page<DetectionRecord> history(long page, long size) {
        Long userId = SecurityUtils.requireUserId();
        return detectionRecordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<DetectionRecord>()
                        .eq(DetectionRecord::getUserId, userId)
                        .orderByDesc(DetectionRecord::getCreateTime));
    }
}
