<template>
  <div class="feedback-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1 v-if="userRole === 'USER'">反馈与投诉</h1>
      <h1 v-else-if="userRole === 'STAFF' && activeIndex === 'feedback'">系统反馈</h1>
      <h1 v-else-if="userRole === 'STAFF' && activeIndex === 'complaint-management'">投诉管理</h1>
      <h1 v-else-if="userRole === 'ADMIN'">反馈管理</h1>
      
      <!-- 普通用户的反馈与投诉表单 -->
      <div v-if="userRole === 'USER'">
        <!-- 反馈记录 -->
        <el-card class="records-card">
          <template #header>
            <div class="card-header">
              <h3>反馈记录</h3>
              <el-button type="primary" @click="openFeedbackDialog">提交反馈</el-button>
            </div>
          </template>
          
          <!-- 查询表单 -->
          <el-form :model="feedbackSearchForm" class="search-form" inline>
            <el-form-item label="标题">
              <el-input v-model="feedbackSearchForm.title" placeholder="请输入标题" clearable></el-input>
            </el-form-item>
            <el-form-item label="提交日期">
              <el-date-picker
                v-model="feedbackSearchForm.submitDate"
                type="date"
                placeholder="请选择提交日期"
                value-format="YYYY-MM-DD"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchFeedbackRecords">查询</el-button>
              <el-button @click="resetFeedbackSearch">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="feedbackSearchResults" style="width: 100%">
            <el-table-column prop="id" label="ID" width="60"></el-table-column>
            <el-table-column label="标题" min-width="300">
              <template #default="scope">
                <a @click="viewFeedbackDetail(scope.row)">{{ scope.row.title }}</a>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="160">
              <template #default="scope">
                {{ formatDateTime(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="isProcessed" label="状态" width="80">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.isProcessed">已处理</el-tag>
                <el-tag type="warning" v-else>未处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="processor" label="处理人" width="100"></el-table-column>
            <el-table-column prop="processTime" label="处理时间" width="160">
              <template #default="scope">
                {{ scope.row.processTime ? formatDateTime(scope.row.processTime) : '暂无' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewFeedbackDetail(scope.row)">查看详细</el-button>
                <el-button type="danger" size="small" @click="withdrawFeedback(scope.row.id)" v-if="!scope.row.isProcessed" style="margin-left: 5px">撤回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 投诉记录 -->
        <el-card class="records-card">
          <template #header>
            <div class="card-header">
              <h3>投诉记录</h3>
              <el-button type="primary" @click="openComplaintDialog">提交投诉</el-button>
            </div>
          </template>

          <!-- 查询表单 -->
          <el-form :model="complaintSearchForm" class="search-form" inline>
            <el-form-item label="标题">
              <el-input v-model="complaintSearchForm.title" placeholder="请输入标题" clearable></el-input>
            </el-form-item>
            <el-form-item label="提交日期">
              <el-date-picker
                v-model="complaintSearchForm.submitDate"
                type="date"
                placeholder="请选择提交日期"
                value-format="YYYY-MM-DD"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchComplaintRecords">查询</el-button>
              <el-button @click="resetComplaintSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="complaintSearchResults" style="width: 100%">
            <el-table-column prop="id" label="ID" width="60"></el-table-column>
            <el-table-column label="标题" min-width="300">
              <template #default="scope">
                <a @click="viewFeedbackDetail(scope.row)">{{ scope.row.title }}</a>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="160">
              <template #default="scope">
                {{ formatDateTime(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="isProcessed" label="状态" width="80">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.isProcessed">已处理</el-tag>
                <el-tag type="warning" v-else>未处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="processor" label="处理人" width="100"></el-table-column>
            <el-table-column prop="processTime" label="处理时间" width="160">
              <template #default="scope">
                {{ scope.row.processTime ? formatDateTime(scope.row.processTime) : '暂无' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewFeedbackDetail(scope.row)">查看详细</el-button>
                <el-button type="danger" size="small" @click="withdrawFeedback(scope.row.id)" v-if="!scope.row.isProcessed" style="margin-left: 5px">撤回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 工作人员的系统反馈表单 -->
      <div v-else-if="userRole === 'STAFF' && activeIndex === 'feedback'">
        <!-- 提交系统反馈表单 -->
        <el-card class="feedback-card">
          <template #header>
            <div class="card-header">
              <h3>提交系统反馈</h3>
            </div>
          </template>
          <el-form :model="feedbackForm" :rules="feedbackRules" ref="feedbackFormRef" label-width="100px">
            <el-form-item label="反馈标题" prop="title">
              <el-input v-model="feedbackForm.title" placeholder="请输入反馈标题"></el-input>
            </el-form-item>
            <el-form-item label="反馈内容" prop="content">
              <el-input v-model="feedbackForm.content" type="textarea" :rows="4" placeholder="请输入系统反馈内容"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitFeedback('feedback')">提交反馈</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 系统反馈记录 -->
        <el-card class="records-card">
          <template #header>
            <div class="card-header">
              <h3>系统反馈记录</h3>
            </div>
          </template>
          <el-table :data="feedbackRecords.filter(record => record.type === 'feedback')" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column label="标题" min-width="300">
              <template #default="scope">
                <a @click="viewFeedbackDetail(scope.row)">{{ scope.row.title }}</a>
              </template>
            </el-table-column>
            <el-table-column prop="submitter" label="提交人" width="120"></el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="isProcessed" label="处理状态" width="100">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.isProcessed">已处理</el-tag>
                <el-tag type="warning" v-else>未处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="processor" label="处理人" width="120"></el-table-column>
            <el-table-column prop="processTime" label="处理时间" width="180">
              <template #default="scope">
                {{ scope.row.processTime ? formatDateTime(scope.row.processTime) : '暂无' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewFeedbackDetail(scope.row)">查看详细</el-button>
                <el-button type="danger" size="small" @click="withdrawFeedback(scope.row.id)" v-if="!scope.row.isProcessed" style="margin-left: 5px">撤回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 工作人员的投诉管理 -->
      <div v-else-if="userRole === 'STAFF' && activeIndex === 'complaint-management'">
        <!-- 查询表单 -->
        <div class="search-form">
          <el-form :model="searchForm" :inline="true">
            <el-form-item label="提交人">
              <el-input v-model="searchForm.submitter" placeholder="请输入提交人姓名" style="width: 180px"></el-input>
            </el-form-item>
            <el-form-item label="处理状态">
              <el-select v-model="searchForm.isProcessed" placeholder="请选择处理状态" style="width: 150px">
                <el-option label="全部" value=""></el-option>
                <el-option label="已处理" :value="true"></el-option>
                <el-option label="未处理" :value="false"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="处理人">
              <el-input v-model="searchForm.processor" placeholder="请输入处理人姓名" style="width: 180px"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchFeedback" style="margin-right: 10px">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <el-card class="records-card">
          <template #header>
            <div class="card-header">
              <h3>投诉管理</h3>
            </div>
          </template>
          
          <el-table :data="feedbackRecords" style="width: 100%">
            <el-table-column prop="id" label="ID" min-width="80"></el-table-column>
            <el-table-column label="标题" min-width="300">
              <template #default="scope">
                <a @click="viewFeedbackDetail(scope.row)">{{ scope.row.title }}</a>
              </template>
            </el-table-column>
            <el-table-column prop="submitter" label="提交人" min-width="120"></el-table-column>
            <el-table-column prop="submitTime" label="提交时间" min-width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="isProcessed" label="处理状态" min-width="100">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.isProcessed">已处理</el-tag>
                <el-tag type="warning" v-else>未处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="processor" label="处理人" min-width="120"></el-table-column>
            <el-table-column prop="processTime" label="处理时间" min-width="180">
              <template #default="scope">
                {{ scope.row.processTime ? formatDateTime(scope.row.processTime) : '暂无' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="300">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewFeedbackDetail(scope.row)">查看详细</el-button>
                <el-button type="primary" size="small" @click="processFeedback(scope.row)" v-if="!scope.row.isProcessed" style="margin-left: 10px">处理</el-button>
                <el-button type="danger" size="small" @click="deleteFeedback(scope.row.id)" style="margin-left: 10px">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 管理员的反馈管理 -->
      <div v-else>
        <!-- 查询表单 -->
        <div class="search-form">
          <el-form :model="adminSearchForm" :inline="true" style="display: flex; align-items: center; flex-wrap: nowrap;">
            <el-form-item label="标题" style="margin-right: 10px;">
              <el-input v-model="adminSearchForm.title" placeholder="请输入标题" style="width: 150px;"></el-input>
            </el-form-item>
            <el-form-item label="提交人" style="margin-right: 10px;">
              <el-input v-model="adminSearchForm.submitter" placeholder="请输入提交人" style="width: 150px;"></el-input>
            </el-form-item>
            <el-form-item label="提交日期" style="margin-right: 10px;">
              <el-date-picker
                v-model="adminSearchForm.submitDate"
                type="date"
                placeholder="请选择提交日期"
                value-format="YYYY-MM-DD"
                style="width: 180px"
              ></el-date-picker>
            </el-form-item>
            <el-form-item style="margin-right: 10px;">
              <el-button type="primary" @click="searchAdminFeedback">查询</el-button>
            </el-form-item>
            <el-form-item>
              <el-button @click="resetAdminSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <el-card class="records-card">
          <template #header>
            <div class="card-header">
              <h3 v-if="userRole === 'ADMIN'">反馈记录</h3>
              <h3 v-else-if="userRole === 'STAFF'">投诉管理</h3>
            </div>
          </template>
          
          <el-table :data="paginatedAdminSearchResults" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>

            <el-table-column label="标题" min-width="300">
              <template #default="scope">
                <a @click="viewFeedbackDetail(scope.row)">{{ scope.row.title }}</a>
              </template>
            </el-table-column>
            <el-table-column prop="submitter" label="提交人" width="120"></el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="isProcessed" label="处理状态" width="100">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.isProcessed">已处理</el-tag>
                <el-tag type="warning" v-else>未处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="processor" label="处理人" width="120"></el-table-column>
            <el-table-column prop="processTime" label="处理时间" width="180">
              <template #default="scope">
                {{ scope.row.processTime ? formatDateTime(scope.row.processTime) : '暂无' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewFeedbackDetail(scope.row)">查看详细</el-button>
                <template v-if="userRole === 'ADMIN'">
                  <el-button type="primary" size="small" @click="processFeedback(scope.row)" v-if="!scope.row.isProcessed" style="margin-left: 10px">处理</el-button>
                  <el-button type="danger" size="small" @click="deleteFeedback(scope.row.id)" style="margin-left: 10px">删除</el-button>
                </template>
                <el-button type="primary" size="small" @click="processFeedback(scope.row)" v-else-if="userRole === 'STAFF' && !scope.row.isProcessed" style="margin-left: 10px">处理</el-button>
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
              :total="adminSearchResults.length"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </div>
    </div>
  </div>
  
  <!-- 处理反馈对话框 -->
  <el-dialog
    v-model="processDialogVisible"
    title="处理反馈"
    width="500px"
  >
    <el-form :model="processForm" :rules="processRules" ref="processFormRef" label-width="100px">
      <el-form-item label="反馈内容">
        <el-input v-model="processForm.content" type="textarea" :rows="4" disabled></el-input>
      </el-form-item>
      <el-form-item label="处理结果" prop="processResult">
        <el-input v-model="processForm.processResult" type="textarea" :rows="4" placeholder="请输入处理结果"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProcessResult">保存处理结果</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 查看详细对话框 -->
  <el-dialog
    v-model="detailDialogVisible"
    title="反馈详情"
    width="600px"
  >
    <el-form :model="detailForm" label-width="100px">
      <el-form-item label="标题">
        <el-input v-model="detailForm.title" disabled></el-input>
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="detailForm.content" type="textarea" :rows="4" disabled></el-input>
      </el-form-item>
      <el-form-item label="提交人">
        <el-input v-model="detailForm.submitter" disabled></el-input>
      </el-form-item>
      <el-form-item label="提交时间">
        <el-input v-model="detailForm.submitTime" disabled></el-input>
      </el-form-item>
      <el-form-item label="处理人">
        <el-input v-model="detailForm.processor" disabled></el-input>
      </el-form-item>
      <el-form-item label="处理时间">
        <el-input v-model="detailForm.processTime" disabled></el-input>
      </el-form-item>
      <el-form-item label="处理结果">
        <el-input v-model="detailForm.processResult" type="textarea" :rows="3" disabled></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 提交反馈对话框 -->
  <el-dialog
    v-model="feedbackDialogVisible"
    title="提交反馈"
    width="500px"
  >
    <el-form :model="feedbackForm" :rules="feedbackRules" ref="feedbackFormRef" label-width="100px">
      <el-form-item label="反馈标题" prop="title">
        <el-input v-model="feedbackForm.title" placeholder="请输入反馈标题"></el-input>
      </el-form-item>
      <el-form-item label="反馈内容" prop="content">
        <el-input v-model="feedbackForm.content" type="textarea" :rows="4" placeholder="请输入反馈内容"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedbackFromDialog">提交</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 提交投诉对话框 -->
  <el-dialog
    v-model="complaintDialogVisible"
    title="提交投诉"
    width="500px"
  >
    <el-form :model="complaintForm" :rules="complaintRules" ref="complaintFormRef" label-width="100px">
      <el-form-item label="投诉标题" prop="title">
        <el-input v-model="complaintForm.title" placeholder="请输入投诉标题"></el-input>
      </el-form-item>
      <el-form-item label="投诉内容" prop="content">
        <el-input v-model="complaintForm.content" type="textarea" :rows="4" placeholder="请输入投诉内容"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="complaintDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplaintFromDialog">提交</el-button>
      </span>
    </template>
  </el-dialog>
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
const feedbackFormRef = ref(null)
const complaintFormRef = ref(null)

const feedbackForm = reactive({
  title: '',
  content: ''
})

const complaintForm = reactive({
  title: '',
  content: ''
})

const feedbackRules = {
  title: [{ required: true, message: '请输入反馈标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
}

const complaintRules = {
  title: [{ required: true, message: '请输入投诉标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入投诉内容', trigger: 'blur' }]
}

const feedbackRecords = ref([])

// 反馈记录查询表单
const feedbackSearchForm = reactive({
  title: '',
  submitDate: null
})

// 反馈记录查询结果
const feedbackSearchResults = ref([])

// 投诉记录查询表单
const complaintSearchForm = reactive({
  title: '',
  submitDate: null
})

// 投诉记录查询结果
const complaintSearchResults = ref([])

// 管理员搜索表单
const adminSearchForm = reactive({
  title: '',
  submitter: '',
  submitDate: null
})

// 管理员搜索结果
const adminSearchResults = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 提交反馈对话框
const feedbackDialogVisible = ref(false)

// 提交投诉对话框
const complaintDialogVisible = ref(false)

// 打开提交反馈对话框
const openFeedbackDialog = () => {
  feedbackForm.title = ''
  feedbackForm.content = ''
  feedbackDialogVisible.value = true
}

// 打开提交投诉对话框
const openComplaintDialog = () => {
  complaintForm.title = ''
  complaintForm.content = ''
  complaintDialogVisible.value = true
}

// 从对话框提交反馈
const submitFeedbackFromDialog = async () => {
  if (!feedbackFormRef.value) return
  await feedbackFormRef.value.validate(async (valid) => {
    if (valid) {
      await submitFeedback('feedback')
      feedbackDialogVisible.value = false
    }
  })
}

// 从对话框提交投诉
const submitComplaintFromDialog = async () => {
  if (!complaintFormRef.value) return
  await complaintFormRef.value.validate(async (valid) => {
    if (valid) {
      await submitFeedback('complaint')
      complaintDialogVisible.value = false
    }
  })
}

// 查询反馈记录
const searchFeedbackRecords = () => {
  // 确保 feedbackRecords 已加载
  if (!feedbackRecords.value || !Array.isArray(feedbackRecords.value)) {
    feedbackSearchResults.value = []
    return
  }

  let result = feedbackRecords.value.filter(record => record && record.type === 'feedback')

  // 根据标题过滤
  if (feedbackSearchForm.title) {
    const searchTitle = feedbackSearchForm.title.toLowerCase()
    result = result.filter(record =>
      record.title && record.title.toLowerCase().includes(searchTitle)
    )
  }

  // 根据提交日期过滤
  if (feedbackSearchForm.submitDate) {
    const searchDate = new Date(feedbackSearchForm.submitDate)
    const searchDateStr = searchDate.toISOString().split('T')[0]

    result = result.filter(record => {
      if (!record.submitTime) return false
      const recordDate = new Date(record.submitTime)
      const recordDateStr = recordDate.toISOString().split('T')[0]
      return recordDateStr === searchDateStr
    })
  }

  feedbackSearchResults.value = result
  ElMessage.success('查询完成')
}

// 查询投诉记录
const searchComplaintRecords = () => {
  // 确保 feedbackRecords 已加载
  if (!feedbackRecords.value || !Array.isArray(feedbackRecords.value)) {
    complaintSearchResults.value = []
    return
  }

  let result = feedbackRecords.value.filter(record => record && record.type === 'complaint')

  // 根据标题过滤
  if (complaintSearchForm.title) {
    const searchTitle = complaintSearchForm.title.toLowerCase()
    result = result.filter(record =>
      record.title && record.title.toLowerCase().includes(searchTitle)
    )
  }

  // 根据提交日期过滤
  if (complaintSearchForm.submitDate) {
    const searchDate = new Date(complaintSearchForm.submitDate)
    const searchDateStr = searchDate.toISOString().split('T')[0]

    result = result.filter(record => {
      if (!record.submitTime) return false
      const recordDate = new Date(record.submitTime)
      const recordDateStr = recordDate.toISOString().split('T')[0]
      return recordDateStr === searchDateStr
    })
  }

  complaintSearchResults.value = result
  ElMessage.success('查询完成')
}

// 重置投诉记录查询
const resetComplaintSearch = () => {
  complaintSearchForm.title = ''
  complaintSearchForm.submitDate = null
  // 重置时显示所有投诉记录
  if (feedbackRecords.value && Array.isArray(feedbackRecords.value)) {
    complaintSearchResults.value = feedbackRecords.value.filter(record => record && record.type === 'complaint')
  } else {
    complaintSearchResults.value = []
  }
  ElMessage.success('已重置查询条件')
}

// 重置反馈记录查询
const resetFeedbackSearch = () => {
  feedbackSearchForm.title = ''
  feedbackSearchForm.submitDate = null
  // 重置时显示所有反馈记录
  if (feedbackRecords.value && Array.isArray(feedbackRecords.value)) {
    feedbackSearchResults.value = feedbackRecords.value.filter(record => record && record.type === 'feedback')
  } else {
    feedbackSearchResults.value = []
  }
  ElMessage.success('已重置查询条件')
}

// 管理员查询反馈记录
const searchAdminFeedback = () => {
  // 确保 feedbackRecords 已加载
  if (!feedbackRecords.value || !Array.isArray(feedbackRecords.value)) {
    adminSearchResults.value = []
    return
  }

  let result = [...feedbackRecords.value]

  // 根据标题过滤
  if (adminSearchForm.title) {
    const searchTitle = adminSearchForm.title.toLowerCase()
    result = result.filter(record =>
      record.title && record.title.toLowerCase().includes(searchTitle)
    )
  }

  // 根据提交人过滤
  if (adminSearchForm.submitter) {
    const searchSubmitter = adminSearchForm.submitter.toLowerCase()
    result = result.filter(record =>
      record.submitter && record.submitter.toLowerCase().includes(searchSubmitter)
    )
  }

  // 根据提交日期过滤
  if (adminSearchForm.submitDate) {
    const searchDate = new Date(adminSearchForm.submitDate)
    const searchDateStr = searchDate.toISOString().split('T')[0]

    result = result.filter(record => {
      if (!record.submitTime) return false
      const recordDate = new Date(record.submitTime)
      const recordDateStr = recordDate.toISOString().split('T')[0]
      return recordDateStr === searchDateStr
    })
  }

  adminSearchResults.value = result
  // 搜索后重置到第一页
  currentPage.value = 1
  ElMessage.success('查询完成')
}

// 分页后的管理员搜索结果
const paginatedAdminSearchResults = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return adminSearchResults.value.slice(start, end)
})

// 重置管理员查询
const resetAdminSearch = () => {
  adminSearchForm.title = ''
  adminSearchForm.submitter = ''
  adminSearchForm.submitDate = null
  // 重置时显示所有记录
  if (feedbackRecords.value && Array.isArray(feedbackRecords.value)) {
    adminSearchResults.value = [...feedbackRecords.value]
  } else {
    adminSearchResults.value = []
  }
  // 重置到第一页
  currentPage.value = 1
  ElMessage.success('已重置查询条件')
}

// 分页方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

// 查询表单相关
const searchForm = reactive({
  submitter: '',
  isProcessed: '',
  processor: ''
})

// 处理反馈相关
const processDialogVisible = ref(false)
const processFormRef = ref(null)
const processForm = reactive({
  id: '',
  content: '',
  processResult: ''
})
const processRules = {
  processResult: [{ required: true, message: '请输入处理结果', trigger: 'blur' }]
}

// 查看详细相关
const detailDialogVisible = ref(false)
const detailForm = reactive({
  id: '',
  title: '',
  content: '',
  submitter: '',
  submitTime: '',
  processor: '',
  processTime: '',
  processResult: ''
})

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)
const activeIndex = ref('feedback')



const submitFeedback = async (type) => {
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  let formRef, form, rules, successMessage
  
  if (type === 'feedback') {
    formRef = feedbackFormRef
    form = feedbackForm
    rules = feedbackRules
    successMessage = '反馈提交成功'
  } else {
    formRef = complaintFormRef
    form = complaintForm
    rules = complaintRules
    successMessage = '投诉提交成功'
  }
  
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 调用后端API提交反馈
        const response = await axios.post('http://localhost:8080/api/feedback', {
          loginId: user.value.id,
          type: type,
          title: form.title,
          content: form.content
        })
        
        if (response.data) {
          ElMessage.success(successMessage)
          // 重置表单
          form.title = ''
          form.content = ''
          // 刷新反馈记录
          loadFeedbackRecords()
        } else {
          ElMessage.error('提交失败，请稍后重试')
        }
      } catch (error) {
        console.error('提交失败', error)
        ElMessage.error('提交失败，请稍后重试')
      }
    }
  })
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const loadFeedbackRecords = async (searchParams = {}) => {
  try {
    let response
    
    if (userRole.value === 'USER' && user.value && user.value.id) {
      // 普通用户获取自己的反馈记录
      response = await axios.get(`http://localhost:8080/api/feedback/login/${user.value.id}`)
    } else if (userRole.value === 'STAFF' && (activeIndex.value === 'feedback' || !activeIndex.value)) {
      // 工作人员获取自己的系统反馈记录
      response = await axios.get(`http://localhost:8080/api/feedback/login/${user.value.id}`)
    } else if (userRole.value === 'STAFF' && activeIndex.value === 'complaint-management') {
      // 工作人员获取需要处理的投诉（targetType为STAFF）
      if (Object.keys(searchParams).length > 0) {
        // 构建查询参数
        const params = new URLSearchParams()
        if (searchParams.isProcessed !== '' && searchParams.isProcessed !== undefined) {
          params.append('isProcessed', searchParams.isProcessed)
        }
        if (searchParams.submitter) {
          params.append('submitter', searchParams.submitter)
        }
        if (searchParams.processor) {
          params.append('processor', searchParams.processor)
        }
        response = await axios.get(`http://localhost:8080/api/feedback/target/STAFF/search?${params.toString()}`)
      } else {
        response = await axios.get('http://localhost:8080/api/feedback/target/STAFF')
      }
    } else if (userRole.value === 'ADMIN') {
      // 管理员获取需要处理的意见反馈（targetType为ADMIN）
      if (Object.keys(searchParams).length > 0) {
        // 构建查询参数
        const params = new URLSearchParams()
        if (searchParams.isProcessed !== '' && searchParams.isProcessed !== undefined) {
          params.append('isProcessed', searchParams.isProcessed)
        }
        if (searchParams.submitter) {
          params.append('submitter', searchParams.submitter)
        }
        if (searchParams.processor) {
          params.append('processor', searchParams.processor)
        }
        response = await axios.get(`http://localhost:8080/api/feedback/target/ADMIN/search?${params.toString()}`)
      } else {
        response = await axios.get('http://localhost:8080/api/feedback/target/ADMIN')
      }
    }
    
    if (response) {
      const feedbacks = response.data
      
      // 处理反馈数据
        if (Array.isArray(feedbacks)) {
          feedbackRecords.value = feedbacks.filter(feedback => feedback).map(feedback => ({
            id: feedback.id,
            type: feedback.type,
            title: feedback.title,
            content: feedback.content,
            submitTime: new Date(feedback.submitTime),
            isProcessed: feedback.isProcessed,
            processResult: feedback.processResult,
            submitter: feedback.login ? feedback.login.username : '',
            processor: feedback.processor ? feedback.processor.username : '',
            processTime: feedback.processTime ? new Date(feedback.processTime) : null
          }))
          // 初始化反馈记录查询结果
          feedbackSearchResults.value = feedbackRecords.value.filter(record => record && record.type === 'feedback')
          // 初始化投诉记录查询结果
          complaintSearchResults.value = feedbackRecords.value.filter(record => record && record.type === 'complaint')
          // 初始化管理员搜索结果
          adminSearchResults.value = [...feedbackRecords.value]
      } else {
        // 如果不是数组，使用空数组
        feedbackRecords.value = []
        ElMessage.error('加载反馈记录失败，数据格式错误')
      }
    } else {
      // 如果没有响应，使用模拟数据
      feedbackRecords.value = [
        {
          id: 1,
          type: 'feedback',
          content: '建议增加更多的电源插座',
          submitTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
          isProcessed: true,
          processResult: '已记录，将在下次改造中考虑',
          submitter: 'user1',
          processor: 'admin1',
          processTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
        },
        {
          id: 2,
          type: 'complaint',
          content: '工作人员态度不好',
          submitTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
          isProcessed: false,
          processResult: '',
          submitter: 'user2',
          processor: '',
          processTime: null
        }
      ]
      // 初始化反馈记录查询结果
      feedbackSearchResults.value = feedbackRecords.value.filter(record => record && record.type === 'feedback')
      // 初始化投诉记录查询结果
      complaintSearchResults.value = feedbackRecords.value.filter(record => record && record.type === 'complaint')
      // 初始化管理员搜索结果
      adminSearchResults.value = [...feedbackRecords.value]
      ElMessage.warning('使用模拟数据')
    }
  } catch (error) {
    console.error('加载反馈记录失败', error)
    // 如果API调用失败，使用模拟数据
    feedbackRecords.value = [
      {
        id: 1,
        type: 'feedback',
        content: '建议增加更多的电源插座',
        submitTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
        isProcessed: true,
        processResult: '已记录，将在下次改造中考虑',
        submitter: 'user1',
        processor: 'admin1',
        processTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
      },
      {
        id: 2,
        type: 'complaint',
        content: '工作人员态度不好',
        submitTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
        isProcessed: false,
        processResult: '',
        submitter: 'user2',
        processor: '',
        processTime: null
      }
    ]
    // 初始化反馈记录查询结果
      feedbackSearchResults.value = feedbackRecords.value.filter(record => record && record.type === 'feedback')
      // 初始化投诉记录查询结果
      complaintSearchResults.value = feedbackRecords.value.filter(record => record && record.type === 'complaint')
      // 初始化管理员搜索结果
      adminSearchResults.value = [...feedbackRecords.value]
      ElMessage.error('加载反馈记录失败，使用模拟数据')
  }
}

// 查询反馈记录
const searchFeedback = () => {
  loadFeedbackRecords(searchForm)
}

// 重置查询表单
const resetSearch = () => {
  searchForm.submitter = ''
  searchForm.isProcessed = ''
  searchForm.processor = ''
  loadFeedbackRecords()
}

const withdrawFeedback = async (feedbackId) => {
  try {
    await ElMessageBox.confirm('确定要撤回这条反馈吗？撤回后将无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API删除反馈
    await axios.delete(`http://localhost:8080/api/feedback/${feedbackId}`)
    ElMessage.success('撤回成功')
    // 刷新反馈记录
    loadFeedbackRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回反馈失败', error)
      ElMessage.error('撤回失败，请稍后重试')
    }
  }
}

const deleteFeedback = async (feedbackId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条反馈吗？删除后将无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API删除反馈
    await axios.delete(`http://localhost:8080/api/feedback/${feedbackId}`)
    ElMessage.success('删除成功')
    // 刷新反馈记录
    loadFeedbackRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除反馈失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const processFeedback = (feedback) => {
  // 打开处理反馈对话框
  processForm.id = feedback.id
  processForm.content = feedback.content
  processForm.processResult = ''
  processDialogVisible.value = true
}

const viewFeedbackDetail = (feedback) => {
  // 打开查看详细对话框
  detailForm.id = feedback.id
  detailForm.title = feedback.title
  detailForm.content = feedback.content
  detailForm.submitter = feedback.submitter
  detailForm.submitTime = formatDateTime(feedback.submitTime)
  detailForm.processor = feedback.processor || '暂无'
  detailForm.processTime = feedback.processTime ? formatDateTime(feedback.processTime) : '暂无'
  detailForm.processResult = feedback.processResult || '暂无'
  detailDialogVisible.value = true
}

const saveProcessResult = async () => {
  if (!processFormRef.value) return
  await processFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 检查用户信息是否存在
        if (!user.value || !user.value.id) {
          ElMessage.error('用户信息获取失败，请重新登录')
          return
        }
        
        // 调用后端API处理反馈
        const response = await axios.put(`http://localhost:8080/api/feedback/process/${processForm.id}`, {
          processResult: processForm.processResult,
          processorId: user.value.id
        })
        
        if (response.data) {
          ElMessage.success('处理成功')
          processDialogVisible.value = false
          // 刷新反馈记录
          loadFeedbackRecords()
        } else {
          ElMessage.error('处理失败，请稍后重试')
        }
      } catch (error) {
        console.error('处理反馈失败', error)
        ElMessage.error('处理失败，请稍后重试')
      }
    }
  })
}

onMounted(() => {
  userStore.loadUserFromStorage()
  // 检查URL参数或导航状态，设置正确的activeIndex
  const path = window.location.pathname
  if (path === '/feedback') {
    // 检查是否从投诉管理导航过来
    const fromComplaint = sessionStorage.getItem('fromComplaint')
    if (fromComplaint === 'true') {
      activeIndex.value = 'complaint-management'
      sessionStorage.removeItem('fromComplaint')
    } else {
      // 检查localStorage中的状态
      const savedActiveIndex = localStorage.getItem('feedbackActiveIndex')
      if (savedActiveIndex) {
        activeIndex.value = savedActiveIndex
      }
    }
  }
  loadFeedbackRecords()
})
</script>

<style scoped>
.feedback-container {
  min-height: 100vh;
  background: #f5f7fa;
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

.feedback-card,
.records-card {
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.feedback-card:hover,
.records-card:hover {
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

.search-form {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.search-form:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>