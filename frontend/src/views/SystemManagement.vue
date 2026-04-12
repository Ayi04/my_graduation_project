<template>
  <div class="system-container">
    <Navbar />

    <div class="content">
      <h1>系统管理</h1>
    
      <!-- 系统设置 -->
      <div class="system-settings">
        <h2>系统基本设置</h2>
        <el-form :model="systemSettings" label-width="120px">
          <el-form-item label="签到时间限制">
            <el-input-number v-model="systemSettings.checkinTimeLimit" :min="1" :max="60" placeholder="请输入签到时间限制（分钟）"></el-input-number>
          </el-form-item>
          
          <el-form-item label="图书馆开放时间">
            <el-input v-model="systemSettings.openTime" placeholder="请输入图书馆开放时间，格式：09:00-22:00"></el-input>
          </el-form-item>
          
          <el-form-item label="关闭图书馆">
            <el-switch v-model="systemSettings.isLibraryClosed" @change="handleLibraryCloseChange"></el-switch>
          </el-form-item>
          
          <el-form-item label="关闭开始时间" v-if="systemSettings.isLibraryClosed">
            <el-date-picker
              v-model="systemSettings.closeStartTime"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              style="width: 100%"
            ></el-date-picker>
          </el-form-item>
          
          <el-form-item label="关闭结束时间" v-if="systemSettings.isLibraryClosed">
            <el-date-picker
              v-model="systemSettings.closeEndTime"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              style="width: 100%"
            ></el-date-picker>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveSettings">保存设置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 轮播图设置 -->
      <div class="carousel-settings">
        <h2>轮播图设置</h2>
        
        <!-- 上传图片 -->
        <el-upload
          class="upload-demo"
          action="http://localhost:8080/api/system/carousel/upload"
          :on-success="handleImageUpload"
          :auto-upload="true"
          :show-file-list="false"
        >
          <el-button type="primary">上传轮播图</el-button>
        </el-upload>
        
        <!-- 轮播图列表 -->
        <el-table :data="carousels" style="margin-top: 20px;">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column label="图片" width="150">
            <template #default="scope">
              <el-image :src="scope.row.imageUrl" fit="cover" style="width: 100px; height: 60px;"></el-image>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题"></el-table-column>
          <el-table-column prop="orderIndex" label="排序" width="80"></el-table-column>
          <el-table-column prop="isActive" label="状态" width="80">
            <template #default="scope">
              <el-switch v-model="scope.row.isActive" @change="updateCarouselStatus(scope.row)"></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" @click="editCarousel(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteCarousel(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 轮播图编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        title="编辑轮播图"
        width="500px"
      >
        <el-form :model="currentCarousel" label-width="100px">
          <el-form-item label="图片URL">
            <el-input v-model="currentCarousel.imageUrl" placeholder="请输入图片URL"></el-input>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="currentCarousel.title" placeholder="请输入轮播图标题"></el-input>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="currentCarousel.orderIndex" :min="1" placeholder="请输入排序号"></el-input-number>
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="currentCarousel.isActive"></el-switch>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" @click="saveCarousel">保存</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const userStore = useUserStore()

const systemSettings = ref({
  checkinTimeLimit: 15,
  openTime: '09:00-22:00',
  isLibraryClosed: false,
  closeStartTime: '',
  closeEndTime: ''
})

// 轮播图相关数据
const carousels = ref([])
const dialogVisible = ref(false)
const currentCarousel = ref({
  id: null,
  imageUrl: '',
  title: '',
  orderIndex: 1,
  isActive: true
})
const tempImageUrl = ref('') // 存储临时上传的图片URL

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

const loadSettings = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/system/settings')
    console.log('加载设置成功', response.data)
    systemSettings.value = response.data
  } catch (error) {
    console.error('加载设置失败', error)
  }
}

const handleLibraryCloseChange = async (value) => {
  if (!value) {
    // 关闭图书馆开关关闭时，清空时间选择器
    systemSettings.value.closeStartTime = ''
    systemSettings.value.closeEndTime = ''
    // 自动保存设置
    await saveSettings()
  }
}

const saveSettings = async () => {
  try {
    // 验证关闭时间是否有效
    if (systemSettings.value.isLibraryClosed) {
      if (!systemSettings.value.closeStartTime || !systemSettings.value.closeEndTime) {
        ElMessageBox.alert('请选择关闭时间', '提示', {
          confirmButtonText: '确定'
        })
        return
      }
      if (new Date(systemSettings.value.closeStartTime) >= new Date(systemSettings.value.closeEndTime)) {
        ElMessageBox.alert('开始时间必须早于结束时间', '提示', {
          confirmButtonText: '确定'
        })
        return
      }
    }
    
    const response = await axios.put('http://localhost:8080/api/system/settings', systemSettings.value)
    if (response.data.success) {
      // 保存成功后重新加载设置
      await loadSettings()
      ElMessageBox.alert('设置保存成功', '提示', {
        confirmButtonText: '确定'
      })
    } else {
      ElMessageBox.alert('设置保存失败: ' + response.data.message, '提示', {
        confirmButtonText: '确定'
      })
    }
  } catch (error) {
    console.error('保存设置失败', error)
    ElMessageBox.alert('设置保存失败，请稍后重试', '提示', {
      confirmButtonText: '确定'
    })
  }
}

// 加载轮播图列表
const loadCarousels = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/system/carousel')
    carousels.value = response.data
  } catch (error) {
    console.error('加载轮播图失败', error)
  }
}

// 处理图片上传成功
const handleImageUpload = async (response) => {
  if (response.success) {
    // 上传成功后打开编辑对话框
    tempImageUrl.value = response.imageUrl // 存储临时图片URL
    currentCarousel.value = {
      id: null,
      imageUrl: response.imageUrl,
      title: '',
      orderIndex: carousels.value.length + 1,
      isActive: true
    }
    dialogVisible.value = true
  } else {
    ElMessageBox.alert('图片上传失败: ' + response.message, '提示', {
      confirmButtonText: '确定'
    })
  }
}

// 编辑轮播图
const editCarousel = (carousel) => {
  tempImageUrl.value = '' // 编辑现有轮播图时，不需要存储临时图片URL
  currentCarousel.value = { ...carousel }
  dialogVisible.value = true
}

// 取消编辑
const cancelEdit = () => {
  // 重置临时图片URL和当前轮播图
  tempImageUrl.value = ''
  currentCarousel.value = {
    id: null,
    imageUrl: '',
    title: '',
    orderIndex: 1,
    isActive: true
  }
  dialogVisible.value = false
}

// 保存轮播图
const saveCarousel = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/system/carousel', currentCarousel.value)
    if (response.data.success) {
      ElMessageBox.alert('轮播图保存成功', '提示', {
        confirmButtonText: '确定'
      })
      tempImageUrl.value = '' // 保存成功后清空临时图片URL
      currentCarousel.value = {
        id: null,
        imageUrl: '',
        title: '',
        orderIndex: 1,
        isActive: true
      }
      dialogVisible.value = false
      loadCarousels()
    } else {
      ElMessageBox.alert('轮播图保存失败: ' + response.data.message, '提示', {
        confirmButtonText: '确定'
      })
    }
  } catch (error) {
    console.error('保存轮播图失败', error)
    ElMessageBox.alert('轮播图保存失败，请稍后重试', '提示', {
      confirmButtonText: '确定'
    })
  }
}

// 更新轮播图状态
const updateCarouselStatus = async (carousel) => {
  try {
    const response = await axios.post('http://localhost:8080/api/system/carousel', carousel)
    if (!response.data.success) {
      ElMessageBox.alert('状态更新失败: ' + response.data.message, '提示', {
        confirmButtonText: '确定'
      })
      // 恢复原状态
      carousel.isActive = !carousel.isActive
    }
  } catch (error) {
    console.error('更新轮播图状态失败', error)
    // 恢复原状态
    carousel.isActive = !carousel.isActive
    ElMessageBox.alert('状态更新失败，请稍后重试', '提示', {
      confirmButtonText: '确定'
    })
  }
}

// 删除轮播图
const deleteCarousel = async (id) => {
  try {
    // 显示确认对话框
    await ElMessageBox.confirm('确定要删除这个轮播图吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 用户确认后执行删除操作
    const response = await axios.delete(`http://localhost:8080/api/system/carousel/${id}`)
    if (response.data.success) {
      ElMessageBox.alert('轮播图删除成功', '提示', {
        confirmButtonText: '确定'
      })
      loadCarousels()
    } else {
      ElMessageBox.alert('轮播图删除失败: ' + response.data.message, '提示', {
        confirmButtonText: '确定'
      })
    }
  } catch (error) {
    // 如果用户取消删除，不显示错误信息
    if (error !== 'cancel') {
      console.error('删除轮播图失败', error)
      ElMessageBox.alert('轮播图删除失败，请稍后重试', '提示', {
        confirmButtonText: '确定'
      })
    }
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadSettings()
  loadCarousels()
})
</script>

<style scoped>
.system-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 30px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.system-settings {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.system-settings h2 {
  color: #333;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.carousel-settings {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.carousel-settings h2 {
  color: #333;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.upload-demo {
  margin-bottom: 20px;
}

.upload-demo .el-button {
  transition: all 0.3s ease;
}

.upload-demo .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.el-table {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.el-table th {
  background-color: #f7f9fc !important;
  font-weight: 600;
  color: #333;
}

.el-table tr {
  transition: all 0.2s ease;
}

.el-table tr:hover {
  background-color: #f5f7fa !important;
}

.el-image {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.el-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.el-button {
  transition: all 0.3s ease;
}

.el-button:hover {
  transform: translateY(-1px);
}

.el-button--danger:hover {
  box-shadow: 0 4px 12px rgba(245, 34, 45, 0.3);
}

.el-dialog {
  border-radius: 10px;
  overflow: hidden;
}

.el-dialog__header {
  background-color: #f7f9fc;
  border-bottom: 1px solid #eaeaea;
}

.el-dialog__title {
  font-weight: 600;
  color: #333;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-form-item__label {
  font-weight: 500;
  color: #333;
}

.el-input, .el-input-number {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.el-input:focus-within, .el-input-number:focus-within {
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.dialog-footer {
  text-align: right;
  padding-top: 15px;
  border-top: 1px solid #eaeaea;
}

.dialog-footer .el-button {
  margin-left: 10px;
}

</style>