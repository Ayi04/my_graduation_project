<template>
  <div class="violation-records-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1>违规记录</h1>
      
      <!-- 查询表单 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="类型">
            <el-select v-model="searchForm.type" placeholder="请选择类型" clearable style="width: 120px">
              <el-option label="信用分扣除" value="VIOLATION"></el-option>
              <el-option label="禁止预约" value="BAN"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="searchForm.createTime"
              type="date"
              placeholder="选择日期"
              style="width: 180px"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchViolationRecords">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="paginatedViolationRecords" style="width: 100%">
        <el-table-column prop="id" label="ID" min-width="80"></el-table-column>
        <el-table-column label="类型" min-width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.type === 'VIOLATION'" type="warning">信用分扣除</el-tag>
            <el-tag v-else-if="scope.row.type === 'BAN'" type="danger">禁止预约</el-tag>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" min-width="100">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">查看详细</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
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
      
      <!-- 查看详细对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="违规详情"
        width="600px"
      >
        <el-form :model="detailForm" label-width="100px">
          <el-form-item label="ID">
            <el-input v-model="detailForm.id" disabled></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-input v-model="detailForm.type" disabled></el-input>
          </el-form-item>
          <el-form-item label="原因">
            <el-input v-model="detailForm.reason" type="textarea" :rows="4" disabled></el-input>
          </el-form-item>
          <el-form-item label="信用分变化">
            <el-input v-model="detailForm.scoreChange" disabled></el-input>
          </el-form-item>
          <el-form-item label="禁止天数">
            <el-input v-model="detailForm.banDays" disabled></el-input>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-input v-model="detailForm.createTime" disabled></el-input>
          </el-form-item>
          <el-form-item label="禁止结束时间">
            <el-input v-model="detailForm.banEndTime" disabled></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="detailDialogVisible = false">关闭</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const userStore = useUserStore()
const violationRecords = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

// 搜索表单
const searchForm = ref({
  type: '',
  createTime: null
})

// 过滤后的违规记录
const filteredViolationRecords = ref([])

// 分页后的违规记录
const paginatedViolationRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredViolationRecords.value.slice(start, end)
})

// 搜索违规记录
const searchViolationRecords = () => {
  // 执行筛选逻辑
  filteredViolationRecords.value = violationRecords.value.filter(record => {
    // 类型匹配
    const typeMatch = !searchForm.value.type || record.type === searchForm.value.type
    
    // 创建时间匹配
    const createTimeMatch = !searchForm.value.createTime || (() => {
      const recordDate = new Date(record.createTime).toDateString()
      const searchDate = searchForm.value.createTime.toDateString()
      return recordDate === searchDate
    })()
    
    return typeMatch && createTimeMatch
  })
  console.log('搜索违规记录:', searchForm.value)
  currentPage.value = 1 // 重置到第一页
}

// 重置搜索
const resetSearch = () => {
  searchForm.value.type = ''
  searchForm.value.createTime = null
  filteredViolationRecords.value = [...violationRecords.value]
  console.log('重置搜索')
  currentPage.value = 1 // 重置到第一页
}

// 分页方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

// 查看详细相关
const detailDialogVisible = ref(false)
const detailForm = ref({
  id: '',
  type: '',
  reason: '',
  scoreChange: '',
  banDays: '',
  createTime: '',
  banEndTime: ''
})

// 查看详细
const viewDetail = (record) => {
  detailForm.value.id = record.id
  detailForm.value.type = record.type === 'VIOLATION' ? '信用分扣除' : '禁止预约'
  detailForm.value.reason = record.reason || ''
  detailForm.value.scoreChange = record.scoreChange ? (record.scoreChange > 0 ? '+' : '') + record.scoreChange : '-'
  detailForm.value.banDays = record.banDays ? record.banDays + '天' : '-'
  detailForm.value.createTime = formatDate(record.createTime)
  detailForm.value.banEndTime = record.banEndTime ? formatDate(record.banEndTime) : '-'
  detailDialogVisible.value = true
}



const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

const loadViolationRecords = async () => {
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  try {
    // 调用后端API获取用户的违规记录
    const response = await axios.get(`http://localhost:8080/api/user/violation-records/${user.value.id}`)
    violationRecords.value = response.data
  } catch (error) {
    console.error('加载违规记录失败', error)
    ElMessage.error('加载违规记录失败，请稍后重试')
  } finally {
    // 初始化过滤后的违规记录
    filteredViolationRecords.value = [...violationRecords.value]
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadViolationRecords()
})
</script>

<style scoped>
.violation-records-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.search-form {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
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
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.content h1 {
  color: #333;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>