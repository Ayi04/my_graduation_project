package com.library.controller;

import com.library.entity.Notice;
import com.library.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    
    @Autowired
    private NoticeRepository noticeRepository;
    
    // 获取所有公告
    @GetMapping
    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }
    
    // 添加公告
    @PostMapping
    public Notice addNotice(@RequestBody Notice notice) {
        return noticeRepository.save(notice);
    }
    
    // 更新公告
    @PutMapping("/{id}")
    public Notice updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        return noticeRepository.save(notice);
    }
    
    // 删除公告
    @DeleteMapping("/{id}")
    public void deleteNotice(@PathVariable Long id) {
        noticeRepository.deleteById(id);
    }
    
    // 根据ID获取公告
    @GetMapping("/{id}")
    public Notice getNoticeById(@PathVariable Long id) {
        return noticeRepository.findById(id).orElse(null);
    }
}