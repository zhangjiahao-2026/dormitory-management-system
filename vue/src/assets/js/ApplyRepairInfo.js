import request from "@/utils/request";

const {ElMessage} = require("element-plus");

export default {
    name: "ApplyRepairInfo",
    data() {
        return {
            name: "",
            username: "",
            aiLoading: false,
            manualLoading: false,
            aiResult: null,
            myRequests: [],
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
        this.loadMine();
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
            this.aiResult = null;
            this.$nextTick(() => {
                this.$refs.form.clearValidate();
            });
        },
        save() {
            this.$refs.form.validate((valid) => {
                if (!valid) return;
                this.manualLoading = true;
                request.post("/repair/add", this.form).then((res) => {
                    if (res.code === "0") {
                        ElMessage({message: "报修提交成功", type: "success"});
                        this.resetApplyFields();
                    } else {
                        ElMessage({message: res.msg, type: "error"});
                    }
                }).catch(() => {
                    ElMessage({message: "人工报修提交失败，请稍后重试", type: "error"});
                }).finally(() => {
                    this.manualLoading = false;
                });
            });
        },
        analyzeAndSubmit() {
            if (!this.form.title || !this.form.content || this.form.content.trim().length < 4) {
                ElMessage({message: "请填写标题和至少 4 个字的问题描述", type: "warning"});
                return;
            }
            if (this.aiLoading) return;
            this.aiLoading = true;
            request.post("/repair/ai/analyze", {
                title: this.form.title,
                description: this.form.content,
            }).then((res) => {
                if (res.code === "0") {
                    this.aiResult = res.data.analysis;
                    ElMessage({message: "AI 分析完成，申请已提交宿管审核", type: "success"});
                    this.loadMine();
                } else {
                    ElMessage({message: res.msg || "AI 服务暂不可用，可直接提交人工报修", type: "warning"});
                }
            }).catch(() => {
                ElMessage({message: "AI 服务暂不可用，可直接提交人工报修", type: "warning"});
            }).finally(() => {
                this.aiLoading = false;
            });
        },
        loadMine() {
            request.get("/repair/ai/mine").then((res) => {
                if (res.code === "0") this.myRequests = res.data || [];
            });
        },
        confidencePercent(value) {
            return `${Math.round((Number(value) || 0) * 100)}%`;
        },
        urgencyName(value) {
            return {EMERGENCY: "紧急", HIGH: "高", NORMAL: "普通", LOW: "低"}[value] || value;
        },
        urgencyTag(value) {
            return {EMERGENCY: "danger", HIGH: "warning", NORMAL: "", LOW: "info"}[value] || "info";
        },
        requestStatusName(value) {
            return {PENDING_REVIEW: "待审核", CONFIRMED: "已确认", REJECTED: "已拒绝"}[value] || value;
        },
        requestStatusType(value) {
            return {PENDING_REVIEW: "warning", CONFIRMED: "success", REJECTED: "danger"}[value] || "info";
        },
    },
};
