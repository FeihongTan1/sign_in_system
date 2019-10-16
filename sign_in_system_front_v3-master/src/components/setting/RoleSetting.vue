<template>
    <el-container class="role-setting-container">
        <el-main class="role-setting-main">
            <h1 class="role-setting-title">请选择一个角色</h1>
            <el-form class="role-setting-form" label-position="left" label-width="100px">
                <el-form-item label="可选角色">
                    <el-radio-group v-model="role">
                        <el-radio v-show="authorities.includes('STUDENT')" :label="`STUDENT`">学生</el-radio>
                        <el-radio v-show="authorities.includes('MONITOR')" :label="`MONITOR`">督导员</el-radio>
                        <el-radio v-show="authorities.includes('TEACHER')" :label="`TEACHER`">老师</el-radio>
                        <el-radio v-show="authorities.includes('ADMINISTRATOR')" :label="`ADMINISTRATOR`">管理员</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="记住我选择">
                    <el-radio-group v-model="remember">
                        <el-radio :label="true">是</el-radio>
                        <el-radio :label="false">否</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-button type="primary" @click="onSure" class="role-setting-button">
                    确定
                </el-button>
            </el-form>
        </el-main>
    </el-container>
</template>

<script>
    import {mapState} from 'vuex'

    export default {
        name: "RoleSetting",
        computed: {
            authorities() {
                return this.user.suAuthorities.map(auth => auth.authority)
            },
            ...mapState({
                user: 'user'
            })
        },
        data() {
            return {
                role: '',
                remember: false
            }
        },
        methods: {
            onSure() {
                if (this.remember) {
                    localStorage.setItem('role', this.role)
                }

                this.$store.commit('setRole', this.role)
                this.$router.push('/index')
            }
        }
    }
</script>

<style scoped>
    .role-setting-container {
        height: 100%;
        border-left: 1px solid #d2d2d2;
        border-right: 1px solid #d2d2d2;
        margin: 0 10%;
        padding: 5% 5%;
    }

    .role-setting-main {
        border-radius: 10px;
        background-color: #c0c0c0;
    }

    .role-setting-title {
        font-size: 3rem;
        font-weight: normal;
        color: #515151;
        padding: 0 47px;
        margin-bottom: 30px;
    }

    .role-setting-form {
        padding: 0 50px;
    }

    .role-setting-button {
        width: 80px;
        margin-top: 8px;
    }
</style>