<template>
  <div class="notice-management-container">
    <!-- 导航栏 -->
    <Navbar />

    <div class="content">
      <h1>公告管理</h1>
      
      <el-button type="primary" @click="dialogVisible = true" style="margin-bottom: 20px;">添加公告</el-button>
      
      <el-table :data="notices" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布者" width="120"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editNotice(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteNotice(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px;"
      />
    </div>
    
    <!-- 公告编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="公告管理"
      width="50%"
    >
      <el-form :model="noticeForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题"></el-input>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="noticeForm.content" type="textarea" rows="4" placeholder="请输入公告内容"></el-input>
        </el-form-item>
        <el-form-item label="发布者">
          <el-input v-model="noticeForm.publisher" placeholder="请输入发布者"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNotice" :disabled="!noticeForm.title || !noticeForm.content || !noticeForm.publisher">保存</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const userStore = useUserStore()
const dialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const notices = ref([])
const noticeForm = ref({
  id: null,
  title: '',
  content: '',
  publisher: ''
})

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)



const loadNotices = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/notice')
    notices.value = response.data
    total.value = response.data.length
  } catch (error) {
    console.error('加载公告失败', error)
    // 使用模拟数据
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
    total.value = notices.value.length
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const saveNotice = async () => {
  try {
    if (noticeForm.value.id) {
      // 编辑公告
      await axios.put(`http://localhost:8080/api/notice/${noticeForm.value.id}`, noticeForm.value)
      ElMessage.success('编辑成功')
    } else {
      // 添加公告
      await axios.post('http://localhost:8080/api/notice', {
        ...noticeForm.value,
        publishTime: new Date()
      })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadNotices()
  } catch (error) {
    console.error('保存公告失败', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

const editNotice = (notice) => {
  noticeForm.value = { ...notice }
  dialogVisible.value = true
}

const deleteNotice = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此公告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`http://localhost:8080/api/notice/${id}`)
    ElMessage.success('删除成功')
    loadNotices()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadNotices()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadNotices()
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadNotices()
})
</script>

<style scoped>
.notice-management-container {
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

.dialog-footer {
  text-align: right;
}
</style>