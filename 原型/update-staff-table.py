#!/usr/bin/env python3
import re

# 读取文件
with open('./assets/js/templates/platform-pages.js', 'r', encoding='utf-8') as f:
    content = f.read()

# 替换表格头
content = re.sub(
    r'<th>参与项目</th>',
    '<th>Token配额</th>',
    content
)

# 替换项目标签为配额显示（为几个主要员工更新）
patterns = [
    # 张三 - 超级管理员
    (r'<td>\s*<span class="badge badge-gray" style="font-size:10px">🛒 商城</span>\s*<span class="badge badge-gray" style="font-size:10px">👤 用户中心</span>\s*<span class="badge badge-gray" style="font-size:10px">\+2</span>\s*</td>',
     '<td><div style="font-size:12px;font-weight:600">500K/月</div><div style="font-size:11px;color:var(--sub)">已用203K (40.6%)</div></td>'),

    # 李四 - 平台管理员
    (r'<td>\s*<span class="badge badge-gray" style="font-size:10px">🛒 商城</span>\s*<span class="badge badge-gray" style="font-size:10px">💳 支付</span>\s*</td>',
     '<td><div style="font-size:12px;font-weight:600">300K/月</div><div style="font-size:11px;color:var(--sub)">已用167K (55.7%)</div></td>'),

    # 王五
    (r'<td>\s*<span class="badge badge-gray" style="font-size:10px">🛒 商城</span>\s*<span class="badge badge-gray" style="font-size:10px">📊 数据</span>\s*</td>',
     '<td><div style="font-size:12px;font-weight:600">300K/月</div><div style="font-size:11px;color:var(--sub)">已用245K (81.7%)</div></td>'),

    # 赵六
    (r'<td>\s*<span class="badge badge-gray" style="font-size:10px">🛒 商城</span>\s*</td>',
     '<td><div style="font-size:12px;font-weight:600">200K/月</div><div style="font-size:11px;color:var(--sub)">已用89K (44.5%)</div></td>'),

    # 钱七
    (r'<td>\s*<span class="badge badge-gray" style="font-size:10px">🛒 商城</span>\s*<span class="badge badge-gray" style="font-size:10px">👤 用户中心</span>\s*</td>',
     '<td><div style="font-size:12px;font-weight:600">200K/月</div><div style="font-size:11px;color:var(--sub)">已用143K (71.5%)</div></td>'),

    # 陈八
    (r'<td>\s*<span class="badge badge-gray" style="font-size:10px">💳 支付</span>\s*</td>',
     '<td><div style="font-size:12px;font-weight:600">200K/月</div><div style="font-size:11px;color:var(--sub)">已用67K (33.5%)</div></td>'),

    # 孙九 - 未激活
    (r'<td>\s*<span class="badge badge-gray" style="font-size:10px">🛒 商城</span>\s*</td>',
     '<td><div style="font-size:12px;font-weight:600">100K/月</div><div style="font-size:11px;color:var(--sub)">未使用</div></td>'),
]

# 逐个替换
for pattern, replacement in patterns:
    content = re.sub(pattern, replacement, content, count=1)

# 处理周十（已停用员工）
content = re.sub(
    r'<td><span style="font-size:11px;color:var\(--sub\)">已移除全部项目</span></td>',
    '<td><div style="font-size:12px;font-weight:600;color:var(--sub)">已暂停</div><div style="font-size:11px;color:var(--sub)">配额冻结</div></td>',
    content
)

# 保存文件
with open('./assets/js/templates/platform-pages.js', 'w', encoding='utf-8') as f:
    f.write(content)

print("Staff table updated successfully!")