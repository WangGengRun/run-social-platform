<template>
  <div class="post-audit">
    <el-card class="audit-card">
      <template #header>
        <div class="card-header">
          <span>动态审核</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :model="queryForm" class="query-form" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="10">
            <el-form-item label="关键词">
              <el-input v-model="queryForm.keyword" placeholder="请输入动态内容关键词"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择状态">
                <el-option label="正常" :value="1"></el-option>
                <el-option label="已删除" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="getPostList" style="margin-top: 24px;">搜索</el-button>
            <el-button @click="resetForm" style="margin-top: 24px;">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
      
      <!-- 动态列表表格 -->
      <el-table :data="postList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="发布者" width="120" />
        <el-table-column prop="content" label="动态内容" min-width="300">
          <template #default="scope">
            <span class="content-text">{{ scope.row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图片数" width="100">
          <template #default="scope">
            <span>{{ scope.row.imageUrls ? scope.row.imageUrls.length : 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞数" width="100" />
        <el-table-column prop="commentCount" label="评论数" width="100" />
        <el-table-column prop="visibility" label="可见性" width="120">
          <template #default="scope">
            <el-tag :type="getVisibilityTagType(scope.row.visibility)">
              {{ getVisibilityText(scope.row.visibility) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewPostDetail(scope.row)">查看详情</el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="danger"
              size="small"
              @click="openAuditDialog(scope.row, 0)"
            >
              删除
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              size="small"
              @click="openAuditDialog(scope.row, 1)"
            >
              恢复
            </el-button>
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
      
      <!-- 审核对话框 -->
      <el-dialog
        v-model="auditDialogVisible"
        title="变更动态状态"
        width="600px"
      >
        <div v-if="currentPost" class="audit-dialog-content">
          <h3>动态详情</h3>
          <p><strong>发布者：</strong>{{ currentPost.username }}</p>
          <p><strong>发布时间：</strong>{{ currentPost.createdAt }}</p>
          <p><strong>内容：</strong>{{ currentPost.content }}</p>
          <div v-if="currentPost.imageUrls && currentPost.imageUrls.length > 0" class="image-preview">
            <strong>图片：</strong>
            <div class="image-list">
              <el-image
                v-for="(url, index) in currentPost.imageUrls"
                :key="index"
                :src="url"
                fit="cover"
                :preview-src-list="currentPost.imageUrls"
                style="width: 100px; height: 100px; margin-right: 10px; margin-top: 10px;"
              />
            </div>
          </div>
          <el-form :model="auditForm" style="margin-top: 20px;">
            <el-form-item label="新状态">
              <el-radio-group v-model="auditForm.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">已删除</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="auditDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitAudit">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import adminApi from '../../api/admin'

// 查询表单
const queryForm = reactive({
  keyword: '',
  status: undefined
})

// 分页参数
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 动态列表
const postList = ref([])

// 审核对话框
const auditDialogVisible = ref(false)
const currentPost = ref(null)
const auditForm = reactive({
  status: 1,
  reason: ''
})

// 获取动态列表
const getPostList = async () => {
  try {
    const response = await adminApi.getPostList(
      pageNum.value,
      pageSize.value,
      queryForm.status,
      queryForm.keyword
    )
    postList.value = response.data?.records || []
    total.value = response.data?.total || 0
  } catch (error) {
    console.error('获取动态列表失败:', error)
    ElMessage.error('获取动态列表失败，请重试')
  }
}

// 重置表单
const resetForm = () => {
  queryForm.keyword = ''
  queryForm.status = undefined
  getPostList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  getPostList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pageNum.value = current
  getPostList()
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  return status === 1 ? 'success' : 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  return status === 1 ? '正常' : '已删除'
}

// 获取可见性标签类型
const getVisibilityTagType = (visibility) => {
  switch (visibility) {
    case 'public':
      return 'success'
    case 'alumni':
      return 'info'
    case 'private':
      return ''
    default:
      return ''
  }
}

// 获取可见性文本
const getVisibilityText = (visibility) => {
  switch (visibility) {
    case 'public':
      return '公开'
    case 'alumni':
      return '仅校友'
    case 'private':
      return '仅自己'
    default:
      return visibility
  }
}

// 查看动态详情
const viewPostDetail = (post) => {
  currentPost.value = post
  auditForm.status = post.status
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 打开审核对话框
const openAuditDialog = (post, status) => {
  currentPost.value = post
  auditForm.status = status
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  if (!currentPost.value) {
    ElMessage.warning('请选择要审核的动态')
    return
  }

  try {
    await adminApi.auditPost(currentPost.value.id, auditForm.status, '')
    ElMessage.success('操作成功')
    auditDialogVisible.value = false
    getPostList()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('操作失败')
  }
}

// 初始加载
getPostList()
</script>

<style scoped>
.post-audit {
  padding: 20px 0;
}

.audit-card {
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

.content-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.audit-dialog-content {
  line-height: 1.6;
}

.image-preview {
  margin-top: 10px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  margin-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>