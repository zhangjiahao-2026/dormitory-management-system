import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    timeout: 5000
})

// request 拦截器 —— 挂在 request 实例上，而非全局 axios
request.interceptors.request.use(function (config) {
    const userStr = window.sessionStorage.getItem('user');
    let user = userStr && userStr !== 'null' ? JSON.parse(userStr) : null;
    if (user) {
        config.headers['token'] = user.token || '';
    }
    return config;
}, function (error) {
    return Promise.reject(error);
});

// response 拦截器
request.interceptors.response.use(
    response => {
        let res = response.data;
        if (response.config.responseType === 'blob') {
            return res
        }
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        if (error.response && error.response.status === 401) {
            window.sessionStorage.removeItem('user')
            window.sessionStorage.removeItem('identity')
            window.location.href = '/Login'
        }
        return Promise.reject(error)
    }
)

export default request
