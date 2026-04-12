package com.library.repository;

import com.library.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findByCategory(String category);
    List<Rule> findAllByOrderBySortOrderAsc();
}
