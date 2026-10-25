package com.yunnongzhizhen.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunnongzhizhen.entity.CertificationApplication;
import com.yunnongzhizhen.entity.Comment;
import com.yunnongzhizhen.entity.Crop;
import com.yunnongzhizhen.entity.DetectionRecord;
import com.yunnongzhizhen.entity.Disease;
import com.yunnongzhizhen.entity.News;
import com.yunnongzhizhen.entity.Post;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.CertStatus;
import com.yunnongzhizhen.entity.enums.PostStatus;
import com.yunnongzhizhen.entity.enums.PostTag;
import com.yunnongzhizhen.entity.enums.PublishStatus;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.entity.enums.UserStatus;
import com.yunnongzhizhen.mapper.CertificationApplicationMapper;
import com.yunnongzhizhen.mapper.CommentMapper;
import com.yunnongzhizhen.mapper.CropMapper;
import com.yunnongzhizhen.mapper.DetectionRecordMapper;
import com.yunnongzhizhen.mapper.DiseaseMapper;
import com.yunnongzhizhen.mapper.NewsMapper;
import com.yunnongzhizhen.mapper.PostMapper;
import com.yunnongzhizhen.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 启动初始化：
 * 1) ADMIN：admin / Admin@123
 * 2) 演示农户与农技员：密码统一 Demo@123
 * 3) 已是 EXPERT 的用户写入认证「已通过」记录
 * 4) 作物与病害库、资讯、社区帖、识别历史
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin@123";
    private static final String DEMO_PASSWORD = "Demo@123";

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final CropMapper cropMapper;
    private final DiseaseMapper diseaseMapper;
    private final NewsMapper newsMapper;
    private final CertificationApplicationMapper certificationApplicationMapper;
    private final DetectionRecordMapper detectionRecordMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureAdmin();
        ensureDemoUsers();
        ensureExpertCertifications();
        syncDemoContent();
    }

    /**
     * 写入演示种子到数据库（作物/病害、资讯、社区帖、识别记录）。
     * 管理端可通过 POST /api/admin/demo/sync 再次补齐。
     */
    public void syncDemoContent() {
        ensureExpertCertifications();
        ensureCropsAndDiseases();
        ensureNewsSeed();
        ensureCommunitySeed();
        ensureDetectionHistory();
    }

    private void ensureAdmin() {
        User admin = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, ADMIN_USERNAME));
        if (admin == null) {
            admin = new User();
            admin.setUsername(ADMIN_USERNAME);
            admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setNickname("系统管理员");
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.NORMAL);
            LocalDateTime now = LocalDateTime.now();
            admin.setCreateTime(now);
            admin.setUpdateTime(now);
            userMapper.insert(admin);
            log.info("已初始化管理员账号 admin / Admin@123");
            return;
        }
        if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.NORMAL);
            admin.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(admin);
            log.info("已重置管理员密码为 Admin@123");
        }
    }

    private void ensureDemoUsers() {
        record DemoUser(String username, String nickname, UserRole role, String region) {}
        List<DemoUser> demos = List.of(
                new DemoUser("farmer_wang", "种田老王", UserRole.FARMER, "湖南长沙"),
                new DemoUser("farmer_zhang", "小麦张", UserRole.FARMER, "河南周口"),
                new DemoUser("farmer_li", "玉米李", UserRole.FARMER, "吉林长春"),
                new DemoUser("farmer_chen", "翠花种植", UserRole.FARMER, "四川眉山"),
                new DemoUser("farmer_liu", "田间小陈", UserRole.FARMER, "江苏盐城"),
                new DemoUser("expert_li", "李农技", UserRole.EXPERT, "湖南农技中心"),
                new DemoUser("expert_zhao", "赵专家", UserRole.EXPERT, "省植保站")
        );
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        for (DemoUser d : demos) {
            User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, d.username()));
            if (u == null) {
                u = new User();
                u.setUsername(d.username());
                u.setPasswordHash(hash);
                u.setNickname(d.nickname());
                u.setRole(d.role());
                u.setStatus(UserStatus.NORMAL);
                u.setRegion(d.region());
                LocalDateTime now = LocalDateTime.now();
                u.setCreateTime(now);
                u.setUpdateTime(now);
                userMapper.insert(u);
                log.info("已初始化演示账号 {} / {}", d.username(), DEMO_PASSWORD);
            } else {
                boolean dirty = false;
                if (!passwordEncoder.matches(DEMO_PASSWORD, u.getPasswordHash())) {
                    u.setPasswordHash(hash);
                    dirty = true;
                }
                if (!d.nickname().equals(u.getNickname())
                        || u.getRole() != d.role()
                        || !Objects.equals(d.region(), u.getRegion())) {
                    u.setNickname(d.nickname());
                    u.setRole(d.role());
                    u.setRegion(d.region());
                    dirty = true;
                }
                if (dirty) {
                    u.setUpdateTime(LocalDateTime.now());
                    userMapper.updateById(u);
                }
            }
        }
    }

    /**
     * 作物 + 病害库：与前端 diseaseCatalog.MOCK_DISEASES 对齐，管理端与用户端共用。
     */
    private void ensureCropsAndDiseases() {
        Long riceId = ensureCrop("水稻", "常见粮食作物");
        Long wheatId = ensureCrop("小麦", "常见粮食作物");
        Long cornId = ensureCrop("玉米", "常见粮食作物");
        Long tomatoId = ensureCrop("番茄", "常见蔬菜作物");
        Long appleId = ensureCrop("苹果", "常见果树");

        int inserted = 0;
        inserted += ensureDisease(riceId, "稻瘟病",
                packDiseaseDesc(
                        "由稻瘟病菌（Magnaporthe oryzae）侵染引起。病谷、病稻草是重要初侵染源，分生孢子随气流传播，在叶片表面有水膜时侵入。",
                        "适温（24～28℃）配合高湿、连阴雨、露水重的天气最易流行；偏施氮肥、密植郁闭、感病品种会显著加重发生。",
                        "稻瘟病是水稻上危害最重的真菌性病害之一，可发生在叶片、节、穗颈等部位，流行年份可造成大幅度减产。"),
                "选用抗病品种；浅水勤灌，避免长期深水与偏氮；破口抽穗期用三环唑、稻瘟灵等预防，发病后改用春雷霉素等治疗剂，注意轮换用药。");
        inserted += ensureDisease(riceId, "水稻纹枯病",
                packDiseaseDesc(
                        "由立枯丝核菌（Rhizoctonia solani）侵染引起。菌核在土壤与病残体上越冬，随水流与农事操作扩散。",
                        "高肥密植、长期深水、高温高湿天气利于扩展；氮肥偏多、田间郁闭会显著加重。",
                        "水稻纹枯病主要危害叶鞘与茎秆，近水面处出现灰绿色水渍状病斑，后扩大为云纹状，严重时引起倒伏与枯孕穗。"),
                "合理密植与浅水勤灌；控制氮肥；打捞菌核；孕穗前后用井岗霉素、噻呋酰胺等药剂保护，注意轮换用药。");
        inserted += ensureDisease(cornId, "玉米普通锈病",
                packDiseaseDesc(
                        "由玉米柄锈菌侵染引起。夏孢子随气流远距离传播，田间病残体与自生苗可提供菌源。",
                        "凉爽高湿、昼夜温差大、叶面结露时间长时扩展快；感病品种、过密种植、偏施氮肥易加重。",
                        "玉米普通锈病以叶部橙黄色夏孢子堆为典型症状，严重时叶片早枯，影响灌浆与产量。"),
                "清除病残体，选用抗病品种；发病初期喷施三唑类或代森锰锌类药剂，7～10 天一次，连喷 2～3 次；保持通风，控制氮肥。");
        inserted += ensureDisease(wheatId, "小麦赤霉病",
                packDiseaseDesc(
                        "主要由禾谷镰孢等镰刀菌侵染。病残体与土壤带菌，孢子借风雨飞溅到花器侵入。",
                        "扬花期遇连阴雨、高温高湿最易大发生；感病品种、氮肥过量、田间郁闭会提高风险。",
                        "小麦赤霉病主要危害穗部，形成粉红霉层与枯白穗，籽粒皱缩，还可能产生真菌毒素，影响食品安全。"),
                "扬花初期及时预防用药（氰烯菌酯、戊唑醇等）；雨前雨后补喷；收获后清除病残，合理轮作与控氮。");
        inserted += ensureDisease(tomatoId, "番茄晚疫病",
                packDiseaseDesc(
                        "由致病疫霉（Phytophthora infestans）侵染。病残体与种薯（相关寄主）可带菌，孢子囊随风雨扩散。",
                        "冷凉高湿、叶面结露、通风不良时极易流行；连作田与密植棚室风险更高。",
                        "番茄晚疫病可在短期内造成叶片、茎秆与果实大面积坏死，棚室与露地均可暴发。"),
                "降温排湿，避免叶面长时间结露；发现中心病株立即清除；烯酰吗啉、霜脲氰等药剂保护与治疗交替使用。");
        inserted += ensureDisease(appleId, "苹果黑星病",
                packDiseaseDesc(
                        "由苹果黑星病菌侵染。病叶病果越冬，子囊孢子与分生孢子借风雨侵染幼嫩组织。",
                        "春季多雨、湿润凉爽时初侵染高峰明显；树冠郁闭、清园不彻底会加重全年发生。",
                        "苹果黑星病危害叶片与果实，形成橄榄色至黑色病斑，导致早期落叶与果实商品性下降。"),
                "秋冬彻底清园；萌芽前石硫合剂；生长季节保护性与治疗性杀菌剂交替，重点抓好幼果期防治。");

        // 兼容旧种子名：已有「水稻纹枯病」则删旧名，避免改名后重复
        Disease legacy = diseaseMapper.selectOne(new LambdaQueryWrapper<Disease>()
                .eq(Disease::getName, "纹枯病专业防治"));
        if (legacy != null) {
            Long existing = diseaseMapper.selectCount(new LambdaQueryWrapper<Disease>()
                    .eq(Disease::getName, "水稻纹枯病")
                    .eq(Disease::getCropId, legacy.getCropId()));
            if (existing != null && existing > 0) {
                diseaseMapper.deleteById(legacy.getId());
            } else {
                legacy.setName("水稻纹枯病");
                legacy.setUpdateTime(LocalDateTime.now());
                diseaseMapper.updateById(legacy);
            }
        }
        // 清理历史重复的「水稻纹枯病」（同作物只保留 id 最小的一条）
        int removed = dedupeDiseaseByName(riceId, "水稻纹枯病");
        if (inserted > 0) {
            log.info("已补齐病害库演示数据 {} 条", inserted);
        }
        if (removed > 0) {
            log.info("已删除重复病害「水稻纹枯病」 {} 条", removed);
        }
    }

    /** 同作物同名病害去重，保留 id 最小的一条，返回删除条数 */
    private int dedupeDiseaseByName(Long cropId, String name) {
        if (cropId == null || name == null) {
            return 0;
        }
        List<Disease> rows = diseaseMapper.selectList(new LambdaQueryWrapper<Disease>()
                .eq(Disease::getCropId, cropId)
                .eq(Disease::getName, name)
                .orderByAsc(Disease::getId));
        if (rows == null || rows.size() <= 1) {
            return 0;
        }
        int removed = 0;
        for (int i = 1; i < rows.size(); i++) {
            diseaseMapper.deleteById(rows.get(i).getId());
            removed++;
        }
        return removed;
    }

    private Long ensureCrop(String name, String description) {
        Crop crop = cropMapper.selectOne(new LambdaQueryWrapper<Crop>().eq(Crop::getName, name));
        if (crop != null) {
            return crop.getId();
        }
        crop = new Crop();
        crop.setName(name);
        crop.setDescription(description);
        LocalDateTime now = LocalDateTime.now();
        crop.setCreateTime(now);
        crop.setUpdateTime(now);
        cropMapper.insert(crop);
        return crop.getId();
    }

    private int ensureDisease(Long cropId, String name, String description, String prevention) {
        if (cropId == null) {
            return 0;
        }
        Long count = diseaseMapper.selectCount(new LambdaQueryWrapper<Disease>()
                .eq(Disease::getName, name)
                .eq(Disease::getCropId, cropId));
        if (count != null && count > 0) {
            return 0;
        }
        Disease d = new Disease();
        d.setCropId(cropId);
        d.setName(name);
        d.setDescription(description);
        d.setPreventionAdvice(prevention);
        LocalDateTime now = LocalDateTime.now();
        d.setCreateTime(now);
        d.setUpdateTime(now);
        diseaseMapper.insert(d);
        return 1;
    }

    /**
     * 已是 EXPERT 的用户补齐认证「已通过」记录，便于管理端认证审核可见。
     */
    private void ensureExpertCertifications() {
        User admin = findUser(ADMIN_USERNAME);
        List<User> experts = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, UserRole.EXPERT));
        if (experts == null || experts.isEmpty()) {
            return;
        }
        int inserted = 0;
        LocalDateTime now = LocalDateTime.now();
        for (User expert : experts) {
            Long approved = certificationApplicationMapper.selectCount(
                    new LambdaQueryWrapper<CertificationApplication>()
                            .eq(CertificationApplication::getUserId, expert.getId())
                            .eq(CertificationApplication::getStatus, CertStatus.APPROVED));
            if (approved != null && approved > 0) {
                continue;
            }
            CertificationApplication app = new CertificationApplication();
            app.setUserId(expert.getId());
            app.setQualificationDesc((expert.getNickname() != null ? expert.getNickname() : expert.getUsername())
                    + "：演示农技员账号，预置已通过认证");
            app.setMaterialPath(null);
            app.setStatus(CertStatus.APPROVED);
            app.setReviewerId(admin != null ? admin.getId() : null);
            app.setReviewComment("演示种子：预置农技员已通过");
            app.setReviewTime(now.minusDays(3));
            app.setCreateTime(now.minusDays(5));
            app.setUpdateTime(now.minusDays(3));
            certificationApplicationMapper.insert(app);
            inserted++;
        }
        if (inserted > 0) {
            log.info("已为 {} 名农技员补齐认证已通过记录", inserted);
        }
    }

    /**
     * 为已有演示用户写入病害识别历史（含 image_path 识别路径）。
     */
    private void ensureDetectionHistory() {
        Long riceId = cropIdByName("水稻");
        Long cornId = cropIdByName("玉米");
        Long wheatId = cropIdByName("小麦");
        Long tomatoId = cropIdByName("番茄");

        record SeedDetect(String username, Long cropId, String result, String imagePath,
                          String confidence, String description, int daysAgo) {}

        List<SeedDetect> seeds = List.of(
                new SeedDetect("farmer_wang", riceId, "稻瘟病", "/images/diseases/rice-blast.png",
                        "0.9300", "叶片梭形灰绿色病斑，疑似稻瘟病。", 5),
                new SeedDetect("farmer_zhang", wheatId, "小麦赤霉病", "/images/diseases/wheat-fusarium.png",
                        "0.8800", "扬花期穗部粉红霉层，疑似赤霉病。", 4),
                new SeedDetect("farmer_li", cornId, "玉米普通锈病", "/images/diseases/corn-common-rust.png",
                        "0.9100", "下部叶片橙黄色粉状物，疑似普通锈病。", 3),
                new SeedDetect("farmer_chen", tomatoId, "番茄晚疫病", "/images/diseases/tomato-late-blight.png",
                        "0.8600", "棚室叶片水渍状病斑，疑似晚疫病。", 2),
                new SeedDetect("farmer_liu", riceId, "水稻纹枯病", "/images/diseases/rice-sheath-blight.png",
                        "0.9000", "近水面叶鞘云纹状病斑，疑似纹枯病。", 2),
                new SeedDetect("expert_li", riceId, "稻瘟病", "/images/diseases/rice-blast.png",
                        "0.9500", "田间巡查示范识别：稻瘟病。", 1),
                new SeedDetect("expert_zhao", tomatoId, "番茄晚疫病", "/images/diseases/tomato-late-blight.png",
                        "0.9200", "棚室示范识别：番茄晚疫病。", 1)
        );

        int inserted = 0;
        for (SeedDetect s : seeds) {
            User u = findUser(s.username());
            if (u == null || s.cropId() == null) {
                continue;
            }
            Long count = detectionRecordMapper.selectCount(new LambdaQueryWrapper<DetectionRecord>()
                    .eq(DetectionRecord::getUserId, u.getId())
                    .eq(DetectionRecord::getResult, s.result()));
            if (count != null && count > 0) {
                continue;
            }
            LocalDateTime t = LocalDateTime.now().minusDays(s.daysAgo());
            DetectionRecord r = new DetectionRecord();
            r.setUserId(u.getId());
            r.setCropId(s.cropId());
            r.setImagePath(s.imagePath());
            r.setResult(s.result());
            r.setConfidence(new BigDecimal(s.confidence()));
            r.setDescription(s.description());
            r.setCreateTime(t);
            r.setUpdateTime(t);
            detectionRecordMapper.insert(r);
            inserted++;
        }
        if (inserted > 0) {
            log.info("已写入病害识别历史 {} 条", inserted);
        }
    }

    private Long cropIdByName(String name) {
        Crop crop = cropMapper.selectOne(new LambdaQueryWrapper<Crop>().eq(Crop::getName, name));
        return crop != null ? crop.getId() : null;
    }

    private static String packDiseaseDesc(String causes, String conditions, String description) {
        return "【发病原因】" + causes
                + "\n【发病条件】" + conditions
                + "\n【描述】" + description;
    }

    /**
     * 资讯：与前端 newsMock.MOCK_NEWS 对齐（按标题补齐，避免库非空时跳过）。
     */
    private void ensureNewsSeed() {
        int inserted = 0;
        inserted += ensureNews(
                "春季田间管理要点：控水控肥防病害",
                "进入返青拔节期，需根据墒情合理灌溉，避免偏施氮肥，重点监测叶部病害早期症状。\n\n"
                        + "春季是作物生长与病虫害交错上升的关键阶段。建议浅水勤灌、增施磷钾肥，结合田间巡查及时发现病斑并取样识别。",
                "/images/news/spring-field.png",
                LocalDateTime.of(2026, 10, 24, 8, 30));
        inserted += ensureNews(
                "南方稻区注意稻瘟病早期监测",
                "近期温湿度适宜稻瘟病菌萌发，破口抽穗前应做好预防用药与田间观察。\n\n"
                        + "稻瘟病流行与气候密切相关。农户可关注当地农技部门预警，选用抗病品种，并在关键生育期落实药剂保护。",
                "/images/news/rice-blast-monitor.png",
                LocalDateTime.of(2026, 10, 24, 7, 50));
        inserted += ensureNews(
                "智能识别如何辅助基层农技服务",
                "拍照识病可缩短初诊时间，结合问诊建议为农户提供可操作的防治路径。\n\n"
                        + "基层农技员可将 AI 识别结果作为初筛参考，再结合田间环境与用药史给出最终方案，提升服务效率与可追溯性。",
                "/images/news/smart-diagnosis.png",
                LocalDateTime.of(2026, 10, 23, 18, 20));
        inserted += ensureNews(
                "农技员认证通道已开放申请",
                "普通农户可提交资质说明与证明材料，审核通过后可发布农技指导内容。\n\n"
                        + "认证农技员将获得专业病害资料查看与农技指导发帖权限，欢迎有经验的种植户与技术员申请。",
                "/images/news/tech-cert.png",
                LocalDateTime.of(2026, 10, 23, 15, 10));
        inserted += ensureNews(
                "雨后田间病害应急处理指南",
                "强降雨后及时排水降湿，清除病叶病果，必要时补喷保护性杀菌剂。\n\n"
                        + "雨后叶面长时间结露利于病原侵入。建议优先改善通风排水，再根据作物与病害类型选择药剂，注意安全间隔期。",
                "/images/hero-farm.jpg",
                LocalDateTime.of(2026, 10, 22, 16, 40));
        if (inserted > 0) {
            log.info("已补齐资讯演示数据 {} 条", inserted);
        }
    }

    private int ensureNews(String title, String content, String cover, LocalDateTime createTime) {
        Long count = newsMapper.selectCount(new LambdaQueryWrapper<News>().eq(News::getTitle, title));
        if (count != null && count > 0) {
            return 0;
        }
        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setCover(cover);
        news.setStatus(PublishStatus.ONLINE);
        news.setCreateTime(createTime);
        news.setUpdateTime(createTime);
        newsMapper.insert(news);
        return 1;
    }

    /**
     * 社区帖：与 communitySeed.SEED_POSTS 对齐（按标题补齐已审核演示帖）。
     */
    private void ensureCommunitySeed() {
        User expertLi = findUser("expert_li");
        User expertZhao = findUser("expert_zhao");
        User farmerLi = findUser("farmer_li");
        User farmerZhang = findUser("farmer_zhang");
        User farmerWang = findUser("farmer_wang");
        User farmerChen = findUser("farmer_chen");
        if (expertLi == null || farmerLi == null) {
            log.warn("演示用户未就绪，跳过社区种子帖");
            return;
        }

        int inserted = 0;
        SeedPost guide = ensurePost(expertLi.getId(),
                "春季稻瘟病早期识别与防治要点（专家科普）",
                "稻瘟病是水稻上最重要的真菌病害之一。叶片出现梭形灰绿色病斑时要提高警惕，破口抽穗前后遇连阴雨更易流行。\n建议选用抗病品种、控制氮肥，破口前预防用药，药剂轮换。",
                "experience||/images/community/rice-blast.png", PostTag.GUIDE, true, 328);
        if (guide.created()) {
            inserted++;
        }

        SeedPost help = ensurePost(farmerLi.getId(),
                "玉米叶子发黄长锈斑，求助各位农友！",
                "自家两亩玉米下部叶片出现橙黄色粉状物，露水重。请问是否普通锈病？求用药建议。",
                "help||/images/community/corn-rust.png", PostTag.NORMAL, false, 156);
        if (help.created()) {
            inserted++;
        }

        SeedPost exp = ensurePost(farmerZhang != null ? farmerZhang.getId() : farmerLi.getId(),
                "小麦扬花期防赤霉病，我的三年实操经验",
                "扬花初期用药、雨前雨后盯紧、药剂轮换、控氮通风。欢迎补充各地差异做法。",
                "experience||/images/community/wheat-fusarium.png", PostTag.NORMAL, false, 210);
        if (exp.created()) {
            inserted++;
        }

        SeedPost tomato = SeedPost.absent();
        if (expertZhao != null) {
            tomato = ensurePost(expertZhao.getId(),
                    "棚室番茄晚疫病：降湿比打药更重要",
                    "先通风排湿，再配合药剂保护；发现中心病株立即清除。",
                    "experience||/images/community/tomato-late-blight.png", PostTag.GUIDE, false, 275);
            if (tomato.created()) {
                inserted++;
            }
        }

        // 仅对新写入的帖补充演示评论，避免重复
        if (guide.created() && farmerWang != null) {
            insertComment(guide.post().getId(), farmerWang.getId(),
                    "写得很清楚，正好我们村最近准备统一预防用药，收藏了！", false, false);
        }
        if (guide.created() && expertZhao != null) {
            insertComment(guide.post().getId(), expertZhao.getId(),
                    "补充一点：感病品种田块破口抽穗期建议连续保护两次，间隔 5～7 天。", true, true);
        }
        if (help.created() && expertLi != null) {
            insertComment(help.post().getId(), expertLi.getId(),
                    "从描述看高度疑似普通锈病。初期可用三唑类药剂，7～10 天一次，连喷 2～3 次。", true, true);
        }
        if (help.created() && farmerChen != null) {
            insertComment(help.post().getId(), farmerChen.getId(),
                    "我们这边也这样，打了两遍好多了，记得别偏施氮肥。", false, false);
        }
        if (exp.created() && farmerWang != null) {
            insertComment(exp.post().getId(), farmerWang.getId(),
                    "学到了，往年扬花遇雨就慌，今年按这个节奏试试看。", false, false);
        }
        if (tomato.created() && farmerChen != null) {
            insertComment(tomato.post().getId(), farmerChen.getId(),
                    "降湿这点太关键了，去年就是闷棚打药效果差。", false, false);
        }
        if (inserted > 0) {
            log.info("已补齐社区演示帖 {} 条", inserted);
        }
    }

    private User findUser(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    private record SeedPost(Post post, boolean created) {
        static SeedPost absent() {
            return new SeedPost(null, false);
        }
    }

    private SeedPost ensurePost(Long authorId, String title, String content, String images,
                                PostTag tag, boolean pinned, int views) {
        Post existing = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getTitle, title));
        if (existing != null) {
            return new SeedPost(existing, false);
        }
        Post post = new Post();
        post.setAuthorId(authorId);
        post.setTitle(title);
        post.setContent(content);
        post.setImages(images);
        post.setTag(tag);
        post.setStatus(PostStatus.APPROVED);
        post.setPinned(pinned);
        post.setViewCount(views);
        LocalDateTime now = LocalDateTime.now();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        postMapper.insert(post);
        return new SeedPost(post, true);
    }

    private void insertComment(Long postId, Long authorId, String content,
                               boolean professional, boolean quality) {
        Comment c = new Comment();
        c.setPostId(postId);
        c.setAuthorId(authorId);
        c.setContent(content);
        c.setIsProfessional(professional);
        c.setIsQuality(quality);
        LocalDateTime now = LocalDateTime.now();
        c.setCreateTime(now);
        c.setUpdateTime(now);
        commentMapper.insert(c);
    }
}
