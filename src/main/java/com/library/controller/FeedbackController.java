package com.library.controller;

import com.library.entity.Feedback;
import com.library.entity.Login;
import com.library.repository.FeedbackRepository;
import com.library.repository.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private LoginRepository loginRepository;
    
    // 提交反馈
    @PostMapping
    public Feedback submitFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        Login login = loginRepository.findById(feedbackRequest.getLoginId()).orElse(null);
        if (login == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Feedback feedback = new Feedback();
        feedback.setLogin(login);
        feedback.setType(feedbackRequest.getType());
        feedback.setTitle(feedbackRequest.getTitle());
        // 根据反馈类型自动设置目标类型
        if ("complaint".equals(feedbackRequest.getType())) {
            feedback.setTargetType("STAFF"); // 投诉提交给工作人员处理
        } else if ("feedback".equals(feedbackRequest.getType())) {
            feedback.setTargetType("ADMIN"); // 意见反馈提交给管理员处理
        }
        feedback.setContent(feedbackRequest.getContent());
        feedback.setSubmitTime(new Date());
        feedback.setIsProcessed(false);
        
        return feedbackRepository.save(feedback);
    }
    
    // 获取用户的反馈记录
    @GetMapping("/login/{loginId}")
    public List<Feedback> getLoginFeedback(@PathVariable Long loginId) {
        return feedbackRepository.findByLoginId(loginId);
    }
    
    // 获取所有反馈记录
    @GetMapping
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }
    
    // 根据目标类型获取反馈记录（用于管理员和工作人员）
    @GetMapping("/target/{targetType}")
    public List<Feedback> getFeedbackByTargetType(@PathVariable String targetType) {
        return feedbackRepository.findByTargetType(targetType);
    }
    
    // 根据目标类型和处理状态获取反馈记录
    @GetMapping("/target/{targetType}/processed/{isProcessed}")
    public List<Feedback> getFeedbackByTargetTypeAndProcessed(@PathVariable String targetType, @PathVariable Boolean isProcessed) {
        return feedbackRepository.findByTargetTypeAndIsProcessed(targetType, isProcessed);
    }
    
    // 根据目标类型、处理状态、提交人、处理人查询反馈记录
    @GetMapping("/target/{targetType}/search")
    public List<Feedback> searchFeedback(
            @PathVariable String targetType,
            @RequestParam(required = false) Boolean isProcessed,
            @RequestParam(required = false) String submitter,
            @RequestParam(required = false) String processor) {
        if (isProcessed != null && submitter != null && processor != null) {
            return feedbackRepository.findByTargetTypeAndIsProcessedAndLoginUsernameContainingAndProcessorUsernameContaining(targetType, isProcessed, submitter, processor);
        } else if (isProcessed != null && submitter != null) {
            return feedbackRepository.findByTargetTypeAndIsProcessedAndLoginUsernameContaining(targetType, isProcessed, submitter);
        } else if (isProcessed != null && processor != null) {
            return feedbackRepository.findByTargetTypeAndIsProcessedAndProcessorUsernameContaining(targetType, isProcessed, processor);
        } else if (submitter != null && processor != null) {
            return feedbackRepository.findByTargetTypeAndLoginUsernameContainingAndProcessorUsernameContaining(targetType, submitter, processor);
        } else if (isProcessed != null) {
            return feedbackRepository.findByTargetTypeAndIsProcessed(targetType, isProcessed);
        } else if (submitter != null) {
            return feedbackRepository.findByTargetTypeAndLoginUsernameContaining(targetType, submitter);
        } else if (processor != null) {
            return feedbackRepository.findByTargetTypeAndProcessorUsernameContaining(targetType, processor);
        } else {
            return feedbackRepository.findByTargetType(targetType);
        }
    }
    
    // 处理反馈
    @PutMapping("/process/{id}")
    public Feedback processFeedback(@PathVariable Long id, @RequestBody ProcessRequest processRequest) {
        Feedback feedback = feedbackRepository.findById(id).orElse(null);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }
        
        // 获取处理人信息
        if (processRequest.getProcessorId() != null) {
            Login processorLogin = loginRepository.findById(processRequest.getProcessorId()).orElse(null);
            if (processorLogin != null) {
                feedback.setProcessor(processorLogin);
            }
        } else {
            // 尝试从当前登录用户获取处理人信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                Login processorLogin = loginRepository.findByUsername(username);
                if (processorLogin != null) {
                    feedback.setProcessor(processorLogin);
                }
            }
        }
        
        feedback.setIsProcessed(true);
        feedback.setProcessResult(processRequest.getProcessResult());
        feedback.setProcessTime(new Date());
        
        return feedbackRepository.save(feedback);
    }
    
    // 删除反馈（撤回）
    @DeleteMapping("/{id}")
    public void deleteFeedback(@PathVariable Long id) {
        Feedback feedback = feedbackRepository.findById(id).orElse(null);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }
        feedbackRepository.delete(feedback);
    }
    
    // 反馈请求参数
    static class FeedbackRequest {
        private Long loginId;
        private String type;
        private String title;
        private String content;
        
        public Long getLoginId() {
            return loginId;
        }
        
        public void setLoginId(Long loginId) {
            this.loginId = loginId;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
    }
    
    // 处理请求参数
    static class ProcessRequest {
        private String processResult;
        private Long processorId;
        
        public String getProcessResult() {
            return processResult;
        }
        
        public void setProcessResult(String processResult) {
            this.processResult = processResult;
        }
        
        public Long getProcessorId() {
            return processorId;
        }
        
        public void setProcessorId(Long processorId) {
            this.processorId = processorId;
        }
    }
}
