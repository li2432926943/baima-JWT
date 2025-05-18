import { reactive } from 'vue'

const store = {
  user: reactive({
    username: '用户名',
    email: 'user@example.com',
    id: null,
    role: 'user',
    registerTime: new Date().toISOString(),
    avatar: null,
    nickname: '',
    gender: 1,
    phone: '',
    qq: '',
    wx: '',
    bio: ''
  }),
  
  get avatarUrl() {
    if (this.user.avatar) {
      return this.user.avatar;
    }
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
  }
}

export function useStore() {
  return store
} 