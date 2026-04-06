<template>
  <div class="data-statistics">
    <el-card class="statistics-card">
      <template #header>
        <div class="card-header">
          <span>数据统计</span>
          <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="用户统计" name="users"></el-tab-pane>
            <el-tab-pane label="活动统计" name="activities"></el-tab-pane>
            <el-tab-pane label="互动统计" name="interactions"></el-tab-pane>
          </el-tabs>
        </div>
      </template>
      
      <!-- 用户统计 -->
      <div v-if="activeTab === 'users'" class="stat-content">
        <el-card class="chart-card">
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div ref="userGrowthChartRef" class="chart-container"></div>
        </el-card>
        
        <el-card class="chart-card">
          <template #header>
            <span>用户角色分布</span>
          </template>
          <div ref="userRoleChartRef" class="chart-container"></div>
        </el-card>
        
        <el-card class="chart-card">
          <template #header>
            <span>用户活跃度</span>
          </template>
          <div ref="userActivityChartRef" class="chart-container"></div>
        </el-card>
      </div>
      
      <!-- 活动统计 -->
      <div v-if="activeTab === 'activities'" class="stat-content">
        <el-card class="chart-card">
          <template #header>
            <span>活动发布趋势</span>
          </template>
          <div ref="activityGrowthChartRef" class="chart-container"></div>
        </el-card>
        
        <el-card class="chart-card">
          <template #header>
            <span>活动状态分布</span>
          </template>
          <div ref="activityStatusChartRef" class="chart-container"></div>
        </el-card>
        
        <el-card class="chart-card">
          <template #header>
            <span>活动参与人数</span>
          </template>
          <div ref="activityParticipationChartRef" class="chart-container"></div>
        </el-card>
      </div>
      
      <!-- 互动统计 -->
      <div v-if="activeTab === 'interactions'" class="stat-content">
        <el-card class="chart-card">
          <template #header>
            <span>互动增长趋势</span>
          </template>
          <div ref="interactionGrowthChartRef" class="chart-container"></div>
        </el-card>
        
        <el-card class="chart-card">
          <template #header>
            <span>互动类型分布</span>
          </template>
          <div ref="interactionTypeChartRef" class="chart-container"></div>
        </el-card>
        
        <el-card class="chart-card">
          <template #header>
            <span>热门内容排行</span>
          </template>
          <div ref="hotContentChartRef" class="chart-container"></div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import adminApi from '../../api/admin'

// 当前激活的标签
const activeTab = ref('users')

// 图表引用
const userGrowthChartRef = ref(null)
const userRoleChartRef = ref(null)
const userActivityChartRef = ref(null)
const activityGrowthChartRef = ref(null)
const activityStatusChartRef = ref(null)
const activityParticipationChartRef = ref(null)
const interactionGrowthChartRef = ref(null)
const interactionTypeChartRef = ref(null)
const hotContentChartRef = ref(null)

// 图表实例
let userGrowthChart = null
let userRoleChart = null
let userActivityChart = null
let activityGrowthChart = null
let activityStatusChart = null
let activityParticipationChart = null
let interactionGrowthChart = null
let interactionTypeChart = null
let hotContentChart = null

// 处理标签切换
const handleTabClick = (tab) => {
  if (tab.props.name === 'users') {
    initUserCharts()
  } else if (tab.props.name === 'activities') {
    initActivityCharts()
  } else {
    initInteractionCharts()
  }
}

// 初始化用户统计图表
const initUserCharts = async () => {
  try {
    const response = await adminApi.getUserStatistics()
    const userData = response.data
    
    // 用户增长趋势
    initUserGrowthChart(userData.growthTrend || [])
    // 用户角色分布
    initUserRoleChart(userData.roleDistribution || {})
    // 用户活跃度
    initUserActivityChart(userData.activity || {})
  } catch (error) {
    console.error('获取用户统计数据失败:', error)
    // 使用模拟数据
    initUserGrowthChart([
      { date: '1月', count: 100 },
      { date: '2月', count: 150 },
      { date: '3月', count: 200 },
      { date: '4月', count: 250 },
      { date: '5月', count: 300 },
      { date: '6月', count: 350 }
    ])
    initUserRoleChart({ ADMIN: 5, USER: 995 })
    initUserActivityChart({ active: 600, inactive: 400 })
  }
}

// 初始化活动统计图表
const initActivityCharts = async () => {
  try {
    const response = await adminApi.getActivityStatistics()
    const activityData = response.data
    
    // 活动发布趋势
    initActivityGrowthChart(activityData.growthTrend || [])
    // 活动状态分布
    initActivityStatusChart(activityData.statusDistribution || {})
    // 活动参与人数
    initActivityParticipationChart(activityData.participation || [])
  } catch (error) {
    console.error('获取活动统计数据失败:', error)
    // 使用模拟数据
    initActivityGrowthChart([
      { date: '1月', count: 10 },
      { date: '2月', count: 15 },
      { date: '3月', count: 20 },
      { date: '4月', count: 25 },
      { date: '5月', count: 30 },
      { date: '6月', count: 35 }
    ])
    initActivityStatusChart({ draft: 5, published: 10, ongoing: 20, ended: 15 })
    initActivityParticipationChart([
      { name: '校友聚会', count: 50 },
      { name: '招聘会', count: 120 },
      { name: '学术讲座', count: 80 },
      { name: '体育活动', count: 60 },
      { name: '文化活动', count: 40 }
    ])
  }
}

// 初始化互动统计图表
const initInteractionCharts = async () => {
  try {
    const response = await adminApi.getInteractionStatistics()
    const interactionData = response.data
    
    // 互动增长趋势
    initInteractionGrowthChart(interactionData.growthTrend || [])
    // 互动类型分布
    initInteractionTypeChart(interactionData.typeDistribution || {})
    // 热门内容排行
    initHotContentChart(interactionData.hotContent || [])
  } catch (error) {
    console.error('获取互动统计数据失败:', error)
    // 使用模拟数据
    initInteractionGrowthChart([
      { date: '1月', count: 500 },
      { date: '2月', count: 650 },
      { date: '3月', count: 800 },
      { date: '4月', count: 950 },
      { date: '5月', count: 1100 },
      { date: '6月', count: 1250 }
    ])
    initInteractionTypeChart({ like: 500, comment: 300, share: 200 })
    initHotContentChart([
      { name: '校友聚会照片', count: 150 },
      { name: '职场经验分享', count: 120 },
      { name: '招聘信息', count: 100 },
      { name: '校园回忆', count: 80 },
      { name: '行业动态', count: 60 }
    ])
  }
}

// 用户增长趋势图表
const initUserGrowthChart = (data) => {
  if (userGrowthChart) {
    userGrowthChart.dispose()
  }
  userGrowthChart = echarts.init(userGrowthChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: data.map(item => item.count),
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(24, 144, 255, 0.5)' },
          { offset: 1, color: 'rgba(24, 144, 255, 0.1)' }
        ])
      }
    }]
  }
  userGrowthChart.setOption(option)
}

// 用户角色分布图表
const initUserRoleChart = (data) => {
  if (userRoleChart) {
    userRoleChart.dispose()
  }
  userRoleChart = echarts.init(userRoleChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: data.ADMIN || 0, name: '管理员' },
        { value: data.ALUMNI || 0, name: '校友' },
        { value: data.USER || 0, name: '普通用户' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  userRoleChart.setOption(option)
}

// 用户活跃度图表
const initUserActivityChart = (data) => {
  if (userActivityChart) {
    userActivityChart.dispose()
  }
  userActivityChart = echarts.init(userActivityChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: data.active || 0, name: '活跃用户' },
        { value: data.inactive || 0, name: '非活跃用户' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  userActivityChart.setOption(option)
}

// 活动发布趋势图表
const initActivityGrowthChart = (data) => {
  if (activityGrowthChart) {
    activityGrowthChart.dispose()
  }
  activityGrowthChart = echarts.init(activityGrowthChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: data.map(item => item.count),
      type: 'bar',
      itemStyle: {
        color: '#5470c6'
      }
    }]
  }
  activityGrowthChart.setOption(option)
}

// 活动状态分布图表
const initActivityStatusChart = (data) => {
  if (activityStatusChart) {
    activityStatusChart.dispose()
  }
  activityStatusChart = echarts.init(activityStatusChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: data.draft || 0, name: '草稿' },
        { value: data.published || 0, name: '已发布' },
        { value: data.ongoing || 0, name: '进行中' },
        { value: data.ended || 0, name: '已结束' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  activityStatusChart.setOption(option)
}

// 活动参与人数图表
const initActivityParticipationChart = (data) => {
  if (activityParticipationChart) {
    activityParticipationChart.dispose()
  }
  activityParticipationChart = echarts.init(activityParticipationChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.name),
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: data.map(item => item.count),
      type: 'bar',
      itemStyle: {
        color: '#91cc75'
      }
    }]
  }
  activityParticipationChart.setOption(option)
}

// 互动增长趋势图表
const initInteractionGrowthChart = (data) => {
  if (interactionGrowthChart) {
    interactionGrowthChart.dispose()
  }
  interactionGrowthChart = echarts.init(interactionGrowthChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: data.map(item => item.count),
      type: 'line',
      smooth: true,
      itemStyle: {
        color: '#ee6666'
      }
    }]
  }
  interactionGrowthChart.setOption(option)
}

// 互动类型分布图表
const initInteractionTypeChart = (data) => {
  if (interactionTypeChart) {
    interactionTypeChart.dispose()
  }
  interactionTypeChart = echarts.init(interactionTypeChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: data.like || 0, name: '点赞' },
        { value: data.comment || 0, name: '评论' },
        { value: data.share || 0, name: '分享' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  interactionTypeChart.setOption(option)
}

// 热门内容排行图表
const initHotContentChart = (data) => {
  if (hotContentChart) {
    hotContentChart.dispose()
  }
  hotContentChart = echarts.init(hotContentChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.name),
      axisLabel: {
        interval: 0
      }
    },
    series: [{
      data: data.map(item => item.count),
      type: 'bar',
      itemStyle: {
        color: '#fac858'
      }
    }]
  }
  hotContentChart.setOption(option)
}

// 监听窗口大小变化
const handleResize = () => {
  userGrowthChart?.resize()
  userRoleChart?.resize()
  userActivityChart?.resize()
  activityGrowthChart?.resize()
  activityStatusChart?.resize()
  activityParticipationChart?.resize()
  interactionGrowthChart?.resize()
  interactionTypeChart?.resize()
  hotContentChart?.resize()
}

// 页面挂载
onMounted(() => {
  initUserCharts()
  window.addEventListener('resize', handleResize)
})

// 页面卸载
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  // 销毁图表实例
  userGrowthChart?.dispose()
  userRoleChart?.dispose()
  userActivityChart?.dispose()
  activityGrowthChart?.dispose()
  activityStatusChart?.dispose()
  activityParticipationChart?.dispose()
  interactionGrowthChart?.dispose()
  interactionTypeChart?.dispose()
  hotContentChart?.dispose()
})
</script>

<style scoped>
.data-statistics {
  padding: 20px 0;
}

.statistics-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-content {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>