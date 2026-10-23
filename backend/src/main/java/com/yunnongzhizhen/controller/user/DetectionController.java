package com.yunnongzhizhen.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.entity.DetectionRecord;
import com.yunnongzhizhen.service.DetectionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 识别接口 /api/user/detection。
 */
@RestController
@RequestMapping("/api/user/detection")
@RequiredArgsConstructor
public class DetectionController {

    private final DetectionService detectionService;

    /**
     * 发起识别。
     */
    @PostMapping("/detect")
    public ApiResult<DetectionRecord> detect(@RequestBody DetectRequest request) {
        return ApiResult.ok(detectionService.detectDisease(request.getImagePath(), request.getCropId()));
    }

    /**
     * 识别历史。
     */
    @GetMapping("/history")
    public ApiResult<Page<DetectionRecord>> history(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(detectionService.history(page, size));
    }

    @Data
    public static class DetectRequest {
        private String imagePath;
        private Long cropId;
    }
}
