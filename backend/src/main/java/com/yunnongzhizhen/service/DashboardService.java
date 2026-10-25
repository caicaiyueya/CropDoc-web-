package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunnongzhizhen.entity.CertificationApplication;
import com.yunnongzhizhen.entity.DetectionRecord;
import com.yunnongzhizhen.entity.Feedback;
import com.yunnongzhizhen.entity.Post;
import com.yunnongzhizhen.entity.Report;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.CertStatus;
import com.yunnongzhizhen.entity.enums.FeedbackStatus;
import com.yunnongzhizhen.entity.enums.PostStatus;
import com.yunnongzhizhen.entity.enums.ReportStatus;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.mapper.CertificationApplicationMapper;
import com.yunnongzhizhen.mapper.DetectionRecordMapper;
import com.yunnongzhizhen.mapper.FeedbackMapper;
import com.yunnongzhizhen.mapper.PostMapper;
import com.yunnongzhizhen.mapper.ReportMapper;
import com.yunnongzhizhen.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端仪表盘统计。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final DetectionRecordMapper detectionRecordMapper;
    private final CertificationApplicationMapper certificationApplicationMapper;
    private final ReportMapper reportMapper;
    private final FeedbackMapper feedbackMapper;

    /**
     * 用例：数据概览。含用户/角色、待审核认证与帖子、今日识别等。
     *
     * @return 统计 Map
     */
    public Map<String, Object> stats() {
        Map<String, Object> map = new HashMap<>();
        map.put("userTotal", userMapper.selectCount(null));
        map.put("farmerCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, UserRole.FARMER)));
        map.put("expertCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, UserRole.EXPERT)));
        map.put("adminCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, UserRole.ADMIN)));
        map.put("postPending", postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getStatus, PostStatus.PENDING)));
        map.put("postApproved", postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getStatus, PostStatus.APPROVED)));
        map.put("detectionTotal", detectionRecordMapper.selectCount(null));
        LocalDateTime dayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        map.put("todayDetection", detectionRecordMapper.selectCount(
                new LambdaQueryWrapper<DetectionRecord>().ge(DetectionRecord::getCreateTime, dayStart)));
        map.put("certPending", certificationApplicationMapper.selectCount(
                new LambdaQueryWrapper<CertificationApplication>().eq(CertificationApplication::getStatus, CertStatus.PENDING)));
        map.put("reportPending", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getStatus, ReportStatus.PENDING)));
        map.put("feedbackOpen", feedbackMapper.selectCount(new LambdaQueryWrapper<Feedback>().eq(Feedback::getStatus, FeedbackStatus.OPEN)));
        map.put("detectionTrend7", detectionTrend(7));
        map.put("detectionTrend30", detectionTrend(30));
        map.put("topDiseases", topDiseases(5));
        return map;
    }

    /** 近 N 天每日识别次数（含当天，按日期升序） */
    private List<Map<String, Object>> detectionTrend(int days) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = LocalDateTime.of(today.minusDays(days - 1L), LocalTime.MIN);
        List<DetectionRecord> rows = detectionRecordMapper.selectList(
                new LambdaQueryWrapper<DetectionRecord>()
                        .ge(DetectionRecord::getCreateTime, from)
                        .select(DetectionRecord::getCreateTime));
        Map<LocalDate, Long> counts = new HashMap<>();
        for (DetectionRecord r : rows) {
            if (r.getCreateTime() == null) {
                continue;
            }
            LocalDate d = r.getCreateTime().toLocalDate();
            counts.merge(d, 1L, Long::sum);
        }
        List<Map<String, Object>> series = new ArrayList<>(days);
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", d.toString());
            point.put("label", d.getMonthValue() + "/" + d.getDayOfMonth());
            point.put("count", counts.getOrDefault(d, 0L));
            series.add(point);
        }
        return series;
    }

    /** 识别结果 TOP N（按 result 字段聚合） */
    private List<Map<String, Object>> topDiseases(int limit) {
        List<DetectionRecord> rows = detectionRecordMapper.selectList(
                new LambdaQueryWrapper<DetectionRecord>()
                        .isNotNull(DetectionRecord::getResult)
                        .ne(DetectionRecord::getResult, "")
                        .select(DetectionRecord::getResult));
        Map<String, Long> counts = rows.stream()
                .map(DetectionRecord::getResult)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", e.getKey());
                    item.put("count", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
