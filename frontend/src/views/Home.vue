<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <!-- 欢迎信息 -->
      <div class="welcome">
        <h1>欢迎回来，{{ user?.username }}</h1>
      </div>
      
      <!-- 轮播图 -->
      <div class="carousel-section">
        <el-carousel :interval="5000" type="card" height="400px">
          <el-carousel-item v-for="carousel in carousels" :key="carousel.id">
            <img :src="carousel.imageUrl" alt="carousel.title" style="width: 100%; height: 100%; object-fit: cover;">
            <div class="carousel-title" v-if="carousel.title">{{ carousel.title }}</div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 公告 -->
      <div class="section">
        <h2>通知公告</h2>
        <div class="notice-list">
          <div 
            v-for="notice in notices" 
            :key="notice.id" 
            @click="navigateToNoticeDetail(notice.id)" 
            class="notice-item"
          >
            <span class="notice-item-icon"></span>
            <span class="notice-item-title">{{ notice.title }}</span>
            <span class="notice-item-time">{{ formatDateShort(notice.publishTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 普通用户看到的内容 -->
      <template v-if="userRole === 'USER'">
        <!-- 数据大屏 -->
        <div class="section">
          <h2>数据大屏</h2>
          
          <!-- 模块1：全局概览 -->
          <div class="overview-cards">
            <el-card class="overview-card">
              <div class="card-content">
                <div class="card-icon total-seats"></div>
                <div class="card-info">
                  <div class="card-value">{{ overviewStats.totalSeats }}</div>
                  <div class="card-label">总座位数</div>
                </div>
              </div>
            </el-card>
            <el-card class="overview-card">
              <div class="card-content">
                <div class="card-icon occupied-seats"></div>
                <div class="card-info">
                  <div class="card-value">{{ overviewStats.occupiedSeats }}</div>
                  <div class="card-label">已占用座位数</div>
                </div>
              </div>
            </el-card>
            <el-card class="overview-card">
              <div class="card-content">
                <div class="card-icon available-seats"></div>
                <div class="card-info">
                  <div class="card-value">{{ overviewStats.availableSeats }}</div>
                  <div class="card-label">空闲座位数</div>
                </div>
              </div>
            </el-card>
            <el-card class="overview-card">
              <div class="card-content">
                <div class="card-icon occupancy-rate"></div>
                <div class="card-info">
                  <div class="card-value">{{ overviewStats.occupancyRate }}%</div>
                  <div class="card-label">整体占用率</div>
                </div>
              </div>
            </el-card>
          </div>
          
          <!-- 模块2：各区域占用率 -->
          <div class="area-occupancy">
            <h3>各区域占用率</h3>
            <div v-for="area in areaOccupancy" :key="area.areaId" class="area-item">
              <div class="area-header">
                <span class="area-name">{{ area.areaName }}</span>
                <span class="area-stats">{{ area.occupiedSeats }}/{{ area.totalSeats }}</span>
              </div>
              <el-progress 
                :percentage="area.occupancyRate" 
                :color="getProgressColor(area.occupancyRate)" 
                :format="() => `${area.occupancyRate}%`"
              />
            </div>
          </div>
          

          
          <!-- 模块4：实时签到动态 -->
          <div class="checkin-dynamic">
            <h3>实时签到动态</h3>
            <div class="checkin-list">
              <div v-for="record in checkinRecords" :key="record.id" class="checkin-item">
                <span class="checkin-user">{{ maskUsername(record.username) }}</span>
                <span class="checkin-seat">{{ record.seatNumber }}</span>
                <span class="checkin-time">{{ formatTime(record.checkinTime) }}</span>
              </div>
              <div v-if="checkinRecords.length === 0" class="no-records">
                暂无签到记录
              </div>
            </div>
          </div>
        </div>
        
        <!-- 常用座位 -->
        <div class="section">
          <h2>常用座位</h2>
          <div v-if="frequentSeats.length > 0" class="frequent-seats">
            <el-card v-for="item in frequentSeats" :key="item.id" class="seat-card">
              <template #header>
                <div class="card-header">
                  <span>{{ item.seat.area.name }}</span>
                  <span class="seat-number">{{ item.seat.seatNumber }}</span>
                  <el-button type="danger" size="small" @click="removeFrequentSeat(item.id)">删除</el-button>
                </div>
              </template>
              <div class="card-body">
                <p>楼层：{{ item.seat.area.floor }}楼</p>
                <p>区域类型：{{ getAreaTypeText(item.seat.area.type) }}</p>
                <p>状态：{{ getSeatStatusText(item.seat.status) }}</p>
                <el-button type="primary" size="small" @click="reserveSeat(item.seat.id)">立即预约</el-button>
              </div>
            </el-card>
          </div>
          <div v-else class="no-frequent-seats">
            <p>您还没有添加常用座位，最多可添加2个常用座位</p>
            <el-button type="primary" @click="openAddFrequentSeatDialog">添加常用座位</el-button>
          </div>
        </div>

      </template>

      <!-- 管理员看到的内容 -->
      <template v-if="userRole === 'ADMIN'">
        <!-- 工作人员管理 -->
        <div class="section">
          <h2>工作人员管理</h2>
          <el-button type="primary" @click="navigateToAdmin('staff')">管理工作人员账户</el-button>
        </div>

        <!-- 用户管理 -->
        <div class="section">
          <h2>用户管理</h2>
          <el-button type="primary" @click="navigateToAdmin('user')">管理用户账户</el-button>
        </div>
      </template>
    </div>

    <!-- 添加常用座位对话框 -->
    <el-dialog
      v-model="addFrequentSeatDialogVisible"
      title="添加常用座位"
      width="500px"
    >
      <div class="dialog-content">
        <p>请选择楼层、区域和座位：</p>
        
        <!-- 楼层选择 -->
        <div class="form-item">
          <label class="form-label">楼层：</label>
          <el-select
            v-model="selectedFloor"
            placeholder="选择楼层"
            class="w-full"
          >
            <el-option label="1楼" value="1" />
            <el-option label="2楼" value="2" />
            <el-option label="3楼" value="3" />
          </el-select>
        </div>
        
        <!-- 区域选择 -->
        <div class="form-item">
          <label class="form-label">区域：</label>
          <el-select
            v-model="selectedAreaId"
            placeholder="选择区域"
            class="w-full"
            @change="loadAvailableSeats"
          >
            <el-option
              v-for="area in filteredAreas"
              :key="area.id"
              :label="`${area.name} (${getAreaTypeText(area.type)})`"
              :value="area.id"
            />
          </el-select>
        </div>
        
        <!-- 座位选择 -->
        <div class="form-item">
          <label class="form-label">座位：</label>
          <el-select
            v-model="selectedSeatId"
            placeholder="选择座位"
            class="w-full"
            :disabled="!selectedAreaId"
          >
            <el-option
              v-for="seat in availableSeats"
              :key="seat.id"
              :label="seat.seatNumber"
              :value="seat.id"
            />
          </el-select>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addFrequentSeatDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addFrequentSeat" :disabled="!selectedSeatId">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import * as echarts from 'echarts'
import Navbar from '../components/Navbar.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const recommendedSeats = ref([])
const notices = ref([])

const carousels = ref([])
const frequentSeats = ref([])
const addFrequentSeatDialogVisible = ref(false)
const availableSeats = ref([])
const selectedSeatId = ref(null)
const areas = ref([])
const selectedFloor = ref('')
const selectedAreaId = ref(null)

// 数据大屏相关数据
const overviewStats = ref({
  totalSeats: 0,
  occupiedSeats: 0,
  availableSeats: 0,
  occupancyRate: 0
})
const areaOccupancy = ref([])
const checkinRecords = ref([])

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

const getAreaTypeText = (type) => {
  const typeMap = {
    SILENT: '静音区',
    BROWSING: '浏览区',
    NORMAL: '普通区'
  }
  return typeMap[type] || type
}

const getSeatStatusText = (status) => {
  const statusMap = {
    AVAILABLE: '可用',
    RESERVED: '已预约',
    OCCUPIED: '已占用'
  }
  return statusMap[status] || status
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const formatDateShort = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
}

const navigateToNoticeDetail = (id) => {
  router.push(`/notice-detail/${id}`)
}

const reserveSeat = (seatId) => {
  // 跳转到座位预约页面
  router.push(`/seats?seatId=${seatId}`)
}

const navigateToAdmin = (type) => {
  // 跳转到相应的管理页面
  if (type === 'staff') {
    router.push('/staff-management')
  } else if (type === 'user') {
    router.push('/user-management')
  }
}

// 加载用户的常用座位
const loadFrequentSeats = async () => {
  try {
    if (!user.value || !user.value.id) return
    
    // 调用后端API获取常用座位
    const response = await axios.get(`http://localhost:8080/api/user/frequent-seats/${user.value.id}`)
    frequentSeats.value = response.data
  } catch (error) {
    console.error('加载常用座位失败', error)
  }
}

// 打开添加常用座位对话框
const openAddFrequentSeatDialog = async () => {
  try {
    // 加载所有区域
    const areasResponse = await axios.get('http://localhost:8080/api/seat/areas')
    areas.value = areasResponse.data
    
    // 重置选择
    selectedFloor.value = ''
    selectedAreaId.value = null
    selectedSeatId.value = null
    availableSeats.value = []
    
    addFrequentSeatDialogVisible.value = true
  } catch (error) {
    console.error('加载区域失败', error)
  }
}

// 加载指定区域的可用座位
const loadAvailableSeats = async () => {
  try {
    if (!selectedAreaId.value) {
      availableSeats.value = []
      return
    }
    
    // 加载指定区域的可用座位
    const seatResponse = await axios.get(`http://localhost:8080/api/seat/availableSeats/${selectedAreaId.value}`)
    const availableSeatsInArea = seatResponse.data
    
    // 获取当前区域信息
    const currentArea = areas.value.find(area => area.id === selectedAreaId.value)
    
    // 格式化座位数据
    const seats = availableSeatsInArea.map(seat => ({
      id: seat.id,
      seatNumber: seat.seatNumber,
      area: currentArea
    }))
    
    availableSeats.value = seats
  } catch (error) {
    console.error('加载可用座位失败', error)
  }
}

// 过滤区域按楼层
const filteredAreas = computed(() => {
  if (!selectedFloor.value) {
    return areas.value
  }
  return areas.value.filter(area => area.floor.toString() === selectedFloor.value)
})

// 添加常用座位
const addFrequentSeat = async () => {
  try {
    if (!user.value || !user.value.id || !selectedSeatId.value) return
    
    // 调用后端API添加常用座位
    await axios.post('http://localhost:8080/api/user/frequent-seats', null, {
      params: {
        loginId: user.value.id,
        seatId: selectedSeatId.value
      }
    })
    
    // 关闭对话框并重新加载常用座位
    addFrequentSeatDialogVisible.value = false
    await loadFrequentSeats()
    
    // 显示成功提示
    ElMessage.success('添加常用座位成功')
  } catch (error) {
    console.error('添加常用座位失败', error)
    ElMessage.error(error.response?.data || '添加常用座位失败')
  }
}

// 删除常用座位
const removeFrequentSeat = async (id) => {
  try {
    // 弹出确认对话框
    await ElMessageBox.confirm('确定要删除该常用座位吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API删除常用座位
    await axios.delete(`http://localhost:8080/api/user/frequent-seats/${id}`)
    
    // 重新加载常用座位
    await loadFrequentSeats()
    
    // 显示成功提示
    ElMessage.success('删除常用座位成功')
  } catch (error) {
    // 如果是用户取消删除，不显示错误信息
    if (error !== 'cancel') {
      console.error('删除常用座位失败', error)
      ElMessage.error('删除常用座位失败')
    }
  }
}

// 数据大屏相关方法

// 加载全局概览数据
const loadOverviewStats = async () => {
  try {
    // 调用后端API获取全局概览数据
    const response = await axios.get('http://localhost:8080/api/statistics/overview')
    overviewStats.value = response.data
  } catch (error) {
    console.error('加载全局概览数据失败', error)
    // 使用模拟数据
    overviewStats.value = {
      totalSeats: 200,
      occupiedSeats: 120,
      availableSeats: 80,
      occupancyRate: 60
    }
  }
}

// 加载各区域占用率数据
const loadAreaOccupancy = async () => {
  try {
    // 调用后端API获取各区域占用率数据
    const response = await axios.get('http://localhost:8080/api/statistics/area-occupancy')
    areaOccupancy.value = response.data
  } catch (error) {
    console.error('加载区域占用率数据失败', error)
    // 使用模拟数据
    areaOccupancy.value = [
      { areaId: 1, areaName: '1楼静音区', totalSeats: 30, occupiedSeats: 18, occupancyRate: 60 },
      { areaId: 2, areaName: '1楼浏览区', totalSeats: 40, occupiedSeats: 28, occupancyRate: 70 },
      { areaId: 3, areaName: '1楼普通区', totalSeats: 50, occupiedSeats: 35, occupancyRate: 70 },
      { areaId: 4, areaName: '2楼静音区', totalSeats: 25, occupiedSeats: 10, occupancyRate: 40 },
      { areaId: 5, areaName: '2楼浏览区', totalSeats: 35, occupiedSeats: 14, occupancyRate: 40 },
      { areaId: 6, areaName: '2楼普通区', totalSeats: 20, occupiedSeats: 5, occupancyRate: 25 }
    ]
  }
}

// 加载实时签到动态数据
const loadCheckinRecords = async () => {
  try {
    // 调用后端API获取最近的签到记录
    const response = await axios.get('http://localhost:8080/api/statistics/recent-checkins')
    checkinRecords.value = response.data
  } catch (error) {
    console.error('加载签到记录失败', error)
    // 使用模拟数据
    checkinRecords.value = [
      { id: 1, username: '张三', seatNumber: '1-S1', checkinTime: new Date() },
      { id: 2, username: '李四', seatNumber: '1-B5', checkinTime: new Date(Date.now() - 1000 * 60 * 5) },
      { id: 3, username: '王五', seatNumber: '2-N3', checkinTime: new Date(Date.now() - 1000 * 60 * 10) },
      { id: 4, username: '赵六', seatNumber: '1-S10', checkinTime: new Date(Date.now() - 1000 * 60 * 15) },
      { id: 5, username: '孙七', seatNumber: '2-B2', checkinTime: new Date(Date.now() - 1000 * 60 * 20) }
    ]
  }
}

// 获取进度条颜色
const getProgressColor = (rate) => {
  if (rate < 40) {
    return '#67C23A'
  } else if (rate < 70) {
    return '#E6A23C'
  } else {
    return '#F56C6C'
  }
}

// 格式化时间
const formatTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}`
}

// 脱敏用户名
const maskUsername = (username) => {
  if (!username || username.length <= 1) return username
  return username[0] + '*'.repeat(username.length - 1)
}

const loadRecommendedSeats = async () => {
  try {
    // 调用后端API获取推荐座位
    const response = await axios.get('http://localhost:8080/api/seat/areas')
    const areas = response.data
    
    // 从每个区域中获取一个可用座位作为推荐
    const seats = []
    for (const area of areas) {
      const seatResponse = await axios.get(`http://localhost:8080/api/seat/availableSeats/${area.id}`)
      const availableSeats = seatResponse.data
      if (availableSeats.length > 0) {
        seats.push({
          id: availableSeats[0].id,
          seatNumber: availableSeats[0].seatNumber,
          status: availableSeats[0].status,
          area: area
        })
      }
    }
    
    recommendedSeats.value = seats.slice(0, 3) // 只显示前3个推荐座位
  } catch (error) {
    console.error('加载推荐座位失败', error)
    // 如果API调用失败，使用模拟数据
    recommendedSeats.value = [
      {
        id: 1,
        seatNumber: '1-S1',
        status: 'AVAILABLE',
        area: {
          id: 1,
          name: '静音区1',
          floor: 1,
          type: 'SILENT'
        }
      },
      {
        id: 2,
        seatNumber: '1-B1',
        status: 'AVAILABLE',
        area: {
          id: 2,
          name: '浏览区1',
          floor: 1,
          type: 'BROWSING'
        }
      },
      {
        id: 3,
        seatNumber: '2-N1',
        status: 'AVAILABLE',
        area: {
          id: 6,
          name: '普通区2',
          floor: 2,
          type: 'NORMAL'
        }
      }
    ]
  }
}

const loadNotices = async () => {
  try {
    // 调用后端API获取公告
    const response = await axios.get('http://localhost:8080/api/notice')
    notices.value = response.data
  } catch (error) {
    console.error('加载公告失败', error)
    // 如果API调用失败，使用模拟数据
    notices.value = [
      {
        id: 1,
        title: '图书馆开放时间调整通知',
        content: '尊敬的读者，因学校安排，图书馆将于本周末（10月15-16日）闭馆进行设备维护，给您带来不便敬请谅解。',
        publishTime: new Date(),
        publisher: '管理员'
      },
      {
        id: 2,
        title: '座位预约系统上线公告',
        content: '亲爱的读者，图书馆座位预约系统已正式上线，您可以通过系统预约座位，合理安排学习时间。',
        publishTime: new Date(),
        publisher: '管理员'
      }
    ]
  }
}

// 加载轮播图
const loadCarousels = async () => {
  try {
    // 调用后端API获取激活的轮播图
    const response = await axios.get('http://localhost:8080/api/system/carousel/active')
    carousels.value = response.data
  } catch (error) {
    console.error('加载轮播图失败', error)
  }
}



onMounted(() => {
  userStore.loadUserFromStorage()
  loadNotices()
  loadCarousels()
  
  // 只有普通用户需要加载常用座位和数据大屏数据
  if (userRole.value === 'USER') {
    loadFrequentSeats()
    loadOverviewStats()
    loadAreaOccupancy()
    loadCheckinRecords()
    
    // 每10秒刷新签到记录
    setInterval(() => {
      loadCheckinRecords()
    }, 10000)
  }
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.carousel-section {
  margin-bottom: 40px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.carousel-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  color: white;
  padding: 30px 20px 20px;
  font-size: 20px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

.el-carousel-item:hover .carousel-title {
  transform: translateY(-5px);
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.8);
}

.welcome {
  margin-bottom: 40px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.welcome h1 {
  color: #333;
  margin-bottom: 10px;
}

.section {
  margin-bottom: 40px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section h2 {
  color: #333;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.recommended-seats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.frequent-seats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.seat-card {
  transition: all 0.3s ease;
}

.seat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seat-number {
  font-weight: bold;
  color: #1890ff;
}

.card-body {
  padding: 15px 0;
}

.card-body p {
  margin-bottom: 10px;
}



.publisher {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
  text-align: right;
}

.notice-list {
  border-top: 1px solid #eaeaea;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eaeaea;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notice-item:hover {
  background-color: #f5f7fa;
}

.notice-item-icon {
  width: 8px;
  height: 8px;
  background-color: #1890ff;
  transform: rotate(45deg);
  margin-right: 10px;
  align-self: center;
}

.notice-item-title {
  color: #333;
  font-size: 16px;
  font-weight: 500;
  flex: 1;
  margin-right: 20px;
}

.notice-item-time {
  color: #999;
  font-size: 14px;
  white-space: nowrap;
}

.no-frequent-seats {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.no-frequent-seats p {
  margin-bottom: 20px;
}

.dialog-content {
  padding: 20px 0;
}

.dialog-content p {
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

/* 数据大屏样式 */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.overview-card {
  transition: all 0.3s ease;
  border-radius: 10px;
}

.overview-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.card-icon.total-seats {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-icon.occupied-seats {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.card-icon.available-seats {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.card-icon.occupancy-rate {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #666;
}

.area-occupancy {
  margin-bottom: 30px;
}

.area-occupancy h3 {
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
  font-weight: 500;
}

.area-item {
  margin-bottom: 15px;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.area-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.area-stats {
  font-size: 14px;
  color: #666;
}



.checkin-dynamic {
  margin-bottom: 20px;
}

.checkin-dynamic h3 {
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
  font-weight: 500;
}

.checkin-list {
  border: 1px solid #eaeaea;
  border-radius: 10px;
  overflow: hidden;
}

.checkin-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eaeaea;
  transition: background-color 0.3s ease;
}

.checkin-item:hover {
  background-color: #f5f7fa;
}

.checkin-item:last-child {
  border-bottom: none;
}

.checkin-user {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.checkin-seat {
  font-size: 14px;
  color: #666;
}

.checkin-time {
  font-size: 14px;
  color: #999;
}

.no-records {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart {
    height: 250px;
  }
}
</style>