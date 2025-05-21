<script setup>
import Card from "@/components/Card.vue";
import {Setting, Switch, Lock} from "@element-plus/icons-vue";
import {reactive, ref} from "vue";
import {get, post} from "@/net";
import {ElMessage} from "element-plus";

const form = reactive({
    password: '',
    new_password: '',
    new_password_repeat: ''
})
const validatePassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== form.new_password) {
        callback(new Error("两次输入的密码不一致"))
    } else {
        callback()
    }
}
const rules = {
    password: [
        { required: true, message: '请输入原来的密码', trigger: 'blur' }
    ],
    new_password: [
        { required: true, message: '请输入新的密码', trigger: 'blur' },
        { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur'] }
    ],
    new_password_repeat: [
        { required: true, message: '请再次输入新的密码', trigger: 'blur' },
        { validator: validatePassword, trigger: ['blur', 'change'] },
    ]
}
const formRef = ref()
const valid = ref(false)
const onValidate = (prop, isValid) => valid.value = isValid

function resetPassword(){
    formRef.value.validate(valid => {
        if(valid) {
            post('/api/user/change-password', form, () => {
                ElMessage.success('修改密码成功！')
                formRef.value.resetFields();
            }, (message) => {
                ElMessage.error(message || '修改密码失败，请稍后再试')
            })
        }
    })
}

const saving = ref(false)
const loadError = ref(false)
const privacy = reactive({
    phone: false,
    wx: false,
    qq: false,
    email: false,
    gender: false
})

// 加载隐私设置
get('/api/user/privacy', data => {
    privacy.phone = data.phone
    privacy.email = data.email
    privacy.wx = data.wx
    privacy.qq = data.qq
    privacy.gender = data.gender
    loadError.value = false
}, (message) => {
    loadError.value = true
    ElMessage.error(message || '加载隐私设置失败')
})

function savePrivacy(type, status){
    saving.value = true
    post('/api/user/save-privacy', {
        type: type,
        status: status
    }, () => {
        ElMessage.success('隐私设置修改成功！')
        saving.value = false
    }, (message) => {
        ElMessage.error(message || '保存设置失败，请重试')
        // 保存失败时恢复原值
        privacy[type] = !status
        saving.value = false
    })
}
</script>

<template>
    <div style="margin: auto;max-width: 600px">
        <div style="margin-top: 20px">
            <card :icon="Setting" title="隐私设置" desc="在这里设置哪些内容可以被其他人看到，请各位小伙伴注重自己的隐私">
                <div v-if="loadError" style="text-align: center;color: var(--el-color-error);padding: 20px">
                    加载隐私设置失败，请刷新页面重试
                </div>
                <div v-else class="checkbox-list">
                    <el-checkbox @change="savePrivacy('phone', privacy.phone)"
                                 v-model="privacy.phone" :disabled="saving">公开展示我的手机号</el-checkbox>
                    <el-checkbox @change="savePrivacy('email', privacy.email)"
                                 v-model="privacy.email" :disabled="saving">公开展示我的电子邮件地址</el-checkbox>
                    <el-checkbox @change="savePrivacy('wx', privacy.wx)"
                                 v-model="privacy.wx" :disabled="saving">公开展示我的微信号</el-checkbox>
                    <el-checkbox @change="savePrivacy('qq', privacy.qq)"
                                 v-model="privacy.qq" :disabled="saving">公开展示我的QQ号</el-checkbox>
                    <el-checkbox @change="savePrivacy('gender', privacy.gender)"
                                 v-model="privacy.gender" :disabled="saving">公开展示我的性别</el-checkbox>
                </div>
            </card>
            <card style="margin: 20px 0" :icon="Setting"
                  title="修改密码" desc="修改密码请在这里进行操作，请务必牢记您的密码">
                <el-form :rules="rules" :model="form" ref="formRef" @validate="onValidate" label-width="100" style="margin: 20px">
                    <el-form-item label="当前密码" prop="password">
                        <el-input type="password" :prefix-icon="Lock" v-model="form.password"
                                  placeholder="当前密码" maxlength="16"/>
                    </el-form-item>
                    <el-form-item label="新密码" prop="new_password">
                        <el-input type="password" :prefix-icon="Lock" v-model="form.new_password"
                                  placeholder="新密码" maxlength="16"/>
                    </el-form-item>
                    <el-form-item label="重复新密码" prop="new_password_repeat">
                        <el-input type="password" :prefix-icon="Lock" v-model="form.new_password_repeat"
                                  placeholder="重新输入新密码" maxlength="16"/>
                    </el-form-item>
                    <div style="text-align: center">
                        <el-button @click="resetPassword" :icon="Switch" type="success">立即重置密码</el-button>
                    </div>
                </el-form>
            </card>
        </div>
    </div>
</template>

<style scoped>
.checkbox-list {
    margin: 10px 0 0 10px;
    display: flex;
    flex-direction: column;
}

.checkbox-list .el-checkbox {
    margin-bottom: 10px;
}
</style> 