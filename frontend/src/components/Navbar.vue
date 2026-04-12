<template>
  <div class="navbar-container">
    <!-- 左侧区域：系统名称 -->
    <div class="navbar-left">
      <h1 class="system-name">智能图书馆座位预约管理系统</h1>
    </div>
    
    <!-- 中间区域：功能菜单项 -->
    <div class="navbar-center">
      <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" @select="handleSelect" :ellipsis="false">
        <el-menu-item index="home">首页</el-menu-item>
        <el-menu-item v-if="userRole === 'USER' || userRole === 'user'" index="seats">座位信息</el-menu-item>
        <el-menu-item v-if="userRole === 'USER' || userRole === 'user'" index="reservations">预约信息</el-menu-item>
        <el-menu-item v-if="userRole === 'USER' || userRole === 'user'" index="checkin-records">签到记录</el-menu-item>
        <el-menu-item v-if="userRole === 'USER' || userRole === 'user'" index="violation-records">违规记录</el-menu-item>
        <el-menu-item v-if="userRole === 'USER' || userRole === 'user'" index="rule">规则</el-menu-item>
        <el-menu-item v-if="(userRole === 'USER' || userRole === 'user')" index="feedback">反馈与投诉</el-menu-item>
        <el-menu-item v-if="(userRole === 'STAFF' || userRole === 'staff')" index="feedback">系统反馈</el-menu-item>
        <el-menu-item v-if="(userRole === 'ADMIN' || userRole === 'admin')" index="feedback">反馈管理</el-menu-item>
        <el-menu-item v-if="userRole === 'STAFF' || userRole === 'staff'" index="complaint-management">投诉管理</el-menu-item>
        <el-menu-item v-if="userRole === 'ADMIN' || userRole === 'admin'" index="user-management">用户管理</el-menu-item>
        <el-menu-item v-if="userRole === 'ADMIN' || userRole === 'admin'" index="staff-management">工作人员管理</el-menu-item>
        <el-menu-item v-if="userRole === 'ADMIN' || userRole === 'admin'" index="statistics">数据统计</el-menu-item>
        <el-menu-item v-if="userRole === 'ADMIN' || userRole === 'admin'" index="system">系统管理</el-menu-item>
        <el-menu-item v-if="userRole === 'ADMIN' || userRole === 'admin'" index="rule-management">规则管理</el-menu-item>
        <el-menu-item v-if="userRole === 'ADMIN' || userRole === 'admin'" index="notice-management">公告管理</el-menu-item>
        <el-menu-item v-if="userRole === 'STAFF' || userRole === 'staff'" index="seat-management">座位管理</el-menu-item>
        <el-menu-item v-if="userRole === 'STAFF' || userRole === 'staff'" index="reservation-management">预约管理</el-menu-item>
        <el-menu-item v-if="userRole === 'STAFF' || userRole === 'staff'" index="checkin-management">签到管理</el-menu-item>
        <el-menu-item v-if="userRole === 'STAFF' || userRole === 'staff'" index="violation-management">违规管理</el-menu-item>
      </el-menu>
    </div>
    
    <!-- 右侧区域：用户头像和下拉菜单 -->
    <div class="navbar-right">
      <div class="user-info" @click="toggleDropdown">
        <div class="user-avatar" v-if="userDetail">
          <img :src="userDetail.avatar ? userDetail.avatar : '/avatars/default-avatar.jpg'" alt="用户头像" />
        </div>
        <div class="user-avatar" v-else>
          <img src="/avatars/default-avatar.jpg" alt="默认头像" />
        </div>
        
        <!-- 下拉菜单 -->
        <div class="dropdown-menu" v-if="dropdownVisible">
          <div class="dropdown-item" @click="navigateToProfile">个人中心</div>
          <div class="dropdown-item" @click="logout">退出登录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store/user';
import axios from 'axios';

const router = useRouter();
const userStore = useUserStore();

const activeIndex = ref('home');
const dropdownVisible = ref(false);
const userDetail = ref(null);

const user = computed(() => userStore.user);
const userRole = computed(() => userStore.userRole || '');

// 获取用户详细信息（包括头像）
const fetchUserDetail = async () => {
  if (user.value && user.value.id) {
    try {
      let response;
      if (userRole.value === 'USER' || userRole.value === 'user') {
        response = await axios.get(`http://localhost:8080/api/user/user/${user.value.id}`);
      } else if (userRole.value === 'STAFF' || userRole.value === 'staff') {
        response = await axios.get(`http://localhost:8080/api/user/staff/${user.value.id}`);
      } else if (userRole.value === 'ADMIN' || userRole.value === 'admin') {
        response = await axios.get(`http://localhost:8080/api/user/admin/${user.value.id}`);
      }
      if (response && response.data) {
        userDetail.value = response.data;
      }
    } catch (error) {
      console.error('获取用户详细信息失败:', error);
    }
  }
};

// 当用户变化时，重新获取用户详细信息
watch(user, () => {
  fetchUserDetail();
}, { immediate: true });


// 处理菜单选择
const handleSelect = (key) => {
  activeIndex.value = key;
  
  // 根据key跳转到对应的路由
  const routeMap = {
    'home': '/home',
    'seats': '/seats',
    'reservations': '/reservations',
    'checkin-records': '/checkin-records',
    'violation-records': '/violation-records',
    'rule': '/rule',
    'feedback': '/feedback',
    'complaint-management': '/complaint-management',
    'user-management': '/user-management',
    'staff-management': '/staff-management',
    'statistics': '/statistics',
    'system': '/system',
    'rule-management': '/rule-management',
    'notice-management': '/notice-management',
    'seat-management': '/seat-management',
    'reservation-management': '/reservation-management',
    'checkin-management': '/checkin-management',
    'violation-management': '/violation-management'
  };
  
  if (routeMap[key]) {
    router.push(routeMap[key]);
  }
};

// 切换下拉菜单显示状态
const toggleDropdown = () => {
  dropdownVisible.value = !dropdownVisible.value;
};

// 跳转到个人中心
const navigateToProfile = () => {
  dropdownVisible.value = false;
  router.push('/profile');
};

// 退出登录
const logout = async () => {
  dropdownVisible.value = false;
  try {
    // 调用退出登录接口
    await userStore.logout();
    // 跳转到登录页
    router.push('/login');
  } catch (error) {
    console.error('退出登录失败:', error);
  }
};

// 初始化时设置当前激活的菜单项
onMounted(() => {
  // 从localStorage加载用户信息
  userStore.loadUserFromStorage();
  
  const currentPath = router.currentRoute.value.path;
  const pathMap = {
    '/': 'home',
    '/seats': 'seats',
    '/reservations': 'reservations',
    '/checkin-records': 'checkin-records',
    '/violation-records': 'violation-records',
    '/rule': 'rule',
    '/feedback': 'feedback',
    '/complaint-management': 'complaint-management',
    '/user-management': 'user-management',
    '/staff-management': 'staff-management',
    '/statistics': 'statistics',
    '/system': 'system',
    '/rule-management': 'rule-management',
    '/notice-management': 'notice-management',
    '/seat-management': 'seat-management',
    '/reservation-management': 'reservation-management',
    '/checkin-management': 'checkin-management',
    '/violation-management': 'violation-management'
  };
  
  // 检查是否是区域详情页面
  if (currentPath.startsWith('/area/')) {
    activeIndex.value = 'seats';
  } else if (pathMap[currentPath]) {
    activeIndex.value = pathMap[currentPath];
  }
});
</script>

<style scoped>
.navbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
  position: relative;
  z-index: 100;
}

.navbar-left {
  flex: 1;
}

.system-name {
  font-size: 18px;
  font-weight: bold;
  color: #1890ff; /* 主题色 */
  margin: 0;
  font-family: 'FZQianLiZhenYongLeDaDianTiS-GB', 'SimSun', 'STSong', '宋体', serif; /* 方正文沁永乐大典体 */
}

.navbar-center {
  flex: 3;
  display: flex;
  justify-content: center;
  overflow: visible;
}

.navbar-center .el-menu {
  border-bottom: none;
  overflow: visible;
}

.navbar-center .el-menu-item {
  margin: 0 10px;
  white-space: nowrap;
  font-weight: bold;
  font-size: 16px; /* 稍微增大字体大小 */
}

.navbar-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  position: relative;
  cursor: pointer;
}

.user-avatar {
  margin-right: 10px;
}

.user-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.dropdown-menu {
  position: absolute;
  top: 50px;
  right: 0;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  min-width: 120px;
  z-index: 1000;
}

.dropdown-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dropdown-item:hover {
  background-color: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .system-name {
    font-size: 16px;
  }
  
  .navbar-center .el-menu-item {
    font-size: 14px;
    margin: 0 8px;
  }
}

@media (max-width: 992px) {
  .navbar-container {
    padding: 0 15px;
  }
  
  .system-name {
    font-size: 14px;
  }
  
  .navbar-center .el-menu-item {
    font-size: 12px;
    margin: 0 6px;
  }
  
  .user-avatar img {
    width: 32px;
    height: 32px;
  }
}
</style>