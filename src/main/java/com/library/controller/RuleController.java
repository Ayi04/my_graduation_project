package com.library.controller;

import com.library.entity.Rule;
import com.library.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rule")
public class RuleController {
    
    @Autowired
    private RuleService ruleService;
    
    // 获取所有规则
    @GetMapping
    public List<Rule> getAllRules() {
        return ruleService.getAllRules();
    }
    
    // 根据ID获取规则
    @GetMapping("/{id}")
    public Rule getRuleById(@PathVariable Long id) {
        return ruleService.getRuleById(id);
    }
    
    // 添加规则
    @PostMapping
    public Rule addRule(@RequestBody Rule rule) {
        return ruleService.saveRule(rule);
    }
    
    // 更新规则
    @PutMapping("/{id}")
    public Rule updateRule(@PathVariable Long id, @RequestBody Rule rule) {
        rule.setId(id);
        return ruleService.saveRule(rule);
    }
    
    // 删除规则
    @DeleteMapping("/{id}")
    public void deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
    }
    
    // 根据分类获取规则
    @GetMapping("/category/{category}")
    public List<Rule> getRulesByCategory(@PathVariable String category) {
        return ruleService.getRulesByCategory(category);
    }
}
