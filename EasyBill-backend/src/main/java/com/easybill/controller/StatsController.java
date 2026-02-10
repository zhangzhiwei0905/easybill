package com.easybill.controller;

import com.easybill.dto.CategoryStatsDTO;
import com.easybill.dto.StatsSummaryDTO;
import com.easybill.entity.TransactionType;
import com.easybill.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 获取统计摘要
     */
    @GetMapping("/summary")
    public ResponseEntity<StatsSummaryDTO> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        Long userId = getCurrentUserId();
        StatsSummaryDTO summary = statsService.getSummary(userId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * 获取分类统计
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryStatsDTO>> getCategoryStats(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        Long userId = getCurrentUserId();
        List<CategoryStatsDTO> stats = statsService.getCategoryStats(userId, type, startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return 1L;
    }
}
