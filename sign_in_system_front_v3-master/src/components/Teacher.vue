<template>
    <el-container class="idx-second-container">
        <el-aside class="idx-el-aside" width="280px">
            <el-menu class="idx-second-menu" background-color="#4b4b4b" text-color="#fff"
                     active-text-color="#ffd04b" :default-active="activeIndex">
                <el-menu-item index="1" @click="courseTable">教学计划</el-menu-item>
                <el-menu-item index="2" @click="historySignIn">历史签到</el-menu-item>
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
        name: "Teacher",
        data() {
            return {
                activeIndex: ""
            }
        },
        created() {
            switch (this.$route.name) {
                case 'tchCourseTable': {
                    this.activeIndex = "1"
                    return
                }
                case 'historySignIn': {
                    this.activeIndex = "2"
                    return
                }
                default: {
                    this.$router.push('/index/teacher/courseTable')
                }
            }
        },
        watch: {
            '$route'(to) {
                const name = to.name
                switch (name) {
                    case 'tchCourseTable': {
                        this.activeIndex = "1"
                        return
                    }
                    case 'historySignIn': {
                        this.activeIndex = "2"
                        return
                    }
                    default: {
                        this.$router.push('/index/teacher/courseTable')
                    }
                }
            }
        },
        methods: {
            courseTable() {
                this.$router.push('/index/teacher/courseTable')
            },
            historySignIn() {
                this.$router.push("/index/teacher/historySignIn")
            }
        }
    }
</script>

<style scoped>

</style>