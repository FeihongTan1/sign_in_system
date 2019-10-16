<template>
    <el-container class="idx-second-container">
        <el-aside class="idx-el-aside" width="280px">
            <el-menu class="idx-second-menu" background-color="#4b4b4b" text-color="#fff"
                     active-text-color="#ffd04b" :default-active="activeIndex">
                <el-menu-item index="1" @click="goBack">返回</el-menu-item>
                <el-menu-item index="2" @click="passwordSetting">修改密码</el-menu-item>
                <el-menu-item index="3" @click="roleSetting" v-if="user.suAuthorities.length > 1">切换角色</el-menu-item>
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
    import {mapState} from 'vuex'

    export default {
        name: "Setting",
        data() {
            return {
                activeIndex: ''
            }
        },
        created() {
            switch (this.$route.name) {
                case 'passwordSetting': {
                    this.activeIndex = "2"
                    return
                }
                case 'roleSetting': {
                    this.activeIndex = "3"
                    return
                }
                default: {
                    this.$router.push('/index/setting/passwordSetting')
                }
            }
        },
        watch: {
            '$route'(to) {
                const name = to.name
                switch (name) {
                    case 'passwordSetting': {
                        this.activeIndex = "2"
                        return
                    }
                    case 'roleSetting': {
                        this.activeIndex = "3"
                        return
                    }
                    default: {
                        this.$router.push('/index/setting/passwordSetting')
                    }
                }
            }
        },
        computed: {
            ...mapState({
                user: 'user',
                backSetting: 'backSetting'
            })
        },
        methods: {
            goBack() {
                if ('' === this.backSetting)
                    return this.$router.push('/index')
                this.$router.push(this.backSetting)
                this.$store.commit('backSetting', '')
            },
            passwordSetting() {
                this.$router.push('/index/setting/passwordSetting')
            },
            roleSetting() {
                this.$router.push('/index/setting/roleSetting')
            }
        }
    }
</script>

<style scoped>

</style>