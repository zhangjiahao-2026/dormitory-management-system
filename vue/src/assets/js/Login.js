import request from "@/utils/request";

const {ElMessage} = require("element-plus");
export default {
    name: "Login",
    data() {
        return {
            identity: "",
            loginError: "",
            submitting: false,
            form: {
                username: "",
                password: "",
                identity: "",
            },
            rules: {
                username: [
                    {required: true, message: "请输入用户名", trigger: "blur"},
                ],
                password: [{required: true, message: "请输入密码", trigger: "blur"}],
                identity: [{required: true, message: "请选择身份", trigger: "blur"}],
            },
        };
    },
    computed: {
        disabled() {
            const {username, password, identity} = this.form;
            return Boolean(username && password && identity && !this.submitting);
        },
    },
    methods: {
        selectRole(identity) {
            this.form.identity = identity;
            this.loginError = "";
        },
        login() {
            this.$refs.form.validate(async (valid) => {
                if (valid && !this.submitting) {
                    this.loginError = "";
                    this.submitting = true;
                    this.identity = this.form.identity;
                    try {
                        const response = await request.post("/" + this.identity + "/login", this.form);
                        // 兼容响应拦截器热更新前后的两种形态：Result 或 AxiosResponse<Result>。
                        const res = response && response.data && response.code === undefined
                            ? response.data
                            : response;
                        if (res && String(res.code) === "0") {
                            ElMessage({
                                message: "登陆成功",
                                type: "success",
                            });
                            // 登陆成功跳转主页
                            window.sessionStorage.setItem("user", JSON.stringify(res.data));
                            window.sessionStorage.setItem("identity", JSON.stringify(this.form.identity));
                            this.$router.replace({path: this.form.identity === "stu" ? "/myRoomInfo" : "/home"});
                        } else {
                            this.loginError = res?.msg || "登录失败，请检查账号、密码和身份";
                        }
                    } catch (error) {
                        this.loginError = error.response?.data?.msg || "无法连接后端服务，请确认后端已启动";
                    } finally {
                        this.submitting = false;
                    }
                }
            });
        },
    },
};
