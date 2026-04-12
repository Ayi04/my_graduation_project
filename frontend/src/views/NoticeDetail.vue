<template>
  <div class="notice-detail-container">
    <!-- 导航栏 -->
    <Navbar />

    <div class="content">
      <h1>公告详情</h1>
      
      <el-card v-if="notice" class="notice-detail-card">
        <template #header>
          <div class="card-header">
            <h2>{{ notice.title }}</h2>
            <div class="notice-meta">
              <span class="publish-time">{{ formatDate(notice.publishTime) }}</span>
              <span class="publisher">发布者：{{ notice.publisher }}</span>
            </div>
          </div>
        </template>
        <div class="notice-content">
          {{ notice.content }}
        </div>
      </el-card>
      
      <el-empty v-else description="公告不存在" />
      
      <el-button type="primary" @click="goBack" style="margin-top: 20px;">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import Navbar from '../components/Navbar.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const notice = ref(null)

const loadNotice = async () => {
  try {
    const id = route.params.id
    const response = await axios.get(`http://localhost:8080/api/notice/${id}`)
    notice.value = response.data
  } catch (error) {
    console.error('加载公告详情失败', error)
    ElMessage.error('加载公告详情失败')
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const goBack = () => {
  router.push('/home')
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadNotice()
})
</script>

<style scoped>
.notice-detail-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.content h1 {
  color: #333;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.notice-detail-card {
  margin-bottom: 20px;
}

.card-header {
  padding: 20px;
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 20px;
  font-weight: bold;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #999;
}

.notice-content {
  padding: 20px;
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
}
</style>