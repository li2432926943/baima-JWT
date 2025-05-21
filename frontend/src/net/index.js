import axios from "axios";
import {ElMessage} from "element-plus";

// 应用启动时清除可能存在的无效token
function clearAllTokens() {
    console.log('清除所有存储的token');
    localStorage.removeItem('access_token');
    sessionStorage.removeItem('access_token');
}

// 立即执行一次清除操作，解决启动时可能存在的无效token导致的问题
clearAllTokens();

const authItemName = "authorize"
const storageKeyName = "access_token"  // 实际存储在localStorage/sessionStorage中的键名

const accessHeader = () => {
    return {
        'Authorization': `Bearer ${takeAccessToken()}`
    }
}

const defaultError = (error) => {
    console.error(error)
    
    // 处理响应错误
    if (error.response) {
        const status = error.response.status
        const data = error.response.data
        
        // 处理常见的HTTP状态码
        if (status === 401) {
            // 检查是否是通知相关API
            if (error.config && error.config.url && error.config.url.includes('/api/notification')) {
                console.warn('通知API请求未授权，但不影响主要功能')
            } else {
                ElMessage.error(data.message || '用户名或密码错误，请重新登录')
            }
        } else if (status === 403) {
            ElMessage.error('您没有权限执行此操作')
        } else if (status === 404) {
            ElMessage.error('请求的资源不存在')
        } else if (status === 500) {
            ElMessage.error('服务器内部错误，请稍后再试')
        } else {
            ElMessage.error(`请求失败 (${status}): ${data.message || '未知错误'}`)
        }
    } else if (error.request) {
        // 请求已发送但未收到响应
        ElMessage.error('无法连接到服务器，请检查网络连接')
    } else {
        // 请求设置出现问题
        ElMessage.error('发生了一些错误，请联系管理员')
    }
}

const defaultFailure = (message, status, url) => {
    console.warn(`请求地址: ${url}, 状态码: ${status}, 错误信息: ${message}`)
    ElMessage.warning(message)
}

function takeAccessToken() {
    try {
        const str = localStorage.getItem(storageKeyName) || sessionStorage.getItem(storageKeyName);
        if (!str) return null;
        
        const authObj = JSON.parse(str);
        console.log('读取到的Token信息:', authObj);
        
        if (!authObj.token || !authObj.expire) {
            console.warn('Token数据格式不正确');
            deleteAccessToken();
            return null;
        }
        
        // 确保expire是Date对象
        let expireDate;
        if (typeof authObj.expire === 'string') {
            expireDate = new Date(authObj.expire);
        } else if (authObj.expire instanceof Date) {
            expireDate = authObj.expire;
        } else {
            console.warn('无效的过期时间格式');
            deleteAccessToken();
            return null;
        }
        
        // 比较日期
        const now = new Date();
        if (expireDate <= now) {
            console.warn('Token已过期:', expireDate, '当前时间:', now);
            deleteAccessToken();
            return null;
        }
        
        // 验证token是否仍然有效（添加一个简单测试请求）
        try {
            // 仅返回token，由业务逻辑处理无效token
            return authObj.token;
        } catch (error) {
            console.warn('Token可能已失效，但未过期');
            return authObj.token;
        }
    } catch (e) {
        console.error('读取Token时出错:', e);
        deleteAccessToken();
        return null;
    }
}

function storeAccessToken(remember, token, expire){
    try {
        console.log('开始存储Token:', { token, expire, remember });
        
        // 确保expire是有效的日期
        let expireDate;
        if (!expire) {
            console.error('错误: 未提供过期时间');
            ElMessage.error('登录失败: 缺少重要信息');
            return;
        }
        
        if (typeof expire === 'string') {
            expireDate = new Date(expire);
        } else if (expire instanceof Date) {
            expireDate = expire;
        } else {
            console.error('错误: 过期时间格式不正确', expire);
            ElMessage.error('登录失败: 数据格式错误');
            return;
        }
        
        // 将日期转换为ISO字符串，避免JSON序列化问题
        const authObj = {
            token: token,
            expire: expireDate.toISOString()
        };
        
        const str = JSON.stringify(authObj);
        console.log('存储Token字符串:', str);
        
        if (remember) {
            localStorage.setItem(storageKeyName, str);
            console.log('Token已存储到localStorage');
        } else {
            sessionStorage.setItem(storageKeyName, str);
            console.log('Token已存储到sessionStorage');
        }
    } catch (e) {
        console.error('存储Token时出错:', e);
        ElMessage.error('无法保存登录状态');
    }
}

function deleteAccessToken() {
    console.log('删除access token')
    localStorage.removeItem(storageKeyName)
    sessionStorage.removeItem(storageKeyName)
}

function internalPost(url, data, headers, success, failure, error = defaultError){
    console.log(`发送POST请求到: ${url}`, data)
    
    axios.post(url, data, { headers: headers })
    .then(response => {
        console.log(`收到响应 (${url}):`, response.data)
        const responseData = response.data
        
        if(responseData.code === 200)
            success(responseData.data)
        else
            failure(responseData.message, responseData.code, url)
    })
    .catch(err => {
        console.error(`请求失败 (${url}):`, err)
        error(err)
    })
}

function internalGet(url, headers, success, failure, error = defaultError){
    console.log(`发送GET请求到: ${url}`, headers)
    
    axios.get(url, { headers: headers }).then(response => {
        console.log(`收到GET响应 (${url}):`, response.data)
        const data = response.data
        
        if(data.code === 200)
            success(data.data)
        else
            failure(data.message, data.code, url)
    }).catch(err => error(err))
}

function login(username, password, remember, success, failure = defaultFailure){
    internalPost('/api/auth/login', {
        username: username,
        password: password
    }, {
        'Content-Type': 'application/json'
    }, (data) => {
        try {
            // 确保data.expire是有效的日期字符串或对象
            if (data.expire) {
                const expireDate = typeof data.expire === 'string' ? new Date(data.expire) : data.expire;
                storeAccessToken(remember, data.token, expireDate);
                ElMessage.success(`登录成功，欢迎 ${data.username} 来到我们的系统`);
                success(data);
            } else {
                console.error('服务器没有返回过期时间', data);
                ElMessage.error('登录数据格式不正确，请联系管理员');
            }
        } catch (e) {
            console.error('处理登录响应时出错', e);
            ElMessage.error('登录处理失败，请稍后重试');
        }
    }, failure)
}

function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader() , success, failure)
}

function logout(success, failure = defaultFailure){
    const token = takeAccessToken()
    if(!token) {
        ElMessage.warning('您已经退出登录')
        success()
        return
    }
    
    internalGet('/api/auth/logout', accessHeader(), () => {
        deleteAccessToken()
        ElMessage.success(`退出登录成功，欢迎您再次使用`)
        success()
    }, failure)
}

function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure)
}

function unauthorized() {
    const token = takeAccessToken();
    const result = !token;
    console.log('检查授权状态 - 是否未授权:', result, '有token:', !!token);
    return result;
}

export { post, get, login, logout, unauthorized, accessHeader, clearAllTokens } 