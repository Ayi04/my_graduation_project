package com.library.repository;

import com.library.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByLoginId(Long loginId);
    List<Feedback> findByIsProcessed(Boolean isProcessed);
    List<Feedback> findByTargetType(String targetType);
    List<Feedback> findByTargetTypeAndIsProcessed(String targetType, Boolean isProcessed);
    List<Feedback> findByTargetTypeAndLoginUsernameContaining(String targetType, String submitter);
    List<Feedback> findByTargetTypeAndIsProcessedAndLoginUsernameContaining(String targetType, Boolean isProcessed, String submitter);
    List<Feedback> findByTargetTypeAndProcessorUsernameContaining(String targetType, String processor);
    List<Feedback> findByTargetTypeAndIsProcessedAndProcessorUsernameContaining(String targetType, Boolean isProcessed, String processor);
    List<Feedback> findByTargetTypeAndLoginUsernameContainingAndProcessorUsernameContaining(String targetType, String submitter, String processor);
    List<Feedback> findByTargetTypeAndIsProcessedAndLoginUsernameContainingAndProcessorUsernameContaining(String targetType, Boolean isProcessed, String submitter, String processor);
}
