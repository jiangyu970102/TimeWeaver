package com.timeweaver.service;

import com.timeweaver.entity.Category;
import com.timeweaver.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategorySuggestionService {

    private final CategoryMapper categoryMapper;

    // 关键词 → 分类名映射
    private static final Map<String, String> KEYWORD_MAP = new LinkedHashMap<>();

    static {
        KEYWORD_MAP.put("bug|调试|debug|fix|error|异常|故障|排查", "开发·调试");
        KEYWORD_MAP.put("review|cr|pr|代码审查", "开发·Code Review");
        KEYWORD_MAP.put("会议|meeting|同步|周会|站会|讨论|沟通|对齐", "会议");
        KEYWORD_MAP.put("文档|doc|wiki|写作|写文档|方案|markdown", "文档·写作");
        KEYWORD_MAP.put("阅读|读书|文档|paper|技术文章|博客", "学习·阅读");
        KEYWORD_MAP.put("课程|教程|学习|网课|培训|听课", "学习·课程");
        KEYWORD_MAP.put("实践|实验|demo|原型|练手|side.project", "学习·实践");
        KEYWORD_MAP.put("短视频|视频|抖音|b站|youtube|刷视频", "娱乐·短视频");
        KEYWORD_MAP.put("游戏|game|打游戏|原神|王者|吃鸡", "娱乐·游戏");
        KEYWORD_MAP.put("社交|social|微信|刷手机|闲聊|朋友圈", "娱乐·社交");
        KEYWORD_MAP.put("午休|nap|休息|小睡|眯一会", "休息·午休");
        KEYWORD_MAP.put("通勤|commute|路上|骑车|开车|地铁|公交", "休息·通勤");
        KEYWORD_MAP.put("运动|sport|跑步|健身|锻炼|游泳|打球", "运动·锻炼");
        KEYWORD_MAP.put("编码|开发|写代码|coding|feature|需求|实现|重构|refactor", "开发·编码");
    }

    /**
     * 根据描述文本推荐分类ID
     */
    public Long suggestCategory(Long userId, String description) {
        if (description == null || description.isBlank()) return null;

        String desc = description.toLowerCase();

        // 1. 关键词匹配
        for (var entry : KEYWORD_MAP.entrySet()) {
            String[] keywords = entry.getKey().split("\\|");
            for (String kw : keywords) {
                if (desc.contains(kw)) {
                    return findCategoryId(userId, entry.getValue());
                }
            }
        }

        // 2. 按分类名称模糊匹配
        List<Category> categories = categoryMapper.selectList(null);
        for (Category cat : categories) {
            if (desc.contains(cat.getName().toLowerCase().replaceAll("·|·", ""))
                    || cat.getName().toLowerCase().contains(desc)) {
                return cat.getId();
            }
        }

        return null;
    }

    private Long findCategoryId(Long userId, String categoryName) {
        return categoryMapper.selectList(null).stream()
                .filter(c -> c.getName().equals(categoryName))
                .findFirst()
                .map(Category::getId)
                .orElse(null);
    }
}
