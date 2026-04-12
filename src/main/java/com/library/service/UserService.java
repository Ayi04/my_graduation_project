package com.library.service;

import com.library.entity.*;
import com.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private LoginRepository loginRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private ViolationRecordRepository violationRecordRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public Login registerUser(User user, String username, String password) {
        // 创建登录记录
        Login login = new Login();
        login.setUsername(username);
        login.setPassword(passwordEncoder.encode(password));
        login.setRole(Login.Role.USER);
        
        // 保存登录记录
        login = loginRepository.save(login);
        
        // 关联登录记录到用户
        user.setLogin(login);
        userRepository.save(user);
        
        return login;
    }
    
    public Login registerStaff(Staff staff, String username, String password) {
        // 创建登录记录
        Login login = new Login();
        login.setUsername(username);
        login.setPassword(passwordEncoder.encode(password));
        login.setRole(Login.Role.STAFF);
        
        // 保存登录记录
        login = loginRepository.save(login);
        
        // 关联登录记录到工作人员
        staff.setLogin(login);
        staffRepository.save(staff);
        
        return login;
    }
    
    public Login registerAdmin(Admin admin, String username, String password) {
        // 创建登录记录
        Login login = new Login();
        login.setUsername(username);
        login.setPassword(passwordEncoder.encode(password));
        login.setRole(Login.Role.ADMIN);
        
        // 保存登录记录
        login = loginRepository.save(login);
        
        // 关联登录记录到管理员
        admin.setLogin(login);
        adminRepository.save(admin);
        
        return login;
    }
    
    public Login login(String username, String password) {
        Login login = loginRepository.findByUsername(username);
        if (login != null) {
            // 临时允许明文密码登录
            if (passwordEncoder.matches(password, login.getPassword()) || password.equals(login.getPassword())) {
                return login;
            }
        }
        return null;
    }
    
    public Login updatePassword(Long loginId, String oldPassword, String newPassword) {
        Login login = loginRepository.findById(loginId).orElse(null);
        if (login != null && (passwordEncoder.matches(oldPassword, login.getPassword()) || oldPassword.equals(login.getPassword()))) {
            login.setPassword(passwordEncoder.encode(newPassword));
            return loginRepository.save(login);
        }
        return null;
    }
    
    public User updateUserInfo(Long loginId, User userInfo) {
        User user = userRepository.findByLoginId(loginId);
        if (user != null) {
            user.setGender(userInfo.getGender());
            user.setEmail(userInfo.getEmail());
            user.setPhone(userInfo.getPhone());
            user.setCreditScore(userInfo.getCreditScore());
            
            // 检查信用分是否低于80，需要禁止预约
            if (user.getCreditScore() < 80) {
                user.setIsBanned(true);
                // 禁止预约3天
                user.setBanEndTime(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000);
            } else if (user.getIsBanned() && user.getCreditScore() >= 80) {
                // 信用分恢复到80以上，解除禁止
                user.setIsBanned(false);
                user.setBanEndTime(null);
            }
            
            return userRepository.save(user);
        }
        return null;
    }
    
    public Staff updateStaffInfo(Long loginId, Staff staffInfo) {
        Staff staff = staffRepository.findByLoginId(loginId);
        if (staff != null) {
            staff.setGender(staffInfo.getGender());
            staff.setEmail(staffInfo.getEmail());
            staff.setPhone(staffInfo.getPhone());
            staff.setResponsibleArea(staffInfo.getResponsibleArea());
            return staffRepository.save(staff);
        }
        return null;
    }
    
    public Admin updateAdminInfo(Long loginId, Admin adminInfo) {
        Admin admin = adminRepository.findByLoginId(loginId);
        if (admin != null) {
            admin.setGender(adminInfo.getGender());
            admin.setEmail(adminInfo.getEmail());
            admin.setPhone(adminInfo.getPhone());
            return adminRepository.save(admin);
        }
        return null;
    }
    
    public Login getLoginById(Long loginId) {
        return loginRepository.findById(loginId).orElse(null);
    }
    
    public User getUserByLoginId(Long loginId) {
        return userRepository.findByLoginId(loginId);
    }
    
    public Staff getStaffByLoginId(Long loginId) {
        return staffRepository.findByLoginId(loginId);
    }
    
    public Admin getAdminByLoginId(Long loginId) {
        return adminRepository.findByLoginId(loginId);
    }
    
    // 生成加密密码（用于测试）
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
    
    // 更新用户信用分（带原因）
    public void updateCreditScore(Long loginId, int scoreChange, String reason) {
        User user = userRepository.findByLoginId(loginId);
        if (user != null) {
            int newScore = user.getCreditScore() + scoreChange;
            // 信用分范围限制在0-100之间
            newScore = Math.max(0, Math.min(100, newScore));
            user.setCreditScore(newScore);
            
            // 检查是否需要禁止预约
            if (newScore < 80) {
                user.setIsBanned(true);
                // 禁止预约3天
                long banEndTime = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000;
                user.setBanEndTime(banEndTime);
                
                // 创建禁止预约记录
                ViolationRecord banRecord = new ViolationRecord();
                banRecord.setUser(user);
                banRecord.setType("BAN");
                banRecord.setReason(reason);
                banRecord.setScoreChange(scoreChange);
                banRecord.setBanDays(3);
                banRecord.setCreateTime(new Date());
                banRecord.setBanEndTime(new Date(banEndTime));
                violationRecordRepository.save(banRecord);
            } else if (user.getIsBanned() && newScore >= 80) {
                // 信用分恢复到80以上，解除禁止
                user.setIsBanned(false);
                user.setBanEndTime(null);
            }
            
            // 如果是扣除信用分，创建违规记录
            if (scoreChange < 0) {
                ViolationRecord violationRecord = new ViolationRecord();
                violationRecord.setUser(user);
                violationRecord.setType("VIOLATION");
                violationRecord.setReason(reason);
                violationRecord.setScoreChange(scoreChange);
                violationRecord.setCreateTime(new Date());
                violationRecordRepository.save(violationRecord);
            }
            
            userRepository.save(user);
        }
    }
    
    // 更新用户信用分（默认原因）
    public void updateCreditScore(Long loginId, int scoreChange) {
        updateCreditScore(loginId, scoreChange, "系统自动扣除");
    }
    
    // 检查用户是否可以预约
    public boolean canReserve(Long loginId) {
        User user = userRepository.findByLoginId(loginId);
        if (user == null) {
            return false;
        }
        
        // 检查是否被禁止预约
        if (user.getIsBanned()) {
            // 检查禁止时间是否已过
            if (user.getBanEndTime() != null && System.currentTimeMillis() > user.getBanEndTime()) {
                // 禁止时间已过，恢复信用分并解除禁止
                user.setIsBanned(false);
                user.setBanEndTime(null);
                user.setCreditScore(80); // 恢复到预约阈值
                userRepository.save(user);
                return true;
            }
            return false;
        }
        
        // 信用分低于80不能预约
        if (user.getCreditScore() < 80) {
            return false;
        }
        
        return true;
    }
    
    // 在座位开放时间内未归还扣除信用分5分
    public void deductCreditScoreForLateReturn(Long loginId) {
        User user = userRepository.findByLoginId(loginId);
        if (user != null) {
            updateCreditScore(loginId, -5);
        }
    }
    
    // 获取所有登录用户（包含所有角色）
    public List<Login> getAllLogins() {
        return loginRepository.findAll();
    }
    
    // 获取所有普通用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // 获取所有工作人员
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }
    
    // 获取所有管理员
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
    
    // 根据用户名和角色筛选登录用户
    public List<Login> getLoginsByUsernameAndRole(String username, String role) {
        if (username != null && !username.isEmpty() && role != null && !role.isEmpty()) {
            return loginRepository.findByUsernameContainingAndRole(username, Login.Role.valueOf(role));
        } else if (username != null && !username.isEmpty()) {
            return loginRepository.findByUsernameContaining(username);
        } else if (role != null && !role.isEmpty()) {
            return loginRepository.findByRole(Login.Role.valueOf(role));
        } else {
            return loginRepository.findAll();
        }
    }
    
    // 删除用户（根据登录ID）
    public boolean deleteUser(Long loginId) {
        try {
            Login login = loginRepository.findById(loginId).orElse(null);
            if (login != null) {
                // 根据角色删除对应的用户信息
                if (login.getRole() == Login.Role.USER) {
                    User user = userRepository.findByLoginId(loginId);
                    if (user != null) {
                        userRepository.delete(user);
                    }
                } else if (login.getRole() == Login.Role.STAFF) {
                    Staff staff = staffRepository.findByLoginId(loginId);
                    if (staff != null) {
                        staffRepository.delete(staff);
                    }
                } else if (login.getRole() == Login.Role.ADMIN) {
                    Admin admin = adminRepository.findByLoginId(loginId);
                    if (admin != null) {
                        adminRepository.delete(admin);
                    }
                }
                // 删除登录信息
                loginRepository.delete(login);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 设置用户禁止预约
    public User setUserBanStatus(Long loginId, boolean isBanned, int banDays, String reason) {
        User user = userRepository.findByLoginId(loginId);
        if (user != null) {
            user.setIsBanned(isBanned);
            if (isBanned) {
                // 设置禁止结束时间
                long banEndTime = System.currentTimeMillis() + banDays * 24 * 60 * 60 * 1000;
                user.setBanEndTime(banEndTime);
                
                // 创建禁止预约记录
                ViolationRecord banRecord = new ViolationRecord();
                banRecord.setUser(user);
                banRecord.setType("BAN");
                banRecord.setReason(reason);
                banRecord.setBanDays(banDays);
                banRecord.setCreateTime(new Date());
                banRecord.setBanEndTime(new Date(banEndTime));
                violationRecordRepository.save(banRecord);
            } else {
                // 解除禁止
                user.setBanEndTime(null);
            }
            return userRepository.save(user);
        }
        return null;
    }
    
    // 上传头像
    public Object uploadAvatar(Long loginId, MultipartFile file) {
        try {
            System.out.println("开始处理头像上传，loginId: " + loginId);
            System.out.println("文件信息: " + file.getOriginalFilename() + ", 大小: " + file.getSize());
            
            Login login = loginRepository.findById(loginId).orElse(null);
            if (login == null) {
                System.out.println("登录信息不存在，loginId: " + loginId);
                return null;
            }
            
            // 创建上传目录 - 使用frontend/public/avatars目录
            String uploadDir = System.getProperty("user.dir") + "/frontend/public/avatars";
            System.out.println("上传目录: " + uploadDir);
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                System.out.println("创建上传目录");
                boolean created = dir.mkdirs();
                System.out.println("目录创建结果: " + created);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            System.out.println("原始文件名: " + originalFilename);
            String fileExtension = ".jpg";
            if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                System.out.println("文件扩展名: " + fileExtension);
            }
            String filename = UUID.randomUUID().toString() + fileExtension;
            System.out.println("生成的文件名: " + filename);
            
            // 保存文件
            File dest = new File(dir, filename);
            System.out.println("文件保存路径: " + dest.getAbsolutePath());
            try {
                file.transferTo(dest);
                System.out.println("文件保存成功");
            } catch (IOException e) {
                System.out.println("文件保存失败:");
                e.printStackTrace();
                return null;
            }
            
            // 更新用户头像路径
            String avatarPath = "/avatars/" + filename;
            System.out.println("头像路径: " + avatarPath);
            
            // 根据用户角色更新对应的表
            if (login.getRole() == Login.Role.USER) {
                User user = userRepository.findByLoginId(loginId);
                if (user != null) {
                    user.setAvatar(avatarPath);
                    User savedUser = userRepository.save(user);
                    System.out.println("用户头像更新成功，userId: " + savedUser.getId());
                    return savedUser;
                }
            } else if (login.getRole() == Login.Role.STAFF) {
                Staff staff = staffRepository.findByLoginId(loginId);
                if (staff != null) {
                    staff.setAvatar(avatarPath);
                    Staff savedStaff = staffRepository.save(staff);
                    System.out.println("工作人员头像更新成功，staffId: " + savedStaff.getId());
                    return savedStaff;
                }
            } else if (login.getRole() == Login.Role.ADMIN) {
                Admin admin = adminRepository.findByLoginId(loginId);
                if (admin != null) {
                    admin.setAvatar(avatarPath);
                    Admin savedAdmin = adminRepository.save(admin);
                    System.out.println("管理员头像更新成功，adminId: " + savedAdmin.getId());
                    return savedAdmin;
                }
            }
            
            System.out.println("用户角色未知或未找到对应角色信息");
            return null;
        } catch (Exception e) {
            System.out.println("头像上传处理失败:");
            e.printStackTrace();
            return null;
        }
    }
}