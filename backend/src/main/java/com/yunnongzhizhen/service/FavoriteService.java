package com.yunnongzhizhen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunnongzhizhen.common.BusinessException;
import com.yunnongzhizhen.entity.Favorite;
import com.yunnongzhizhen.entity.Post;
import com.yunnongzhizhen.entity.enums.PostStatus;
import com.yunnongzhizhen.mapper.FavoriteMapper;
import com.yunnongzhizhen.mapper.PostMapper;
import com.yunnongzhizhen.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏业务。
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;

    /**
     * 用例：收藏切换 toggleFavorite。
     *
     * @param postId 帖子ID
     * @return favorited 是否已收藏
     */
    public Map<String, Object> toggleFavorite(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != PostStatus.APPROVED) {
            throw new BusinessException("帖子不存在");
        }
        Long userId = SecurityUtils.requireUserId();
        Favorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getPostId, postId));
        boolean favorited;
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            favorited = false;
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setPostId(postId);
            LocalDateTime now = LocalDateTime.now();
            fav.setCreateTime(now);
            fav.setUpdateTime(now);
            favoriteMapper.insert(fav);
            favorited = true;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("favorited", favorited);
        map.put("postId", postId);
        return map;
    }

    /**
     * 我的收藏列表。
     *
     * @return 帖子列表
     */
    public List<Post> myFavorites() {
        Long userId = SecurityUtils.requireUserId();
        List<Favorite> favs = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));
        if (favs.isEmpty()) {
            return List.of();
        }
        List<Long> ids = favs.stream().map(Favorite::getPostId).collect(Collectors.toList());
        return postMapper.selectBatchIds(ids);
    }
}
