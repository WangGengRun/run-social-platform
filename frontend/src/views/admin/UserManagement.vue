<template>
  <div class="user-management">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>
      
      <!-- 查询表单 -->
      <el-form :model="queryForm" class="query-form" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="用户名">
              <el-input v-model="queryForm.username" placeholder="请输入用户名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="邮箱">
              <el-input v-model="queryForm.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="手机号">
              <el-input v-model="queryForm.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="真实姓名">
              <el-input v-model="queryForm.realName" placeholder="请输入真实姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="学号">
              <el-input v-model="queryForm.studentId" placeholder="请输入学号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="角色">
              <el-select v-model="queryForm.role" placeholder="请选择角色">
                <el-option label="普通用户" value="USER"></el-option>
                <el-option label="校友" value="ALUMNI"></el-option>
                <el-option label="管理员" value="ADMIN"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择状态">
                <el-option label="正常" :value="1"></el-option>
                <el-option label="禁用" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="getUserList" style="margin-top: 24px;">查询</el-button>
            <el-button @click="resetForm" style="margin-top: 24px;">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
      
      <!-- 用户列表 -->
      <el-table :data="userList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar || ''">
              {{ scope.row.username.charAt(0).toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
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
        <el-table-column prop="verifyStatus" label="校友认证" width="110">
          <template #default="scope">
            <el-tag :type="getVerifyTagType(scope.row.verifyStatus)">
              {{ getVerifyText(scope.row.verifyStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" sortable />
        <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
        <el-table-column label="操作" width="360">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewUserDetail(scope.row.id)">查看详情</el-button>
            <el-button type="success" size="small" @click="openStatusDialog(scope.row)">修改状态</el-button>
            <el-button type="warning" size="small" @click="openRoleDialog(scope.row)">修改角色</el-button>
            <el-button
              v-if="scope.row.verifyStatus === 0"
              type="success"
              size="small"
              @click="quickAudit(scope.row, 1)"
            >
              认证通过
            </el-button>
            <el-button
              v-if="scope.row.verifyStatus === 0"
              type="danger"
              size="small"
              @click="quickAudit(scope.row, 2)"
            >
              驳回
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
      
      <!-- 用户详情抽屉 -->
      <el-drawer
        v-model="userDetailDrawerVisible"
        title="用户详情"
        direction="rtl"
        size="50%"
      >
        <div v-if="userDetail" class="user-detail">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户ID">{{ userDetail.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ userDetail.username }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userDetail.email }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userDetail.phone || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="头像">
              <el-avatar :size="60" :src="userDetail.avatar || ''">
                {{ userDetail.username.charAt(0).toUpperCase() }}
              </el-avatar>
            </el-descriptions-item>
            <el-descriptions-item label="角色">{{ getRoleText(userDetail.role) }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ getStatusText(userDetail.status) }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ userDetail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="最后登录时间">{{ userDetail.lastLoginTime || '未登录' }}</el-descriptions-item>
          </el-descriptions>
          
          <h3 style="margin-top: 20px; margin-bottom: 10px;">校友信息</h3>
          <el-descriptions :column="1" border v-if="userDetail.alumniInfo">
            <el-descriptions-item label="真实姓名">{{ userDetail.alumniInfo.realName || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ userDetail.alumniInfo.studentId || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="入学年份">{{ userDetail.alumniInfo.admissionYear || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="毕业年份">{{ userDetail.alumniInfo.graduationYear || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="学院">{{ userDetail.alumniInfo.college || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ userDetail.alumniInfo.major || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="公司">{{ userDetail.alumniInfo.company || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="职位">{{ userDetail.alumniInfo.position || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="城市">{{ userDetail.alumniInfo.city || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="个人简介">{{ userDetail.alumniInfo.bio || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="认证状态">
              <el-tag :type="getVerifyTagType(userDetail.alumniInfo.verifyStatus)">
                {{ getVerifyText(userDetail.alumniInfo.verifyStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核备注">{{ userDetail.alumniInfo.verifyNotes || '无' }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ userDetail.alumniInfo.verifyTime || '无' }}</el-descriptions-item>
          </el-descriptions>
          <div v-else class="no-alumni-info">
            暂无校友信息
          </div>
          <div v-if="userDetail.alumniInfo && userDetail.alumniInfo.verifyStatus === 0" style="margin-top: 12px;">
            <el-button type="success" @click="quickAudit(userDetail, 1)">认证通过</el-button>
            <el-button type="danger" @click="quickAudit(userDetail, 2)">驳回</el-button>
          </div>
        </div>
      </el-drawer>
      
      <!-- 修改状态对话框 -->
      <el-dialog
        v-model="statusDialogVisible"
        title="修改用户状态"
        width="400px"
      >
        <el-form>
          <el-form-item label="新状态">
            <el-select v-model="newStatus" placeholder="请选择新状态">
              <el-option label="正常" :value="1"></el-option>
              <el-option label="禁用" :value="0"></el-option>
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
      
      <!-- 修改角色对话框 -->
      <el-dialog
        v-model="roleDialogVisible"
        title="修改用户角色"
        width="400px"
      >
        <el-form>
          <el-form-item label="新角色">
            <el-select v-model="newRole" placeholder="请选择新角色">
              <el-option label="普通用户" value="USER"></el-option>
              <el-option label="校友" value="ALUMNI"></el-option>
              <el-option label="管理员" value="ADMIN"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="roleDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmUpdateRole">确定</el-button>
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

// 查询表单
const queryForm = reactive({
  username: '',
  email: '',
  phone: '',
  realName: '',
  studentId: '',
  role: '',
  status: null
})

// 分页参数
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 用户列表
const userList = ref([])

// 用户详情
const userDetailDrawerVisible = ref(false)
const userDetail = ref(null)

// 修改状态相关
const statusDialogVisible = ref(false)
const currentUser = ref(null)
const newStatus = ref('')

// 修改角色相关
const roleDialogVisible = ref(false)
const newRole = ref('')

// 获取用户列表
const getUserList = async () => {
  try {
    const response = await adminApi.getUserList(pageNum.value, pageSize.value, queryForm)
    userList.value = response.data?.records || []
    total.value = response.data?.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 重置表单
const resetForm = () => {
  queryForm.username = ''
  queryForm.email = ''
  queryForm.phone = ''
  queryForm.realName = ''
  queryForm.studentId = ''
  queryForm.role = ''
  queryForm.status = null
  getUserList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  getUserList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pageNum.value = current
  getUserList()
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  switch (role) {
    case 'ADMIN':
      return 'danger'
    case 'ALUMNI':
      return 'warning'
    case 'USER':
      return 'success'
    default:
      return ''
  }
}

// 获取角色文本
const getRoleText = (role) => {
  switch (role) {
    case 'ADMIN':
      return '管理员'
    case 'ALUMNI':
      return '校友'
    case 'USER':
      return '普通用户'
    default:
      return role
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 1:
      return 'success'
    case 0:
      return 'danger'
    default:
      return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 1:
      return '正常'
    case 0:
      return '禁用'
    default:
      return '未知'
  }
}

const getVerifyTagType = (status) => {
  switch (status) {
    case 1:
      return 'success'
    case 0:
      return 'warning'
    case 2:
      return 'danger'
    default:
      return 'info'
  }
}

const getVerifyText = (status) => {
  switch (status) {
    case 1:
      return '已通过'
    case 0:
      return '待审核'
    case 2:
      return '已驳回'
    default:
      return '未提交'
  }
}

// 查看用户详情
const viewUserDetail = async (userId) => {
  try {
    const response = await adminApi.getUserDetail(userId)
    userDetail.value = response.data
    userDetailDrawerVisible.value = true
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

// 打开修改状态对话框
const openStatusDialog = (user) => {
  currentUser.value = user
  newStatus.value = user.status
  statusDialogVisible.value = true
}

// 确认更新状态
const confirmUpdateStatus = async () => {
  if (!currentUser.value || !newStatus.value) {
    ElMessage.warning('请选择新状态')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要修改用户状态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminApi.updateUserStatus(currentUser.value.id, newStatus.value)
    ElMessage.success('更新用户状态成功')
    statusDialogVisible.value = false
    getUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新用户状态失败:', error)
      ElMessage.error('更新用户状态失败')
    }
  }
}

const quickAudit = async (row, verifyStatus) => {
  const userId = row.id
  try {
    let verifyNotes = ''
    if (verifyStatus === 2) {
      const promptResult = await ElMessageBox.prompt('请输入驳回原因', '驳回认证', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '例如：学号与姓名不匹配',
        inputValidator: (value) => !!String(value || '').trim() || '驳回原因不能为空'
      })
      verifyNotes = promptResult.value
    } else {
      await ElMessageBox.confirm('确认通过该用户的校友认证吗？', '认证审核', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    }

    await adminApi.auditAlumniVerify(userId, verifyStatus, verifyNotes)
    ElMessage.success(verifyStatus === 1 ? '已通过认证' : '已驳回认证')
    if (userDetail.value?.id === userId) {
      await viewUserDetail(userId)
    }
    await getUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核认证失败:', error)
      ElMessage.error('审核认证失败')
    }
  }
}

// 打开修改角色对话框
const openRoleDialog = (user) => {
  currentUser.value = user
  newRole.value = user.role
  roleDialogVisible.value = true
}

// 确认更新角色
const confirmUpdateRole = async () => {
  if (!currentUser.value || !newRole.value) {
    ElMessage.warning('请选择新角色')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要修改用户角色吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminApi.updateUserRole(currentUser.value.id, newRole.value)
    ElMessage.success('更新用户角色成功')
    roleDialogVisible.value = false
    getUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新用户角色失败:', error)
      ElMessage.error('更新用户角色失败')
    }
  }
}

// 初始加载
getUserList()
</script>

<style scoped>
.user-management {
  padding: 20px 0;
}

.user-card {
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

.user-detail {
  padding: 10px;
}

.no-alumni-info {
  padding: 20px;
  text-align: center;
  color: #909399;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>