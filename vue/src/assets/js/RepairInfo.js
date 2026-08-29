import request from "@/utils/request";

const {ElMessage, ElMessageBox} = require("element-plus");

export default {
    name: "RepairInfo",
    components: {},
    data() {
        const checkOrderState = (rule, value, callback) => {
            if (this.judge) {
                if (value === "未完成" && this.form.orderFinishTime === null) {
                    callback();
                } else if (value === "完成" && this.form.orderFinishTime !== null) {
                    callback();
                } else {
                    callback(new Error("请检查订单完成状态与选择时间是否匹配"));
                }
            } else {
                callback();
            }
        };
        return {
            buildTimeDisabled: true,
            loading: true,
            disabled: false,
            judge: false,
            dialogVisible: false,
            detailDialog: false,
            search: "",
            currentPage: 1,
            pageSize: 10,
            total: 0,
            tableData: [],
            detail: {},
            form: {},
            rules: {
                title: [{required: true, message: "请输入标题", trigger: "blur"}],
                content: [{required: true, message: "请输入内容", trigger: "blur"}],
                repairer: [
                    {required: true, message: "请输入申请人", trigger: "blur"},
                ],
                orderBuildTime: [
                    {required: true, message: "请选择时间", trigger: "blur"},
                ],
                state: [{validator: checkOrderState, trigger: "blur"}],
            },
            finishTime: {
                display: "none",
            },
            aiLoading: false,
            aiSubmitting: false,
            aiPending: [],
            aiTotal: 0,
            aiPageNum: 1,
            aiPageSize: 10,
            aiSearch: "",
            aiDialog: false,
            aiDetail: {},
            aiDecision: {},
            categoryOptions: [
                {value: "ELECTRICAL", label: "电路故障"},
                {value: "PLUMBING", label: "给排水故障"},
                {value: "NETWORK", label: "网络故障"},
                {value: "DOOR_LOCK", label: "门锁故障"},
                {value: "AIR_CONDITIONER", label: "空调故障"},
                {value: "FURNITURE", label: "家具设施"},
                {value: "PUBLIC_AREA", label: "公共区域"},
                {value: "OTHER", label: "其他"},
            ],
            urgencyOptions: [
                {value: "EMERGENCY", label: "紧急"},
                {value: "HIGH", label: "高"},
                {value: "NORMAL", label: "普通"},
                {value: "LOW", label: "低"},
            ],
            departmentOptions: [
                {value: "WATER_ELECTRIC", label: "水电维修组"},
                {value: "NETWORK_OPERATIONS", label: "网络运维组"},
                {value: "FACILITY_MAINTENANCE", label: "设施维修组"},
                {value: "CAMPUS_EMERGENCY", label: "校园应急协调组"},
                {value: "GENERAL_SERVICES", label: "综合维修组"},
            ],
        };
    },
    created() {
        this.load();
        this.loadAiPending();
        this.loading = true;
        setTimeout(() => {
            //设置延迟执行
            this.loading = false;
        }, 1000);
    },
    methods: {
        async load() {
            request.get("/repair/find", {
                params: {
                    pageNum: this.currentPage,
                    pageSize: this.pageSize,
                    search: this.search,
                },
            }).then((res) => {
                console.log(res);
                this.tableData = res.data.records;
                this.total = res.data.total;
                this.loading = false;
            });
        },
        reset() {
            this.search = ''
            request.get("/repair/find", {
                params: {
                    pageNum: 1,
                    pageSize: this.pageSize,
                    search: this.search,
                },
            }).then((res) => {
                console.log(res);
                this.tableData = res.data.records;
                this.total = res.data.total;
                this.loading = false;
            });
        },
        filterTag(value, row) {
            return row.state === value;
        },
        showDetail(row) {
            this.detailDialog = true;
            this.$nextTick(() => {
                this.detail = row;
            });
        },
        closeDetails() {
            this.detailDialog = false;
        },
        add() {
            this.dialogVisible = true;
            this.$nextTick(() => {
                this.$refs.form.resetFields();
                this.buildTimeDisabled = false;
                this.finishTime = {display: "none"};
                this.disabled = false;
                this.form = {};
                this.judge = false;
            });
        },
        save() {
            this.$refs.form.validate(async (valid) => {
                if (valid) {
                    if (this.judge === false) {
                        //新增
                        await request.post("/repair/add", this.form).then((res) => {
                            console.log(res);
                            if (res.code === "0") {
                                ElMessage({
                                    message: "新增成功",
                                    type: "success",
                                });
                                this.search = "";
                                this.load();
                                this.dialogVisible = false;
                            } else {
                                ElMessage({
                                    message: res.msg,
                                    type: "error",
                                });
                            }
                        });
                    } else {
                        //修改
                        await request.put("/repair/update", this.form).then((res) => {
                            console.log(res);
                            if (res.code === "0") {
                                ElMessage({
                                    message: "修改成功",
                                    type: "success",
                                });
                                this.search = "";
                                this.load();
                                this.dialogVisible = false;
                            } else {
                                ElMessage({
                                    message: res.msg,
                                    type: "error",
                                });
                            }
                        });
                    }
                }
            });
        },
        cancel() {
            this.$refs.form.resetFields();
            this.dialogVisible = false;
        },
        handleEdit(row) {
            //修改
            this.judge = true;
            this.dialogVisible = true;
            this.$nextTick(() => {
                this.$refs.form.resetFields();
                // 生拷贝
                this.form = JSON.parse(JSON.stringify(row));
                this.disabled = true;
                this.buildTimeDisabled = true;
                this.finishTime = {display: "flex"};
            });
        },
        handleDelete(id) {
            console.log(id);
            request.delete("/repair/delete/" + id).then((res) => {
                if (res.code === "0") {
                    ElMessage({
                        message: "删除成功",
                        type: "success",
                    });
                    this.search = "";
                    this.load();
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            });
        },
        handleSizeChange(pageSize) {
            //改变每页个数
            this.pageSize = pageSize;
            this.load();
        },
        handleCurrentChange(pageNum) {
            //改变页码
            this.currentPage = pageNum;
            this.load();
        },
        loadAiPending() {
            this.aiLoading = true;
            request.get("/repair/ai/pending", {
                params: {pageNum: this.aiPageNum, pageSize: this.aiPageSize, search: this.aiSearch},
            }).then((res) => {
                if (res.code === "0") {
                    this.aiPending = res.data.records || [];
                    this.aiTotal = res.data.total || 0;
                } else {
                    ElMessage({message: res.msg, type: "error"});
                }
            }).finally(() => {
                this.aiLoading = false;
            });
        },
        openAiReview(row) {
            this.aiDetail = row;
            this.aiDecision = {
                category: row.category || "OTHER",
                urgency: row.urgency || "NORMAL",
                department: row.department || "GENERAL_SERVICES",
                operatorComment: "",
            };
            this.aiDialog = true;
        },
        confirmAi() {
            if (!this.aiDecision.category || !this.aiDecision.urgency || !this.aiDecision.department) {
                ElMessage({message: "请完整选择类别、紧急程度和处理部门", type: "warning"});
                return;
            }
            this.aiSubmitting = true;
            request.post(`/repair/ai/${this.aiDetail.requestId}/confirm`, this.aiDecision).then((res) => {
                if (res.code === "0") {
                    ElMessage({message: `正式工单已创建，工单号 ${res.data.repairId}`, type: "success"});
                    this.aiDialog = false;
                    this.loadAiPending();
                    this.load();
                } else {
                    ElMessage({message: res.msg, type: "error"});
                }
            }).finally(() => {
                this.aiSubmitting = false;
            });
        },
        rejectAi() {
            ElMessageBox.prompt("请输入拒绝原因", "拒绝 AI 报修申请", {
                confirmButtonText: "确认拒绝",
                cancelButtonText: "取消",
                inputValidator: value => Boolean(value && value.trim()),
                inputErrorMessage: "拒绝原因不能为空",
            }).then(({value}) => request.post(`/repair/ai/${this.aiDetail.requestId}/reject`, {reason: value})
                .then((res) => {
                    if (res.code === "0") {
                        ElMessage({message: "申请已拒绝", type: "success"});
                        this.aiDialog = false;
                        this.loadAiPending();
                    } else {
                        ElMessage({message: res.msg, type: "error"});
                    }
                })).catch(() => {});
        },
        parseJson(value) {
            if (Array.isArray(value)) return value;
            try {
                const parsed = JSON.parse(value || "[]");
                return Array.isArray(parsed) ? parsed : [];
            } catch (e) {
                return [];
            }
        },
        confidencePercent(value) {
            return `${Math.round((Number(value) || 0) * 100)}%`;
        },
    },
};
