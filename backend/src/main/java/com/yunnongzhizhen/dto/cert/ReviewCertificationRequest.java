package com.yunnongzhizhen.dto.cert;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审核认证申请请求。
 */
@Data
public class ReviewCertificationRequest {

    @Size(max = 500)
    private String reviewComment;
}
