<template>
  <section class="dashboard-grid user-dashboard-grid">
    <article class="hero-panel user-hero">
      <p class="page-kicker">User Portal</p>
      <h3>用户端真实业务已开始落地</h3>
      <p>
        当前阶段会逐步打通车辆投放地点、我的订单、归还申请和个人中心，让用户端能够基于当前登录用户独立完成查看和操作。
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
        <h3>当前进度</h3>
      </div>
      <ul class="tip-list">
        <li>先完成投放地点浏览，确认用户可以看到可租车辆。</li>
        <li>再接我的订单，让 user01 只看到自己的订单记录。</li>
        <li>随后接归还申请和个人中心资料维护。</li>
      </ul>
    </article>
  </section>
</template>

<script setup>
import { userMenus } from '@/constants/menus'
</script>