<template>
  <div class="rule-management-container">
    <!-- 导航栏 -->
    <Navbar />

    <div class="content">
      <h1>规则管理</h1>
      
      <el-button type="primary" @click="addRule" style="margin-bottom: 20px;">添加规则</el-button>
      
      <el-table :data="rules" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="category" label="分类" width="150"></el-table-column>
        <el-table-column prop="content" label="规则内容"></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editRule(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteRule(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 规则编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="规则管理"
      width="600px"
    >
      <el-form :model="ruleForm" :rules="ruleRules" ref="ruleFormRef" label-width="100px">
        <el-form-item label="分类" prop="category">
          <el-select v-model="ruleForm.category" placeholder="请选择分类">
            <el-option label="黑名单规则" value="黑名单规则"></el-option>
            <el-option label="信用分机制" value="信用分机制"></el-option>
            <el-option label="预约规则" value="预约规则"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="规则内容" prop="content">
          <el-input v-model="ruleForm.content" type="textarea" :rows="4" placeholder="请输入规则内容，多行请用换行符分隔"></el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="ruleForm.sortOrder" :min="1" :max="100" placeholder="请输入排序序号"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveRule">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const userStore = useUserStore()

const dialogVisible = ref(false)
const ruleFormRef = ref(null)

const rules = ref([])
const ruleForm = ref({
  id: null,
  category: '',
  content: '',
  sortOrder: 1
})

const ruleRules = {
  category: [{ required: true, message: '请选择分类', trigger: 'blur' }],
  content: [{ required: true, message: '请输入规则内容', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序序号', trigger: 'blur' }]
}

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)


const loadRules = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/rule')
    rules.value = response.data
  } catch (error) {
    console.error('加载规则失败', error)
    ElMessage.error('加载规则失败，请稍后重试')
  }
}

const saveRule = async () => {
  if (!ruleFormRef.value) return
  await ruleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (ruleForm.value.id) {
          // 编辑规则
          response = await axios.put(`http://localhost:8080/api/rule/${ruleForm.value.id}`, ruleForm.value)
        } else {
          // 添加规则
          response = await axios.post('http://localhost:8080/api/rule', ruleForm.value)
        }
        
        if (response.data) {
          ElMessage.success('保存成功')
          dialogVisible.value = false
          // 重新加载规则列表
          loadRules()
        } else {
          ElMessage.error('保存失败')
        }
      } catch (error) {
        console.error('保存规则失败', error)
        ElMessage.error('保存失败，请稍后重试')
      }
    }
  })
}

const addRule = () => {
  ruleForm.value = {
    id: null,
    category: '',
    content: '',
    sortOrder: 1
  }
  dialogVisible.value = true
}

const editRule = (rule) => {
  ruleForm.value = { ...rule }
  dialogVisible.value = true
}

const deleteRule = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`http://localhost:8080/api/rule/${id}`)
    ElMessage.success('删除成功')
    // 重新加载规则列表
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除规则失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadRules()
})
</script>

<style scoped>
.rule-management-container {
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
}

.dialog-footer {
  text-align: right;
}
</style>
