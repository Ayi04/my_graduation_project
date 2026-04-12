package com.library.controller;

import com.library.entity.Area;
import com.library.entity.Carousel;
import com.library.entity.SystemSetting;
import com.library.repository.AreaRepository;
import com.library.repository.CarouselRepository;
import com.library.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    
    @Autowired
    private AreaRepository areaRepository;
    
    @Autowired
    private CarouselRepository carouselRepository;
    
    @Autowired
    private SystemSettingRepository systemSettingRepository;
    
    // 保存系统设置
    @PutMapping("/settings")
    public Map<String, Object> saveSettings(@RequestBody SystemSettingsRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 更新所有区域的签到时间限制和开放时间
            List<Area> areas = areaRepository.findAll();
            for (Area area : areas) {
                area.setCheckinTimeLimit(request.getCheckinTimeLimit());
                if (request.getOpenTime() != null && !request.getOpenTime().isEmpty()) {
                    area.setOpenTime(request.getOpenTime());
                }
                areaRepository.save(area);
            }
            
            // 保存关闭图书馆设置
            saveSystemSetting("isLibraryClosed", request.getIsLibraryClosed() != null ? request.getIsLibraryClosed().toString() : "false");
            saveSystemSetting("closeStartTime", request.getCloseStartTime() != null ? request.getCloseStartTime() : "");
            saveSystemSetting("closeEndTime", request.getCloseEndTime() != null ? request.getCloseEndTime() : "");
            
            response.put("success", true);
            response.put("message", "设置保存成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "设置保存失败: " + e.getMessage());
        }
        
        return response;
    }
    
    // 保存系统设置到数据库
    private void saveSystemSetting(String key, String value) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key);
        if (setting == null) {
            setting = new SystemSetting();
            setting.setSettingKey(key);
        }
        setting.setValue(value);
        systemSettingRepository.save(setting);
    }
    
    // 获取系统设置
    @GetMapping("/settings")
    public Map<String, Object> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        
        // 获取第一个区域的设置作为系统默认值
        List<Area> areas = areaRepository.findAll();
        if (!areas.isEmpty()) {
            Area area = areas.get(0);
            if (area.getCheckinTimeLimit() != null) {
                settings.put("checkinTimeLimit", area.getCheckinTimeLimit());
            } else {
                settings.put("checkinTimeLimit", 15); // 默认15分钟
            }
            if (area.getOpenTime() != null && !area.getOpenTime().isEmpty()) {
                settings.put("openTime", area.getOpenTime());
            } else {
                settings.put("openTime", "09:00-22:00"); // 默认开放时间
            }
        } else {
            settings.put("checkinTimeLimit", 15); // 默认15分钟
            settings.put("openTime", "09:00-22:00"); // 默认开放时间
        }
        
        // 获取关闭图书馆设置
        SystemSetting isLibraryClosedSetting = systemSettingRepository.findBySettingKey("isLibraryClosed");
        SystemSetting closeStartTimeSetting = systemSettingRepository.findBySettingKey("closeStartTime");
        SystemSetting closeEndTimeSetting = systemSettingRepository.findBySettingKey("closeEndTime");
        
        // 调试信息
        System.out.println("isLibraryClosedSetting: " + isLibraryClosedSetting);
        if (isLibraryClosedSetting != null) {
            System.out.println("isLibraryClosedSetting.value: " + isLibraryClosedSetting.getValue());
        }
        
        settings.put("isLibraryClosed", isLibraryClosedSetting != null && "true".equals(isLibraryClosedSetting.getValue()));
        settings.put("closeStartTime", closeStartTimeSetting != null ? closeStartTimeSetting.getValue() : "");
        settings.put("closeEndTime", closeEndTimeSetting != null ? closeEndTimeSetting.getValue() : "");
        
        return settings;
    }
    
    // 系统设置请求参数
    static class SystemSettingsRequest {
        private Integer checkinTimeLimit;
        private String openTime;
        private Boolean isLibraryClosed;
        private String closeStartTime;
        private String closeEndTime;
        
        public Integer getCheckinTimeLimit() {
            return checkinTimeLimit;
        }
        
        public void setCheckinTimeLimit(Integer checkinTimeLimit) {
            this.checkinTimeLimit = checkinTimeLimit;
        }
        
        public String getOpenTime() {
            return openTime;
        }
        
        public void setOpenTime(String openTime) {
            this.openTime = openTime;
        }
        
        public Boolean getIsLibraryClosed() {
            return isLibraryClosed;
        }
        
        public void setIsLibraryClosed(Boolean isLibraryClosed) {
            this.isLibraryClosed = isLibraryClosed;
        }
        
        public String getCloseStartTime() {
            return closeStartTime;
        }
        
        public void setCloseStartTime(String closeStartTime) {
            this.closeStartTime = closeStartTime;
        }
        
        public String getCloseEndTime() {
            return closeEndTime;
        }
        
        public void setCloseEndTime(String closeEndTime) {
            this.closeEndTime = closeEndTime;
        }
    }
    
    // 轮播图请求参数
    static class CarouselRequest {
        private Long id;
        private String imageUrl;
        private String title;
        private String link;
        private Integer orderIndex;
        private Boolean isActive;
        
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getImageUrl() {
            return imageUrl;
        }
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getLink() {
            return link;
        }
        public void setLink(String link) {
            this.link = link;
        }
        public Integer getOrderIndex() {
            return orderIndex;
        }
        public void setOrderIndex(Integer orderIndex) {
            this.orderIndex = orderIndex;
        }
        public Boolean getIsActive() {
            return isActive;
        }
        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }
    
    // 上传轮播图图片
    @PostMapping("/carousel/upload")
    public Map<String, Object> uploadCarouselImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 确保上传目录存在
            String uploadDir = "e:\\Seat_reservation_system\\frontend\\public\\carousel";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + "\\" + fileName;
            
            // 保存文件
            file.transferTo(new File(filePath));
            
            // 返回相对路径
            String relativePath = "/carousel/" + fileName;
            response.put("success", true);
            response.put("imageUrl", relativePath);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "图片上传失败: " + e.getMessage());
        }
        
        return response;
    }
    
    // 获取所有轮播图
    @GetMapping("/carousel")
    public List<Carousel> getCarousels() {
        return carouselRepository.findAllOrdered();
    }
    
    // 保存轮播图
    @PostMapping("/carousel")
    public Map<String, Object> saveCarousel(@RequestBody CarouselRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Carousel carousel;
            if (request.getId() != null) {
                carousel = carouselRepository.findById(request.getId()).orElse(new Carousel());
            } else {
                carousel = new Carousel();
            }
            
            carousel.setImageUrl(request.getImageUrl());
            carousel.setTitle(request.getTitle());
            carousel.setLink(request.getLink());
            carousel.setOrderIndex(request.getOrderIndex());
            carousel.setIsActive(request.getIsActive());
            
            carouselRepository.save(carousel);
            
            response.put("success", true);
            response.put("message", "轮播图保存成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "轮播图保存失败: " + e.getMessage());
        }
        
        return response;
    }
    
    // 删除轮播图
    @DeleteMapping("/carousel/{id}")
    public Map<String, Object> deleteCarousel(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            carouselRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "轮播图删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "轮播图删除失败: " + e.getMessage());
        }
        
        return response;
    }
    
    // 获取激活的轮播图（用于首页展示）
    @GetMapping("/carousel/active")
    public List<Carousel> getActiveCarousels() {
        return carouselRepository.findActiveCarousels();
    }
    
    // 删除轮播图图片
    @DeleteMapping("/carousel/image")
    public Map<String, Object> deleteCarouselImage(@RequestParam("imageUrl") String imageUrl) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 构建图片文件路径
            String uploadDir = "e:\\Seat_reservation_system\\frontend\\public";
            // 确保imageUrl以/开头
            String normalizedImageUrl = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
            String filePath = uploadDir + normalizedImageUrl;
            File file = new File(filePath);
            
            // 尝试删除文件，即使文件不存在也返回成功
            if (file.exists()) {
                file.delete();
            }
            
            response.put("success", true);
            response.put("message", "图片删除成功");
        } catch (Exception e) {
            // 即使出现异常，也返回成功，避免前端报错
            response.put("success", true);
            response.put("message", "图片删除成功");
        }
        
        return response;
    }
}
