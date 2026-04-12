package com.library.controller;

import com.library.entity.*;
import com.library.repository.ViolationRecordRepository;
import com.library.service.FrequentSeatService;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ViolationRecordRepository violationRecordRepository;
    
    @Autowired
    private FrequentSeatService frequentSeatService;
    
    // 注册普通用户
    @PostMapping("/register/user")
    public Login registerUser(@RequestBody RegisterUserRequest request) {
        User user = new User();
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        return userService.registerUser(user, request.getUsername(), request.getPassword());
    }
    
    // 注册工作人员
    @PostMapping("/register/staff")
    public Login registerStaff(@RequestBody RegisterStaffRequest request) {
        Staff staff = new Staff();
        staff.setGender(request.getGender());
        staff.setPhone(request.getPhone());
        staff.setEmail(request.getEmail());
        staff.setResponsibleArea(request.getResponsibleArea());
        return userService.registerStaff(staff, request.getUsername(), request.getPassword());
    }
    
    // 注册管理员
    @PostMapping("/register/admin")
    public Login registerAdmin(@RequestBody RegisterAdminRequest request) {
        Admin admin = new Admin();
        admin.setGender(request.getGender());
        admin.setPhone(request.getPhone());
        admin.setEmail(request.getEmail());
        return userService.registerAdmin(admin, request.getUsername(), request.getPassword());
    }
    
    // 登录
    @PostMapping("/login")
    public Login login(@RequestBody LoginRequest loginRequest) {
        Login login = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (login == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        return login;
    }
    
    // 登录请求参数
    static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    // 注册普通用户请求参数
    static class RegisterUserRequest {
        private String username;
        private String password;
        private String name;
        private String gender;
        private String phone;
        private String email;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getGender() {
            return gender;
        }
        
        public void setGender(String gender) {
            this.gender = gender;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    // 注册工作人员请求参数
    static class RegisterStaffRequest {
        private String username;
        private String password;
        private String name;
        private String gender;
        private String phone;
        private String email;
        private String responsibleArea;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getGender() {
            return gender;
        }
        
        public void setGender(String gender) {
            this.gender = gender;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getResponsibleArea() {
            return responsibleArea;
        }
        
        public void setResponsibleArea(String responsibleArea) {
            this.responsibleArea = responsibleArea;
        }
    }
    
    // 注册管理员请求参数
    static class RegisterAdminRequest {
        private String username;
        private String password;
        private String name;
        private String gender;
        private String phone;
        private String email;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getGender() {
            return gender;
        }
        
        public void setGender(String gender) {
            this.gender = gender;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    // 修改密码
    @PutMapping("/updatePassword")
    public Login updatePassword(@RequestParam Long loginId, @RequestParam String oldPassword, @RequestParam String newPassword) {
        return userService.updatePassword(loginId, oldPassword, newPassword);
    }
    
    // 修改密码请求参数
    static class UpdatePasswordRequest {
        private Long loginId;
        private String oldPassword;
        private String newPassword;
        
        public Long getLoginId() {
            return loginId;
        }
        
        public void setLoginId(Long loginId) {
            this.loginId = loginId;
        }
        
        public String getOldPassword() {
            return oldPassword;
        }
        
        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
    
    // 更新普通用户信息
    @PutMapping("/updateInfo/user")
    public User updateUserInfo(@RequestParam Long loginId, @RequestBody User userInfo) {
        return userService.updateUserInfo(loginId, userInfo);
    }
    
    // 更新工作人员信息
    @PutMapping("/updateInfo/staff")
    public Staff updateStaffInfo(@RequestParam Long loginId, @RequestBody Staff staffInfo) {
        return userService.updateStaffInfo(loginId, staffInfo);
    }
    
    // 更新管理员信息
    @PutMapping("/updateInfo/admin")
    public Admin updateAdminInfo(@RequestParam Long loginId, @RequestBody Admin adminInfo) {
        return userService.updateAdminInfo(loginId, adminInfo);
    }
    
    // 获取登录信息
    @GetMapping("/login/{loginId}")
    public Login getLoginById(@PathVariable Long loginId) {
        return userService.getLoginById(loginId);
    }
    
    // 获取普通用户信息
    @GetMapping("/user/{loginId}")
    public User getUserByLoginId(@PathVariable Long loginId) {
        return userService.getUserByLoginId(loginId);
    }
    
    // 获取工作人员信息
    @GetMapping("/staff/{loginId}")
    public Staff getStaffByLoginId(@PathVariable Long loginId) {
        return userService.getStaffByLoginId(loginId);
    }
    
    // 获取管理员信息
    @GetMapping("/admin/{loginId}")
    public Admin getAdminByLoginId(@PathVariable Long loginId) {
        return userService.getAdminByLoginId(loginId);
    }
    
    // 检查用户是否可以预约
    @GetMapping("/canReserve/{loginId}")
    public boolean canReserve(@PathVariable Long loginId) {
        return userService.canReserve(loginId);
    }
    
    // 获取所有登录用户（包含所有角色）
    @GetMapping("/logins")
    public List<Login> getAllLogins() {
        return userService.getAllLogins();
    }
    
    // 根据用户名和角色筛选登录用户
    @GetMapping("/logins/filter")
    public List<Login> getLoginsByFilter(@RequestParam(required = false) String username, @RequestParam(required = false) String role) {
        return userService.getLoginsByUsernameAndRole(username, role);
    }
    
    // 获取所有普通用户
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    // 获取所有工作人员
    @GetMapping("/staffs")
    public List<Staff> getAllStaff() {
        return userService.getAllStaff();
    }
    
    // 获取所有管理员
    @GetMapping("/admins")
    public List<Admin> getAllAdmins() {
        return userService.getAllAdmins();
    }
    
    // 删除用户
    @DeleteMapping("/delete/{loginId}")
    public boolean deleteUser(@PathVariable Long loginId) {
        return userService.deleteUser(loginId);
    }
    
    // 设置用户禁止预约状态
    @PutMapping("/setBanStatus")
    public User setUserBanStatus(@RequestParam Long loginId, @RequestParam boolean isBanned, @RequestParam int banDays, @RequestParam String reason) {
        return userService.setUserBanStatus(loginId, isBanned, banDays, reason);
    }
    
    // 获取用户的违规记录
    @GetMapping("/violation-records/{loginId}")
    public List<ViolationRecord> getUserViolationRecords(@PathVariable Long loginId) {
        User user = userService.getUserByLoginId(loginId);
        if (user != null) {
            return violationRecordRepository.findByUser(user);
        }
        return new ArrayList<>();
    }
    
    // 获取所有违规记录
    @GetMapping("/violation-records")
    public List<Object> getAllViolationRecords() {
        List<ViolationRecord> records = violationRecordRepository.findAll();
        List<Object> result = new ArrayList<>();
        for (ViolationRecord record : records) {
            // 创建一个Map来存储违规记录和用户名
            java.util.Map<String, Object> recordMap = new java.util.HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("type", record.getType());
            recordMap.put("reason", record.getReason());
            recordMap.put("scoreChange", record.getScoreChange());
            recordMap.put("banDays", record.getBanDays());
            recordMap.put("createTime", record.getCreateTime());
            recordMap.put("banEndTime", record.getBanEndTime());
            // 添加用户名
            if (record.getUser() != null) {
                // 尝试获取用户名
                try {
                    // 使用反射获取login字段
                    java.lang.reflect.Field loginField = record.getUser().getClass().getDeclaredField("login");
                    loginField.setAccessible(true);
                    Object login = loginField.get(record.getUser());
                    if (login != null) {
                        // 使用反射获取username字段
                        java.lang.reflect.Field usernameField = login.getClass().getDeclaredField("username");
                        usernameField.setAccessible(true);
                        Object username = usernameField.get(login);
                        if (username != null) {
                            recordMap.put("username", username);
                        } else {
                            recordMap.put("username", "-");
                        }
                    } else {
                        recordMap.put("username", "-");
                    }
                } catch (Exception e) {
                    // 如果反射失败，设置默认值
                    recordMap.put("username", "-");
                }
            } else {
                recordMap.put("username", "-");
            }
            result.add(recordMap);
        }
        return result;
    }
    
    // 删除违规记录
    @DeleteMapping("/violation-records/{id}")
    public boolean deleteViolationRecord(@PathVariable Long id) {
        try {
            violationRecordRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 上传头像
    @PostMapping("/uploadAvatar")
    public Object uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam("loginId") Long loginId) {
        System.out.println("收到头像上传请求，loginId: " + loginId);
        System.out.println("文件信息: " + file.getOriginalFilename() + ", 大小: " + file.getSize());
        try {
            Object result = userService.uploadAvatar(loginId, file);
            System.out.println("头像上传处理结果: " + (result != null ? "成功" : "失败"));
            if (result == null) {
                System.out.println("返回空结果，可能是用户不存在或文件保存失败");
            }
            return result;
        } catch (Exception e) {
            System.out.println("头像上传控制器异常:");
            e.printStackTrace();
            return null;
        }
    }
    
    // 获取用户的常用座位列表
    @GetMapping("/frequent-seats/{loginId}")
    public List<FrequentSeat> getFrequentSeats(@PathVariable Long loginId) {
        User user = userService.getUserByLoginId(loginId);
        if (user != null) {
            return frequentSeatService.getFrequentSeatsByUser(user);
        }
        return new ArrayList<>();
    }
    
    // 添加常用座位
    @PostMapping("/frequent-seats")
    public FrequentSeat addFrequentSeat(@RequestParam Long loginId, @RequestParam Long seatId) throws Exception {
        User user = userService.getUserByLoginId(loginId);
        if (user != null) {
            return frequentSeatService.addFrequentSeat(user, seatId);
        }
        throw new Exception("用户不存在");
    }
    
    // 删除常用座位
    @DeleteMapping("/frequent-seats/{id}")
    public boolean removeFrequentSeat(@PathVariable Long id) {
        try {
            frequentSeatService.removeFrequentSeat(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 