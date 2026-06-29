package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.entity.AiSession;
import com.timeweaver.service.AiSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-sessions")
@RequiredArgsConstructor
public class AiSessionController {

    private final AiSessionService aiSessionService;

    @GetMapping
    public ResponseResult<List<AiSession>> list(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (date != null) {
            return ResponseResult.success(aiSessionService.getSessions(userId, date));
        }
        if (startDate != null && endDate != null) {
            return ResponseResult.success(aiSessionService.getSessionsByRange(userId, startDate, endDate));
        }
        return ResponseResult.success(aiSessionService.getSessions(userId, LocalDate.now()));
    }

    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> stats(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseResult.success(aiSessionService.getStats(userId, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseResult<AiSession> getById(@AuthenticationPrincipal Long userId,
                                              @PathVariable Long id) {
        return ResponseResult.success(aiSessionService.getById(userId, id));
    }

    @PostMapping
    public ResponseResult<AiSession> create(@AuthenticationPrincipal Long userId,
                                            @RequestBody AiSession session) {
        return ResponseResult.created(aiSessionService.createSession(userId, session));
    }

    @PutMapping("/{id}")
    public ResponseResult<AiSession> update(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long id,
                                            @RequestBody AiSession session) {
        return ResponseResult.success(aiSessionService.updateSession(userId, id, session));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@AuthenticationPrincipal Long userId,
                                       @PathVariable Long id) {
        aiSessionService.deleteSession(userId, id);
        return ResponseResult.success();
    }
}
