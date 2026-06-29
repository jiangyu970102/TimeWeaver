package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.entity.Goal;
import com.timeweaver.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public ResponseResult<?> list(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseResult.success(goalService.listGoals(userId, activeOnly));
    }

    @GetMapping("/summary")
    public ResponseResult<Map<String, Object>> summary(@AuthenticationPrincipal Long userId) {
        return ResponseResult.success(goalService.getSummary(userId));
    }

    @GetMapping("/{id}")
    public ResponseResult<Goal> getById(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ResponseResult.success(goalService.getById(userId, id));
    }

    @PostMapping
    public ResponseResult<Goal> create(@AuthenticationPrincipal Long userId, @RequestBody Goal goal) {
        return ResponseResult.success(goalService.create(userId, goal));
    }

    @PutMapping("/{id}")
    public ResponseResult<Goal> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody Goal goal) {
        return ResponseResult.success(goalService.update(userId, id, goal));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        goalService.delete(userId, id);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/status")
    public ResponseResult<Goal> updateStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        return ResponseResult.success(goalService.updateStatus(userId, id, body.get("status")));
    }

    @PostMapping("/{id}/recalculate")
    public ResponseResult<Goal> recalculate(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ResponseResult.success(goalService.recalculateProgress(userId, id));
    }
}
