package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.dto.cert.ReviewCertificationRequest;
import com.yunnongzhizhen.dto.cert.SubmitCertificationRequest;
import com.yunnongzhizhen.entity.CertificationApplication;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.CertStatus;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.mapper.CertificationApplicationMapper;
import com.yunnongzhizhen.mapper.UserMapper;
import com.yunnongzhizhen.security.LoginUser;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证状态流转集中于此：PENDING→APPROVED/REJECTED；通过后将用户 role 升为 EXPERT。
 */
@Service
@RequiredArgsConstructor
public class CertificationService {

    private final CertificationApplicationMapper certificationApplicationMapper;
    private final UserMapper userMapper;
    private final AdminLogService adminLogService;

    /**
     * 用例：提交农技员认证。顺序图：校验FARMER→查PENDING→insert PENDING
     *
     * @param request 申请内容
     * @return 申请记录
     */
    public CertificationApplication submitCertification(SubmitCertificationRequest request) {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        if (loginUser.getRole() != UserRole.FARMER) {
            throw new BusinessException("仅普通农户可提交认证申请");
        }
        Long pending = certificationApplicationMapper.selectCount(new LambdaQueryWrapper<CertificationApplication>()
                .eq(CertificationApplication::getUserId, loginUser.getId())
                .eq(CertificationApplication::getStatus, CertStatus.PENDING));
        if (pending != null && pending > 0) {
            throw new BusinessException("已有待审核申请，请勿重复提交");
        }
        CertificationApplication app = new CertificationApplication();
        app.setUserId(loginUser.getId());
        app.setQualificationDesc(request.getQualificationDesc());
        app.setMaterialPath(request.getMaterialPath());
        app.setStatus(CertStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();
        app.setCreateTime(now);
        app.setUpdateTime(now);
        certificationApplicationMapper.insert(app);
        return app;
    }

    /**
     * 用例：查询我的认证申请列表。
     *
     * @return 申请列表
     */
    public List<CertificationApplication> myCertifications() {
        Long userId = SecurityUtils.requireUserId();
        return certificationApplicationMapper.selectList(new LambdaQueryWrapper<CertificationApplication>()
                .eq(CertificationApplication::getUserId, userId)
                .orderByDesc(CertificationApplication::getCreateTime));
    }

    /**
     * 用例：查询当前认证状态（最新一条）。
     *
     * @return status 与申请摘要
     */
    public Map<String, Object> certificationStatus() {
        Long userId = SecurityUtils.requireUserId();
        UserRole role = SecurityUtils.requireRole();
        CertificationApplication latest = certificationApplicationMapper.selectOne(
                new LambdaQueryWrapper<CertificationApplication>()
                        .eq(CertificationApplication::getUserId, userId)
                        .orderByDesc(CertificationApplication::getCreateTime)
                        .last("LIMIT 1"));
        Map<String, Object> map = new HashMap<>();
        map.put("role", role);
        map.put("latest", latest);
        map.put("status", latest != null ? latest.getStatus() : null);
        return map;
    }

    /**
     * 用例：管理员分页查询认证申请。
     *
     * @param status 可选状态
     * @param page   页码
     * @param size   页大小
     * @return 分页结果
     */
    public Page<CertificationApplication> listCertifications(CertStatus status, long page, long size) {
        LambdaQueryWrapper<CertificationApplication> qw = new LambdaQueryWrapper<CertificationApplication>()
                .orderByDesc(CertificationApplication::getCreateTime);
        if (status != null) {
            qw.eq(CertificationApplication::getStatus, status);
        }
        return certificationApplicationMapper.selectPage(new Page<>(page, size), qw);
    }

    /**
     * 用例：审核通过认证。顺序图：PENDING→APPROVED；User.role→EXPERT；写操作日志。
     * 说明：JWT 不含 role，过滤器每次请求从 DB 加载最新 role，
     * 故审核通过后用户无需重新登录，下一次请求即以 EXPERT 身份生效。
     *
     * @param id      申请ID
     * @param request 审核意见
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveCertification(Long id, ReviewCertificationRequest request) {
        CertificationApplication app = requirePending(id);
        Long reviewerId = SecurityUtils.requireUserId();
        LocalDateTime now = LocalDateTime.now();
        app.setStatus(CertStatus.APPROVED);
        app.setReviewerId(reviewerId);
        app.setReviewComment(request != null ? request.getReviewComment() : null);
        app.setReviewTime(now);
        app.setUpdateTime(now);
        certificationApplicationMapper.updateById(app);

        User user = userMapper.selectById(app.getUserId());
        if (user == null) {
            throw new BusinessException("申请人不存在");
        }
        user.setRole(UserRole.EXPERT);
        user.setUpdateTime(now);
        userMapper.updateById(user);

        adminLogService.log(reviewerId, user.getId(), "APPROVE_CERTIFICATION",
                "通过认证申请#" + id + "，用户升级为EXPERT");
    }

    /**
     * 用例：驳回认证。顺序图：PENDING→REJECTED；不修改 User.role，保持 FARMER。
     *
     * @param id      申请ID
     * @param request 审核意见
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectCertification(Long id, ReviewCertificationRequest request) {
        CertificationApplication app = requirePending(id);
        Long reviewerId = SecurityUtils.requireUserId();
        LocalDateTime now = LocalDateTime.now();
        app.setStatus(CertStatus.REJECTED);
        app.setReviewerId(reviewerId);
        app.setReviewComment(request != null ? request.getReviewComment() : null);
        app.setReviewTime(now);
        app.setUpdateTime(now);
        certificationApplicationMapper.updateById(app);
        // 故意不改 User.role，申请人继续为 FARMER
        adminLogService.log(reviewerId, app.getUserId(), "REJECT_CERTIFICATION",
                "驳回认证申请#" + id);
    }

    /**
     * 查询认证相关操作日志。
     *
     * @return 日志列表
     */
    public List<com.yunnongzhizhen.entity.AdminOperationLog> certificationLogs() {
        return adminLogService.listCertificationLogs();
    }

    private CertificationApplication requirePending(Long id) {
        CertificationApplication app = certificationApplicationMapper.selectById(id);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if (app.getStatus() != CertStatus.PENDING) {
            throw new BusinessException("申请已审核，无法重复操作");
        }
        return app;
    }
}
