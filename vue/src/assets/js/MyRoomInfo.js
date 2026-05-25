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
                firstBed: "",
                secondBed: "",
                thirdBed: "",
                fourthBed: "",
            },
            studentNames: {},
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
            return [
                {key: "firstBed", label: "1 号床", value: this.room.firstBed},
                {key: "secondBed", label: "2 号床", value: this.room.secondBed},
                {key: "thirdBed", label: "3 号床", value: this.room.thirdBed},
                {key: "fourthBed", label: "4 号床", value: this.room.fourthBed},
            ].map((bed) => ({
                ...bed,
                displayName: bed.value ? (this.studentNames[bed.value] || bed.value) : "",
                isMine: bed.value === this.name,
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
                    this.loadStudentNames();
                    this.loadMonthlyUtility();
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            });
        },
        loadStudentNames() {
            const usernames = [
                this.room.firstBed,
                this.room.secondBed,
                this.room.thirdBed,
                this.room.fourthBed,
            ].filter(Boolean);

            usernames.forEach((username) => {
                request.get("/stu/exist/" + username).then((res) => {
                    if (res.code === "0" && res.data) {
                        this.studentNames[username] = res.data.name || username;
                    } else {
                        this.studentNames[username] = username;
                    }
                });
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
