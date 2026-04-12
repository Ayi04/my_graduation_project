<template>
  <div class="violation-management-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1>违规管理</h1>
      
      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form" style="display: flex; align-items: center; justify-content: space-between;">
        <el-form-item label="用户名" style="margin-right: 10px;">
          <el-input v-model="searchForm.username" placeholder="输入用户名" style="width: 150px;"></el-input>
        </el-form-item>
        <el-form-item label="日期" style="margin-right: 10px;">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            placeholder="选择日期"
            style="width: 180px"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
      </el-form>
      
      <!-- 违规记录表格 -->
      <el-card class="violation-card">
        <template #header>
          <div class="card-header">
            <h3>违规记录</h3>
          </div>
        </template>
        <el-table :data="paginatedViolationRecords" style="width: 100%">
          <el-table-column prop="id" label="ID" min-width="80"></el-table-column>
          <el-table-column label="用户" min-width="150">
            <template #default="scope">
              {{ scope.row.username || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="类型" min-width="120">
            <template #default="scope">
              <el-tag v-if="scope.row.type === 'VIOLATION'" type="warning">信用分扣除</el-tag>
              <el-tag v-else-if="scope.row.type === 'BAN'" type="danger">禁止预约</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="原因"></el-table-column>
          <el-table-column label="信用分变化" min-width="120">
            <template #default="scope">
              <span v-if="scope.row.scoreChange" :class="scope.row.scoreChange < 0 ? 'text-danger' : 'text-success'">
                {{ scope.row.scoreChange > 0 ? '+' : '' }}{{ scope.row.scoreChange }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="禁止天数" min-width="100">
            <template #default="scope">
              <span v-if="scope.row.banDays">{{ scope.row.banDays }}天</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="禁止结束时间" min-width="180">
            <template #default="scope">
              <span v-if="scope.row.banEndTime">{{ formatDate(scope.row.banEndTime) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="100">
            <template #default="scope">
              <el-button type="danger" size="small" @click="deleteViolationRecord(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页组件 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredViolationRecords.length"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const userStore = useUserStore()
const violationRecords = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  username: '',
  date: ''
})

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

const handleSelect = (key) => {
  switch (key) {
    case 'home':
      router.push('/home')
      break
    case 'seat-management':
      router.push('/seat-management')
      break
    case 'reservation-management':
      router.push('/reservation-management')
      break
    case 'checkin-management':
      router.push('/checkin-management')
      break
    case 'violation-management':
      router.push('/violation-management')
      break
    case 'feedback':
      router.push('/feedback')
      break
    case 'complaint-management':
      router.push('/feedback')
      break
    case 'profile':
      router.push('/profile')
      break
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 过滤后的违规记录
const filteredViolationRecords = ref([])

// 分页后的违规记录
const paginatedViolationRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredViolationRecords.value.slice(start, end)
})

const handleSearch = () => {
  // 执行筛选逻辑
  let filtered = [...violationRecords.value]
  
  // 按用户名过滤
  if (searchForm.username) {
    const username = searchForm.username.toLowerCase()
    filtered = filtered.filter(record => 
      record.username?.toLowerCase().includes(username)
    )
  }
  
  // 按日期过滤
  if (searchForm.date) {
    const searchDate = searchForm.date
    const year = searchDate.getFullYear()
    const month = String(searchDate.getMonth() + 1).padStart(2, '0')
    const day = String(searchDate.getDate()).padStart(2, '0')
    const searchDateString = `${year}-${month}-${day}`
    
    filtered = filtered.filter(record => {
      if (!record.createTime) return false
      const recordDate = new Date(record.createTime)
      const recordYear = recordDate.getFullYear()
      const recordMonth = String(recordDate.getMonth() + 1).padStart(2, '0')
      const recordDay = String(recordDate.getDate()).padStart(2, '0')
      const recordDateString = `${recordYear}-${recordMonth}-${recordDay}`
      return recordDateString === searchDateString
    })
  }
  
  filteredViolationRecords.value = filtered
  // 搜索后重置到第一页
  currentPage.value = 1
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.date = ''
  // 重置后显示所有违规记录
  filteredViolationRecords.value = [...violationRecords.value]
  // 重置到第一页
  currentPage.value = 1
}

// 分页方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

const loadViolationRecords = async () => {
  try {
    // 调用后端API获取所有违规记录
    const response = await axios.get('http://localhost:8080/api/user/violation-records')
    violationRecords.value = response.data
  } catch (error) {
    console.error('加载违规记录失败', error)
    ElMessage.error('加载违规记录失败，请稍后重试')
  } finally {
    // 初始化过滤后的违规记录
    filteredViolationRecords.value = [...violationRecords.value]
  }
}

const deleteViolationRecord = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条违规记录吗？删除后将无法恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API删除违规记录
    const response = await axios.delete(`http://localhost:8080/api/user/violation-records/${id}`)
    if (response.data) {
      ElMessage.success('删除成功')
      // 重新加载违规记录
      loadViolationRecords()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除违规记录失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadViolationRecords()
})
</script>

<style scoped>
.violation-management-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.content h1 {
  color: #333;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.search-form {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-form .el-form {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-form .el-form-item {
  margin-bottom: 0;
  margin-right: 10px;
}

.search-form .el-form-item:last-child {
  margin-right: 0;
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}

.violation-card {
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.violation-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>