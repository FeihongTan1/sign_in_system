import Vue from 'vue'
import Router from 'vue-router'
import Login from './views/Login.vue'

Vue.use(Router)

export default new Router({
    mode: 'history',
    base: process.env.BASE_URL,
    routes: [
        {
            path: '/',
            name: 'login',
            component: Login
        },
        {
            path: '/index',
            name: 'index',
            children: [
                {
                    path: 'student',
                    name: 'student',
                    redirect: 'student/courseTable',
                    children: [
                        {
                            path: 'courseTable',
                            name: 'courseTable',
                            component: () => import('./components/student/CourseTable.vue')
                        }
                    ],
                    component: () => import('./components/Student.vue')
                },
                {
                    path: 'teacher',
                    name: 'teacher',
                    redirect: 'teacher/courseTable',
                    children: [
                        {
                            path: 'courseTable',
                            name: 'tchCourseTable',
                            component: () => import('./components/teacher/CourseTable.vue')
                        },
                        {
                            path: 'historySignIn',
                            name: 'historySignIn',
                            component: () => import('./components/teacher/HistorySignIn.vue')
                        }
                    ],
                    component: () => import('./components/Teacher.vue')
                },
                {
                    path: 'monitor',
                    name: 'monitor',
                    redirect: 'monitor/courseTable',
                    children: [
                        {
                            path: 'courseTable',
                            name: 'monCourseTable',
                            component: () => import('./components/monitor/CourseTable.vue')
                        },
                        {
                            path: 'monitorPool',
                            name: 'monitorPool',
                            component: () => import('./components/monitor/MonitorPool.vue')
                        },
                        {
                            path: 'myAcceptTrans',
                            name: 'myAcceptTrans',
                            component: () => import('./components/monitor/MyAcceptTrans.vue')
                        },
                        {
                            path: 'myUntreatedTrans',
                            name: 'myUntreatedTrans',
                            component: () => import('./components/monitor/MyUntreatedTrans.vue')
                        },
                        {
                            path: 'myApplyTrans',
                            name: 'myApplyTrans',
                            component: () => import('./components/monitor/MyApplyTrans.vue')
                        }
                    ],
                    component: () => import('./components/Monitor.vue')
                },
                {
                    path: 'administrator',
                    name: 'administrator',
                    redirect: 'administrator/courseStatistic',
                    children: [
                        {
                            path: 'courseStatistic',
                            name: 'courseStatistic',
                            component: () => import('./components/administrator/CourseStatistic.vue')
                        },
                        {
                            path: 'courseManage',
                            name: 'courseManage',
                            component: () => import('./components/administrator/CourseManage.vue')
                        },
                        {
                            path: 'monitorManage',
                            name: 'monitorManage',
                            component: () => import('./components/administrator/MonitorManage.vue')
                        },
                        {
                            path: 'studentManage',
                            name: 'studentManage',
                            component: () => import('./components/administrator/StudentManage.vue')
                        },
                        {
                            path: 'teacherManage',
                            name: 'teacherManage',
                            component: () => import('./components/administrator/TeacherManage.vue')
                        },
                        {
                            path: 'importService',
                            name: 'importService',
                            component: () => import('./components/administrator/ImportService.vue')
                        },
                        {
                            path: 'locationManage',
                            name: 'locationManage',
                            component: () => import('./components/administrator/LocationManage.vue')
                        },
                        {
                            path: 'cozInfoManage',
                            name: 'cozInfoManage',
                            component: () => import('./components/administrator/CozInfoManage.vue')
                        },
                        {
                            path: 'departManage',
                            name: 'departManage',
                            component: () => import('./components/administrator/DepartManage.vue')
                        }
                    ],
                    component: () => import('./components/Administrator.vue')
                },
                {
                    path: 'setting',
                    name: 'setting',
                    redirect: 'setting/passwordSetting',
                    children: [
                        {
                            path: 'passwordSetting',
                            name: 'passwordSetting',
                            component: () => import('./components/setting/PasswordSetting.vue')
                        },
                        {
                            path: 'roleSetting',
                            name: 'roleSetting',
                            component: () => import('./components/setting/RoleSetting.vue')
                        }],
                    component: () => import('./components/Setting.vue')
                }
            ],
            // route level
            // this generates a separate chunk (about.[hash].js) for this route
            // which is lazy-loaded when the route is visited.
            component: () => import(/* webpackChunkName: "about" */ './views/Index.vue'),
        },
        {
            path: '*',
            component: () => import('./views/NotFound.vue')
        }
    ]
})
