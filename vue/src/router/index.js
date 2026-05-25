import Layout from '../layout/Layout.vue'
import {createRouter, createWebHistory} from "vue-router";


export const constantRoutes = [
    {path: '/Login', name: 'Login', component: () => import("@/views/Login")},
    {
        path: '/Layout', name: 'Layout', component: Layout, children: [
            // 管理员 & 宿管页面
            {path: '/home', name: 'Home', component: () => import("@/views/Home")},
            {path: '/stuInfo', name: 'StuInfo', component: () => import("@/views/StuInfo"), meta: {roles: ['admin']}},
            {path: '/dormManagerInfo', name: 'DormManagerInfo', component: () => import("@/views/DormManagerInfo"), meta: {roles: ['admin']}},
            {path: '/buildingInfo', name: 'BuildingInfo', component: () => import("@/views/BuildingInfo"), meta: {roles: ['admin', 'dormManager']}},
            {path: '/roomInfo', name: 'RoomInfo', component: () => import("@/views/RoomInfo"), meta: {roles: ['admin', 'dormManager']}},
            {path: '/utilityInfo', name: 'UtilityInfo', component: () => import("@/views/UtilityInfo"), meta: {roles: ['admin', 'dormManager']}},
            {path: '/accessInfo', name: 'AccessInfo', component: () => import("@/views/AccessInfo"), meta: {roles: ['admin', 'dormManager']}},
            {path: '/noticeInfo', name: 'NoticeInfo', component: () => import("@/views/NoticeInfo"), meta: {roles: ['admin', 'dormManager']}},
            {path: '/adjustRoomInfo', name: 'AdjustRoomInfo', component: () => import("@/views/AdjustRoomInfo"), meta: {roles: ['admin', 'dormManager']}},
            {path: '/repairInfo', name: 'RepairInfo', component: () => import("@/views/RepairInfo"), meta: {roles: ['admin', 'dormManager']}},
            {path: '/visitorInfo', name: 'VisitorInfo', component: () => import("@/views/VisitorInfo"), meta: {roles: ['admin', 'dormManager']}},
            // 学生页面
            {path: '/myRoomInfo', name: 'MyRoomInfo', component: () => import("@/views/MyRoomInfo"), meta: {roles: ['stu']}},
            {path: '/applyRepairInfo', name: 'ApplyRepairInfo', component: () => import("@/views/ApplyRepairInfo"), meta: {roles: ['stu']}},
            {path: '/applyChangeRoom', name: 'ApplyChangeRoom', component: () => import("@/views/ApplyChangeRoom"), meta: {roles: ['stu']}},
            // 所有角色
            {path: '/selfInfo', name: 'SelfInfo', component: () => import("@/views/SelfInfo")},
        ]
    },

]
const router = createRouter({
    routes: constantRoutes,
    history: createWebHistory(process.env.BASE_URL)
})
//路由守卫
router.beforeEach((to, from, next) => {
    const user = window.sessionStorage.getItem('user')
    if (to.path === '/Login') {
        return next();
    }
    if (!user) {
        return next('/Login')
    }
    const identity = JSON.parse(window.sessionStorage.getItem('identity'))
    if (to.path === '/' && user) {
        return next(identity === 'stu' ? '/myRoomInfo' : '/home')
    }
    if (to.path === '/home' && identity === 'stu') {
        return next('/myRoomInfo')
    }
    // 角色权限校验
    if (to.meta && to.meta.roles) {
        if (!to.meta.roles.includes(identity)) {
            return next(identity === 'stu' ? '/myRoomInfo' : '/home')
        }
    }
    next()
})

export default router
