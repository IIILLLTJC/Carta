<template>
  <section class="module-card">
    <div class="module-card__head">
      <div>
        <p class="page-kicker">{{ roleLabel }}</p>
        <h3>{{ title }}</h3>
      </div>
      <el-tag type="warning">阶段一占位页</el-tag>
    </div>
    <p class="module-copy">{{ description }}</p>
    <el-alert
      title="当前页面已预留路由与布局，下一阶段可直接接入表格、表单和真实接口。"
      type="info"
      :closable="false"
      show-icon
    />
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '模块页面'
  },
  description: {
    type: String,
    default: '模块说明'
  },
  roleLabel: {
    type: String,
    default: '系统'
  }
})
</script>
