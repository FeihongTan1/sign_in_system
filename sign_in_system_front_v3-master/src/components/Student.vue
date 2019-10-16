<template>
    <el-container class="idx-second-container">
        <el-aside class="idx-el-aside" width="280px">
            <el-menu class="idx-second-menu" background-color="#4b4b4b" text-color="#fff"
                     active-text-color="#ffd04b" :default-active="activeIndex">
                <el-menu-item index="1" @click="courseTable">查看课表</el-menu-item>
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
        name: 'Student',
        data() {
            return {
                activeIndex: ''
            }
        },
        created() {
            switch (this.$route.name) {
                case 'courseTable': {
                    this.activeIndex = "1"
                    return
                }
                default: {
                    this.$router.push('/index/student/courseTable')
                }
            }
        },
        watch: {
            '$route'(to) {
                const name = to.name
                switch (name) {
                    case 'courseTable': {
                        this.activeIndex = "1"
                        return
                    }
                    default: {
                        this.$router.push('/index/student/courseTable')
                    }
                }
            }
        },
        methods: {
            courseTable() {
                this.$router.push('/index/student/courseTable')
            }
        }
    }
</script>

<style scoped>
</style>
