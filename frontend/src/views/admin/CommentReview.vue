<template>
  <div class="comment-review">
    <el-card class="review-card">
      <template #header>
        <div class="card-header">
          <span>评论审核</span>
          <el-button type="primary" @click="refreshComments">
            <el-icon><Refresh /></el-icon>
            <span>刷新</span>
          </el-button>
        </div>
      </template>
      
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索评论内容"
          prefix-icon="Search"
          style="width: 300px; margin-right: 10px"
          @keyup.enter="searchComments"
        />
        <el-select
          v-model="statusFilter"
          placeholder="选择状态"
          style="width: 150px; margin-right: 10px"
          @change="searchComments"
        >
          <el-option label="全部" value="" />
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        <el-button type="primary" @click="searchComments">
          <el-icon><Search /></el-icon>
          <span>搜索</span>
        </el-button>
      </div>
      
      <!-- 评论列表 -->
      <el-table :data="comments" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="评论内容" min-width="400" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="postTitle" label="关联动态" width="200" />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'pending'"
              type="success"
              size="small"
              @click="auditComment(scope.row.id, 'approved')"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              type="danger"
              size="small"
              @click="handleReject(scope.row.id)"
            >
              拒绝
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="viewCommentDetail(scope.row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <!-- 拒绝原因对话框 -->
      <el-dialog
        v-model="rejectDialogVisible"
        title="拒绝评论"
        width="400px"
      >
        <el-form>
          <el-form-item label="拒绝原因">
            <el-input
              v-model="rejectReason"
              type="textarea"
              rows="4"
              placeholder="请输入拒绝原因"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="rejectDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmReject">确定</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 评论详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="评论详情"
        width="600px"
      >
        <div v-if="currentComment" class="comment-detail">
          <p><strong>评论内容：</strong>{{ currentComment.content }}</p>
          <p><strong>用户：</strong>{{ currentComment.userName }}</p>
          <p><strong>关联动态：</strong>{{ currentComment.postTitle }}</p>
          <p><strong>创建时间：</strong>{{ currentComment.createdTime }}</p>
          <p><strong>状态：</strong>
            <el-tag :type="getStatusTagType(currentComment.status)">
              {{ getStatusText(currentComment.status) }}
            </el-tag>
          </p>
          <p v-if="currentComment.rejectReason"><strong>拒绝原因：</strong>{{ currentComment.rejectReason }}</p>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import adminApi from '../../api/admin'
import { ElMessage } from 'element-plus'

// 评论列表
const comments = ref([])
// 总条数
const total = ref(0)
// 当前页码
const currentPage = ref(1)
// 每页条数
const pageSize = ref(10)
// 搜索关键词
const searchKeyword = ref('')
// 状态筛选
const statusFilter = ref('')
// 拒绝对话框可见性
const rejectDialogVisible = ref(false)
// 详情对话框可见性
const detailDialogVisible = ref(false)
// 拒绝原因
const rejectReason = ref('')
// 当前操作的评论ID
const currentCommentId = ref(null)
// 当前查看的评论
const currentComment = ref(null)

// 获取评论列表
const getComments = async () => {
  try {
    const response = await adminApi.getCommentList(
      currentPage.value,
      pageSize.value,
      statusFilter.value,
      searchKeyword.value
    )
    comments.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
    // 模拟数据
    comments.value = [
      {
        id: 1,
        content: '这是一条待审核的评论',
        userName: '张三',
        postTitle: '分享一篇文章',
        createdTime: '2026-01-28 10:00:00',
        status: 'pending'
      },
      {
        id: 2,
        content: '这是一条已通过的评论',
        userName: '李四',
        postTitle: '讨论一个话题',
        createdTime: '2026-01-27 15:30:00',
        status: 'approved'
      },
      {
        id: 3,
        content: '这是一条已拒绝的评论',
        userName: '王五',
        postTitle: '发布一个活动',
        createdTime: '2026-01-26 09:00:00',
        status: 'rejected',
        rejectReason: '内容不符合规定'
      }
    ]
    total.value = 3
  }
}

// 搜索评论
const searchComments = () => {
  currentPage.value = 1
  getComments()
}

// 刷新评论列表
const refreshComments = () => {
  getComments()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  getComments()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  currentPage.value = current
  getComments()
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'approved':
      return 'success'
    case 'rejected':
      return 'danger'
    default:
      return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待审核'
    case 'approved':
      return '已通过'
    case 'rejected':
      return '已拒绝'
    default:
      return status
  }
}

// 审核评论
const auditComment = async (commentId, status) => {
  try {
    await adminApi.auditComment(commentId, status, '')
    ElMessage.success(`评论${status === 'approved' ? '通过' : '拒绝'}成功`)
    getComments()
  } catch (error) {
    console.error('审核评论失败:', error)
    ElMessage.error('审核评论失败')
  }
}

// 处理拒绝
const handleReject = (commentId) => {
  currentCommentId.value = commentId
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectReason.value) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    await adminApi.auditComment(currentCommentId.value, 'rejected', rejectReason.value)
    ElMessage.success('评论拒绝成功')
    rejectDialogVisible.value = false
    getComments()
  } catch (error) {
    console.error('拒绝评论失败:', error)
    ElMessage.error('拒绝评论失败')
  }
}

// 查看评论详情
const viewCommentDetail = (comment) => {
  currentComment.value = comment
  detailDialogVisible.value = true
}

// 页面挂载时获取评论列表
onMounted(() => {
  getComments()
})
</script>

<style scoped>
.comment-review {
  padding: 20px 0;
}

.review-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-filter {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.comment-detail {
  line-height: 1.6;
}

.comment-detail p {
  margin: 10px 0;
}
</style>