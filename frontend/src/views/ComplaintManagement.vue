<template>
  <div class="complaint-management-container">
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 内容区域 -->
    <div class="content">
      <h1>投诉管理</h1>
      
      <!-- 搜索和筛选 -->
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form" style="display: flex; align-items: center; flex-wrap: nowrap;">
          <el-form-item label="用户名" style="margin-right: 10px;">
            <el-input v-model="searchForm.userName" placeholder="请输入用户名" style="width: 150px;"></el-input>
          </el-form-item>
          <el-form-item label="标题" style="margin-right: 10px;">
            <el-input v-model="searchForm.title" placeholder="请输入标题" style="width: 150px;"></el-input>
          </el-form-item>
          <el-form-item label="提交时间" style="margin-right: 10px;">
            <el-date-picker
              v-model="searchForm.submitTime"
              type="date"
              placeholder="选择日期"
              style="width: 180px"
            ></el-date-picker>
          </el-form-item>
          <el-form-item style="margin-right: 10px;">
            <el-button type="primary" @click="searchComplaints">查询</el-button>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 投诉列表 -->
      <el-card class="complaints-card">
        <template #header>
          <div class="card-header">
            <h3>投诉列表</h3>
          </div>
        </template>
        <el-table :data="paginatedComplaints" style="width: 100%">
          <el-table-column prop="id" label="投诉ID" width="80" />
          <el-table-column prop="userName" label="用户名" width="100" />
          <el-table-column label="投诉标题" min-width="200">
            <template #default="scope">
              <a @click="viewComplaintDetail(scope.row)">{{ scope.row.title }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'PENDING' ? 'warning' : 'success'">
                {{ scope.row.status === 'PENDING' ? '待处理' : '已处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="160" />
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewComplaintDetail(scope.row)">查看详细</el-button>
              <el-button type="primary" size="small" @click="handleComplaint(scope.row)" style="margin-left: 5px">
                {{ scope.row.status === 'PENDING' ? '处理' : '查看' }}
              </el-button>
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
            :total="filteredComplaints.length"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
  <!-- 查看详细对话框 -->
  <el-dialog
    v-model="detailDialogVisible"
    title="投诉详情"
    width="600px"
  >
    <el-form :model="detailForm" label-width="100px">
      <el-form-item label="标题">
        <el-input v-model="detailForm.title" disabled></el-input>
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="detailForm.content" type="textarea" :rows="4" disabled></el-input>
      </el-form-item>
      <el-form-item label="用户姓名">
        <el-input v-model="detailForm.userName" disabled></el-input>
      </el-form-item>
      <el-form-item label="提交时间">
        <el-input v-model="detailForm.submitTime" disabled></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import Navbar from '../components/Navbar.vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const complaints = ref([])

// 搜索表单
const searchForm = reactive({
  userName: '',
  title: '',
  submitTime: null
})

// 查看详细相关
const detailDialogVisible = ref(false)
const detailForm = reactive({
  id: '',
  title: '',
  content: '',
  userName: '',
  submitTime: ''
})

// 过滤后的投诉列表
const filteredComplaints = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 分页后的投诉列表
const paginatedComplaints = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredComplaints.value.slice(start, end)
})

// 搜索投诉
const searchComplaints = () => {
  // 执行筛选逻辑
  filteredComplaints.value = complaints.value.filter(item => {
    // 用户名匹配
    const userNameMatch = !searchForm.userName || item.userName.toLowerCase().includes(searchForm.userName.toLowerCase())
    
    // 标题匹配
    const titleMatch = !searchForm.title || item.title.toLowerCase().includes(searchForm.title.toLowerCase())
    
    // 提交时间匹配
    const submitTimeMatch = !searchForm.submitTime || (() => {
      const itemDate = new Date(item.submitTime).toDateString()
      const searchDate = searchForm.submitTime.toDateString()
      return itemDate === searchDate
    })()
    
    return userNameMatch && titleMatch && submitTimeMatch
  })
  // 搜索后重置到第一页
  currentPage.value = 1
  console.log('搜索投诉:', searchForm)
}

// 重置搜索
const resetSearch = () => {
  searchForm.userName = ''
  searchForm.title = ''
  searchForm.submitTime = null
  // 重置后显示所有投诉记录
  filteredComplaints.value = [...complaints.value]
  // 重置到第一页
  currentPage.value = 1
  console.log('重置搜索')
}

// 分页方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

// 加载投诉列表
const loadComplaints = async () => {
  try {
    // 调用后端API获取提交给工作人员的反馈（投诉）
    const response = await axios.get('http://localhost:8080/api/feedback/target/STAFF')
    // 转换数据格式
    complaints.value = response.data.map(item => ({
      id: item.id,
      userName: item.login?.username || '未知用户',
      title: item.title || '无标题',
      content: item.content,
      status: item.isProcessed ? 'PROCESSED' : 'PENDING',
      submitTime: new Date(item.submitTime).toLocaleString()
    }))
  } catch (error) {
    console.error('加载投诉列表失败', error)
    // 如果API调用失败，使用模拟数据
    complaints.value = [
      {
        id: 1,
        userName: '张三',
        title: '座位预约系统故障',
        content: '预约座位时系统提示错误，无法完成预约',
        status: 'PENDING',
        submitTime: '2026-03-20 10:30:00'
      },
      {
        id: 2,
        userName: '李四',
        title: '图书馆环境问题',
        content: '静音区有人大声说话，影响学习',
        status: 'PENDING',
        submitTime: '2026-03-21 14:20:00'
      },
      {
        id: 3,
        userName: '王五',
        title: '网络连接不稳定',
        content: '图书馆WiFi经常断开，影响学习效率',
        status: 'PROCESSED',
        submitTime: '2026-03-19 09:15:00'
      }
    ]
  } finally {
    // 初始化过滤后的投诉列表
    filteredComplaints.value = [...complaints.value]
  }
}

// 处理投诉
const handleComplaint = (complaint) => {
  // 这里可以打开一个对话框来处理投诉
  console.log('处理投诉:', complaint)
  ElMessage.info('处理投诉功能开发中')
}

// 查看详细
const viewComplaintDetail = (complaint) => {
  // 打开查看详细对话框
  detailForm.id = complaint.id
  detailForm.title = complaint.title
  detailForm.content = complaint.content
  detailForm.userName = complaint.userName
  detailForm.submitTime = complaint.submitTime
  detailDialogVisible.value = true
}

onMounted(() => {
  loadComplaints()
})
</script>

<style scoped>
.complaint-management-container {
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
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.search-card {
  margin-bottom: 20px;
  transition: all 0.3s ease;
  padding: 5px 10px;
}

.search-card :deep(.el-form) {
  margin-bottom: 0;
}

.search-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.search-form {
  margin-bottom: 0;
}

.complaints-card {
  transition: all 0.3s ease;
}

.complaints-card:hover {
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