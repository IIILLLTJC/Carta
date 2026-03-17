<template>
  <section class="dashboard-grid user-dashboard-grid">
    <article class="hero-panel user-hero">
      <p class="page-kicker">User Portal</p>
      <h3>用户端基础入口已准备完成</h3>
      <p>
        当前可通过登录页进入用户端，后续将逐步接入投放点查询、下单、订单管理和归还流程。
      </p>
    </article>

    <article class="module-card">
      <div class="module-card__head">
        <h3>用户模块</h3>
        <el-tag type="success">4 个模块</el-tag>
      </div>
      <div class="chip-list">
        <span v-for="item in userMenus.slice(1)" :key="item.path" class="soft-chip">{{ item.title }}</span>
      </div>
    </article>

    <article class="module-card">
      <div class="module-card__head">
        <h3>下一步落点</h3>
      </div>
      <ul class="tip-list">
        <li>先打通车辆列表与区域可视化查询。</li>
        <li>再实现用户下单、订单详情、归还申请。</li>
        <li>最后串联管理端审核与费用结算。</li>
      </ul>
    </article>
  </section>
</template>

<script setup>
import { userMenus } from '@/constants/menus'
</script>
