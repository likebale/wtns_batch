package com.widetns.batch.controller;

import com.widetns.batch.dto.BatchInfoDto;
import com.widetns.batch.entity.BatchHistory;
import com.widetns.batch.entity.BatchInfo;
import com.widetns.batch.repository.BatchHistoryRepository;
import com.widetns.batch.repository.BatchInfoRepository;
import com.widetns.batch.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;
    private final BatchInfoRepository batchInfoRepository;
    private final BatchHistoryRepository batchHistoryRepository;

    @GetMapping("/management")
    public String batchManagement(Model model, Pageable pageable) {
        Page<BatchInfo> batches = batchInfoRepository.findAll(pageable);
        model.addAttribute("batches", batches);
        return "batch/management";
    }

    @GetMapping("/scan")
    @ResponseBody
    public ResponseEntity<List<String>> scanUnregisteredBatches() {
        try {
            List<String> unregistered = batchService.scanUnregisteredBatches();
            return ResponseEntity.ok(unregistered);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<BatchInfo> registerBatch(@RequestBody String className) {
        try {
            BatchInfo batchInfo = batchService.registerBatch(className);
            return ResponseEntity.ok(batchInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{batchId}/execute")
    @ResponseBody
    public ResponseEntity<String> executeBatch(@PathVariable String batchId) {
        try {
            batchService.executeBatch(batchId, true);
            return ResponseEntity.ok("배치 실행이 시작되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<BatchInfo> updateBatch(
            @PathVariable Long id,
            @RequestBody BatchInfoDto dto) {
        BatchInfo batchInfo = batchInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("배치를 찾을 수 없습니다."));

        batchInfo.setBatchName(dto.getBatchName());
        batchInfo.setBatchDescription(dto.getBatchDescription());
        batchInfo.setCronExpression(dto.getCronExpression());
        batchInfo.setRetryCount(dto.getRetryCount());
        batchInfo.setRetryInterval(dto.getRetryInterval());
        batchInfo.setUseYn(dto.getUseYn());

        return ResponseEntity.ok(batchInfoRepository.save(batchInfo));
    }

    @GetMapping("/history")
    public String batchHistory(Model model, Pageable pageable) {
        Page<BatchHistory> histories = batchHistoryRepository.findAllWithRetries(pageable);
        model.addAttribute("histories", histories);
        return "batch/history";
    }

    @GetMapping("/history/{id}")
    public String batchHistoryDetail(@PathVariable Long id, Model model) {
        BatchHistory history = batchHistoryRepository.findByIdWithRetries(id)
                .orElseThrow(() -> new RuntimeException("이력을 찾을 수 없습니다."));
        model.addAttribute("history", history);
        return "batch/history-detail";
    }
}
