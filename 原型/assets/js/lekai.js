// 乐知助手对话功能
(function() {
  var lekaiConversations = {
    '今天项目有什么 Bug 或告警吗？': [
      '正在检索事故中心与监控数据...',
      '🚨 **当前有 1 个待处理事故**\n\n**支付服务 NullPointerException**（严重）\n- 位置：`PaymentService.processOrder:142`\n- 根因：checkout 接口未对 `paymentMethod` 做非空校验\n- 建议：在 `OrderController:89` 加参数校验\n- 状态：处理中（张三负责）\n\n生产环境监控正常，P99 延迟 120ms，无其他告警。'
    ],
    '帮我查一下生产环境的服务状态': [
      '正在查询服务健康状态...',
      '✅ **生产环境整体正常**\n\nmall-backend 🟢 v2.3.1 · P99 120ms\nmall-frontend 🔵 feature/search 构建中（约 15 分钟）\nmall-mobile 🟢 v2.1.0 健康\n\n不影响生产流量，预计 15 分钟后构建完成。'
    ],
    '帮我解释一下订单状态机的设计': [
      '正在检索知识库...（命中「系统架构设计文档」· 相关度 95%）',
      '📚 **订单状态机设计**\n\n待支付 → 已支付 → 备货中 → 已发货 → 已完成\n↓\n已取消（可从待支付/已支付转入）\n\n**关键规则：**\n- 超时 30 分钟未支付自动取消\n- 已发货不可直接取消，需走退货流程\n- 状态流转均记录操作日志\n\n当前 Sprint 正在重构此模块（进度 65%），预计明天完成。'
    ],
    '最近一次 Sprint 完成情况怎么样？': [
      '正在分析 Sprint 数据...',
      '📋 **Sprint 8 进度**（3/1 - 3/14）\n\n已完成 23 SP / 共 40 SP（58%），剩余 6 天\n完成概率：**78%**\n\n⚠️ 订单状态机重构（8SP）进度 65%，轻微延期风险\n⚠️ 支付联调依赖第三方沙箱，建议今天协调\n\n按当前速率月底 Token 预计 496K，不超限。'
    ]
  };

  window.lekaiSendQuick = function(text) {
    var input = document.getElementById('lekai-input');
    if (input) { input.value = text; }
    window.lekaiSend();
  };

  window.lekaiSend = function() {
    var input = document.getElementById('lekai-input');
    var messages = document.getElementById('lekai-messages');
    var welcome = document.getElementById('lekai-welcome');
    if (!input || !messages) return;
    var text = input.value.trim();
    if (!text) return;
    if (welcome) welcome.style.display = 'none';
    input.value = '';
    input.style.height = 'auto';

    // 用户消息气泡
    var userMsg = document.createElement('div');
    userMsg.style.cssText = 'display:flex;justify-content:flex-end;margin-bottom:4px';
    userMsg.innerHTML = '<div style="max-width:70%;background:var(--primary);color:white;border-radius:14px 14px 2px 14px;padding:10px 14px;font-size:13px;line-height:1.6">' + text.replace(/</g,'&lt;').replace(/>/g,'&gt;') + '</div>';
    messages.appendChild(userMsg);

    // AI 回复气泡（loading 态）
    var aiMsg = document.createElement('div');
    aiMsg.style.cssText = 'display:flex;align-items:flex-start;gap:10px;margin-bottom:4px';
    aiMsg.innerHTML = '<div style="width:32px;height:32px;background:linear-gradient(135deg,#4F6EF7,#7A5AF8);border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:16px;flex-shrink:0">🔮</div><div id="lekai-bubble-tmp" class="ai-bubble" style="border-radius:2px 14px 14px 14px;font-size:13px;color:var(--sub)">乐知正在思考...</div>';
    messages.appendChild(aiMsg);
    messages.scrollTop = messages.scrollHeight;

    var conv = lekaiConversations[text];
    var thinkText = conv ? conv[0] : '正在检索项目知识库...';
    var replyText = conv ? conv[1] : '关于「' + text + '」的查询已收到。\n\n这是乐知原型演示，实际部署后将通过 RAG 检索项目知识库，提供基于真实项目数据的精准回答。';
    var bubble = aiMsg.querySelector('#lekai-bubble-tmp');

    setTimeout(function() {
      if (bubble) bubble.textContent = thinkText;
      messages.scrollTop = messages.scrollHeight;
      setTimeout(function() {
        if (bubble) {
          bubble.innerHTML = replyText
            .replace(/\n/g, '<br>')
            .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
            .replace(/`([^`]+)`/g, '<code>$1</code>');
          bubble.style.color = 'var(--text)';
          bubble.removeAttribute('id');
        }
        messages.scrollTop = messages.scrollHeight;
      }, 1000);
    }, 800);
  };
})();
