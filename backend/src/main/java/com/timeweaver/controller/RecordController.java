package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.dto.request.RecordCreateRequest;
import com.timeweaver.entity.TimeRecord;
import com.timeweaver.service.CategorySuggestionService;
import com.timeweaver.service.TimeRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final TimeRecordService timeRecordService;
    private final CategorySuggestionService categorySuggestionService;

    @GetMapping
    public ResponseResult<List<TimeRecord>> list(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseResult.success(timeRecordService.getRecordsByDate(userId, date));
    }

    @GetMapping("/current")
    public ResponseResult<TimeRecord> getCurrent(@AuthenticationPrincipal Long userId) {
        TimeRecord record = timeRecordService.getCurrentRecord(userId);
        return ResponseResult.success(record);
    }

    @PostMapping
    public ResponseResult<TimeRecord> create(@AuthenticationPrincipal Long userId,
                                             @Valid @RequestBody RecordCreateRequest request) {
        TimeRecord record = timeRecordService.startRecord(userId, request);
        return ResponseResult.created(record);
    }

    @PostMapping("/manual")
    public ResponseResult<TimeRecord> createManual(@AuthenticationPrincipal Long userId,
                                                   @Valid @RequestBody RecordCreateRequest request) {
        TimeRecord record = timeRecordService.createManualRecord(userId, request);
        return ResponseResult.created(record);
    }

    @PutMapping("/{id}/stop")
    public ResponseResult<TimeRecord> stop(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long id) {
        return ResponseResult.success(timeRecordService.stopRecord(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseResult<TimeRecord> update(@AuthenticationPrincipal Long userId,
                                             @PathVariable Long id,
                                             @Valid @RequestBody RecordCreateRequest request) {
        return ResponseResult.success(timeRecordService.updateRecord(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@AuthenticationPrincipal Long userId,
                                       @PathVariable Long id) {
        timeRecordService.deleteRecord(userId, id);
        return ResponseResult.success();
    }

    @PostMapping("/classify")
    public ResponseResult<Map<String, Object>> classify(
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> body) {
        String description = body.getOrDefault("description", "");
        Long categoryId = categorySuggestionService.suggestCategory(userId, description);
        return ResponseResult.success(Map.of(
                "suggestedCategoryId", categoryId,
                "description", description
        ));
    }
}
