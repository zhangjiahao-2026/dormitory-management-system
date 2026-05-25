import request from "@/utils/request";

const {ElMessage} = require("element-plus");

export default {
    name: "ApplyChangeRoom",
    data() {
        const checkRoomState = (rule, value, callback) => {
            if (typeof value !== "number") {
                callback(new Error("请输入正确的房间号"));
                return;
            }
            this.dormRoomId = value;
            request.get("/room/checkRoomExist/" + value).then((existResult) => {
                if (existResult.code === "-1") {
                    callback(new Error(existResult.msg));
                    return;
                }
                request.get("/room/checkRoomState/" + value).then((stateResult) => {
                    if (stateResult.code === "-1") {
                        callback(new Error(stateResult.msg));
                        return;
                    }
                    callback();
                });
            });
        };
        const checkBedState = (rule, value, callback) => {
            if (typeof value !== "number") {
                callback(new Error("请输入正确的床位号"));
                return;
            }
            if (!this.dormRoomId) {
                callback(new Error("请先填写目标房间号"));
                return;
            }
            request.get("/room/checkBedState/" + this.dormRoomId + "/" + value).then((res) => {
                if (res.code === "0") {
                    callback();
                } else {
                    callback(new Error(res.msg));
                }
            });
        };
        return {
            dormRoomId: 0,
            form: {
                username: "",
                name: "",
                currentRoomId: "",
                currentBedId: "",
                towardsRoomId: "",
                towardsBedId: "",
                applyTime: "",
            },
            rules: {
                username: [{required: true, message: "请填写学号", trigger: "blur"}],
                name: [{required: true, message: "请填写姓名", trigger: "blur"}],
                currentRoomId: [{required: true, message: "未获取到当前房间号", trigger: "blur"}],
                currentBedId: [{required: true, message: "未获取到当前床位号", trigger: "blur"}],
                towardsRoomId: [{required: true, validator: checkRoomState, trigger: "blur"}],
                towardsBedId: [{required: true, validator: checkBedState, trigger: "blur"}],
                applyTime: [{required: true, message: "请选择申请时间", trigger: "change"}],
            },
        };
    },
    created() {
        this.init();
    },
    methods: {
        init() {
            const user = JSON.parse(sessionStorage.getItem("user"));
            this.form.username = user.username;
            this.form.name = user.name;
            request.get("/room/getMyRoom/" + this.form.username).then((res) => {
                if (res.code === "0") {
                    this.form.currentRoomId = res.data.dormRoomId;
                    this.form.currentBedId = this.calBedNum(this.form.username, res.data);
                } else {
                    ElMessage({message: res.msg, type: "error"});
                }
            });
        },
        calBedNum(username, data) {
            if (data.firstBed === username) return 1;
            if (data.secondBed === username) return 2;
            if (data.thirdBed === username) return 3;
            if (data.fourthBed === username) return 4;
            return "";
        },
        resetApplyFields() {
            this.form.towardsRoomId = "";
            this.form.towardsBedId = "";
            this.form.applyTime = "";
            this.dormRoomId = 0;
            this.$nextTick(() => {
                this.$refs.form.clearValidate();
            });
        },
        save() {
            this.$refs.form.validate((valid) => {
                if (!valid) return;
                request.post("/adjustRoom/add", this.form).then((res) => {
                    if (res.code === "0") {
                        ElMessage({message: "申请提交成功", type: "success"});
                        this.resetApplyFields();
                    } else {
                        ElMessage({message: res.msg, type: "error"});
                    }
                });
            });
        },
    },
};
