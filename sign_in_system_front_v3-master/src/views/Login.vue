<template>
    <el-container class="login-box-container">
        <el-row class="login-box-row" type="flex" justify="center">
            <el-col class="login-box-col" :xl="5" :xs="12" :md="8" :sm="9" :span="12">
                <h1 class="login-box-brand">Happy Sign In</h1>
                <el-form class="login-form" ref="loginForm" :model="loginForm" :rules="loginRules">
                    <el-form-item label="用户名/学号/教工号" prop="suId">
                        <el-input v-model="loginForm.suId" @keyup.enter.native="onSubmit"/>
                    </el-form-item>
                    <el-form-item label="密码" prop="suPassword">
                        <el-input type="password" v-model="loginForm.suPassword" @keyup.enter.native="onSubmit"/>
                    </el-form-item>
                    <el-form-item>
                        <el-button class="login-form-button" type="primary" @click="onSubmit"
                                   :loading="isLoginProcessing"
                                   :disabled="isLoginProcessing">
                            登录
                        </el-button>
                    </el-form-item>
                </el-form>
            </el-col>
            <el-dialog width="600px"
                       title="多角色账户，请选择角色进入"
                       :center="true"
                       :visible="dialogVisible"
                       :close-on-click-modal="false"
                       :close-on-press-escape="false"
                       :show-close="false">
                <el-form class="login-dialog-form" label-position="left" label-width="100px">
                    <el-form-item label="可选角色">
                        <el-radio-group v-model="role">
                            <el-radio v-show="authorities.includes('STUDENT')" :label="`STUDENT`">学生</el-radio>
                            <el-radio v-show="authorities.includes('MONITOR')" :label="`MONITOR`">督导员</el-radio>
                            <el-radio v-show="authorities.includes('TEACHER')" :label="`TEACHER`">老师</el-radio>
                            <el-radio v-show="authorities.includes('ADMINISTRATOR')" :label="`ADMINISTRATOR`">管理员
                            </el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="记住我选择">
                        <el-radio-group v-model="remember">
                            <el-radio :label="true">是</el-radio>
                            <el-radio :label="false">否</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-button type="primary" @click="onSure">
                        确定
                    </el-button>
                </el-form>
            </el-dialog>
        </el-row>
        <el-footer class="login-footer">
            <el-row type="flex" justify="center">
                <el-col :span="6">
                    <el-button type="text" @click="toGov">粤ICP备18035033号-1</el-button>
                </el-col>
            </el-row>
        </el-footer>
    </el-container>
</template>

<script>
    export default {
        name: 'home',
        data() {
            return {
                isLoginProcessing: false,
                dialogVisible: false,
                role: '',
                remember: false,
                authorities: [],
                loginForm: {
                    suId: '',
                    suPassword: ''
                },
                loginRules: {
                    suId: [{
                        required: true,
                        message: '用户名不能为空',
                        trigger: 'blur'
                    }],
                    suPassword: [{
                        required: true,
                        message: '密码不能为空',
                        trigger: 'blur'
                    }]
                },
                loginData: null
            }
        },
        methods: {
            onSubmit() {
                this.isLoginProcessing = true
                this.$refs['loginForm'].validate((valid) => {
                    if (!valid) {
                        this.isLoginProcessing = false
                        return
                    }

                    this.$request.getToken(this.loginForm)
                        .then(res => {
                            if (!res.data.success) {
                                this.$message.error(res.data.message);
                                this.isLoginProcessing = false
                                return
                            }

                            const role = localStorage.getItem('role')
                            if (null != role && '' !== role) {
                                this.$store.commit('setRole', role)
                                this.goIndex(res.data.data)
                                return
                            }

                            const user = res.data.data.sisUser;
                            if (user.suAuthorities.length > 1) {
                                this.loginData = res.data.data
                                this.dialogVisible = true
                                this.isLoginProcessing = false
                                this.authorities = user.suAuthorities.map(auth => auth.authority)
                            } else if (user.suAuthorities.length === 1) {
                                this.$store.commit('setRole', user.suAuthorities[0].authority)
                                localStorage.setItem('role', user.suAuthorities[0].authority)
                                this.goIndex(res.data.data)
                            } else {
                                this.$message.error("该账号无权限登录，请联系管理员")
                            }
                        })
                        .catch(err => {
                            console.error(err)
                            this.isLoginProcessing = false
                        })
                });
            },
            onSure() {
                if (this.remember) {
                    localStorage.setItem('role', this.role)
                }

                this.$store.commit('setRole', this.role)
                this.goIndex(this.loginData)
            },
            goIndex(loginData) {
                this.$store.dispatch('login', loginData).then(() => {
                    this.$router.push('/index')
                    this.isLoginProcessing = false
                })
            },
            toGov() {
                document.location = 'http://www.miitbeian.gov.cn'
            }
        }
    }
</script>

<style>
    .login-form .el-form-item.is-required .el-form-item__label:before {
        content: none;
    }

    .login-box-container {
        height: calc(100vh - 16px);
    }

    .login-box-row {
        height: calc(100vh - 56px);
    }

    .login-box-col {
        padding: 10px 60px;
        border-radius: 10px;
        margin: auto auto;
        background-color: #d8d8d8;
    }

    .login-box-brand {
        color: #686f77;
    }

    .login-form-button {
        width: 100%;
        margin-top: 24px !important;
        margin-bottom: 12px !important;
    }

    .login-dialog-form {
        padding: 40px;
    }

    .login-footer {
        text-align: center;
        height: 40px;
    }
</style>
