<template>
  <div class="content-review">
    <el-card class="content-card">
      <template #header>
        <div class="card-header">
          <span>内容审核</span>
          <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="动态审核" name="posts"></el-tab-pane>
            <el-tab-pane label="评论审核" name="comments"></el-tab-pane>
          </el-tabs>
        </div>
      </template>
      
      <!-- 动态审核 -->
      <div v-if="activeTab === 'posts'">
        <!-- 查询表单 -->
        <el-form :model="postQueryForm" class="query-form" label-width="80px">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="postQueryForm.status" placeholder="请选择状态">
                  <el-option label="待审核" value="0"></el-option>
                  <el-option label="已通过" value="1"></el-option>
                  <el-option label="已拒绝" value="2"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="关键词">
                <el-input v-model="postQueryForm.keyword" placeholder="请输入标题或内容关键词"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-button type="primary" @click="getPostList" style="margin-top: 24px;">查询</el-button>
              <el-button @click="resetPostForm" style="margin-top: 24px;">重置</el-button>
            </el-col>
          </el-row>
        </el-form>
        
        <!-- 动态列表 -->
        <el-table :data="postList" style="width: 100%" border>
          <el-table-column prop="id" label="动态ID" width="80" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="content" label="内容" width="300">
            <template #default="scope">
              <span class="content-text">{{ scope.row.content }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="发布时间" width="180" />
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button type="primary" size="small" @click="auditPost(scope.row, 1)" v-if="scope.row.status === 0">通过</el-button>
              <el-button type="danger" size="small" @click="auditPost(scope.row, 2)" v-if="scope.row.status === 0">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="postPageNum"
            v-model:page-size="postPageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="postTotal"
            @size-change="handlePostSizeChange"
            @current-change="handlePostCurrentChange"
          />
        </div>
      </div>
      
      <!-- 评论审核 -->
      <div v-if="activeTab === 'comments'">
        <!-- 查询表单 -->
        <el-form :model="commentQueryForm" class="query-form" label-width="80px">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="commentQueryForm.status" placeholder="请选择状态">
                  <el-option label="待审核" value="0"></el-option>
                  <el-option label="已通过" value="1"></el-option>
                  <el-option label="已拒绝" value="2"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="关键词">
                <el-input v-model="commentQueryForm.keyword" placeholder="请输入评论内容关键词"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-button type="primary" @click="getCommentList" style="margin-top: 24px;">查询</el-button>
              <el-button @click="resetCommentForm" style="margin-top: 24px;">重置</el-button>
            </el-col>
          </el-row>
        </el-form>
        
        <!-- 评论列表 -->
        <el-table :data="commentList" style="width: 100%" border>
          <el-table-column prop="id" label="评论ID" width="80" />
          <el-table-column prop="content" label="评论内容" />
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column prop="postTitle" label="所属动态" width="200">
            <template #default="scope">
              <span class="post-title">{{ scope.row.postTitle }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="发布时间" width="180" />
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button type="primary" size="small" @click="auditComment(scope.row, 1)" v-if="scope.row.status === 0">通过</el-button>
              <el-button type="danger" size="small" @click="auditComment(scope.row, 2)" v-if="scope.row.status === 0">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="commentPageNum"
            v-model:page-size="commentPageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="commentTotal"
            @size-change="handleCommentSizeChange"
            @current-change="handleCommentCurrentChange"
          />
        </div>
      </div>
      
      <!-- 审核对话框 -->
      <el-dialog
        v-model="auditDialogVisible"
        :title="auditDialogTitle"
        width="500px"
      >
        <el-form :model="auditForm">
          <el-form-item label="审核结果">
            <el-radio-group v-model="auditForm.status">
              <el-radio label="1">通过</el-radio>
              <el-radio label="2">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="拒绝原因" v-if="auditForm.status === 2">
            <el-input
              v-model="auditForm.reason"
              type="textarea"
              rows="3"
              placeholder="请输入拒绝原因"
            ></el-input>
          </el-form-item>
        </el-form>
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

// 当前激活的标签
const activeTab = ref('posts')

// 动态审核相关
const postQueryForm = reactive({
  status: '',
  keyword: ''
})
const postPageNum = ref(1)
const postPageSize = ref(10)
const postTotal = ref(0)
const postList = ref([])

// 评论审核相关
const commentQueryForm = reactive({
  status: '',
  keyword: ''
})
const commentPageNum = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)
const commentList = ref([])

// 审核对话框
const auditDialogVisible = ref(false)
const auditDialogTitle = ref('')
const auditForm = reactive({
  status: 1,
  reason: '',
  id: '',
  type: '' // 'post' 或 'comment'
})

// 处理标签切换
const handleTabClick = (tab) => {
  if (tab.props.name === 'posts') {
    getPostList()
  } else {
    getCommentList()
  }
}

// 获取动态列表
const getPostList = async () => {
  try {
    const response = await adminApi.getPostList(
      postPageNum.value, 
      postPageSize.value, 
      postQueryForm.status, 
      postQueryForm.keyword
    )
    postList.value = response.data.records
    postTotal.value = response.data.total
  } catch (error) {
    console.error('获取动态列表失败:', error)
    // 模拟数据
    postList.value = [
      { id: 1, title: '校友聚会分享', content: '昨天的校友聚会很开心，见到了很多老朋友', author: 'user1', status: 0, createdAt: '2026-01-28' },
      { id: 2, title: '招聘信息', content: '我司正在招聘前端工程师，有意向的校友可以联系我', author: 'user2', status: 1, createdAt: '2026-01-27' },
      { id: 3, title: '分享一本书', content: '最近读了一本很好的书，推荐给大家', author: 'user3', status: 2, createdAt: '2026-01-26' }
    ]
    postTotal.value = 3
  }
}

// 获取评论列表
const getCommentList = async () => {
  try {
    const response = await adminApi.getCommentList(
      commentPageNum.value, 
      commentPageSize.value, 
      commentQueryForm.status, 
      commentQueryForm.keyword
    )
    commentList.value = response.data.records
    commentTotal.value = response.data.total
  } catch (error) {
    console.error('获取评论列表失败:', error)
    // 模拟数据
    commentList.value = [
      { id: 1, content: '很棒的分享', author: 'user1', postTitle: '校友聚会分享', status: 0, createdAt: '2026-01-28' },
      { id: 2, content: '感谢分享', author: 'user2', postTitle: '招聘信息', status: 1, createdAt: '2026-01-27' },
      { id: 3, content: '这本书我也看过，确实不错', author: 'user3', postTitle: '分享一本书', status: 1, createdAt: '2026-01-26' }
    ]
    commentTotal.value = 3
  }
}

// 重置动态查询表单
const resetPostForm = () => {
  postQueryForm.status = ''
  postQueryForm.keyword = ''
  getPostList()
}

// 重置评论查询表单
const resetCommentForm = () => {
  commentQueryForm.status = ''
  commentQueryForm.keyword = ''
  getCommentList()
}

// 动态分页大小变化
const handlePostSizeChange = (size) => {
  postPageSize.value = size
  getPostList()
}

// 动态当前页变化
const handlePostCurrentChange = (current) => {
  postPageNum.value = current
  getPostList()
}

// 评论分页大小变化
const handleCommentSizeChange = (size) => {
  commentPageSize.value = size
  getCommentList()
}

// 评论当前页变化
const handleCommentCurrentChange = (current) => {
  commentPageNum.value = current
  getCommentList()
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return ''
  }
}

// 审核动态
const auditPost = (post, status) => {
  auditDialogTitle.value = '审核动态'
  auditForm.id = post.id
  auditForm.type = 'post'
  auditForm.status = status
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 审核评论
const auditComment = (comment, status) => {
  auditDialogTitle.value = '审核评论'
  auditForm.id = comment.id
  auditForm.type = 'comment'
  auditForm.status = status
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  try {
    if (auditForm.type === 'post') {
      await adminApi.auditPost(auditForm.id, auditForm.status, auditForm.reason)
      ElMessage.success('审核成功')
    } else {
      await adminApi.auditComment(auditForm.id, auditForm.status, auditForm.reason)
      ElMessage.success('审核成功')
    }
    auditDialogVisible.value = false
    if (activeTab.value === 'posts') {
      getPostList()
    } else {
      getCommentList()
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 初始加载
getPostList()
</script>

<style scoped>
.content-review {
  padding: 20px 0;
}

.content-card {
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

.content-text, .post-title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style>