import request from "@/utils/request";

const {ElMessage} = require("element-plus");

export default {
    name: "ApplyRepairInfo",
    data() {
        return {
            name: "",
            username: "",
            form: {
                dormBuildId: "",
                dormRoomId: "",
                repairer: "",
                title: "",
                content: "",
                orderBuildTime: "",
            },
            rules: {
                dormBuildId: [{required: true, message: "未获取到楼宇号", trigger: "blur"}],
                dormRoomId: [{required: true, message: "未获取到房间号", trigger: "blur"}],
                repairer: [{required: true, message: "未获取到申请人", trigger: "blur"}],
                title: [{required: true, message: "请输入标题", trigger: "blur"}],
                content: [{required: true, message: "请输入内容", trigger: "blur"}],
                orderBuildTime: [{required: true, message: "请选择创建时间", trigger: "change"}],
            },
        };
    },
    created() {
        this.init();
        this.getInfo();
    },
    methods: {
        init() {
            const user = JSON.parse(sessionStorage.getItem("user"));
            this.name = user.name;
            this.username = user.username;
            this.form.repairer = this.name;
        },
        getInfo() {
            request.get("/room/getMyRoom/" + this.username).then((res) => {
                if (res.code === "0") {
                    this.form.dormBuildId = res.data.dormBuildId;
                    this.form.dormRoomId = res.data.dormRoomId;
                } else {
                    ElMessage({message: res.msg, type: "error"});
                }
            });
        },
        resetApplyFields() {
            this.form.title = "";
            this.form.content = "";
            this.form.orderBuildTime = "";
            this.$nextTick(() => {
                this.$refs.form.clearValidate();
            });
        },
        save() {
            this.$refs.form.validate((valid) => {
                if (!valid) return;
                request.post("/repair/add", this.form).then((res) => {
                    if (res.code === "0") {
                        ElMessage({message: "报修提交成功", type: "success"});
                        this.resetApplyFields();
                    } else {
                        ElMessage({message: res.msg, type: "error"});
                    }
                });
            });
        },
    },
};
