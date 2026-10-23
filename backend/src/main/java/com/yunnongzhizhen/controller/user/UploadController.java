package com.yunnongzhizhen.controller.user;

import com.yunnongzhizhen.common.ApiResult;
import com.yunnongzhizhen.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传 /api/user/upload。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    /**
     * 上传图片：校验 JPEG/PNG/GIF/WebP 与大小，落盘后返回相对路径与元数据。
     */
    @PostMapping("/upload")
    public ApiResult<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        return ApiResult.ok(fileStorageService.storeWithMeta(file));
    }
}
