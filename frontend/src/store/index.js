import { defineStore } from "pinia";
import axios from "axios";

export const useStore = defineStore('general', {
    state: () => {
        return {
            user: {
                id: -1,
                username: '',
                email: '',
                role: '',
                avatar: null,
                registerTime: null
            },
            forum: {
                types: [
                    {name: '全部', id: 0, color: 'linear-gradient(45deg, white, red, orange, gold, green, blue)', desc: '展示所有类型的帖子'},
                    {name: '技术讨论', id: 1, color: '#409EFF', desc: '分享技术心得、讨论技术问题'},
                    {name: '生活分享', id: 2, color: '#67C23A', desc: '分享生活中的美好时光'},
                    {name: '问题求助', id: 3, color: '#E6A23C', desc: '寻求帮助、解决问题'}
                ]
            }
        }
    }, getters: {
        avatarUrl() {
            if(this.user.avatar)
                return `${axios.defaults.baseURL}/images${this.user.avatar}`
            else
                return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }
    }, actions: {
        findTypeById(id) {
            for (let type of this.forum.types) {
                if(type.id === id)
                    return type
            }
        },
        avatarUserUrl(avatar) {
            if(avatar)
                return `${axios.defaults.baseURL}/images${avatar}`
            else
                return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }
    }
})
