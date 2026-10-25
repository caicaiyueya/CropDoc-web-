package com.yunnongzhizhen.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunnongzhizhen.entity.Comment;
import com.yunnongzhizhen.entity.Post;
import com.yunnongzhizhen.entity.User;
import com.yunnongzhizhen.entity.enums.PostStatus;
import com.yunnongzhizhen.entity.enums.PostTag;
import com.yunnongzhizhen.entity.enums.UserRole;
import com.yunnongzhizhen.entity.enums.UserStatus;
import com.yunnongzhizhen.mapper.CommentMapper;
import com.yunnongzhizhen.mapper.PostMapper;
import com.yunnongzhizhen.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 启动初始化：
 * 1) ADMIN：admin / Admin@123
 * 2) 演示农户与农技员：密码统一 Demo@123
 * 3) 若无已审核帖子，写入社区演示帖与评论
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureAdmin();
        ensureDemoUsers();
        ensureCommunitySeed();
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

    private void ensureCommunitySeed() {
        Long approved = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, PostStatus.APPROVED));
        if (approved != null && approved > 0) {
            return;
        }
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

        Post guide = insertPost(expertLi.getId(),
                "春季稻瘟病早期识别与防治要点（专家科普）",
                "稻瘟病是水稻上最重要的真菌病害之一。叶片出现梭形灰绿色病斑时要提高警惕，破口抽穗前后遇连阴雨更易流行。\n建议选用抗病品种、控制氮肥，破口前预防用药，药剂轮换。",
                "experience||/images/hero-farm.jpg", PostTag.GUIDE, true, 328);

        Post help = insertPost(farmerLi.getId(),
                "玉米叶子发黄长锈斑，求助各位农友！",
                "自家两亩玉米下部叶片出现橙黄色粉状物，露水重。请问是否普通锈病？求用药建议。",
                "help||/images/hero-farm.jpg", PostTag.NORMAL, false, 156);

        Post exp = insertPost(farmerZhang != null ? farmerZhang.getId() : farmerLi.getId(),
                "小麦扬花期防赤霉病，我的三年实操经验",
                "扬花初期用药、雨前雨后盯紧、药剂轮换、控氮通风。欢迎补充各地差异做法。",
                "experience||/images/hero-farm.jpg", PostTag.NORMAL, false, 210);

        Post tomato = null;
        if (expertZhao != null) {
            tomato = insertPost(expertZhao.getId(),
                    "棚室番茄晚疫病：降湿比打药更重要",
                    "先通风排湿，再配合药剂保护；发现中心病株立即清除。",
                    "experience||/images/hero-farm.jpg", PostTag.GUIDE, false, 275);
        }

        if (guide != null && farmerWang != null) {
            insertComment(guide.getId(), farmerWang.getId(),
                    "写得很清楚，正好我们村最近准备统一预防用药，收藏了！", false, false);
        }
        if (guide != null && expertZhao != null) {
            insertComment(guide.getId(), expertZhao.getId(),
                    "补充一点：感病品种田块破口抽穗期建议连续保护两次，间隔 5～7 天。", true, true);
        }
        if (help != null && expertLi != null) {
            insertComment(help.getId(), expertLi.getId(),
                    "从描述看高度疑似普通锈病。初期可用三唑类药剂，7～10 天一次，连喷 2～3 次。", true, true);
        }
        if (help != null && farmerChen != null) {
            insertComment(help.getId(), farmerChen.getId(),
                    "我们这边也这样，打了两遍好多了，记得别偏施氮肥。", false, false);
        }
        if (exp != null && farmerWang != null) {
            insertComment(exp.getId(), farmerWang.getId(),
                    "学到了，往年扬花遇雨就慌，今年按这个节奏试试看。", false, false);
        }
        if (tomato != null && farmerChen != null) {
            insertComment(tomato.getId(), farmerChen.getId(),
                    "降湿这点太关键了，去年就是闷棚打药效果差。", false, false);
        }
        log.info("已写入社区演示帖与评论");
    }

    private User findUser(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    private Post insertPost(Long authorId, String title, String content, String images,
                            PostTag tag, boolean pinned, int views) {
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
        return post;
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
