<template>
  <div class="activity-management">
    <el-card class="activity-card">
      <template #header>
        <div class="card-header">
          <span>活动管理</span>
        </div>
      </template>
      
      <!-- 查询表单 -->
      <el-form :model="queryForm" class="query-form" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="10">
            <el-form-item label="关键词">
              <el-input v-model="queryForm.keyword" placeholder="请输入活动标题"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择状态">
                <el-option label="待发布" :value="0"></el-option>
                <el-option label="进行中" :value="1"></el-option>
                <el-option label="已结束" :value="2"></el-option>
                <el-option label="已取消" :value="3"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="getActivityList" style="margin-top: 24px;">搜索</el-button>
            <el-button @click="resetForm" style="margin-top: 24px;">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
      
      <!-- 活动列表 -->
      <el-table :data="activityList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面图" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.coverImage"
              :src="scope.row.coverUrl || scope.row.coverImage"
              fit="cover"
              :preview-src-list="[scope.row.coverUrl || scope.row.coverImage]"
              style="width: 80px; height: 60px;"
            />
            <div v-else class="no-cover">无封面</div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="organizerName" label="组织者" width="120" />
        <el-table-column prop="location" label="地点" min-width="150" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="人数" width="120">
          <template #default="scope">
            {{ scope.row.currentParticipants || 0 }}/{{ scope.row.maxParticipants || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewActivityDetail(scope.row.id)">查看详情</el-button>
            <el-button type="success" size="small" @click="openStatusDialog(scope.row)">修改状态</el-button>
            <el-button type="danger" size="small" @click="handleDeleteActivity(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <!-- 更新状态对话框 -->
      <el-dialog
        v-model="statusDialogVisible"
        title="修改活动状态"
        width="400px"
      >
        <el-form>
          <el-form-item label="当前状态">
            <el-tag :type="getStatusTagType(currentActivity.status)">
              {{ getStatusText(currentActivity.status) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="新状态">
            <el-select v-model="newStatus" placeholder="请选择新状态">
              <el-option label="待发布" :value="0"></el-option>
              <el-option label="进行中" :value="1"></el-option>
              <el-option label="已结束" :value="2"></el-option>
              <el-option label="已取消" :value="3"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="statusDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmUpdateStatus">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminApi from '../../api/admin'
import { resolveAvatarUrl } from '../../utils/avatarUrl'

// 查询表单
const queryForm = reactive({
  status: undefined,
  keyword: ''
})

// 分页参数
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 活动列表
const activityList = ref([])

// 状态对话框
const statusDialogVisible = ref(false)
const currentActivity = ref({ id: '', status: '' })
const newStatus = ref('')

// 获取活动列表
const getActivityList = async () => {
  try {
    const response = await adminApi.getActivityList(
      pageNum.value,
      pageSize.value,
      queryForm.status,
      queryForm.keyword
    )
    const raw = response.data?.records || []
    activityList.value = await Promise.all(
      raw.map(async (a) => ({
        ...a,
        coverUrl: a.coverImage ? await resolveAvatarUrl(a.coverImage) : ''
      }))
    )
    total.value = response.data?.total || 0
  } catch (error) {
    console.error('获取活动列表失败:', error)
    ElMessage.error('获取活动列表失败，请重试')
  }
}

// 重置表单
const resetForm = () => {
  queryForm.status = undefined
  queryForm.keyword = ''
  getActivityList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  getActivityList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pageNum.value = current
  getActivityList()
}

// 查看活动详情（跳转前台活动详情页）
const viewActivityDetail = (activityId) => {
  // 跳转到前台活动详情页
  window.open(`/activity/${activityId}`, '_blank')
}

// 打开状态对话框
const openStatusDialog = (activity) => {
  currentActivity.value = activity
  newStatus.value = activity.status
  statusDialogVisible.value = true
}

// 确认更新状态
const confirmUpdateStatus = async () => {
  if (!newStatus.value) {
    ElMessage.warning('请选择新状态')
    return
  }
  
  try {
    await adminApi.updateActivityStatus(currentActivity.value.id, newStatus.value)
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    getActivityList()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 处理删除活动
const handleDeleteActivity = async (activityId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminApi.deleteActivity(activityId)
    ElMessage.success('删除活动成功')
    getActivityList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除活动失败:', error)
      ElMessage.error('删除活动失败')
    }
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (Number(status)) {
    case 0:
      return 'info'
    case 1:
      return 'success'
    case 2:
      return 'default'
    case 3:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (Number(status)) {
    case 0:
      return '待发布'
    case 1:
      return '进行中'
    case 2:
      return '已结束'
    case 3:
      return '已取消'
    default:
      return String(status)
  }
}

// 初始加载
getActivityList()
</script>

<style scoped>
.activity-management {
  padding: 20px 0;
}

.activity-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.no-cover {
  width: 80px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  color: #999;
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>