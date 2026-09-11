import request from "@/utils/request";

const {ElMessage} = require("element-plus");

export default {
    name: "MyRoomInfo",
    data() {
        return {
            name: "",
            form: {
                username: "",
            },
            room: {
                dormRoomId: "",
                dormBuildId: "",
                floorNum: "",
                maxCapacity: "",
                currentCapacity: "",
                ownBedNumber: "",
                beds: [],
            },
            utilityUsage: {
                electricUsageSum: "0.00",
                waterUsageSum: "0.00",
            },
        };
    },
    created() {
        this.loadUser();
    },
    computed: {
        hasRoom() {
            return Boolean(this.room && this.room.dormRoomId);
        },
        occupancyText() {
            return `${this.room.currentCapacity || 0}/${this.room.maxCapacity || 0} 人入住`;
        },
        beds() {
            const summaries = Array.isArray(this.room.beds) ? this.room.beds : [];
            return summaries.map((bed) => ({
                key: `bed-${bed.bedNumber}`,
                label: `${bed.bedNumber} 号床`,
                value: bed.occupied,
                displayName: bed.mine ? (this.form.name || "我") : (bed.occupied ? "已入住" : ""),
                isMine: Boolean(bed.mine),
            }));
        },
        monthlyElectricUsage() {
            return this.formatUsage(this.utilityUsage.electricUsageSum);
        },
        monthlyWaterUsage() {
            return this.formatUsage(this.utilityUsage.waterUsageSum);
        },
    },
    methods: {
        loadUser() {
            const userStr = sessionStorage.getItem("user");
            const applyUser = (user) => {
                if (!user) {
                    return;
                }
                this.form = user;
                this.name = user.username;
                this.getInfo();
            };
            if (userStr && userStr !== "null") {
                applyUser(JSON.parse(userStr));
                return;
            }
            request.get("/main/loadUserInfo").then((res) => {
                if (res.code === "0") {
                    sessionStorage.setItem("user", JSON.stringify(res.data));
                    applyUser(res.data);
                }
            });
        },
        getInfo() {
            request.get("/room/getMyRoom/" + this.name).then((res) => {
                if (res.code === "0") {
                    this.room = res.data;
                    this.loadMonthlyUtility();
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            });
        },
        loadMonthlyUtility() {
            if (!this.room.dormRoomId) return;
            request.get("/utility/monthly/" + this.room.dormRoomId).then((res) => {
                if (res.code === "0" && res.data) {
                    this.utilityUsage = res.data;
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            });
        },
        formatUsage(value) {
            const numberValue = Number(value);
            if (Number.isNaN(numberValue)) return "0.00";
            return numberValue.toFixed(2);
        },
    },
};
