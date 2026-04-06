<template>
  <div class="verify-audit">
    <el-card class="audit-card">
      <template #header>
        <div class="card-header">
          <span>校友认证审核</span>
        </div>
      </template>

      <el-form :inline="true" class="query-row">
        <el-form-item label="关键词">
          <el-input
            v-model="keyword"
            placeholder="姓名/学号/学院/专业"
            clearable
            @keyup.enter="fetchPendingList(true)"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchPendingList(true)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="records" border>
        <el-table-column prop="id" label="用户ID" width="90" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row.id)">详情</el-button>
            <el-button size="small" type="success" @click="approve(scope.row)">通过</el-button>
            <el-button size="small" type="danger" @click="reject(scope.row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchPendingList(true)"
          @current-change="fetchPendingList(false)"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="认证详情" size="45%" direction="rtl">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="用户ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.email || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ detail.alumniInfo?.realName || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detail.alumniInfo?.studentId || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ detail.alumniInfo?.admissionYear || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="毕业年份">{{ detail.alumniInfo?.graduationYear || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ detail.alumniInfo?.college || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ detail.alumniInfo?.major || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="公司">{{ detail.alumniInfo?.company || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ detail.alumniInfo?.position || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ detail.alumniInfo?.city || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="简介">{{ detail.alumniInfo?.bio || '未设置' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminApi from '../../api/admin'

const keyword = ref('')
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const detail = ref(null)

const fetchPendingList = async (resetPage = false) => {
  if (resetPage) pageNum.value = 1
  try {
    const res = await adminApi.getPendingAlumniVerifyList(pageNum.value, pageSize.value, keyword.value || undefined)
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取待审核列表失败:', error)
    ElMessage.error('获取待审核列表失败')
  }
}

const resetQuery = () => {
  keyword.value = ''
  fetchPendingList(true)
}

const viewDetail = async (userId) => {
  try {
    const res = await adminApi.getUserDetail(userId)
    detail.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error('获取认证详情失败:', error)
    ElMessage.error('获取认证详情失败')
  }
}

const approve = async (row) => {
  try {
    await ElMessageBox.confirm(`确认通过用户 ${row.username} 的校友认证？`, '认证审核', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await adminApi.auditAlumniVerify(row.id, 1, '审核通过')
    ElMessage.success('已通过认证')
    fetchPendingList(false)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
      ElMessage.error('审核通过失败')
    }
  }
}

const reject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回认证', {
      inputType: 'textarea',
      inputPlaceholder: '例如：学号与姓名不匹配',
      inputValidator: (v) => !!String(v || '').trim() || '请输入驳回原因',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await adminApi.auditAlumniVerify(row.id, 2, value)
    ElMessage.success('已驳回认证')
    fetchPendingList(false)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('驳回认证失败:', error)
      ElMessage.error('驳回认证失败')
    }
  }
}

fetchPendingList(true)
</script>

<style scoped>
.verify-audit { padding: 20px 0; }
.query-row { margin-bottom: 14px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
