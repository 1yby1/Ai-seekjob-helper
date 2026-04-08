<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <span class="logo-icon">🤖</span>
          <h1>AI 编程助手</h1>
        </div>
        <p class="subtitle">您的智能编程学习与面试伙伴</p>
      </div>

      <div class="tab-switch">
        <button 
          :class="['tab-btn', { active: isLogin }]" 
          @click="isLogin = true"
        >
          登录
        </button>
        <button 
          :class="['tab-btn', { active: !isLogin }]" 
          @click="isLogin = false"
        >
          注册
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="login-form">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            id="username"
            v-model="form.username" 
            type="text" 
            placeholder="请输入用户名"
            required
          />
        </div>

        <div class="form-group" v-if="!isLogin">
          <label for="email">邮箱</label>
          <input 
            id="email"
            v-model="form.email" 
            type="email" 
            placeholder="请输入邮箱"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input 
            id="password"
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            required
          />
        </div>

        <div class="form-group" v-if="!isLogin">
          <label for="confirmPassword">确认密码</label>
          <input 
            id="confirmPassword"
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码"
            required
          />
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          {{ isLogin ? '登录' : '注册' }}
        </button>
      </form>

      <div class="login-footer">
        <p v-if="isLogin">
          还没有账号？
          <a href="#" @click.prevent="isLogin = false">立即注册</a>
        </p>
        <p v-else>
          已有账号？
          <a href="#" @click.prevent="isLogin = true">立即登录</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '../api/auth'

const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const handleSubmit = async () => {
  if (!isLogin.value && form.password !== form.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }

  loading.value = true
  
  try {
    const response = isLogin.value
      ? await login({
          username: form.username,
          password: form.password
        })
      : await register({
          username: form.username,
          email: form.email,
          password: form.password
        })
     
    // 检查响应是否成功
    if (!response.success) {
      alert(response.message || '操作失败，请重试')
      return
    }
    
    // auth.ts 已经自动保存了 token 和 current_user
    // 跳转到聊天页面
    router.push('/chat')
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
// 科技感配色 - 白色系
$bg-light: #f8f9fc;
$bg-card: #ffffff;
$border-color: #e5e7eb;
$text-primary: #1a1a2e;
$text-secondary: #6b7280;
$accent: #00c896;
$accent-dim: rgba(0, 200, 150, 0.08);

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-light;
  padding: 20px;
  position: relative;
  overflow: hidden;

  // 网格背景
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: 
      linear-gradient(rgba(0,0,0,0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0,0,0,0.03) 1px, transparent 1px);
    background-size: 60px 60px;
  }
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: 12px;
  padding: 40px;
  position: relative;
  z-index: 1;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 8px;

    .logo-icon {
      font-size: 36px;
    }

    h1 {
      font-size: 24px;
      font-weight: 600;
      color: $text-primary;
      margin: 0;
      letter-spacing: -0.5px;
    }
  }

  .subtitle {
    color: $text-secondary;
    font-size: 14px;
    margin: 0;
  }
}

.tab-switch {
  display: flex;
  background: $bg-light;
  border: 1px solid $border-color;
  border-radius: 8px;
  padding: 4px;
  margin-bottom: 28px;

  .tab-btn {
    flex: 1;
    padding: 10px;
    border: none;
    background: transparent;
    font-size: 14px;
    font-weight: 500;
    color: $text-secondary;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s ease;

    &.active {
      background: $accent;
      color: #fff;
    }

    &:hover:not(.active) {
      color: $text-primary;
    }
  }
}

.login-form {
  .form-group {
    margin-bottom: 20px;

    label {
      display: block;
      margin-bottom: 8px;
      font-size: 13px;
      font-weight: 500;
      color: $text-secondary;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    input {
      width: 100%;
      padding: 14px 16px;
      background: $bg-light;
      border: 1px solid $border-color;
      border-radius: 8px;
      font-size: 15px;
      color: $text-primary;
      transition: all 0.2s ease;
      box-sizing: border-box;

      &:focus {
        outline: none;
        border-color: $accent;
        background: $accent-dim;
      }

      &::placeholder {
        color: #9ca3af;
      }
    }
  }
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: $accent;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &:hover:not(:disabled) {
    filter: brightness(1.05);
    box-shadow: 0 4px 16px rgba(0, 200, 150, 0.3);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;

  p {
    color: $text-secondary;
    font-size: 14px;
    margin: 0;

    a {
      color: $accent;
      text-decoration: none;
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}
.login-card {
  width: 100%;
  max-width: 400px;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: 12px;
  padding: 40px;
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 8px;

    .logo-icon {
      font-size: 36px;
      filter: grayscale(1) brightness(1.2);
    }

    h1 {
      font-size: 24px;
      font-weight: 600;
      color: $text-primary;
      margin: 0;
      letter-spacing: -0.5px;
    }
  }

  .subtitle {
    color: $text-secondary;
    font-size: 14px;
    margin: 0;
  }
}

.tab-switch {
  display: flex;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid $border-color;
  border-radius: 8px;
  padding: 4px;
  margin-bottom: 28px;

  .tab-btn {
    flex: 1;
    padding: 10px;
    border: none;
    background: transparent;
    font-size: 14px;
    font-weight: 500;
    color: $text-secondary;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s ease;

    &.active {
      background: $accent;
      color: #fff;
    }

    &:hover:not(.active) {
      color: $text-primary;
    }
  }
}

.login-form {
  .form-group {
    margin-bottom: 20px;

    label {
      display: block;
      margin-bottom: 8px;
      font-size: 13px;
      font-weight: 500;
      color: $text-secondary;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    input {
      width: 100%;
      padding: 14px 16px;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid $border-color;
      border-radius: 8px;
      font-size: 15px;
      color: $text-primary;
      transition: all 0.2s ease;
      box-sizing: border-box;

      &:focus {
        outline: none;
        border-color: $accent;
        background: $accent-dim;
      }

      &::placeholder {
        color: $text-secondary;
      }
    }
  }
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: $accent;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &:hover:not(:disabled) {
    filter: brightness(1.1);
    box-shadow: 0 0 20px rgba(0, 212, 170, 0.3);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(0, 0, 0, 0.2);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;

  p {
    color: $text-secondary;
    font-size: 14px;
    margin: 0;

    a {
      color: $accent;
      text-decoration: none;
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
