package com.timeweaver.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.timeweaver.common.constant.ApiCode;
import com.timeweaver.common.exception.BusinessException;
import com.timeweaver.entity.TimeRecordTemplate;
import com.timeweaver.mapper.TimeRecordTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeRecordTemplateService {

    private final TimeRecordTemplateMapper templateMapper;

    public List<TimeRecordTemplate> listTemplates(Long userId) {
        return templateMapper.selectList(
                Wrappers.<TimeRecordTemplate>lambdaQuery()
                        .eq(TimeRecordTemplate::getUserId, userId)
                        .orderByAsc(TimeRecordTemplate::getSortOrder)
        );
    }

    public TimeRecordTemplate getById(Long userId, Long id) {
        TimeRecordTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "模板不存在");
        }
        if (!template.getUserId().equals(userId)) {
            throw new BusinessException(ApiCode.FORBIDDEN);
        }
        return template;
    }

    public TimeRecordTemplate create(Long userId, String name, Long categoryId, String description) {
        TimeRecordTemplate template = new TimeRecordTemplate();
        template.setUserId(userId);
        template.setName(name);
        template.setCategoryId(categoryId);
        template.setDescription(description);
        template.setSortOrder(0);
        templateMapper.insert(template);
        return template;
    }

    public TimeRecordTemplate update(Long userId, Long id, String name, Long categoryId, String description) {
        TimeRecordTemplate template = getById(userId, id);
        template.setName(name);
        if (categoryId != null) template.setCategoryId(categoryId);
        template.setDescription(description);
        templateMapper.updateById(template);
        return template;
    }

    public void delete(Long userId, Long id) {
        TimeRecordTemplate template = getById(userId, id);
        templateMapper.deleteById(template.getId());
    }
}
