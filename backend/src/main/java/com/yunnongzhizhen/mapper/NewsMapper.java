package com.yunnongzhizhen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunnongzhizhen.entity.News;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资讯 Mapper。
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {
}
