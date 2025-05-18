import axios from "axios";
import {ElMessage} from "element-plus";

// 开发环境标志，打包时改为false
const isDevelopment = true;

// 模拟响应延迟
const mockDelay = 500;

const authItemName = "authorize"
const storageKeyName = "access_token"  // 实际存储在localStorage/sessionStorage中的键名

// 为开发环境提供一个模拟的token
if (isDevelopment && !localStorage.getItem(storageKeyName) && !sessionStorage.getItem(storageKeyName)) {
    const mockToken = {
        token: "mock-jwt-token-for-development",
        expire: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24小时后过期
    };
    localStorage.setItem(storageKeyName, JSON.stringify(mockToken));
    console.log('已创建开发环境模拟token');
}

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
            ElMessage.error(data.message || '用户名或密码错误，请重新登录')
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
            ElMessage.warning("登录状态已过期，请重新登录！");
            return null;
        }
        
        return authObj.token;
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
    localStorage.removeItem(storageKeyName)
    sessionStorage.removeItem(storageKeyName)
}

function internalPost(url, data, headers, success, failure, error = defaultError){
    // 开发环境使用模拟数据
    if (isDevelopment) {
        console.log(`[开发模式] 模拟POST请求: ${url}`, data);
        setTimeout(() => {
            // 根据URL返回不同的模拟数据
            if (url.includes('/api/auth/login')) {
                success({
                    username: '开发测试用户',
                    token: 'mock-token-xxx',
                    expire: new Date(Date.now() + 24 * 60 * 60 * 1000)
                });
            } else if (url.includes('/api/auth/logout')) {
                success({});
            } else {
                success({});
            }
        }, mockDelay);
        return;
    }
    
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
    // 开发环境使用模拟数据
    if (isDevelopment) {
        console.log(`[开发模式] 模拟GET请求: ${url}`, headers);
        setTimeout(() => {
            // 根据URL返回不同的模拟数据
            if (url.includes('/api/user/info')) {
                success({
                    username: '开发测试用户',
                    email: 'dev@example.com',
                    id: 1,
                    role: 'admin',
                    avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
                });
            } else if (url.includes('/api/notification/list')) {
                success([]);
            } else {
                success({});
            }
        }, mockDelay);
        return;
    }
    
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
    // 开发环境下始终返回已授权
    if (isDevelopment) {
        return false;
    }
    return !takeAccessToken()
}

export { post, get, login, logout, unauthorized, accessHeader } 