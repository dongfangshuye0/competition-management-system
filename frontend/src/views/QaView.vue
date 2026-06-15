<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Promotion } from '@element-plus/icons-vue'
import { http, type ApiRecord } from '../api/http'

const loading = ref(false)
const form = reactive({ question: '怎么报名竞赛？' })
const answer = ref<ApiRecord | null>(null)

async function ask() {
  loading.value = true
  try {
    answer.value = await http.post('/ai/qa', form)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page" v-loading="loading">
    <h2>智能答疑</h2>
    <el-card shadow="never">
      <el-form :model="form">
        <el-form-item>
          <el-input v-model="form.question" type="textarea" :rows="4" />
        </el-form-item>
        <el-button type="primary" :icon="Promotion" @click="ask">提问</el-button>
      </el-form>
    </el-card>
    <el-card v-if="answer" shadow="never">
      <p>{{ answer.answer }}</p>
      <el-tag v-for="item in answer.suggestions" :key="item" style="margin-right: 8px">{{ item }}</el-tag>
    </el-card>
  </section>
</template>
