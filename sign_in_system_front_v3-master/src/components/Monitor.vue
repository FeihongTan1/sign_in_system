<template>
    <el-container class="idx-second-container">
        <el-aside class="idx-el-aside" width="280px">
            <el-menu class="idx-second-menu" background-color="#4b4b4b" text-color="#fff"
                     active-text-color="#ffd04b" :default-active="activeIndex">
                <el-menu-item index="1" @click="courseTable">督导课表</el-menu-item>
                <el-menu-item index="2" @click="monitorPool">查看督导池</el-menu-item>
                <el-submenu index="3">
                    <template slot="title">督导转接</template>
                    <el-menu-item index="3-1" @click="myAcceptTrans">已接受转接</el-menu-item>
                    <el-menu-item index="3-2" @click="myUntreatedTrans">未处理转接</el-menu-item>
                    <el-menu-item index="3-3" @click="myApplyTrans">我发起的转接</el-menu-item>
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
        name: "Monitor",
        data() {
            return {
                activeIndex: ''
            }
        },
        created() {
            switch (this.$route.name) {
                case 'monCourseTable': {
                    this.activeIndex = '1'
                    return
                }
                case 'monitorPool': {
                    this.activeIndex = '2'
                    return
                }
                case 'myAcceptTrans': {
                    this.activeIndex = '3-1'
                    return
                }
                case 'myUntreatedTrans': {
                    this.activeIndex = '3-2'
                    return
                }
                case 'myApplyTrans': {
                    this.activeIndex = '3-3'
                    return
                }
                default: {
                    this.$router.push('/index/monitor/courseTable')
                }
            }
        },
        watch: {
            '$route'(to) {
                const name = to.name
                switch (name) {
                    case 'tchCourseTable': {
                        this.activeIndex = '1'
                        return
                    }
                    case 'monitorPool': {
                        this.activeIndex = '2'
                        return
                    }
                    case 'myAcceptTrans': {
                        this.activeIndex = '3-1'
                        return
                    }
                    case 'myUntreatedTrans': {
                        this.activeIndex = '3-2'
                        return
                    }
                    case 'myApplyTrans': {
                        this.activeIndex = '3-3'
                        return
                    }
                    default: {
                        this.$router.push('/index/monitor/courseTable')
                    }
                }
            }
        },
        methods: {
            courseTable() {
                this.$router.push('/index/monitor/courseTable')
            },
            monitorPool() {
                this.$router.push('/index/monitor/monitorPool')
            },
            myAcceptTrans() {
                this.$router.push('/index/monitor/myAcceptTrans')
            },
            myUntreatedTrans() {
                this.$router.push('/index/monitor/myUntreatedTrans')
            },
            myApplyTrans() {
                this.$router.push('/index/monitor/myApplyTrans')
            },
        }
    }
</script>

<style scoped>

</style>