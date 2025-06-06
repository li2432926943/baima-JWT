其实就controller和serviceimpl值得写，其他的什么工具类，知道怎么使用方法就好了

## jwt交互基本流程

![image-20230724010807761](https://s2.loli.net/2023/07/24/4bThtMwA9XsP5uc.png)

## JWT的格式

JWT令牌的格式如下：

![image-20230307000004710](https://s2.loli.net/2023/03/07/Xu8lxYhKoJNr6it.png)

**一个JWT令牌由3部分组成：标头(Header)、有效载荷(Payload)和签名(Signature)。在传输的时候，会将JWT的前2部分分别进行Base64编码后用 `.`进行连接形成最终需要传输的字符串。**

* 标头HEADER：包含一些元数据信息，比如JWT签名所使用的加密算法，还有类型，这里统一都是JWT。
* **有效载荷DATA：包括用户名称、令牌发布时间、过期时间、JWT ID等，当然我们也可以自定义添加字段，我们的用户信息一般都在这里存放。**
* 签名VERIFY SIGNATURE：首先需要指定一个密钥，该密钥仅仅保存在服务器中，保证不能让其他用户知道。然后使用Header中指定的算法对Header和Payload进行base64加密之后的结果通过密钥计算哈希值，然后就得出一个签名哈希。这个会用于之后验证内容是否被篡改。



## Base64编码与解码(加密，防止过程泄露)

在 JWT（JSON Web Token） 的生成和传输过程中，Base64 编码 主要用于 JWT 的组成部分编码，而不是直接用于密码或用户数据的编码。以下是具体涉及 Base64 的环节：

---

**1. Base64 在 JWT 中的作用**
JWT 由三部分组成（`Header.Payload.Signature`），每个部分都会经过 Base64URL 编码（一种变种的 Base64 编码，兼容 URL 安全）：
1. Header  
   • 描述令牌类型（`"alg": "HS256"`）和签名算法。  

   • 示例：`{"alg": "HS256", "typ": "JWT"}` → Base64 编码后变成 `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9`。


2. Payload  
   • 存放用户信息（如 `{"sub": "123", "name": "Alice"}`）。  

   • Base64 编码后变成 `eyJzdWIiOiIxMjMiLCJuYW1lIjoiQWxpY2UifQ`。


3. Signature  
   • 对前两部分签名（防止篡改），签名结果也会被 Base64 编码。  

   • 示例：`HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)` → 编码后变成 `SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c`。


最终组合成完整的 JWT：  
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjMiLCJuYW1lIjoiQWxpY2UifQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

---

**2. 在用户认证流程中的具体位置**
根据你提供的图片流程，Base64 编码出现在以下步骤中：

**步骤 2：服务器生成 JWT**
• 服务器将用户信息（Payload）和元数据（Header）用 Base64URL 编码，并生成签名。  

• 关键点：Base64 编码的是 JWT 的结构化数据（JSON），不是原始密码（密码在步骤 1 中已通过 HTTPS 加密传输）。


**步骤 4-5：浏览器发送 JWT / 服务器解析 JWT**
• 浏览器通过 Cookie 或 `Authorization` 头发送 JWT（已是 Base64 编码后的字符串）。  

• 服务器收到 JWT 后，会先 Base64 解码 Header 和 Payload，再验证签名。


---

**3. 为什么用 Base64？**
1. 兼容性  
   Base64 将二进制数据（如签名）转换为 ASCII 字符串，确保 JWT 能安全通过 HTTP 协议传输（避免特殊字符冲突）。

2. 可读性  
   Base64 编码后的数据对人类不可读，但解码后可以还原原始 JSON 结构（方便调试）。

3. 非加密！  
   Base64 只是编码，不是加密！JWT 的敏感信息（如密码）不应放在 Payload 中，除非额外加密。

---

**4. 代码示例（JWT 的 Base64 编码/解码）**
```java
import java.util.Base64;

public class JwtBase64Example {
    public static void main(String[] args) {
        // 模拟 JWT 的 Header（JSON）
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        // Base64URL 编码
        String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
        System.out.println("Encoded Header: " + encodedHeader); // eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9

        // 解码还原
        byte[] decodedHeaderBytes = Base64.getUrlDecoder().decode(encodedHeader);
        System.out.println("Decoded Header: " + new String(decodedHeaderBytes)); // {"alg":"HS256","typ":"JWT"}
    }
}
```

---

**总结**
• Base64 编码出现在 JWT 的生成和解析过程中（Header/Payload/Signature 的编码）。  

• 不涉及密码的 Base64 编码（密码应通过 HTTPS 加密传输）。  

• JWT 的安全性依赖于签名（Signature），而非 Base64 编码本身。  


如果你有更具体的场景（如某一步骤的代码实现），可以进一步讨论！

## 数字签名(验证jwt是否还是原来那个，只有服务器知道)

![](https://s2.loli.net/2025/04/24/lZgqfCW5kKYDETa.png)





## SpringSecurity整合JWT

客户端发起的请求中会携带这样的的特殊请求头Authorization,里面包含jwt令牌

```java
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJzZWxmIiwic3ViIjoidXNlciIsImV4cCI6MTY5MDIxODE2NCwiaWF0IjoxNjkwMTgyMTY0LCJzY29wZSI6ImFwcCJ9.Z5-WMeulZyx60WeNxrQg2z2GiVquEHrsBl9V4dixbRkAD6rFp-6gCrcAXWkebs0i-we4xTQ7TZW0ltuhGYZ1GmEaj4F6BP9VN8fLq2aT7GhCJDgjikaTs-w5BbbOD2PN_vTAK_KeVGvYhWU4_l81cvilJWVXAhzMtwgPsz1Dkd04cWTCpI7ZZi-RQaBGYlullXtUrehYcjprla8N-bSpmeb3CBVM3kpAdehzfRpAGWXotN27PIKyAbtiJ0rqdvRmvlSztNY0_1IoO4TprMTUr-wjilGbJ5QTQaYUKRHcK3OJrProz9m8ztClSq0GRvFIB7HuMlYWNYwf7lkKpGvKDg
```



### JwtUtils

这种你直接复制粘贴就好了

包含创建jwt令牌和解析jwt令牌的方法。实体类用的是security的UserDetails，自己写一个也没问题

```java
public class JwtUtils {
  	//Jwt秘钥
    private static final String key = "abcdefghijklmn";

  	//根据用户信息创建Jwt令牌
    public static String createJwt(UserDetails user){
        Algorithm algorithm = Algorithm.HMAC256(key);
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.SECOND, 3600 * 24 * 7);
        return JWT.create()
                .withClaim("name", user.getUsername())  //配置JWT自定义信息
                .withClaim("authorities", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(calendar.getTime())  //设置过期时间
                .withIssuedAt(now)    //设置创建创建时间
                .sign(algorithm);   //最终签名
    }

  	//根据Jwt验证并解析用户信息
    public static UserDetails resolveJwt(String token){
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);  //对JWT令牌进行验证，看看是否被修改
            Map<String, Claim> claims = verify.getClaims();  //获取令牌中内容
            if(new Date().after(claims.get("exp").asDate())) //如果是过期令牌则返回null
                return null;
            else
              	//重新组装为UserDetails对象，包括用户名、授权信息等
                return User
                        .withUsername(claims.get("name").asString())
                        .password("")
                        .authorities(claims.get("authorities").asArray(String.class))
                        .build();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
```



### JwtAuthenticationFilter(这是处理jwt的过滤器，加入到security默认的过滤器链中)

从请求头取出jwt，解析，如果可以，就解析成UserDetails对象，塞给SecurityContext表示完成验证。(因为只要SecurityContext有验证信息，就代表是验证通过的)

如果没有就直接放行，走下一个过滤器。

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {  
//继承OncePerRequestFilter表示每次请求过滤一次，用于快速编写JWT校验规则

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      	//首先从Header中取出JWT
        String authorization = request.getHeader("Authorization");
      	//判断是否包含JWT且格式正确
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
          	//开始解析成UserDetails对象，如果得到的是null说明解析失败，JWT有问题
            UserDetails user = JwtUtils.resolveJwt(token);
            if(user != null) {
              	//验证没有问题，那么就可以开始创建Authentication了，这里我们跟默认情况保持一致
              	//使用UsernamePasswordAuthenticationToken作为实体，填写相关用户信息进去
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              	//然后直接把配置好的Authentication塞给SecurityContext表示已经完成验证
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
      	//最后放行，继续下一个过滤器
      	//可能各位小伙伴会好奇，要是没验证成功不是应该拦截吗？这个其实没有关系的
      	//因为如果没有验证失败上面是不会给SecurityContext设置Authentication的，后面直接就被拦截掉了
      	//而且有可能用户发起的是用户名密码登录请求，这种情况也要放行的，不然怎么登录，所以说直接放行就好
        filterChain.doFilter(request, response);
    }
}
```



#### 然后去SecurityConfigration里面的SecurityFilterChain里面配置我们的过滤器JwtAuthenticationFilter

在用户名和密码的校验之前，去校验我们的jwt



# p3:功能：登录，返回登录成功或失败，统一相应格式RestBean

securityconfigration

RestBean：统一登录成功失败相应格式

# p4:功能：登录成功，服务器要创建jwt并返回给用户(AuthorizeVo统一相应)

securityconfigration

jwtutils

AuthorizeVo

# p5：1：功能:直接请求不行，请求中要携带token才能请求成功，并且服务器知道你是哪个用户。2：用户验证通过，但这个角色比如user没法去访问管理员权限的页面(登录但没权限)

1：

请求头中携带jwt，我们肯定要进行解析。要实现这个功能，我们就要按照security内部的机制来走，把我们自己写的过滤器加入到security的过滤器链里面去。

jwtutils：封好工具方法

JwtAuthenticationFilter：写具体从请求中获取authorization，从中解析jwt令牌，获取具体的用户信息，都合法的话放到Security上下文中代表认证通过

testcontroller测试

securityconfigration：最后写好过滤器之后，还要把过滤器加入到security的过滤器链，去securityconfigration里面配置.addFilterBefore。接下来配置没有登录的情况下，返回的处理.exceptionHandling。RestBean统一返回401什么的unauthorized

2：

封装一个RestBean做返回信息，403.forbidden

securityconfigration：没权限，走.exceptionHandling下的.accessDeniedHandler(this::handleProcess)。自己再写一个方法引用







# p6:功能：用户退出登录(后端黑名单token)(也可以用redis持久化这个功能，存黑名单列表)

黑名单方案:这个token已经退出登录，不能再用，下次用户登录查一下有没有这个黑名单token，有的话就不能登录

jwtutils：1：完善creatjwt()给每个jwt设置一个唯一的随机的uuid，2：补一个deletetoken()方法，3：完善invalidatejwt()让jwt令牌失效方法处理加入黑名单。4:然后再完善resolvejwt()方法判断uuid是否处于黑名单中.5:补充isInvalidatetoken()判断令牌是否过期。方法里面的StringRedisTemplate template可以理解为是和redis建立链接,在其他方法里面使用，就完成了存入redis持久化，redis黑名单机制

const:存储经常使用的常量

securityconfigration:然后去退出登录的方法里面完善逻辑. .logoutSuccessHandler(this::onLogoutSuccess)



redis实现黑名单机制：

1：导入依赖

加uuid，退出登录的话，就把uuid直接存到redis里面的黑名单。下次登录的时候检查一下这个uuid是否被拉入黑名单





# 功能：自动续签jwt令牌





# p7:不再用security默认的账户信息，我们创建新的实体类存储我们的用户信息

建表

DTO:account

Accountmapper:用户相关信息查询

AccountService接口

AccountServiceImpl





# p8：把DTO转为VO返回给前端(因为Dto可能有密码啥的，不适合直接返回)(BaseData，利用反射，可以直接复制，理解方法怎么用和传什么参数就好了)

这种写法本质内部是用的反射写的，所以高级，但是可读性有点差，但其实挺方便，还是建议使用。或者利用官方自带的BeanUtils.copyProperties()方法

建立一个接口basedata，加一个方法快速转化成指定的vo对象。然后用DTO里面的Account去实现basedata，就能调用里面的方法快速转换。这个涉及反射，可以直接复制

## 1：dto和vo共有的字段直接转化

basedata：声明方法

Account实现basedata接口

securityconfigration：利用account调用basedata的方法进行转换，并且我们指定转换为哪个vo对象就好了

![image-20250508105938572](https://s2.loli.net/2025/05/08/36cDfombT8SKVrX.png)



## 2：vo特有的字段再特殊设置

那还有一些vo特有的字段怎么办呢？总不可能一个个设置吧，再加一个asViewObject(Class < V > *clazz*, Consumer < V >(消费型)。所以basedata里面有两个教asViewObject的方法，总之来说就是lambda，就是调用这个方法，去自己设置自己的值。这样就写到一起，比较高级。

![image-20250508110839418](https://s2.loli.net/2025/05/08/Kmx3QcFfdLtgMlE.png)





# p9，p10：前端：先设计，先配置路由，然后再写组件

![image-20250508111156237](https://s2.loli.net/2025/05/08/5Wnc3eXAE2QUSxj.png)

让我来帮你分析这个登录页面的组件结构。首先我来搜索相关的代码文件。

Search files...
让我来为你分析这个登录页面的组件结构和它们之间的关系：

1. 整体布局组件：`WelcomeView.vue`
- 这是登录页面的父组件，提供了整体的页面布局
- 左侧是一个背景图片
- 中间有欢迎文字
- 右侧是一个卡片区域，用于显示子路由组件（登录、注册、忘记密码等）

2. 登录组件：`LoginPage.vue`
- 作为`WelcomeView.vue`的子组件通过路由显示
- 包含以下主要功能：
  - 用户名/邮箱输入框
  - 密码输入框
  - "记住我"复选框
  - 登录按钮
  - 忘记密码链接
  - 注册账号按钮

3. 注册组件：`RegisterPage.vue`
- 同样作为`WelcomeView.vue`的子组件
- 包含注册表单：
  - 用户名输入
  - 密码输入
  - 确认密码
  - 邮箱输入
  - 验证码功能

4. 忘记密码组件：`ForgetPage.vue`
- 也是`WelcomeView.vue`的子组件
- 采用步骤式设计：
  - 第一步：验证邮箱
  - 第二步：重置密码

这些组件之间的关系是通过Vue Router进行管理的，在`router/index.js`中可以看到路由配置：

```javascript
{
    path: '/',
    name: 'welcome',
    component: () => import('@/views/WelcomeView.vue'),
    children: [
        {
            path: '',
            name: 'welcome-login',
            component: () => import('@/views/welcome/LoginPage.vue')
        }, {
            path: 'register',
            name: 'welcome-register',
            component: () => import('@/views/welcome/RegisterPage.vue')
        }, {
            path: 'forget',
            name: 'welcome-forget',
            component: () => import('@/views/welcome/ForgetPage.vue')
        }
    ]
}
```

实现方式：

1. 状态管理：
- 使用Pinia进行状态管理（`store/index.js`）
- 存储用户信息、头像等数据

2. 网络请求：
- 使用封装的`net/index.js`处理所有API请求
- 包含登录、注册、重置密码等接口调用

3. 组件通信：
- 父子组件通过路由进行切换
- 使用Pinia store进行全局状态管理
- 使用事件总线处理组件间的通信

4. 路由守卫：
- 在`router/index.js`中实现了路由守卫
- 用于控制未登录用户访问权限
- 处理已登录用户的重定向

5. UI实现：
- 使用Element Plus组件库
- 包含表单验证
- 响应式设计
- 过渡动画效果

这是一个典型的Vue3单页应用架构，采用了组件化、模块化的设计思想，通过路由系统实现页面切换，通过状态管理实现数据共享，通过API封装实现后端交互。

### WelcomeView.vue是整个页面的所有部分。只不过右边那部分是通过 < router-view >渲染的

让我详细解释一下`WelcomeView.vue`的布局结构。

从代码中可以看到，`WelcomeView.vue`是整个欢迎页面的容器组件，包含了整个页面的所有部分。让我们看看它的模板结构：

```1:19:src/views/WelcomeView.vue
<template>
    <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
        <div style="flex: 1">
            <el-image style="width: 100%;height: 100%" fit="cover"
                      src="https://img1.baidu.com/it/u=4097856652,4033702227&fm=253&fmt=auto&app=120&f=JPEG?w=1422&h=800"/>
        </div>
        <div class="welcome-title">
            <div style="font-size: 30px;font-weight: bold">欢迎来到我们的学习平台</div>
            <div style="margin-top: 10px">在这里你可以学习如何使用Java，如何搭建网站，并且与Java之父密切交流。</div>
            <div style="margin-top: 5px">在这里你可以同性交友，因为都是男的，没有学Java的女生。</div>
        </div>
        <div class="right-card">
            <router-view v-slot="{ Component }">
                <transition name="el-fade-in-linear" mode="out-in">
                    <component :is="Component" style="height: 100%"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>
```

`WelcomeView.vue`包含三个主要部分：

1. 左侧背景图片区域：
```vue
<div style="flex: 1">
    <el-image style="width: 100%;height: 100%" fit="cover" src="..."/>
</div>
```

2. 中间的欢迎文字（绝对定位在左下角）：
```vue
<div class="welcome-title">
    <div style="font-size: 30px;font-weight: bold">欢迎来到我们的学习平台</div>
    <div style="margin-top: 10px">在这里你可以学习如何使用Java...</div>
    <div style="margin-top: 5px">在这里你可以同性交友...</div>
</div>
```

3. 右侧卡片区域（用于显示登录、注册等组件）：
```vue
<div class="right-card">
    <router-view>...</router-view>
</div>
```

从样式定义可以看出：

```25:41:src/views/WelcomeView.vue
<style scoped>
.right-card {
  width: 400px;
  z-index: 1;
  background-color: var(--el-bg-color);
}

.welcome-title {
    position: absolute;
    bottom: 30px;
    left: 30px;
    color: white;
    text-shadow: 0 0 10px black;
}
</style>
```

所以，`WelcomeView.vue`不是只包含左边部分，而是整个欢迎页面的容器组件，包含：
- 左边的全屏背景图
- 左下角的欢迎文字
- 右边固定宽度（400px）的白色卡片区域，用于显示登录/注册表单

这种布局设计是一个常见的登录页面模式，左侧是大图展示区域，右侧是功能操作区域。通过`router-view`在右侧卡片中动态切换显示不同的功能组件（登录、注册、忘记密码等）。

### 是怎么进行切换的？

右侧白色区域的切换是通过Vue Router实现的。让我详细解释这个切换机制：

1. 首先在路由配置文件`router/index.js`中定义了路由规则：

```javascript
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',           // 默认子路由，访问 / 时显示
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',    // 访问 /register 时显示
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',      // 访问 /forget 时显示
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        }
    ]
})
```

2. 在`WelcomeView.vue`中使用`router-view`来显示子路由组件：

```vue
<div class="right-card">
    <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
            <component :is="Component" style="height: 100%"/>
        </transition>
    </router-view>
</div>
```

3. 在各个组件中通过路由跳转触发切换：

比如在`LoginPage.vue`中：
```vue
<el-link @click="router.push('/forget')">忘记密码？</el-link>
...
<el-button @click="router.push('/register')" type="warning" plain>注册账号</el-button>
```

在`RegisterPage.vue`中：
```vue
<el-link type="primary" @click="router.push('/')">立即登录</el-link>
```

切换过程是这样的：

1. 用户点击"注册账号"按钮
2. 触发`router.push('/register')`
3. Vue Router检测到路由变化
4. 找到匹配的路由配置（`/register`）
5. 加载`RegisterPage.vue`组件
6. `router-view`将新组件渲染到右侧白色区域
7. 同时会应用过渡动画（`el-fade-in-linear`）

特别注意的是过渡动画的实现：
```vue
<router-view v-slot="{ Component }">
    <transition name="el-fade-in-linear" mode="out-in">
        <component :is="Component" style="height: 100%"/>
    </transition>
</router-view>
```
- `transition`组件提供了动画效果
- `mode="out-in"`表示先移除旧组件，再显示新组件
- `el-fade-in-linear`是Element Plus提供的渐变动画

这种设计的优点是：
1. 保持了页面的整体布局不变
2. 只需要更新右侧内容区域
3. 提供了平滑的过渡动画
4. 组件按需加载，提高性能
5. 路由可以被浏览器记录，支持前进后退



#### 我可以这么理解吗WelcomeView.vue是整个页面的所有部分。包括右边那部分的白色区域，只不过右边白色区域内部那部分是通过 < router-view >渲染的

是的，你的理解完全正确！

让我用一个简单的图来说明这个结构：

```
WelcomeView.vue
+------------------------------------------+
|                                    |      |
|  左侧背景图                        |白色区|
|                                    |域    |
|                                    |(.right-card)|
|                                    |      |
|                                    |<router-view>|
|  欢迎文字                          |在这里渲染：|
|  (welcome-title)                   |- LoginPage |
|                                    |- RegisterPage|
|                                    |- ForgetPage|
|                                    |      |
+------------------------------------------+
```

所以：
1. `WelcomeView.vue`确实包含了整个页面的所有部分：
   - 左侧的背景图
   - 左下角的欢迎文字
   - 右侧的白色区域容器

2. 右侧白色区域（`.right-card`）是`WelcomeView.vue`的一部分，但它里面的具体内容：
   - 是通过`<router-view>`动态渲染的
   - 可以是`LoginPage.vue`
   - 可以是`RegisterPage.vue`
   - 可以是`ForgetPage.vue`

这就像是`WelcomeView.vue`提供了一个"画框"，而`<router-view>`决定在这个"画框"里展示哪幅"画"（登录页、注册页或忘记密码页）。





# p11:1：定义axios请求(net包下)，2：再去原页面写函数调用axios请求(组件)，3：再去组件绑定自己写的函数(组件)





# p12：配置跨域，我们自己重新写一个CorsFilter.java，因为之后要写限流什么的，用security默认的不行，因为有些请求到不了security这里。

配置跨域的话：只需要在响应头里面告诉浏览器，我们是支持这个站点





# p13：退出登录axios请求，以及路由守卫：防止用户没有登录就直接访问登录成功之后的界面

路由守卫：router文件夹下的index.js的router.beforeEach





# p14：1：用户请求验证码。2：然后用户拿着验证码来进行注册操作

用户申请验证码邮件，我们把验证码邮件丢到消息队列里面，再有我们的监听器消费消息队列里面的验证码请求

application.yml配置邮箱，

Configuration.java配置Rabbit。

去AccountService写方法，调用rabbitmq提供的rabbitTemplate操作rabbitmq

FlowUtils还有不同发起太多请求，所以我们要限流

listener创建监听器消费消息队列里面的消息

Authorizecontroller接受邮件验证码请求

是的，您的理解是正确的。根据之前查看的代码，验证码的获取流程如下：

### 验证码获取流程

1. **前端发起验证码请求**：
   用户点击"获取验证码"按钮，前端通过 GET 请求调用 `/api/auth/ask-code` 接口，传递邮箱地址和验证类型（如 register、reset、modify）。

2. **后端生成随机验证码**：
   在 `AccountServiceImpl` 的 `registerEmailVerifyCode` 方法中：
   ```java
   Random random = new Random();
   int code = random.nextInt(899999) + 100000;  // 生成6位随机数字
   ```

3. **存储验证码到 Redis**：
   ```java
   String redisKey = Const.VERIFY_EMAIL_DATA + email;  // 构建Redis键
   stringRedisTemplate.opsForValue()
          .set(redisKey, String.valueOf(code), 3, TimeUnit.MINUTES);  // 存储验证码，有效期3分钟
   ```
   Redis 键格式为：`verify:email:data:用户邮箱`

4. **发送验证码到用户邮箱**：
   ```java
   Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
   rabbitTemplate.convertAndSend(Const.MQ_MAIL, data);  // 通过消息队列发送邮件任务
   ```
   系统使用 RabbitMQ 消息队列异步发送邮件，邮件中包含验证码。

5. **用户收到并输入验证码**：
   用户从邮箱中获取验证码，并在注册表单中填写。

6. **验证码校验过程**：
   当用户提交注册请求时，在 `AccountServiceImpl` 的 `registerEmailAccount` 方法中：
   ```java
   String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);  // 从Redis获取之前存储的验证码
   if(code == null) return "请先获取验证码";
   if(!code.equals(vo.getCode())) return "验证码错误,请重新输入";
   ```

7. **注册成功后清理验证码**：
   ```java
   if(this.save(account)){
       stringRedisTemplate.delete(redisKey);  // 注册成功后删除Redis中的验证码
       return null;
   }
   ```

### 技术细节

- **Redis 存储**：使用键值对存储，键是邮箱地址加前缀，值是验证码
- **有效期**：验证码在 Redis 中的有效期为 3 分钟
- **异步发送**：使用 RabbitMQ 消息队列异步发送邮件，不阻塞主请求
- **防刷机制**：系统实现了 IP 限流功能，防止频繁请求验证码
  ```java
  if(!this.verifyLimit(address))
      return "请求频繁，请稍后再试";
  ```

这种设计有以下优点：
1. 验证码存储在 Redis 中，即使应用重启也不会丢失
2. 设置了有效期，提高安全性
3. 使用消息队列发送邮件，提高系统响应速度
4. 实现了防刷机制，增强安全性

整个流程设计合理，符合现代Web应用的安全最佳实践。



# P15:注册接口实现：用户提交表单json，我们mapper向后端拿邮箱和手机号，再和json对比写逻辑，如果没有就可以注册。

```mermaid
sequenceDiagram
    participant 前端
    participant 后端
    participant 数据库
    participant 第三方服务

    前端->>后端: 1. 提交注册信息（邮箱+密码+验证码）
    后端->>后端: 2. 校验数据格式（邮箱格式、密码强度）
    后端->>数据库: 3. 检查邮箱是否已注册
    数据库-->>后端: 返回查询结果
    后端->>后端: 4. 验证码校验（Redis比对）
    后端->>后端: 5. 密码加密（BCrypt）
    后端->>数据库: 6. 保存用户信息
    后端->>第三方服务: 7. 发送欢迎邮件（异步）
    后端-->>前端: 8. 返回注册结果

```







接口参数校验：导入依赖。如果用户发送不规范的话，我们再定义一个exceptionController，让返回结果更规范一点

## 总结您遇到的问题和解决方法：

1. 安全认证问题：

- 最初问题：请求返回401错误 "认证失败: Full authentication is required to access this resource"

- 原因：Spring Security配置中，JWT过滤器没有正确跳过/api/auth/**路径

- 解决方法：修改JwtAuthenticationFilter中的shouldNotFilter方法，确保它跳过所有/api/auth/**路径的请求

1. 数据库连接问题：

- 问题：应用尝试连接不存在的数据库security_db，后来又尝试连接不存在的db_account数据库

- 原因：配置文件中指定的数据库名称与实际不符

- 解决方法：在application-dev.yml中修改数据库URL为正确的数据库名称test

1. 端口占用问题：

- 问题：应用启动失败，显示"Port 8080 was already in use"，后来又是"Port 8081 was already in use"

- 原因：之前启动的应用实例仍在后台运行，没有彻底关闭

- 解决方法：使用netstat命令找到占用端口的进程，然后用taskkill命令强制终止

1. 邮件发送问题：

- 问题：邮件无法发送，显示"Invalid Addresses"错误

- 原因：邮件地址格式不正确，缺少"@"符号

- 解决方法：在MailQueueListener中添加邮件地址格式检查和更详细的错误处理

1. RabbitMQ连接问题：

- 问题：RabbitMQ消息队列连接失败

- 原因：端口25672已被占用，可能是另一个RabbitMQ实例正在运行

- 影响：不影响核心功能，只影响邮件发送

通过这些修改，您的应用程序现在可以正常运行，用户能够成功注册。尽管邮件发送功能可能仍有一些问题，但核心的注册和身份验证功能已经正常工作。

总的来说，问题主要集中在配置上：安全配置、数据库配置和端口配置。这些是Spring Boot应用中最常见的问题类型，通过适当的配置和调试，您已经成功解决了它们

## 注册的流程

目前的话，主流的注册一般是前端提交表单数据，然后我们配置转化为json发送到后端，然后到数据库校验，返回json给前端校验对吗



是的，目前主流的前后端分离架构中，用户注册流程通常如下：

---

### **1. 前端处理**
- **表单提交**：用户填写注册表单（用户名、密码、邮箱等）
- **前端校验**：先进行基础校验（如非空、密码复杂度、邮箱格式等）
- **转为 JSON**：将表单数据组装成 JSON 格式（例如 `{"username":"test","password":"123456","email":"test@example.com"}`）
- **HTTP 请求**：通过 `POST` 请求发送到后端 API（如 `/api/auth/register`）

---

### **2. 后端处理**
- **接收数据**：后端（Spring Boot/Django/Node.js 等）接收 JSON 数据并解析为对象。
- **数据校验**：
  - **基础校验**：检查字段是否合法（如长度、格式）。
  - **业务校验**：检查用户名/邮箱是否已存在（查询数据库）。
- **数据持久化**：
  - 密码加密（如 BCrypt）。
  - 将用户数据存入数据库（如 MySQL/MongoDB）。
- **生成响应**：
  - 成功：返回 HTTP 200 或 201，附带 JSON 格式的成功消息（如 `{"code":200, "message":"注册成功"}`）。
  - 失败：返回 HTTP 400/409 等，附带错误原因（如 `{"code":409, "message":"用户名已存在"}`）。

---

### **3. 数据库交互**
- 后端通过 ORM（如 MyBatis/Hibernate）或原生 SQL 操作数据库。
- 关键操作：
  ```sql
  SELECT * FROM users WHERE username = ? OR email = ?; -- 校验是否已存在
  INSERT INTO users (username, password, email) VALUES (?, ?, ?); -- 注册新用户
  ```

---

### **4. 前端响应**
- 根据后端返回的 JSON 结果：
  - 成功：跳转到登录页或显示成功提示。
  - 失败：在表单中显示错误信息（如“邮箱已被注册”）。

---



# p16：注册界面编写

const loginForm = reactive({  username: '',  password: '',  remember: false })这些才vue中相当于是定义数据，定义对象，的数据。然后可以操作它

< input v-model="loginForm.username" placeholder="用户名">    < input v-model="loginForm.password" type="password" placeholder="密码">这里就相当于是从用户输入的表单获取数据，赋予给我们定义的这些值



# p17：注册完善：前端发送axios到后端

声明axios，调用axios(中间会加一层js函数做中介)

所以一般是先声明axios，然后注册界面那里先定义一个函数调用axios，然后组件使用函数

# p18:重置密码页面设计：

先声明路由，然后使用路由，就能实现路由页面跳转(axios请求也是如此类似)(中间可以加层js函数作为中介)

重置密码：1：先验证邮箱（有没有权限）.2：再重置密码

可以用elementui的进度条来使用户体验更好





# p19：重置密码后端

## 方案一：(要往redis存标记)

用户先带着验证码请求对应接口，然后后端存储对应用户已经通过的标记，用户填写新的密码之后 然后请求重置密码的接口，接口验证是否已经通过，然后才重置密码

## 方案二：

用户带着验证码请求对应接口，然后后端仅对验证码是否正确进行验证(后端会返回正确或者错误给前端，前端判断，正确就下一步)，用户填写新的密码之后，请求重置密码接口，不仅需要带上密码还要之前的验证码一起，然后再次验证验证码如果正确，就重置密码

这里我们使用方案二

## 核心流程

您的项目中重置密码的完整流程如下：

1. **忘记密码入口**
   - 用户在登录界面点击"忘记密码"链接
   - 系统跳转到忘记密码页面(ForgetPage.vue)

2. **邮箱验证与验证码获取（ForgetPage.vue - 第一步）**
   - 用户输入需要重置密码的邮箱地址
   - 用户点击"获取验证码"按钮
   - 前端调用API: `GET /api/auth/ask-code?email=用户邮箱&type=reset`
   - 后端生成6位数字验证码并发送到用户邮箱
   - 前端显示倒计时(60秒)防止频繁请求

3. **验证码校验（ForgetPage.vue）**
   - 用户收到邮件并输入验证码
   - 用户点击"验证并继续"按钮
   - 前端调用API: `POST /api/auth/reset-confirm` 发送邮箱和验证码
   - 后端验证验证码是否正确
   - 验证成功后，前端自动切换到第二步界面(同一页面内切换)

4. **重置密码（ForgetPage.vue - 第二步）**
   - 用户输入新密码和确认密码
   - 系统检查两次输入是否一致及密码强度
   - 用户点击"确认重置"按钮
   - 前端调用API: `POST /api/auth/reset-password` 发送邮箱、验证码和新密码
   - 后端再次验证验证码，然后更新用户密码
   - 更新成功后，系统显示"密码重置成功"消息
   - 2秒后自动跳转到登录页面

5. **后端处理流程**
   - 验证码在Redis中存储3分钟
   - 重置密码成功后，系统自动删除Redis中的验证码
   - 密码使用加密算法存储到数据库中

整个流程遵循电子邮件验证的安全标准，确保只有能够访问该邮箱的用户才能重置密码，并通过验证码的时效性(3分钟)增加了安全性。



# p20：实现限流操作

自定义一个FlowLimitFilter加入到security的过滤器链中，在跨域之后。

我们限定用户在某时间段不能超过20次(假如)，如果计数超过20次，我们就封禁它，封个几分钟或几秒钟。或者不让他发请求了。





# p21：深色模式(利用element-plus给我们提供好的)
