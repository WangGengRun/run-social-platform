# WebSocket 实时私信功能

## 功能说明

实现了基于 WebSocket 的实时私信推送功能，消息发送后立即推送给在线用户。

## 连接方式

### WebSocket 连接地址
```
ws://localhost:8120/api/ws/message?token=YOUR_JWT_TOKEN
```

或者通过请求头传递Token：
```
ws://localhost:8120/api/ws/message
Headers: Authorization: Bearer YOUR_JWT_TOKEN
```

## 消息格式

### 客户端发送消息
```json
{
  "type": "ping",
  "data": "ping",
  "timestamp": 1234567890
}
```

### 服务器推送消息
```json
{
  "type": "new_message",
  "data": {
    "id": 1,
    "senderId": 123,
    "receiverId": 456,
    "content": "你好",
    "messageType": 1,
    "isRead": false,
    "isSelf": false,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1234567890
}
```

## 消息类型

- `connected`: 连接成功
- `ping`: 心跳请求
- `pong`: 心跳响应
- `new_message`: 新私信消息
- `message_read`: 消息已读通知
- `error`: 错误消息

## 使用示例

### JavaScript 客户端示例
```javascript
const token = 'YOUR_JWT_TOKEN';
const ws = new WebSocket(`ws://localhost:8120/api/ws/message?token=${token}`);

ws.onopen = () => {
  console.log('WebSocket连接成功');
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  console.log('收到消息:', message);
  
  if (message.type === 'new_message') {
    // 处理新私信
    handleNewMessage(message.data);
  }
};

ws.onerror = (error) => {
  console.error('WebSocket错误:', error);
};

ws.onclose = () => {
  console.log('WebSocket连接关闭');
};

// 发送心跳
setInterval(() => {
  if (ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      type: 'ping',
      data: 'ping',
      timestamp: Date.now()
    }));
  }
}, 30000); // 每30秒发送一次心跳
```

## 特性

1. **实时推送**: 消息发送后立即推送给在线用户
2. **多设备支持**: 一个用户可以在多个设备同时在线
3. **自动重连**: 客户端需要实现重连逻辑
4. **心跳检测**: 支持ping/pong心跳保持连接
5. **JWT认证**: 连接时验证JWT Token

## 注意事项

1. 需要先运行 `mvn clean install` 或刷新Maven依赖
2. 生产环境需要配置具体的WebSocket域名
3. 建议客户端实现自动重连机制
4. 建议实现心跳检测，保持连接活跃

