<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListUsers, adminPatchUserStatus, adminResetPassword, type AdminUser } from '../../../services/admin'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<AdminUser[]>([])

const role = ref<string>('')
const keyword = ref<string>('')

async function refresh() {
  loading.value = true
  try {
    const res = await adminListUsers(pageNum.value, pageSize.value, role.value || undefined, keyword.value || undefined)
    if (res.code === 0) {
      total.value = res.data.total
      list.value = res.data.list || []
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } finally {
    loading.value = false
  }
}

async function toggleStatus(u: AdminUser) {
  const next = u.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const res = await adminPatchUserStatus(u.id, next)
  if (res.code === 0) {
    ElMessage.success('已更新')
    await refresh()
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

async function onResetPassword(u: AdminUser) {
  const { value } = await ElMessageBox.prompt('输入新密码', '重置密码', {
    inputType: 'password',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
  const res = await adminResetPassword(u.id, value)
  if (res.code === 0) {
    ElMessage.success('已重置')
  } else {
    ElMessage.error(res.msg || '重置失败')
  }
}

onMounted(refresh)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="role" placeholder="角色" clearable style="width: 160px">
        <el-option label="STUDENT" value="STUDENT" />
        <el-option label="CONSULTANT" value="CONSULTANT" />
        <el-option label="ADMIN" value="ADMIN" />
      </el-select>
      <el-input v-model="keyword" placeholder="搜索 username/nickname" style="width: 240px" clearable />
      <el-button type="primary" @click="refresh" :loading="loading">查询</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="160" />
        <el-table-column prop="role" label="角色" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button size="small" type="warning" plain @click="toggleStatus(row)">
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="onResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, sizes"
          @current-change="refresh"
          @size-change="refresh"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>

