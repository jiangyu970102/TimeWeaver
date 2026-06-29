package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.entity.TimeRecordTemplate;
import com.timeweaver.service.TimeRecordTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/record-templates")
@RequiredArgsConstructor
public class RecordTemplateController {

    private final TimeRecordTemplateService templateService;

    @GetMapping
    public ResponseResult<java.util.List<TimeRecordTemplate>> list(@AuthenticationPrincipal Long userId) {
        return ResponseResult.success(templateService.listTemplates(userId));
    }

    @PostMapping
    public ResponseResult<TimeRecordTemplate> create(@AuthenticationPrincipal Long userId,
                                                      @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Long categoryId = Long.valueOf(body.get("categoryId").toString());
        String description = (String) body.get("description");
        return ResponseResult.success(templateService.create(userId, name, categoryId, description));
    }

    @PutMapping("/{id}")
    public ResponseResult<TimeRecordTemplate> update(@AuthenticationPrincipal Long userId,
                                                      @PathVariable Long id,
                                                      @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Long categoryId = body.get("categoryId") != null ? Long.valueOf(body.get("categoryId").toString()) : null;
        String description = (String) body.get("description");
        return ResponseResult.success(templateService.update(userId, id, name, categoryId, description));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@AuthenticationPrincipal Long userId,
                                        @PathVariable Long id) {
        templateService.delete(userId, id);
        return ResponseResult.success();
    }
}
