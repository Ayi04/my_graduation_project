package com.library.service.impl;

import com.library.entity.Rule;
import com.library.repository.RuleRepository;
import com.library.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleServiceImpl implements RuleService {
    
    @Autowired
    private RuleRepository ruleRepository;
    
    @Override
    public List<Rule> getAllRules() {
        return ruleRepository.findAllByOrderBySortOrderAsc();
    }
    
    @Override
    public Rule getRuleById(Long id) {
        return ruleRepository.findById(id).orElse(null);
    }
    
    @Override
    public Rule saveRule(Rule rule) {
        return ruleRepository.save(rule);
    }
    
    @Override
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }
    
    @Override
    public List<Rule> getRulesByCategory(String category) {
        return ruleRepository.findByCategory(category);
    }
}
