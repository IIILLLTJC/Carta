<template>
  <section class="dashboard-grid">
    <article class="hero-panel">
      <p class="page-kicker">Phase 2</p>
      <h3>管理端核心 CRUD 已进入落地阶段</h3>
      <p>
        当前优先完成区域管理、车辆信息管理、用户管理三大模块。区域管理会率先接入真实后端接口，并作为后续联调的基础。
      </p>
    </article>

    <article class="module-card">
      <div class="module-card__head">
        <h3>优先模块</h3>
        <el-tag type="success">3 个核心模块</el-tag>
      </div>
      <div class="chip-list">
        <span class="soft-chip">区域管理</span>
        <span class="soft-chip">车辆信息管理</span>
        <span class="soft-chip">用户管理</span>
      </div>
    </article>

    <article class="module-card">
      <div class="module-card__head">
        <h3>当前建议</h3>
      </div>
      <ul class="tip-list">
        <li>先验证区域管理查询、新增、编辑、删除、状态切换全链路。</li>
        <li>随后接入车辆管理，并用区域下拉完成关联录入。</li>
        <li>最后完成用户管理与演示登录账号的数据维护。</li>
      </ul>
    </article>
  </section>
</template>