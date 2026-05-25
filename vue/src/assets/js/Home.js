import weather from "@/components/weather";
import Calender from "@/components/Calendar";
import request from "@/utils/request";
import home_echarts from "@/components/home_echarts";

export default {
    name: "Home",
    components: {
        weather,
        Calender,
        home_echarts,
    },
    data() {
        return {
            studentNum: "",
            haveRoomStudentNum: "",
            repairOrderNum: "",
            noFullRoomNum: "",
            activities: [],
            utilityAlerts: {
                unhandledCount: 0,
                recentAlerts: [],
            },
            accessAlerts: {
                unhandledCount: 0,
                recentAlerts: [],
            },
        };
    },
    computed: {
        canViewUtility() {
            const identity = JSON.parse(sessionStorage.getItem("identity") || '""');
            return identity === "admin" || identity === "dormManager";
        },
        canViewAccess() {
            const identity = JSON.parse(sessionStorage.getItem("identity") || '""');
            return identity === "admin" || identity === "dormManager";
        },
        stats() {
            return [
                { label: "学生统计", value: this.studentNum || 0, icon: "user", tone: "teal" },
                { label: "住宿人数", value: this.haveRoomStudentNum || 0, icon: "house", tone: "blue" },
                { label: "报修统计", value: this.repairOrderNum || 0, icon: "set-up", tone: "green" },
                { label: "空宿舍", value: this.noFullRoomNum || 0, icon: "office-building", tone: "cyan" },
                { label: "晚归告警", value: this.accessAlerts.unhandledCount || 0, icon: "clock", tone: "danger", action: "goAccess" },
                { label: "用电告警", value: this.utilityAlerts.unhandledCount || 0, icon: "warning", tone: "warning", action: "goUtility" },
            ];
        },
    },
    created() {
        this.getHomePageNotice();
        this.getStuNum();
        this.getHaveRoomNum();
        this.getOrderNum();
        this.getNoFullRoom();
        this.getUtilityAlerts();
        this.getAccessAlerts();
    },
    methods: {
        async getStuNum() {
            request.get("/stu/stuNum").then((res) => {
                if (res.code === "0") {
                    this.studentNum = res.data;
                }
            });
        },
        async getHaveRoomNum() {
            request.get("/room/selectHaveRoomStuNum").then((res) => {
                if (res.code === "0") {
                    this.haveRoomStudentNum = res.data;
                }
            });
        },
        async getOrderNum() {
            request.get("/repair/orderNum").then((res) => {
                if (res.code === "0") {
                    this.repairOrderNum = res.data;
                }
            });
        },
        async getNoFullRoom() {
            request.get("/room/noFullRoom").then((res) => {
                if (res.code === "0") {
                    this.noFullRoomNum = res.data;
                }
            });
        },
        async getHomePageNotice() {
            request.get("/notice/homePageNotice").then((res) => {
                if (res.code === "0") {
                    this.activities = res.data;
                }
            });
        },
        async getUtilityAlerts() {
            if (!this.canViewUtility) return;
            request.get("/utility/homeAlerts").then((res) => {
                if (res.code === "0") {
                    this.utilityAlerts = res.data;
                }
            });
        },
        async getAccessAlerts() {
            if (!this.canViewAccess) return;
            request.get("/access/homeAlerts").then((res) => {
                if (res.code === "0") {
                    this.accessAlerts = res.data;
                }
            });
        },
        triggerStat(stat) {
            if (stat.action && typeof this[stat.action] === "function") {
                this[stat.action]();
            }
        },
        goUtility() {
            this.$router.push("/utilityInfo");
        },
        goAccess() {
            this.$router.push("/accessInfo");
        },
    },
};
