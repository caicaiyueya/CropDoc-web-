package com.yunnongzhizhen.service;

import com.yunnongzhizhen.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储：磁盘存文件，数据库只存元数据/相对路径。
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    /** 与 application.yml spring.servlet.multipart.max-file-size 对齐的业务上限（字节） */
    private static final long MAX_BYTES = 10L * 1024 * 1024;

    @Value("${app.upload.base-path}")
    private String basePath;

    @Value("${app.upload.url-prefix}")
    private String urlPrefix;

    /**
     * 用例：上传文件。顺序图：校验类型与大小→写磁盘→返回相对路径与元数据
     *
     * @param file 上传文件
     * @return path/url/originalName/size/contentType
     */
    public Map<String, Object> storeWithMeta(MultipartFile file) {
        validateImage(file);
        String original = file.getOriginalFilename();
        String ext = resolveExtension(original, file.getContentType());
        String relative = LocalDate.now() + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = Paths.get(basePath).toAbsolutePath().normalize().resolve(relative);
        try {
            Files.createDirectories(dest.getParent());
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }
        String path = relative.replace("\\", "/");
        return Map.of(
                "path", path,
                "url", toUrl(path),
                "originalName", original != null ? original : "",
                "size", file.getSize(),
                "contentType", StringUtils.hasText(file.getContentType()) ? file.getContentType() : guessContentType(ext)
        );
    }

    /**
     * 兼容旧调用：仅返回相对路径。
     *
     * @param file 上传文件
     * @return 相对路径
     */
    public String store(MultipartFile file) {
        return String.valueOf(storeWithMeta(file).get("path"));
    }

    /**
     * @param relativePath 相对路径
     * @return 对外访问 URL 前缀拼接
     */
    public String toUrl(String relativePath) {
        String prefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
        return prefix + "/" + relativePath;
    }

    /**
     * @param relativePath 相对路径
     * @return 绝对文件路径
     */
    public Path resolveAbsolute(String relativePath) {
        return Paths.get(basePath).toAbsolutePath().normalize().resolve(relativePath);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("图片大小不能超过 10MB");
        }
        String contentType = file.getContentType();
        String original = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.hasText(original) && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        boolean typeOk = StringUtils.hasText(contentType) && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
        boolean extOk = ALLOWED_EXTENSIONS.contains(ext);
        if (!typeOk && !extOk) {
            throw new BusinessException("仅支持 JPEG/PNG/GIF/WebP 图片");
        }
    }

    private String resolveExtension(String original, String contentType) {
        if (StringUtils.hasText(original) && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ALLOWED_EXTENSIONS.contains(ext)) {
                return ext.equals(".jpg") ? ".jpg" : ext;
            }
        }
        if (contentType == null) {
            return ".jpg";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String guessContentType(String ext) {
        return switch (ext.toLowerCase(Locale.ROOT)) {
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
