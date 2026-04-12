<template>
  <div class="rule-container">
    <!-- 导航栏 -->
    <Navbar />

    <div class="content">
      <h1>图书馆规则</h1>
      <div class="rule-card">
        <el-alert
          title="仔细阅读，可以帮助你更好的了解使用规则"
          type="info"
          show-icon
        />
        
        <div v-for="category in ruleCategories" :key="category" class="rule-category">
          <h2>{{ category }}</h2>
          <div v-for="rule in getRulesByCategory(category)" :key="rule.id" class="rule-item">
            <p>{{ rule.content }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const userStore = useUserStore()
const rules = ref([])

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

const ruleCategories = ref([
  '黑名单规则',
  '信用分机制',
  '预约规则'
])

const getRulesByCategory = (category) => {
  return rules.value.filter(rule => rule.category === category)
}

const loadRules = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/rule')
    rules.value = response.data
  } catch (error) {
    console.error('加载规则失败', error)
    // 使用模拟数据
    rules.value = [
      // 黑名单规则
      {
        id: 1,
        content: '7天内产生3次违规记录将被拉黑3天',
        category: '黑名单规则',
        sortOrder: 1
      },
      // 信用分机制
      {
        id: 2,
        content: '信用分低于80不能预约且被禁止3天预约座位\n预约后取消不扣分\n预约后倒计时结束未签到扣5分\n未在约定时间内归还座位扣5分',
        category: '信用分机制',
        sortOrder: 1
      },
      // 预约规则
      {
        id: 3,
        content: '2小时内可暂离保留2次',
        category: '预约规则',
        sortOrder: 1
      },
      {
        id: 4,
        content: '退座1分钟后可再次选座',
        category: '预约规则',
        sortOrder: 2
      },
      {
        id: 5,
        content: '闭馆前20分钟发送闭馆提醒\n闭馆前0分钟将停止预约',
        category: '预约规则',
        sortOrder: 3
      },
      {
        id: 6,
        content: '每个用户可监督占座2次/天，每次间隔30分钟\n被监督占座后15分钟内回签有效',
        category: '预约规则',
        sortOrder: 4
      },
      {
        id: 7,
        content: '学习0分钟后即视为一次有效打卡\n10天内累计3次未打卡将进入未打卡名单',
        category: '预约规则',
        sortOrder: 5
      },
      {
        id: 8,
        content: '淡季期间，预约规则可能会有所调整',
        category: '预约规则',
        sortOrder: 6
      }
    ]
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadRules()
})
</script>

<style scoped>
.rule-container {
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
  margin-bottom: 30px;
  text-align: center;
}

.rule-card {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.rule-category {
  margin-top: 30px;
}

.rule-category h2 {
  color: #1890ff;
  margin-bottom: 15px;
  font-size: 18px;
  border-left: 4px solid #1890ff;
  padding-left: 10px;
}

.rule-item {
  margin-bottom: 20px;
  padding-left: 20px;
}

.rule-item h3 {
  color: #333;
  margin-bottom: 8px;
  font-size: 16px;
}

.rule-item p {
  color: #666;
  line-height: 1.5;
  white-space: pre-line;
}

.el-alert {
  margin-bottom: 30px;
}
</style>
