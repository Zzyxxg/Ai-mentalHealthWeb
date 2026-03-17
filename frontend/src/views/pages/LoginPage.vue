<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'

import { router } from '../../router'
import { login, me, registerStudent, registerCounselor } from '../../services/auth'
import { useAuthStore } from '../../stores/auth'
import type { UserRole } from '../../types/auth'

const authStore = useAuthStore()
const route = useRoute()

const submitting = ref(false)
const loginFormRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
})

const loginRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 32, message: '长度在 4 到 32 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '长度在 6 到 32 个字符', trigger: 'blur' }
  ]
})

async function onSubmit() {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const res = await login({ username: form.username, password: form.password })
      if (res.code !== 0) {
        ElMessage.error(res.msg || '登录失败')
        return
      }
      
      // 1. 先保存 Token，确保后续 me() 请求带上 Authorization
      authStore.setToken(res.data.token)
      
      // 2. 获取真实用户信息（角色以服务端为准）
      const userRes = await me()
      if (userRes.code !== 0) {
        authStore.logout()
        ElMessage.error('获取用户信息失败')
        return
      }
      
      const userData = userRes.data
      const actualRole = userData.role as UserRole
      
      // 3. 更新 Store（先更新状态，再进行跳转）
      authStore.setMe({
        id: userData.id,
        username: userData.username,
        role: actualRole,
        nickname: userData.nickname,
        avatarUrl: userData.avatarUrl
      })
      authStore.setRole(actualRole)

      // 4. 计算跳转路径
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
      const targetPath = (actualRole === 'ADMIN' ? '/admin' : actualRole === 'CONSULTANT' ? '/counselor' : '/student')
      
      ElMessage.success('登录成功')

      // 逻辑：如果重定向地址与当前角色不匹配，则强制跳转到角色的默认首页
      let finalPath = targetPath
      if (redirect && redirect.startsWith('/')) {
        const isRedirectMatch = 
          (actualRole === 'ADMIN' && redirect.startsWith('/admin')) ||
          (actualRole === 'CONSULTANT' && redirect.startsWith('/counselor')) ||
          (actualRole === 'STUDENT' && redirect.startsWith('/student'))
        
        if (isRedirectMatch) {
          finalPath = redirect
        }
      }
      
      await router.replace(finalPath)
    } catch (e: any) {
      const msg = e?.response?.data?.msg || e?.message || '登录失败'
      ElMessage.error(msg)
      authStore.logout()
    } finally {
      submitting.value = false
    }
  })
}

// --- 注册相关逻辑 ---
const studentRegisterVisible = ref(false)
const counselorRegisterVisible = ref(false)
const registerSubmitting = ref(false)
const studentFormRef = ref<FormInstance>()
const counselorFormRef = ref<FormInstance>()

const studentForm = reactive({
  username: '',
  password: '',
  nickname: '',
  realName: '',
  studentNo: ''
})

const counselorForm = reactive({
  username: '',
  password: '',
  realName: '',
  title: '',
  expertise: '',
  intro: ''
})

const studentRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]{4,32}$/, message: '4-32位字母、数字、下划线或减号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '长度在 6 到 32 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 64, message: '最长 64 个字符', trigger: 'blur' }
  ]
})

const counselorRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]{4,32}$/, message: '4-32位字母、数字、下划线或减号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '长度在 6 到 32 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入职称', trigger: 'blur' }
  ],
  expertise: [
    { required: true, message: '请输入专长', trigger: 'blur' }
  ]
})

async function handleStudentRegister() {
  if (!studentFormRef.value) return
  
  await studentFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    registerSubmitting.value = true
    try {
      const res = await registerStudent(studentForm)
      if (res.code === 0) {
        ElMessage.success('注册成功，请登录')
        studentRegisterVisible.value = false
        form.username = studentForm.username
        form.password = ''
      } else {
        ElMessage.error(res.msg || '注册失败')
      }
    } catch (e: any) {
      const msg = e?.response?.data?.msg || e?.message || '网络错误'
      ElMessage.error(msg)
    } finally {
      registerSubmitting.value = false
    }
  })
}

async function handleCounselorRegister() {
  if (!counselorFormRef.value) return

  await counselorFormRef.value.validate(async (valid) => {
    if (!valid) return

    registerSubmitting.value = true
    try {
      const res = await registerCounselor(counselorForm)
      if (res.code === 0) {
        ElMessage.success('注册成功，请登录')
        counselorRegisterVisible.value = false
        form.username = counselorForm.username
        form.password = ''
      } else {
        ElMessage.error(res.msg || '注册失败')
      }
    } catch (e: any) {
      const msg = e?.response?.data?.msg || e?.message || '网络错误'
      ElMessage.error(msg)
    } finally {
      registerSubmitting.value = false
    }
  })
}

function openStudentRegister() {
  studentForm.username = ''
  studentForm.password = ''
  studentForm.nickname = ''
  studentForm.realName = ''
  studentForm.studentNo = ''
  studentRegisterVisible.value = true
}

function openCounselorRegister() {
  counselorForm.username = ''
  counselorForm.password = ''
  counselorForm.realName = ''
  counselorForm.title = ''
  counselorForm.expertise = ''
  counselorForm.intro = ''
  counselorRegisterVisible.value = true
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="login-header">
        <h2 class="login-title">心理健康咨询系统</h2>
        <p class="login-subtitle">请使用您的账号登录平台</p>
      </div>
      <el-form ref="loginFormRef" :model="form" :rules="loginRules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-button type="primary" :loading="submitting" class="submit-btn" @click="onSubmit">
          {{ submitting ? '登录中...' : '立即登录' }}
        </el-button>
      </el-form>
      
      <div class="register-links">
        <el-button link type="primary" @click="openStudentRegister">学生注册</el-button>
        <el-divider direction="vertical" />
        <el-button link type="primary" @click="openCounselorRegister">咨询师入驻</el-button>
      </div>
    </el-card>

    <!-- 学生注册弹窗 -->
    <el-dialog v-model="studentRegisterVisible" title="学生注册" width="400px" append-to-body destroy-on-close>
      <el-form ref="studentFormRef" :model="studentForm" :rules="studentRules" label-position="top">
        <el-form-item label="用户名 (必填)" prop="username">
          <el-input v-model="studentForm.username" placeholder="4-32位字符" />
        </el-form-item>
        <el-form-item label="密码 (必填)" prop="password">
          <el-input v-model="studentForm.password" type="password" show-password placeholder="6-32位字符" />
        </el-form-item>
        <el-form-item label="昵称 (必填)" prop="nickname">
          <el-input v-model="studentForm.nickname" placeholder="用于显示的名称" />
        </el-form-item>
        <el-form-item label="真实姓名 (选填)" prop="realName">
          <el-input v-model="studentForm.realName" placeholder="仅咨询师可见" />
        </el-form-item>
        <el-form-item label="学号 (选填)" prop="studentNo">
          <el-input v-model="studentForm.studentNo" placeholder="仅咨询师可见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="studentRegisterVisible = false">取消</el-button>
        <el-button type="primary" :loading="registerSubmitting" @click="handleStudentRegister">提交注册</el-button>
      </template>
    </el-dialog>

    <!-- 咨询师注册弹窗 -->
    <el-dialog v-model="counselorRegisterVisible" title="咨询师入驻申请" width="500px" append-to-body destroy-on-close>
      <el-form ref="counselorFormRef" :model="counselorForm" :rules="counselorRules" label-position="top">
        <el-form-item label="用户名 (必填)" prop="username">
          <el-input v-model="counselorForm.username" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="密码 (必填)" prop="password">
          <el-input v-model="counselorForm.password" type="password" show-password placeholder="登录密码" />
        </el-form-item>
        <el-form-item label="真实姓名 (必填)" prop="realName">
          <el-input v-model="counselorForm.realName" placeholder="用于展示给学生" />
        </el-form-item>
        <el-form-item label="职称/头衔 (必填)" prop="title">
          <el-input v-model="counselorForm.title" placeholder="例如：国家二级心理咨询师" />
        </el-form-item>
        <el-form-item label="擅长领域 (必填)" prop="expertise">
          <el-input v-model="counselorForm.expertise" placeholder="例如：情绪管理、人际关系" />
        </el-form-item>
        <el-form-item label="个人简介 (选填)" prop="intro">
          <el-input v-model="counselorForm.intro" type="textarea" :rows="3" placeholder="简要介绍您的从业经历..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="counselorRegisterVisible = false">取消</el-button>
        <el-button type="primary" :loading="registerSubmitting" @click="handleCounselorRegister">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
.login-card {
  width: 420px;
  padding: 10px 20px 20px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-title {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}
.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
.submit-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
  margin-top: 15px;
  border-radius: 8px;
}
.register-links {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
