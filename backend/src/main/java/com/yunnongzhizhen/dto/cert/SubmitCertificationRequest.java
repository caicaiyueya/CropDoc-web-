package com.yunnongzhizhen.dto.cert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交认证申请请求。
 */
@Data
public class SubmitCertificationRequest {

    @NotBlank(message = "资质说明不能为空")
    @Size(max = 1000)
    private String qualificationDesc;

    @Size(max = 512)
    private String materialPath;
}
