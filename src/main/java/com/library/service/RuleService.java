package com.library.service;

import com.library.entity.Rule;
import java.util.List;

public interface RuleService {
    List<Rule> getAllRules();
    Rule getRuleById(Long id);
    Rule saveRule(Rule rule);
    void deleteRule(Long id);
    List<Rule> getRulesByCategory(String category);
}
