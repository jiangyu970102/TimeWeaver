package com.timeweaver.controller;

import com.timeweaver.common.utils.ResponseResult;
import com.timeweaver.entity.Pomodoro;
import com.timeweaver.service.PomodoroService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pomodoros")
@RequiredArgsConstructor
public class PomodoroController {

    private final PomodoroService pomodoroService;

    @PostMapping("/start")
    public ResponseResult<Pomodoro> start(@AuthenticationPrincipal Long userId,
                                          @RequestParam(required = false) String taskName,
                                          @RequestParam(required = false, defaultValue = "25") Integer durationMin) {
        return ResponseResult.created(pomodoroService.start(userId, taskName, durationMin));
    }

    @PutMapping("/{id}/complete")
    public ResponseResult<Pomodoro> complete(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long id) {
        return ResponseResult.success(pomodoroService.complete(userId, id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseResult<Pomodoro> cancel(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long id) {
        return ResponseResult.success(pomodoroService.cancel(userId, id));
    }

    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> stats(@AuthenticationPrincipal Long userId) {
        return ResponseResult.success(pomodoroService.getStats(userId));
    }
}
