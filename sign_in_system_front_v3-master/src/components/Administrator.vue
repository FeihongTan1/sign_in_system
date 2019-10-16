<template>
    <el-container class="idx-second-container">
        <el-aside class="idx-el-aside" width="280px">
            <el-menu class="idx-second-menu" background-color="#4b4b4b" text-color="#fff"
                     active-text-color="#ffd04b" :default-active="activeIndex">
                <el-menu-item index="1" @click="courseStatistic">课程统计</el-menu-item>
                <el-submenu index="2">
                    <template slot="title">督导管理</template>
                    <el-menu-item index="2-1" @click="courseManage">设置督导</el-menu-item>
                    <el-menu-item index="2-2" @click="monitorManage">督导员管理</el-menu-item>
                </el-submenu>
                <el-menu-item index="3" @click="studentManage">学生历史</el-menu-item>
                <el-menu-item index="6" @click="teacherManage">老师课程</el-menu-item>
                <el-menu-item index="4" @click="importService">上传信息</el-menu-item>
                <el-submenu index="5">
                    <template slot="title">系统管理</template>
                    <el-menu-item index="5-2" @click="cozInfoManage">课程管理</el-menu-item>
                    <el-menu-item index="5-1" @click="locationManage">地点管理</el-menu-item>
                    <el-menu-item index="5-3" @click="departManage">学院管理</el-menu-item>
                </el-submenu>
            </el-menu>
        </el-aside>
        <el-main class="idx-main">
            <transition name="el-fade-in" mode="out-in">
                <router-view>
                </router-view>
            </transition>
        </el-main>
    </el-container>
</template>

<script>
    export default {
        name: "Administrator",
        data() {
            return {
                activeIndex: '',
            }
        },
        created() {
            switch (this.$route.name) {
                case 'courseStatistic': {
                    this.activeIndex = '1'
                    return
                }
                case 'courseManage': {
                    this.activeIndex = '2-1'
                    return
                }
                case 'monitorManage': {
                    this.activeIndex = '2-2'
                    return
                }
                case 'studentManage': {
                    this.activeIndex = '3'
                    return
                }
                case 'teacherManage': {
                    this.activeIndex = '6'
                    return
                }
                case 'importService': {
                    this.activeIndex = '4'
                    return
                }
                case 'locationManage': {
                    this.activeIndex = '5-1'
                    return
                }
                case 'cozInfoManage': {
                    this.activeIndex = '5-2'
                    return
                }
                case 'departManage': {
                    this.activeIndex = '5-3'
                    return
                }
                default: {
                    this.$router.push('/index/administrator/courseStatistic')
                }
            }
        },
        watch: {
            '$route'(to) {
                const name = to.name
                switch (name) {
                    case 'courseStatistic': {
                        this.activeIndex = '1'
                        return
                    }
                    case 'courseManage': {
                        this.activeIndex = '2-1'
                        return
                    }
                    case 'monitorManage': {
                        this.activeIndex = '2-2'
                        return
                    }
                    case 'studentManage': {
                        this.activeIndex = '3'
                        return
                    }
                    case 'teacherManage': {
                        this.activeIndex = '6'
                        return
                    }
                    case 'importService': {
                        this.activeIndex = '4'
                        return
                    }
                    case 'locationManage': {
                        this.activeIndex = '5-1'
                        return
                    }
                    case 'cozInfoManage': {
                        this.activeIndex = '5-2'
                        return
                    }
                    case 'departManage': {
                        this.activeIndex = '5-3'
                        return
                    }
                    default: {
                        this.$router.push('/index/administrator/courseStatistic')
                    }
                }
            }
        },
        methods: {
            courseStatistic() {
                this.$router.push('/index/administrator/courseStatistic')
            },
            courseManage() {
                this.$router.push('/index/administrator/courseManage')
            },
            monitorManage() {
                this.$router.push('/index/administrator/monitorManage')
            },
            teacherManage() {
                this.$router.push('/index/administrator/teacherManage')
            },
            studentManage() {
                this.$router.push('/index/administrator/studentManage')
            },
            importService() {
                this.$router.push('/index/administrator/importService')
            },
            locationManage() {
                this.$router.push('/index/administrator/locationManage')
            },
            cozInfoManage() {
                this.$router.push('/index/administrator/cozInfoManage')
            },
            departManage() {
                this.$router.push('/index/administrator/departManage')
            }
        }
    }
</script>

<style scoped>

</style>