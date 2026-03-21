(function () {
  var pageRoot = document.getElementById('page-root');
  var pages = window.PrototypePages || {};

  if (pageRoot) {
    pageRoot.innerHTML = Object.keys(pages).map(function (key) {
      return pages[key];
    }).join('\n\n');
  }

// Generate dashboard chart bars
  const heights = [40,55,35,70,85,60,90,75,65,80,95,70,55,85,100,75,60,80,70,65,90,85,75,60,50,70,80,90];
  const chart = document.getElementById('chartArea');
  if (chart) {
    heights.forEach(h => {
      const bar = document.createElement('div');
      bar.className = 'chart-bar';
      bar.style.height = h + '%';
      chart.appendChild(bar);
    });
  }

  // Generate AI capability usage chart bars
  const aiCapHeights = [72, 78, 85, 80, 92, 88];
  const aiCapChart = document.getElementById('aiCapChart');
  if (aiCapChart) {
    aiCapHeights.forEach(h => {
      const bar = document.createElement('div');
      bar.className = 'chart-bar';
      bar.style.height = h + '%';
      bar.style.background = '#12B76A';
      aiCapChart.appendChild(bar);
    });
  }

  let currentServiceKey = 'backend';

  function escapeHtml(text) {
    return String(text || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function normalizeText(text) {
    return String(text || '').replace(/\s+/g, ' ').trim();
  }

  function showToast(message, type = 'info') {
    const stack = document.getElementById('toastStack');
    if (!stack) return;
    const toast = document.createElement('div');
    toast.className = 'toast' + (type === 'success' ? ' success' : '');
    toast.textContent = message;
    stack.appendChild(toast);
    requestAnimationFrame(() => toast.classList.add('show'));
    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => toast.remove(), 220);
    }, 2200);
  }

  function openActionPreview(title, desc, items = []) {
    const titleEl = document.getElementById('actionPreviewTitle');
    const descEl = document.getElementById('actionPreviewDesc');
    const listEl = document.getElementById('actionPreviewList');
    if (!titleEl || !descEl || !listEl) return;
    titleEl.textContent = title || '交互预览';
    descEl.textContent = desc || '这里展示原型态的详情说明与下一步动作。';
    listEl.innerHTML = items.map(item => `<div class="modal-list-item">${escapeHtml(item)}</div>`).join('');
    openModal('actionPreview');
  }

  function getContextName(source) {
    const row = source && source.closest ? source.closest('tr') : null;
    if (row) {
      const firstCell = row.querySelector('td');
      if (firstCell) return normalizeText(firstCell.textContent);
    }
    const card = source && source.closest ? source.closest('.card, .project-card, .service-card, .atomic-card, .env-card, .skill-item') : null;
    if (card) {
      const titleEl = card.querySelector('.card-title, .project-name, .atomic-name, .skill-name, .mcp-title');
      if (titleEl) return normalizeText(titleEl.textContent);
    }
    return '当前内容';
  }

  function inferServiceKey(source) {
    const serviceCard = source && source.closest ? source.closest('.service-card') : null;
    if (!serviceCard) return currentServiceKey || 'backend';
    const inline = serviceCard.getAttribute('onclick') || '';
    const match = inline.match(/showServiceDetail\('([^']+)'\)/);
    return match ? match[1] : (currentServiceKey || 'backend');
  }

  // ── 全局页面导航 ──
  function showPage(name) {
    // 切换回全局 sidebar（如果在项目内）
    document.getElementById('sidebar-global').style.display = 'flex';
    document.getElementById('sidebar-project').style.display = 'none';

    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('#sidebar-global .nav-item').forEach(n => n.classList.remove('active'));
    const nextPage = document.getElementById('page-' + name);
    if (!nextPage) {
      showToast('该页面原型仍在补充中');
      return;
    }
    nextPage.classList.add('active');
    const titles = {
      dashboard:'工作台', efficiency:'用量看板',
      'my-credential':'我的凭证', 'my-ability':'我的能力', 'my-usage':'我的用量',
      projects:'项目空间', mcp:'MCP 接入', incidents:'事故中心',
      cicd:'CI / CD', envs:'环境管理',
      atomic:'原子能力中心', knowledge:'全局知识库',
      skills:'全局技能库', templates:'代码模板库',
      'global-tools':'全局工具集', integrations:'集成市场',
      staff:'员工管理', keys:'凭证管理', permissions:'权限管理', audit:'审计安全', settings:'设置',
      workflows:'Agent 工作流', functions:'工具注册中心',
      'ai-monitor':'AI 执行监控', evals:'AI 评估中心'
    };
    document.getElementById('topbar-title').textContent = titles[name] || name;
    document.querySelectorAll('#sidebar-global .nav-item').forEach(item => {
      if(item.getAttribute('onclick') && item.getAttribute('onclick').includes("'"+name+"'")) {
        item.classList.add('active');
      }
    });
  }

  // ── 进入项目 ──
  function enterProject(icon, name, status) {
    document.getElementById('sidebar-global').style.display = 'none';
    document.getElementById('sidebar-project').style.display = 'flex';
    document.getElementById('proj-icon').textContent = icon;
    document.getElementById('proj-name').textContent = name;
    document.getElementById('proj-status').textContent = status;
    showProjectPage('overview');
  }

  // ── 退出项目 ──
  function exitProject() {
    document.getElementById('sidebar-global').style.display = 'flex';
    document.getElementById('sidebar-project').style.display = 'none';
    showPage('projects');
  }

  // ── 项目内部页面导航 ──
  const projPageTitles = {
    overview:'概览', knowledge:'知识库',
    incidents:'事故与告警', services:'代码服务', members:'成员权限',
    'ai-cap':'AI 能力', keymanagement:'配额管理', psettings:'项目设置',
    workspace:'接入与凭证'
  };

  function showProjectPage(name) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('#sidebar-project .nav-item').forEach(n => n.classList.remove('active'));
    const pageId = name === 'agile' ? 'page-proj-agile'
                 : name === 'ai-cap' ? 'page-proj-ai-cap'
                 : name === 'keymanagement' ? 'page-proj-keymanagement'
                 : name === 'workspace' ? 'page-proj-workspace'
                 : 'page-proj-' + name;
    const el = document.getElementById(pageId);
    if(!el) {
      showToast('该项目页面原型仍在补充中');
      return;
    }
    el.classList.add('active');
    document.getElementById('topbar-title').textContent =
            document.getElementById('proj-name').textContent + ' · ' + (projPageTitles[name] || name);
    const nav = document.getElementById('pnav-' + name);
    if(nav) nav.classList.add('active');
  }

  // ── 服务详情页 ──
  const svcData = {
    backend:  { icon:'🍃', name:'mall-backend',  tech:'Spring Boot 3' },
    frontend: { icon:'⚛️', name:'mall-frontend', tech:'React 18' },
    mobile:   { icon:'📱', name:'mall-mobile',   tech:'React Native' }
  };

  function showServiceDetail(svcKey) {
    currentServiceKey = svcKey;
    const svc = svcData[svcKey] || { icon:'⚙️', name:svcKey, tech:'' };
    document.getElementById('svc-icon').textContent = svc.icon;
    document.getElementById('svc-name').textContent = svc.name;
    document.getElementById('svc-tech').textContent = svc.tech;
    // 重置 tabs
    const tabs = document.querySelectorAll('#svcTabs .tab');
    tabs.forEach(t => t.classList.remove('active'));
    if(tabs[0]) tabs[0].classList.add('active');
    document.querySelectorAll('[id^="svc-"]').forEach(s => s.style.display = 'none');
    const ov = document.getElementById('svc-overview');
    if(ov) ov.style.display = '';
    // 显示服务详情页
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById('page-svc-detail').classList.add('active');
    document.getElementById('topbar-title').textContent =
            document.getElementById('proj-name').textContent + ' · ' + svc.name;
    document.querySelectorAll('#sidebar-project .nav-item').forEach(n => n.classList.remove('active'));
    const pnav = document.getElementById('pnav-services');
    if(pnav) pnav.classList.add('active');
  }

  function switchTab(groupId, showId, triggerEl) {
    const group = document.getElementById(groupId);
    if (!group) return;
    const tabs = group.querySelectorAll('.tab');
    tabs.forEach(tab => tab.classList.remove('active'));
    const activeTab = triggerEl || (typeof event !== 'undefined' ? event.target : null);
    if (activeTab && activeTab.classList) activeTab.classList.add('active');
    const prefix = showId.split('-')[0] + '-';
    const parent = group.parentElement;
    parent.querySelectorAll('[id^="' + prefix + '"]').forEach(s => s.style.display = 'none');
    const target = document.getElementById(showId);
    if(target) target.style.display = '';
  }

  function openModal(name) {
    const modal = document.getElementById('modal-'+name);
    if (!modal) {
      showToast('该弹窗原型仍在补充中');
      return;
    }
    if (name === 'newProject') {
      initProjectCreateForm();
    }
    modal.classList.add('open');
  }
  function closeModal(name) {
    const modal = document.getElementById('modal-'+name);
    if (modal) modal.classList.remove('open');
  }
  document.querySelectorAll('.modal').forEach(m => {
    m.addEventListener('click', e => { if(e.target === m) m.classList.remove('open'); });
  });

  function getProjectCreateSource() {
    return {
      members: [
        { id: 'member-lisi', name: '李四', meta: '后端开发 · li.si@company.com' },
        { id: 'member-wangwu', name: '王五', meta: '前端开发 · wang.wu@company.com' },
        { id: 'member-zhaoqi', name: '赵七', meta: '产品经理 · zhao.qi@company.com' },
        { id: 'member-liting', name: '李婷', meta: '测试工程师 · li.ting@company.com' },
        { id: 'member-lizhe', name: '李哲', meta: '产品经理 · li.zhe@company.com' },
        { id: 'member-sunba', name: '孙八', meta: '客户端开发 · sun.ba@company.com' }
      ],
      knowledge: [
        { id: 'kb-code', name: '公司代码规范', meta: '全局知识库 · 自动注入摘要' },
        { id: 'kb-security', name: '安全开发手册', meta: '全局知识库 · 安全审查推荐' },
        { id: 'kb-ui', name: 'UI 设计规范', meta: '全局知识库 · 前端项目常用' },
        { id: 'kb-arch', name: '微服务架构指南', meta: '全局知识库 · 平台项目常用' },
        { id: 'kb-payment', name: '支付领域词典', meta: '项目知识资产模板' }
      ],
      skills: [
        { id: 'skill-review', name: '代码审查', meta: '企业通用 · /code-review' },
        { id: 'skill-bug', name: 'Bug 分析', meta: '企业通用 · /bug-analysis' },
        { id: 'skill-doc', name: '文档生成', meta: '企业通用 · /doc-gen' },
        { id: 'skill-prd', name: '需求拆解', meta: '企业通用 · /prd-split' },
        { id: 'skill-test', name: '测试用例生成', meta: '企业通用 · /test-gen' }
      ]
    };
  }

  function getProjectCreateDefaults() {
    return {
      members: ['member-lisi', 'member-wangwu', 'member-zhaoqi'],
      knowledge: ['kb-code', 'kb-security'],
      skills: ['skill-review']
    };
  }

  var projectCreateState = null;

  function initProjectCreateForm() {
    const defaults = getProjectCreateDefaults();
    projectCreateState = {
      members: [...defaults.members],
      knowledge: [...defaults.knowledge],
      skills: [...defaults.skills]
    };

    const memberInput = document.getElementById('create-members-input');
    const knowledgeInput = document.getElementById('create-knowledge-input');
    const skillsInput = document.getElementById('create-skills-input');
    if (memberInput) memberInput.value = '';
    if (knowledgeInput) knowledgeInput.value = '';
    if (skillsInput) skillsInput.value = '';

    ['members', 'knowledge', 'skills'].forEach(renderProjectCreatePicker);
  }

  function getProjectCreateInput(type) {
    return document.getElementById(`create-${type}-input`);
  }

  function getProjectCreatePanel(type) {
    return document.getElementById(`create-${type}-options`);
  }

  function getProjectCreateChips(type) {
    return document.getElementById(`create-${type}-chips`);
  }

  function getProjectCreateSelected(type) {
    if (!projectCreateState) initProjectCreateForm();
    return projectCreateState[type] || [];
  }

  function renderProjectCreatePicker(type) {
    const input = getProjectCreateInput(type);
    const panel = getProjectCreatePanel(type);
    const chips = getProjectCreateChips(type);
    if (!input || !panel || !chips) return;

    const keyword = normalizeText(input.value).toLowerCase();
    const selectedIds = getProjectCreateSelected(type);
    const selectedSet = new Set(selectedIds);
    const options = getProjectCreateSource()[type] || [];
    const filtered = options.filter(option => {
      if (!keyword) return true;
      return `${option.name} ${option.meta}`.toLowerCase().includes(keyword);
    });

    panel.innerHTML = filtered.length
      ? filtered.map(option => `
          <div class="picker-option ${selectedSet.has(option.id) ? 'is-selected' : ''}" data-picker-type="${type}" data-picker-id="${option.id}">
            <div class="picker-main">
              <div class="picker-name">${escapeHtml(option.name)}</div>
              <div class="picker-meta">${escapeHtml(option.meta)}</div>
            </div>
            <button type="button" class="picker-action ${selectedSet.has(option.id) ? 'is-selected' : ''}" data-picker-type="${type}" data-picker-id="${option.id}" data-picker-action="${selectedSet.has(option.id) ? 'remove' : 'add'}">
              ${selectedSet.has(option.id) ? '已添加' : '添加'}
            </button>
          </div>
        `).join('')
      : `<div class="picker-empty">没有匹配结果，换个关键词试试</div>`;

    chips.innerHTML = selectedIds.length
      ? selectedIds.map(id => {
          const option = options.find(item => item.id === id);
          if (!option) return '';
          return `
            <span class="picker-chip">
              ${escapeHtml(option.name)}
              <button type="button" class="picker-chip-remove" data-picker-type="${type}" data-picker-id="${id}" data-picker-action="remove" aria-label="移除 ${escapeHtml(option.name)}">×</button>
            </span>
          `;
        }).join('')
      : `<span style="font-size:12px;color:var(--sub)">暂未选择</span>`;
  }

  function updateProjectCreateSelection(type, id, action) {
    if (!projectCreateState || !projectCreateState[type]) return;
    const selected = projectCreateState[type];
    const has = selected.includes(id);
    if (action === 'add' && !has) selected.push(id);
    if (action === 'remove' && has) projectCreateState[type] = selected.filter(item => item !== id);
    renderProjectCreatePicker(type);
  }

  function bindProjectCreateInteractions() {
    ['members', 'knowledge', 'skills'].forEach(type => {
      const input = getProjectCreateInput(type);
      if (!input) return;

      input.addEventListener('input', () => renderProjectCreatePicker(type));
      input.addEventListener('focus', () => renderProjectCreatePicker(type));
      input.addEventListener('keydown', event => {
        if (event.key !== 'Enter') return;
        event.preventDefault();
        const keyword = normalizeText(input.value).toLowerCase();
        const firstMatch = (getProjectCreateSource()[type] || []).find(option => {
          const matched = !keyword || `${option.name} ${option.meta}`.toLowerCase().includes(keyword);
          return matched && !getProjectCreateSelected(type).includes(option.id);
        });
        if (firstMatch) {
          updateProjectCreateSelection(type, firstMatch.id, 'add');
          input.value = '';
          renderProjectCreatePicker(type);
        }
      });
    });

    document.addEventListener('click', event => {
      const actionTarget = event.target.closest('[data-picker-action]');
      if (actionTarget) {
        const { pickerType, pickerId, pickerAction } = actionTarget.dataset;
        if (pickerType && pickerId && pickerAction) {
          event.preventDefault();
          event.stopPropagation();
          updateProjectCreateSelection(pickerType, pickerId, pickerAction);
          return;
        }
      }

      const optionTarget = event.target.closest('.picker-option[data-picker-id]');
      if (optionTarget) {
        const { pickerType, pickerId } = optionTarget.dataset;
        if (pickerType && pickerId) {
          const selected = getProjectCreateSelected(pickerType);
          updateProjectCreateSelection(pickerType, pickerId, selected.includes(pickerId) ? 'remove' : 'add');
        }
      }
    });
  }

  // ── Agent Workflow Detail ──
  const wfMeta = {
    review: { title:'🔍 代码 Review Agent · 流程图', count:'87 次', rate:'96.6%', time:'18s', token:'3,200' },
    bugfix: { title:'🐛 Bug 诊断修复 Agent · 流程图', count:'12 次', rate:'91.7%', time:'68s', token:'7,800' },
    prd:    { title:'📋 需求拆分 Agent · 流程图', count:'5 次', rate:'100%', time:'45s', token:'9,100' }
  };
  function showWorkflowDetail(key) {
    const meta = wfMeta[key];
    if (!meta) return;
    document.getElementById('wf-detail-title').textContent = meta.title;
    ['review','bugfix','prd'].forEach(k => {
      const el = document.getElementById('wf-canvas-' + k);
      if (el) el.style.display = (k === key) ? '' : 'none';
    });
    document.getElementById('wf-stat-count').textContent = meta.count;
    document.getElementById('wf-stat-rate').textContent = meta.rate;
    document.getElementById('wf-stat-time').textContent = meta.time;
    document.getElementById('wf-stat-token').textContent = meta.token;
  }

  function handlePrototypeButton(btn) {
    const label = normalizeText(btn.textContent);
    if (!label) return false;
    const page = btn.closest('.page');
    const pageId = page ? page.id : '';
    const isInProject = document.getElementById('sidebar-project').style.display !== 'none';

    if (label.includes('创建项目')) {
      closeModal('newProject');
      showToast('已创建项目空间（原型演示）', 'success');
      enterProject('🧩', '新项目空间', '0 个能力已配置 · 待接入');
      return true;
    }

    if (label.includes('创建技能') || label.includes('新建技能')) {
      openModal('createSkill');
      return true;
    }

    if (label.includes('使用此模板创建')) {
      closeModal('templateDetail');
      openModal('addService');
      return true;
    }

    if (label.includes('使用模板')) {
      openModal('addService');
      return true;
    }

    if (label.includes('上传自定义模板')) {
      openModal('uploadTemplate');
      return true;
    }

    if (label.includes('导入并发布')) {
      closeModal('uploadTemplate');
      showToast('模板正在导入中，扫描完成后自动发布', 'success');
      return true;
    }

    if (label.includes('编辑模板')) {
      openModal('editTemplate');
      return true;
    }

    if (label.includes('同步最新版本')) {
      showToast('正在从源仓库同步最新代码...', 'info');
      return true;
    }

    if (label.includes('归档模板')) {
      openActionPreview('归档模板', '将模板标记为归档状态', [
        '归档后不再出现在模板选择列表中',
        '已使用此模板创建的服务不受影响',
        '可随时取消归档恢复可用'
      ]);
      return true;
    }

    if (label.includes('上传文档')) {
      openModal('uploadDoc');
      return true;
    }

    if (label.includes('RAG Pipeline') || label.includes('RAG') && label.includes('配置')) {
      openModal('ragConfig');
      return true;
    }

    if (label.includes('新增告警') || label.includes('添加规则') || (label.includes('告警') && label.includes('规则'))) {
      openModal('alertRule');
      return true;
    }

    if (label.includes('创建服务账号')) {
      openModal('serviceAccount');
      return true;
    }

    if (label.includes('创建临时凭证')) {
      openActionPreview('创建临时凭证', '为临时协作或体验创建 24 小时有效的临时凭证', [
        '凭证类型：临时凭证（24h 自动过期）',
        '权限：只读、单项目',
        '无需审批，立即生效',
        '到期自动吊销，不可续签'
      ]);
      return true;
    }

    if (label.includes('审核流程')) {
      openActionPreview('技能审核流程', '技能发布需经过完整的审核流程', [
        '📝 草稿：创建者编辑 Prompt 模板和配置',
        '🧪 测试：在线试运行验证效果',
        '🔍 审核中：提交审核，等待管理员审批',
        '✅ 已发布：通过审核，全员可用',
        '🔄 可回滚：支持版本回退到任意历史版本',
        '📊 反馈收集：用户可👍/👎评价技能效果'
      ]);
      return true;
    }

    if (label.includes('管理继承')) {
      openActionPreview('继承关系管理', '配置项目对全局知识库的继承规则', [
        '✅ 公司制度与行为规范 (12篇) → 继承中',
        '✅ 技术规范与编码标准 (15篇) → 继承中',
        '✅ 产品线文档 (11篇) → 继承中',
        '❌ 安全合规文档 (10篇) → 已关闭',
        '检索权重：项目文档 0.7 / 全局文档 0.3',
        '点击可单独开启/关闭某个类别的继承'
      ]);
      return true;
    }

    if (label.includes('批量操作')) {
      openActionPreview('批量操作', '选择要执行的批量操作', [
        '批量生成凭证：为所有未配置成员一键生成凭证',
        '批量发送接入指南：通知成员完成工具配置',
        '批量续签凭证：为即将过期成员续签凭证',
        '批量调整角色：统一修改选中成员角色',
        '导出成员列表：导出 CSV 含接入状态'
      ]);
      return true;
    }

    if (label.includes('保存并同步')) {
      showToast('System Prompt 已更新并同步至代理层', 'success');
      return true;
    }

    if (label.includes('测试注入')) {
      openActionPreview('Prompt 注入测试', '测试 System Prompt 注入效果', [
        '发送测试请求至代理层',
        '验证注入的 System Prompt 内容',
        '检查响应是否符合项目规范',
        '查看注入后的 Token 消耗'
      ]);
      return true;
    }

    if (label.includes('测试检索')) {
      showToast('检索效果测试中...', 'info');
      return true;
    }

    if (label.includes('克隆')) {
      const name = getContextName(btn);
      openActionPreview('克隆技能 · ' + name, '基于此技能创建项目定制版本', [
        '复制原始 Prompt 模板和配置',
        '允许修改参数和关联知识库',
        '克隆后为项目级技能（独立维护）',
        '原技能更新不会自动同步'
      ]);
      return true;
    }

    if (label.includes('注册新工具') || (label.includes('注册') && label.includes('工具'))) {
      openModal('registerTool');
      return true;
    }

    if (label.includes('发布新能力') || (label.includes('发布') && label.includes('能力'))) {
      openModal('publishAtomic');
      return true;
    }

    if (label.includes('接入自建') || label.includes('接入自建 MCP')) {
      openModal('createIntegration');
      return true;
    }

    if (label.includes('邀请员工')) {
      openModal('inviteStaff');
      return true;
    }

    if (label.includes('重发邀请')) {
      showToast('已重新发送激活邮件', 'success');
      return true;
    }

    if (label.includes('添加成员') && !label.includes('分发')) {
      if (isInProject || pageId.includes('proj-members')) {
        openModal('addMember');
        return true;
      }
    }

    if (label.includes('为成员生成凭证')) {
      openModal('addMember');
      return true;
    }

    if (label.includes('改角色') || (label.includes('修改') && label.includes('角色'))) {
      openModal('changeRole');
      return true;
    }

    if (label.includes('申请权限')) {
      showToast('已发送权限申请，等待管理员审批', 'success');
      return true;
    }

    if (label.includes('重置为默认')) {
      openActionPreview('重置为默认模板', '将本项目权限恢复为平台默认配置', [
        '所有角色权限将恢复到平台模板默认值',
        '已有的项目级自定义配置将丢失',
        '成员的能力范围会实时调整',
        '此操作不可撤回'
      ]);
      return true;
    }

    if (label.includes('编辑能力范围')) {
      const name = getContextName(btn);
      openActionPreview('编辑角色能力范围', '调整角色在本项目中可使用的具体 AI 能力', [
        '选择可访问的知识库列表',
        '选择可调用的技能列表',
        '选择可使用的工具列表',
        '设置默认 Token 配额'
      ]);
      return true;
    }

    if (label.includes('编辑') && btn.closest('[id*="perm"]')) {
      openActionPreview('编辑角色模板', '修改角色的默认权限配置', [
        '调整各模块的权限等级',
        '设置默认 Token 配额',
        '配置可访问的 AI 能力范围',
        '修改后仅影响新建项目'
      ]);
      return true;
    }

    if (label.includes('进入服务详情')) {
      showServiceDetail(inferServiceKey(btn));
      return true;
    }

    if (label.includes('前往 AI 能力')) {
      showProjectPage('ai-cap');
      return true;
    }

    if (label.includes('去接入指南')) {
      showPage('my-credential');
      return true;
    }

    if (label.includes('新建Sprint') || label.includes('新建 Sprint')) {
      showToast('Sprint 管理请前往 Jira / Linear 等项目管理工具', 'info');
      return true;
    }

    if (label.includes('检索测试') || label.includes('执行检索')) {
      openActionPreview('检索测试', '查看召回片段、命中知识源和重排序结果，便于验证 RAG 效果。', [
        '命中需求文档 3 段（相关度 95%）',
        '命中接口文档 2 段（相关度 88%）',
        '命中代码规范 1 段（相关度 72%）',
        '综合相关度 92%'
      ]);
      return true;
    }

    if (label.includes('查看日志') || label === '日志') {
      const serviceKey = inferServiceKey(btn);
      const serviceName = (svcData[serviceKey] || {}).name || getContextName(btn);
      openActionPreview('运行日志 · ' + serviceName, '查看服务的构建、部署和运行日志。', [
        '最近一次构建耗时 6 分 12 秒',
        '单测通过率 98%（196/200）',
        '代码覆盖率 82%',
        '最近一次部署完成于 10 分钟前'
      ]);
      return true;
    }

    if (label.includes('发起部署')) {
      openActionPreview('部署确认', '选择部署环境并确认部署。', [
        'DEV 环境 → 自动部署，无需审批',
        'PRE 环境 → 自动部署，通知负责人',
        'PROD 环境 → 需项目负责人审批后执行',
        '部署后自动运行冒烟测试并回写状态'
      ]);
      showToast('已模拟发起部署', 'success');
      return true;
    }

    if (label.includes('导出月报') || label.includes('导出')) {
      showToast('正在生成报告，下载将自动开始...', 'success');
      return true;
    }

    if (label.includes('生成月度效能报告') || label.includes('效能报告')) {
      openActionPreview('月度效能报告', '系统正在汇总数据，生成包含以下内容的效能报告。', [
        'AI 使用效率指标（覆盖率、采纳率、ROI）',
        'DORA 四项指标趋势',
        '按项目维度的 Token 消耗排行',
        '按团队维度的 AI 能力使用热力图',
        '优化建议与下月目标'
      ]);
      showToast('报告生成中...', 'success');
      return true;
    }

    if (label.includes('重新检测') || label.includes('检测所有连接')) {
      showToast('正在检测所有连接状态...', 'info');
      setTimeout(() => showToast('所有连接检测完成，4/4 正常', 'success'), 1200);
      return true;
    }

    if (label.includes('生成一键安装')) {
      openActionPreview('一键安装链接', '已生成包含凭证的安装链接，复制发送给成员即可完成配置。', [
        '链接有效期 24 小时，单次使用',
        '安装脚本自动配置环境变量 + MCP',
        '执行后约 30 秒完成全部接入'
      ]);
      return true;
    }

    if (label.includes('续签凭证')) {
      openActionPreview('凭证续签', '选择续签时长，系统自动延长凭证有效期。', [
        '续签 30 天 → 新到期日 2026-07-19',
        '续签 90 天 → 新到期日 2026-09-19',
        '续签 180 天 → 新到期日 2026-12-19'
      ]);
      return true;
    }

    if (label.includes('查看') || label.includes('详情') || label.includes('查看文档') || label.includes('查看追踪') || label.includes('查看 PR') || label.includes('查看相关代码') || label.includes('历史凭证')) {
      const name = getContextName(btn);
      openActionPreview('详情预览 · ' + name, '查看完整信息和关联状态。', [
        '基础信息与状态详情',
        '关联的操作记录与时间线',
        '支持继续跳转到相关页面'
      ]);
      return true;
    }

    if (label.includes('接入') || label.includes('配置') || label.includes('测试连通性') || label.includes('保存并启用')) {
      const name = getContextName(btn);
      openActionPreview('接入配置 · ' + name, '完成认证、权限和工具启用。', [
        '支持 OAuth / Token / Bearer Token / API Key',
        '支持按工具粒度控制权限',
        '支持连通性测试与一键启停',
        '配置变更自动记录审计日志'
      ]);
      return true;
    }

    if (label.includes('显示') && label.includes('复制')) {
      showToast('plt_uid_zhang3_8f2a91c4d7e6b350（已复制到剪贴板）', 'success');
      return true;
    }

    if (label.includes('复制')) {
      showToast('已复制到剪贴板', 'success');
      return true;
    }

    if (label.includes('下载')) {
      showToast('文件下载中...', 'success');
      return true;
    }

    if (label.includes('编辑')) {
      const name = getContextName(btn);
      openActionPreview('编辑 · ' + name, '修改配置后点击保存生效。', [
        '支持修改基础信息与配置',
        '变更历史自动记录',
        '关键变更需审批后生效'
      ]);
      return true;
    }

    if (label.includes('测试') && !label.includes('连通') && !label.includes('检测')) {
      const name = getContextName(btn);
      openActionPreview('测试运行 · ' + name, '使用示例输入测试当前配置的执行效果。', [
        '使用默认测试数据执行',
        '查看 AI 响应内容与耗时',
        '查看 Token 消耗与工具调用链',
        '支持自定义输入进行测试'
      ]);
      return true;
    }

    if (label.includes('提交审核')) {
      showToast('已提交审核，管理员将在 24 小时内处理', 'success');
      return true;
    }

    if (label.includes('启用') || label.includes('禁用') || label.includes('发布') || label.includes('审批') || label.includes('拒绝') || label.includes('轮换') || label.includes('吊销') || label.includes('调整') || label.includes('扩容') || label.includes('暂停') || label.includes('保存') || label.includes('更新') || label.includes('删除') || label.includes('添加') || label.includes('新建') || label.includes('生成') || label.includes('发送') || label.includes('重置')) {
      showToast(`「${label}」操作成功`, 'success');
      return true;
    }

    return false;
  }

  function handlePrototypeCard(card) {
    const name = getContextName(card);

    if (card.classList.contains('project-card') && card.closest('#page-templates')) {
      openActionPreview('模板详情', `已补充「${name}」的模板预览入口，可继续带入新建项目向导。`, [
        '展示技术栈与内置能力',
        '支持一键带入项目创建流程',
        '支持继续配置知识库与原子能力'
      ]);
      return true;
    }

    if (card.classList.contains('mcp-card')) {
      openActionPreview('接入说明', `已补充「${name}」的接入说明入口，可继续查看配置方式与可用能力。`, [
        '支持平台托管与自建接入',
        '支持凭证、知识库与工具联动',
        '当前为原型态说明页'
      ]);
      return true;
    }

    if (card.classList.contains('skill-item')) {
      openActionPreview(name, '已补充该项的详情预览，可继续查看触发条件、运行方式与适用场景。', [
        '展示触发方式与适用范围',
        '展示最近调用量与成功率',
        '支持继续进入配置或测试'
      ]);
      return true;
    }

    if (card.classList.contains('atomic-card')) {
      openActionPreview(`${name} 集成`, '已补充该集成的详情预览，可继续查看认证方式、开放工具与接入范围。', [
        '支持查看认证方式',
        '支持查看可开放 Tools',
        '支持继续进入接入配置'
      ]);
      return true;
    }

    return false;
  }

  function handlePrototypeStatCard(stat) {
    const label = normalizeText(stat.querySelector('.stat-label')?.textContent || '');
    const page = stat.closest('.page');
    const pageId = page ? page.id : '';

    if (pageId === 'page-dashboard') {
      if (label.includes('Token')) { showPage('efficiency'); return true; }
      if (label.includes('凭证')) { showPage('keys'); return true; }
      if (label.includes('技能')) { showPage('skills'); return true; }
      if (label.includes('过期')) { showPage('keys'); return true; }
    }
    if (pageId === 'page-proj-overview') {
      if (label.includes('能力')) { showProjectPage('ai-cap'); return true; }
      if (label.includes('服务')) { showProjectPage('services'); return true; }
      if (label.includes('事故')) { showProjectPage('incidents'); return true; }
      if (label.includes('Token')) { showProjectPage('keymanagement'); return true; }
    }
    if (pageId === 'page-proj-members') {
      if (label.includes('成员')) { return false; }
      if (label.includes('凭证')) { showProjectPage('workspace'); return true; }
      if (label.includes('AI')) { showProjectPage('ai-cap'); return true; }
      if (label.includes('Token')) { showProjectPage('keymanagement'); return true; }
    }
    if (pageId === 'page-proj-keymanagement') {
      if (label.includes('Token')) { return false; }
      if (label.includes('成员')) { showProjectPage('members'); return true; }
      if (label.includes('超额')) { return false; }
    }
    if (pageId === 'page-knowledge') {
      if (label.includes('检索')) { openActionPreview('检索统计', '全局知识库检索次数统计，可查看热门查询和命中率详情。', ['本月检索 3,241 次','命中率 78%','TOP 查询：支付流程、订单状态']); return true; }
      if (label.includes('命中')) { openActionPreview('命中率分析', '按知识库维度查看命中率分布与优化建议。', ['代码规范命中率 92%','需求文档命中率 71%','建议补充接口文档']); return true; }
    }
    if (pageId === 'page-skills') {
      if (label.includes('调用')) { showPage('efficiency'); return true; }
      if (label.includes('满意')) { openActionPreview('满意度详情', '按技能维度查看用户反馈评分与改进建议。', ['代码审查 4.8/5','Bug 分析 4.5/5','文档生成 4.2/5']); return true; }
    }
    if (pageId === 'page-keys') {
      if (label.includes('过期') || label.includes('即将')) { openActionPreview('即将过期凭证', '以下凭证将在 7 天内过期，请及时续签。', ['plt_w5_****（王五）- 3 天后过期','plt_z7_****（赵七）- 5 天后过期','plt_lt_****（李婷）- 7 天后过期']); return true; }
    }
    if (pageId === 'page-global-tools') {
      if (label.includes('调用')) { showPage('efficiency'); return true; }
    }
    if (pageId === 'page-integrations') {
      if (label.includes('接入')) { return false; }
    }

    const value = normalizeText(stat.querySelector('.stat-value')?.textContent || '');
    openActionPreview('统计详情', `${label}：${value}`, ['点击查看完整趋势与分布', '支持按时间范围筛选', '可导出统计数据']);
    return true;
  }

  function handlePrototypeMetricCard(card) {
    const label = normalizeText(card.querySelector('.metric-label')?.textContent || '');
    const value = normalizeText(card.querySelector('.metric-value')?.textContent || '');
    openActionPreview('指标详情 · ' + label, `当前值：${value}`, ['支持查看历史趋势', '支持按项目/团队维度下钻', '可导出原始数据']);
    return true;
  }

  function handlePrototypeEnvCard(card) {
    const name = normalizeText(card.querySelector('.env-name')?.textContent || card.querySelector('.badge')?.textContent || '环境');
    openActionPreview('环境详情 · ' + name, '查看当前环境的运行状态、部署记录和配置变更。', ['最近部署：10 分钟前', '当前实例：3 个 Pod', '健康检查：全部通过']);
    return true;
  }

  function handlePrototypeTableRow(row) {
    if (row.closest('thead')) return false;
    const firstCell = row.querySelector('td');
    if (!firstCell) return false;
    const name = normalizeText(firstCell.textContent);
    if (!name) return false;
    openActionPreview('详情 · ' + name, '查看「' + name + '」的完整信息、关联状态和操作记录。', ['基础信息与当前状态', '关联的项目/服务/成员', '最近的操作与变更记录']);
    return true;
  }

  function handlePrototypeToggle(toggle) {
    const dot = toggle.querySelector('div');
    if (!dot) return false;
    const isOn = toggle.style.background === 'rgb(18, 183, 106)' || toggle.style.background === '#12B76A';
    if (isOn) {
      toggle.style.background = '#D0D5DD';
      if (dot) { dot.style.right = ''; dot.style.left = '2px'; }
      showToast('已关闭（原型演示）', 'info');
    } else {
      toggle.style.background = '#12B76A';
      if (dot) { dot.style.left = ''; dot.style.right = '2px'; }
      showToast('已开启（原型演示）', 'success');
    }
    return true;
  }

  function handlePrototypeServiceCard(card) {
    const name = normalizeText(card.querySelector('.service-name')?.textContent || '');
    if (name.includes('backend') || name.includes('后端')) { showServiceDetail('backend'); return true; }
    if (name.includes('frontend') || name.includes('前端')) { showServiceDetail('frontend'); return true; }
    if (name.includes('mobile') || name.includes('移动')) { showServiceDetail('mobile'); return true; }
    showServiceDetail('backend');
    return true;
  }

  function handleIncidentItem(item) {
    const title = normalizeText(item.querySelector('.incident-title')?.textContent || '事件');
    openActionPreview('事件详情 · ' + title, '查看事件的完整时间线、根因分析和处置记录。', ['事件时间线与影响范围', 'AI 辅助根因分析', '处置记录与后续改进']);
    return true;
  }

  function handleKanbanCard(card) {
    const title = normalizeText(card.querySelector('.kanban-card-title')?.textContent || '任务');
    openActionPreview('任务详情 · ' + title, '查看任务的完整描述、关联信息和进度状态。', ['任务描述与验收标准', '关联代码 PR / 分支', '成员分配与工时记录']);
    return true;
  }

  function bindPrototypeInteractions() {
    document.addEventListener('click', event => {
      const btn = event.target.closest('button.btn');
      if (btn && !btn.getAttribute('onclick')) {
        if (handlePrototypeButton(btn)) {
          event.preventDefault();
          event.stopPropagation();
          return;
        }
      }

      const toggle = event.target.closest('[style*="border-radius:10px"][style*="position:relative"][style*="cursor:pointer"]');
      if (toggle && toggle.querySelector('div[style*="border-radius:50%"]')) {
        if (handlePrototypeToggle(toggle)) {
          event.preventDefault();
          event.stopPropagation();
          return;
        }
      }

      const stat = event.target.closest('.stat-card');
      if (stat) {
        if (handlePrototypeStatCard(stat)) {
          event.preventDefault();
          event.stopPropagation();
          return;
        }
      }

      const metric = event.target.closest('.metric-card');
      if (metric) {
        if (handlePrototypeMetricCard(metric)) {
          event.preventDefault();
          event.stopPropagation();
          return;
        }
      }

      const envCard = event.target.closest('.env-card');
      if (envCard && !envCard.getAttribute('onclick')) {
        if (handlePrototypeEnvCard(envCard)) {
          event.preventDefault();
          return;
        }
      }

      const svcCard = event.target.closest('.service-card');
      if (svcCard && !svcCard.getAttribute('onclick')) {
        if (handlePrototypeServiceCard(svcCard)) {
          event.preventDefault();
          return;
        }
      }

      const kanban = event.target.closest('.kanban-card');
      if (kanban && !kanban.getAttribute('onclick')) {
        if (handleKanbanCard(kanban)) {
          event.preventDefault();
          return;
        }
      }

      const incident = event.target.closest('.incident-item');
      if (incident && !incident.getAttribute('onclick')) {
        if (handleIncidentItem(incident)) {
          event.preventDefault();
          return;
        }
      }

      const card = event.target.closest('.project-card, .mcp-card, .skill-item, .atomic-card');
      if (card && !card.getAttribute('onclick')) {
        if (handlePrototypeCard(card)) {
          event.preventDefault();
        }
        return;
      }

      const link = event.target.closest('a');
      if (link && (!link.getAttribute('href') || link.getAttribute('href') === '#')) {
        event.preventDefault();
        const linkText = normalizeText(link.textContent);
        if (linkText.includes('文档')) {
          openActionPreview('开发文档', '查看 MCP Server 开发文档，包含接入规范、工具定义和示例代码。', [
            'MCP Server 接入规范与最佳实践',
            'Tool / Resource / Prompt 定义方式',
            'SDK 使用示例（Python / TypeScript）',
            '认证与权限配置指南'
          ]);
        } else {
          openActionPreview('链接预览', '「' + linkText + '」的详情页面。', ['支持查看完整信息','支持继续操作']);
        }
        return;
      }

      const row = event.target.closest('tr');
      if (row && !row.closest('thead') && !event.target.closest('button, a, select, input')) {
        if (handlePrototypeTableRow(row)) {
          event.preventDefault();
          return;
        }
      }
    });

    document.querySelectorAll('.stat-card, .metric-card, .env-card, .kanban-card, .incident-item, tr:not(thead tr)').forEach(el => {
      el.style.cursor = 'pointer';
    });
  }

  // ── Key Management Data Models ──

  // 成员Key分配数据模型 - 项目-成员-key完整关系
  const memberKeyAssignments = {
    'proj-1': { // 商城系统项目
      members: {
        'member-1': { // 张三 (Owner)
          id: 'member-1',
          name: '张三',
          email: 'zhangsan@company.com',
          role: 'Owner',
          avatar: '👨‍💻',
          assignedKeys: ['key-prod', 'key-dev', 'key-shared'],
          keyQuotas: {
            'key-prod': { allocated: 50000, used: 32150, status: 'active' },
            'key-dev': { allocated: 30000, used: 18900, status: 'active' },
            'key-shared': { allocated: 20000, used: 5240, status: 'active' }
          }
        },
        'member-2': { // 李四 (Member)
          id: 'member-2',
          name: '李四',
          email: 'lisi@company.com',
          role: 'Member',
          avatar: '👨‍🔧',
          assignedKeys: ['key-dev', 'key-shared'],
          keyQuotas: {
            'key-dev': { allocated: 25000, used: 21300, status: 'active' },
            'key-shared': { allocated: 15000, used: 8750, status: 'active' }
          }
        },
        'member-3': { // 王五 (Member)
          id: 'member-3',
          name: '王五',
          email: 'wangwu@company.com',
          role: 'Member',
          avatar: '👩‍💻',
          assignedKeys: ['key-dev', 'key-shared'],
          keyQuotas: {
            'key-dev': { allocated: 20000, used: 12400, status: 'active' },
            'key-shared': { allocated: 10000, used: 6100, status: 'active' }
          }
        },
        'member-4': { // 赵六 (Viewer)
          id: 'member-4',
          name: '赵六',
          email: 'zhaoliu@company.com',
          role: 'Viewer',
          avatar: '👨‍🎓',
          assignedKeys: ['key-shared'],
          keyQuotas: {
            'key-shared': { allocated: 5000, used: 1850, status: 'active' }
          }
        }
      },
      keys: {
        'key-prod': {
          id: 'key-prod',
          name: '生产主密钥',
          type: 'production',
          provider: 'Claude',
          model: 'claude-3.5-sonnet',
          totalQuota: 100000,
          usedTokens: 32150,
          rateLimit: '1000/min',
          status: 'active',
          createdAt: '2026-03-10',
          description: '生产环境专用API密钥，严格权限控制'
        },
        'key-dev': {
          id: 'key-dev',
          name: '开发密钥',
          type: 'development',
          provider: 'Claude',
          model: 'claude-3.5-sonnet',
          totalQuota: 150000,
          usedTokens: 52600,
          rateLimit: '500/min',
          status: 'active',
          createdAt: '2026-03-08',
          description: '开发环境API密钥，支持团队开发调试'
        },
        'key-shared': {
          id: 'key-shared',
          name: '团队共享密钥',
          type: 'shared',
          provider: 'Claude',
          model: 'claude-3.5-haiku',
          totalQuota: 80000,
          usedTokens: 21940,
          rateLimit: '300/min',
          status: 'active',
          createdAt: '2026-03-05',
          description: '团队协作共享密钥，适用于文档生成等场景'
        }
      }
    }
  };

  // Key使用日志数据模型 - 最近7天使用记录
  const keyUsageLogs = [
    // 2026-03-17 (今天)
    {
      logId: 'log-001', keyId: 'key-prod', memberId: 'member-1', memberName: '张三',
      timestamp: '2026-03-17 14:30:25', tokens: 2150, endpoint: '/v1/messages',
      client: 'Claude Desktop', model: 'claude-3.5-sonnet', cost: 0.043,
      purpose: '生产代码审查', status: 'success'
    },
    {
      logId: 'log-002', keyId: 'key-dev', memberId: 'member-2', memberName: '李四',
      timestamp: '2026-03-17 13:45:12', tokens: 1850, endpoint: '/v1/messages',
      client: 'Cursor', model: 'claude-3.5-sonnet', cost: 0.037,
      purpose: '功能开发调试', status: 'success'
    },
    {
      logId: 'log-003', keyId: 'key-shared', memberId: 'member-3', memberName: '王五',
      timestamp: '2026-03-17 11:20:08', tokens: 890, endpoint: '/v1/messages',
      client: 'GitHub Copilot', model: 'claude-3.5-haiku', cost: 0.018,
      purpose: '文档生成', status: 'success'
    },
    {
      logId: 'log-004', keyId: 'key-dev', memberId: 'member-1', memberName: '张三',
      timestamp: '2026-03-17 09:15:33', tokens: 3200, endpoint: '/v1/messages',
      client: 'Claude Desktop', model: 'claude-3.5-sonnet', cost: 0.064,
      purpose: '架构设计讨论', status: 'success'
    },

    // 2026-03-16 (昨天)
    {
      logId: 'log-005', keyId: 'key-prod', memberId: 'member-1', memberName: '张三',
      timestamp: '2026-03-16 16:45:22', tokens: 1950, endpoint: '/v1/messages',
      client: 'Claude Desktop', model: 'claude-3.5-sonnet', cost: 0.039,
      purpose: '生产问题诊断', status: 'success'
    },
    {
      logId: 'log-006', keyId: 'key-dev', memberId: 'member-2', memberName: '李四',
      timestamp: '2026-03-16 15:30:15', tokens: 2400, endpoint: '/v1/messages',
      client: 'Cursor', model: 'claude-3.5-sonnet', cost: 0.048,
      purpose: 'Bug修复', status: 'success'
    },
    {
      logId: 'log-007', keyId: 'key-shared', memberId: 'member-4', memberName: '赵六',
      timestamp: '2026-03-16 14:20:45', tokens: 650, endpoint: '/v1/messages',
      client: 'Claude Web', model: 'claude-3.5-haiku', cost: 0.013,
      purpose: '需求理解', status: 'success'
    },
    {
      logId: 'log-008', keyId: 'key-dev', memberId: 'member-3', memberName: '王五',
      timestamp: '2026-03-16 10:45:18', tokens: 1750, endpoint: '/v1/messages',
      client: 'GitHub Copilot', model: 'claude-3.5-sonnet', cost: 0.035,
      purpose: '单元测试编写', status: 'success'
    },

    // 2026-03-15
    {
      logId: 'log-009', keyId: 'key-prod', memberId: 'member-1', memberName: '张三',
      timestamp: '2026-03-15 17:20:30', tokens: 2800, endpoint: '/v1/messages',
      client: 'Claude Desktop', model: 'claude-3.5-sonnet', cost: 0.056,
      purpose: '性能优化分析', status: 'success'
    },
    {
      logId: 'log-010', keyId: 'key-shared', memberId: 'member-2', memberName: '李四',
      timestamp: '2026-03-15 16:10:25', tokens: 1100, endpoint: '/v1/messages',
      client: 'Claude Web', model: 'claude-3.5-haiku', cost: 0.022,
      purpose: 'API文档生成', status: 'success'
    },
    {
      logId: 'log-011', keyId: 'key-dev', memberId: 'member-3', memberName: '王五',
      timestamp: '2026-03-15 14:35:12', tokens: 2100, endpoint: '/v1/messages',
      client: 'Cursor', model: 'claude-3.5-sonnet', cost: 0.042,
      purpose: '代码重构', status: 'success'
    },

    // 2026-03-14
    {
      logId: 'log-012', keyId: 'key-dev', memberId: 'member-2', memberName: '李四',
      timestamp: '2026-03-14 15:45:40', tokens: 1600, endpoint: '/v1/messages',
      client: 'GitHub Copilot', model: 'claude-3.5-sonnet', cost: 0.032,
      purpose: '新功能开发', status: 'success'
    },
    {
      logId: 'log-013', keyId: 'key-shared', memberId: 'member-4', memberName: '赵六',
      timestamp: '2026-03-14 11:20:15', tokens: 720, endpoint: '/v1/messages',
      client: 'Claude Web', model: 'claude-3.5-haiku', cost: 0.014,
      purpose: '代码阅读理解', status: 'success'
    },
    {
      logId: 'log-014', keyId: 'key-prod', memberId: 'member-1', memberName: '张三',
      timestamp: '2026-03-14 09:30:22', tokens: 3500, endpoint: '/v1/messages',
      client: 'Claude Desktop', model: 'claude-3.5-sonnet', cost: 0.070,
      purpose: '系统监控分析', status: 'success'
    },

    // 2026-03-13
    {
      logId: 'log-015', keyId: 'key-dev', memberId: 'member-3', memberName: '王五',
      timestamp: '2026-03-13 16:25:33', tokens: 1950, endpoint: '/v1/messages',
      client: 'Cursor', model: 'claude-3.5-sonnet', cost: 0.039,
      purpose: '集成测试', status: 'success'
    },
    {
      logId: 'log-016', keyId: 'key-shared', memberId: 'member-2', memberName: '李四',
      timestamp: '2026-03-13 13:40:18', tokens: 850, endpoint: '/v1/messages',
      client: 'Claude Web', model: 'claude-3.5-haiku', cost: 0.017,
      purpose: '技术文档编写', status: 'success'
    },

    // 2026-03-12
    {
      logId: 'log-017', keyId: 'key-prod', memberId: 'member-1', memberName: '张三',
      timestamp: '2026-03-12 18:15:45', tokens: 2650, endpoint: '/v1/messages',
      client: 'Claude Desktop', model: 'claude-3.5-sonnet', cost: 0.053,
      purpose: '部署前检查', status: 'success'
    },
    {
      logId: 'log-018', keyId: 'key-dev', memberId: 'member-2', memberName: '李四',
      timestamp: '2026-03-12 14:50:20', tokens: 1800, endpoint: '/v1/messages',
      client: 'GitHub Copilot', model: 'claude-3.5-sonnet', cost: 0.036,
      purpose: '数据库优化', status: 'success'
    },
    {
      logId: 'log-019', keyId: 'key-shared', memberId: 'member-3', memberName: '王五',
      timestamp: '2026-03-12 10:30:10', tokens: 920, endpoint: '/v1/messages',
      client: 'Claude Web', model: 'claude-3.5-haiku', cost: 0.018,
      purpose: '接口设计讨论', status: 'success'
    },

    // 2026-03-11
    {
      logId: 'log-020', keyId: 'key-dev', memberId: 'member-1', memberName: '张三',
      timestamp: '2026-03-11 17:40:55', tokens: 2200, endpoint: '/v1/messages',
      client: 'Claude Desktop', model: 'claude-3.5-sonnet', cost: 0.044,
      purpose: '架构Review', status: 'success'
    },
    {
      logId: 'log-021', keyId: 'key-shared', memberId: 'member-4', memberName: '赵六',
      timestamp: '2026-03-11 15:25:30', tokens: 580, endpoint: '/v1/messages',
      client: 'Claude Web', model: 'claude-3.5-haiku', cost: 0.012,
      purpose: '学习代码规范', status: 'success'
    },
    {
      logId: 'log-022', keyId: 'key-prod', memberId: 'member-1', memberName: '张三',
      timestamp: '2026-03-11 11:15:25', tokens: 1850, endpoint: '/v1/messages',
      client: 'Claude Desktop', model: 'claude-3.5-sonnet', cost: 0.037,
      purpose: '生产环境配置', status: 'success'
    }
  ];

  // ── Tool Card Select ──
  function selectTool(el) {
    document.querySelectorAll('.tool-card').forEach(c => c.classList.remove('selected'));
    el.classList.add('selected');
  }

  // ── Init extra charts ──
  const toolChartEl = document.getElementById('toolMetricsChart');
  if (toolChartEl) {
    [30,45,60,55,80,70,90,85,75,60,50,40,55,70,88,76,65,80,95,88,72,60,45,55].forEach(h => {
      const b = document.createElement('div');
      b.className = 'chart-bar';
      b.style.height = h + '%';
      b.style.background = '#12B76A';
      toolChartEl.appendChild(b);
    });
  }
  const evalsChartEl = document.getElementById('evalsChart');
  if (evalsChartEl) {
    [70,75,80,88,89,91].forEach(h => {
      const b = document.createElement('div');
      b.className = 'chart-bar';
      b.style.height = h + '%';
      evalsChartEl.appendChild(b);
    });
  }

  // 我的用量趋势图
  const myUsageChartEl = document.getElementById('myUsageChart');
  if (myUsageChartEl) {
    [20,35,28,50,42,60,55,70,48,65,80,72,60,85,90,78,65,80,92,88,75,60,55,70,82,78,90,85,92,100].forEach(h => {
      const b = document.createElement('div');
      b.className = 'chart-bar';
      b.style.height = h + '%';
      b.style.background = '#7B61FF';
      myUsageChartEl.appendChild(b);
    });
  }

bindProjectCreateInteractions();
initProjectCreateForm();
bindPrototypeInteractions();

  Object.assign(window, {
    showPage: showPage,
    enterProject: enterProject,
    exitProject: exitProject,
    showProjectPage: showProjectPage,
    showServiceDetail: showServiceDetail,
    switchTab: switchTab,
    openModal: openModal,
    closeModal: closeModal,
    showWorkflowDetail: showWorkflowDetail,
    selectTool: selectTool,
    openActionPreview: openActionPreview,
    selectServiceOption: function(option) {
      if (option === 'template') {
        document.getElementById('svc-template-section').style.display = 'block';
        document.getElementById('svc-repo-section').style.display = 'none';
        document.querySelectorAll('.mcp-card').forEach(el => el.style.border = '1px solid var(--border)');
        document.querySelectorAll('.mcp-card')[0].style.border = '2px solid var(--primary)';
        document.querySelectorAll('.mcp-card')[0].style.background = 'var(--primary-bg)';
        document.querySelectorAll('.mcp-card')[1].style.background = '#fff';
      } else {
        document.getElementById('svc-template-section').style.display = 'none';
        document.getElementById('svc-repo-section').style.display = 'block';
        document.querySelectorAll('.mcp-card').forEach(el => el.style.border = '1px solid var(--border)');
        document.querySelectorAll('.mcp-card')[1].style.border = '2px solid var(--primary)';
        document.querySelectorAll('.mcp-card')[1].style.background = 'var(--primary-bg)';
        document.querySelectorAll('.mcp-card')[0].style.background = '#fff';
      }
    },
    showToast: showToast
  });
})();