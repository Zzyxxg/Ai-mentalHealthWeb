<script setup lang="ts">
import { router } from '../../router'
</script>

<template>
  <div class="page">
    <el-result icon="info" title="页面不存在" sub-title="请检查访问路径是否正确">
      <template #extra>
        <el-button type="primary" @click="router.replace('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
</style>
