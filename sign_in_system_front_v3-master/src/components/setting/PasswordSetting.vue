<template>
    <el-container class="password-setting-container">
        <el-main class="password-setting-main">
            <h1 class="password-setting-title">请填入以下信息</h1>
            <el-form class="password-setting-form" label-position="left" label-width="100px" ref="modifyPwdForm"
                     :rules="modifyPwdFormRules"
                     :model="modifyPwdForm">
                <el-form-item label="原密码" prop="oldPassword">
                    <el-input type="password" v-model="modifyPwdForm.oldPassword" auto-complete="off"></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                    <el-input type="password" v-model="modifyPwdForm.newPassword" auto-complete="off"></el-input>
                </el-form-item>
                <el-form-item label="确认新密码" prop="ensurePassword">
                    <el-input type="password" v-model="modifyPwdForm.ensurePassword" auto-complete="off"></el-input>
                </el-form-item>
                <el-button type="primary" @click="onSure" class="password-setting-button">
                    确定
                </el-button>
            </el-form>
        </el-main>
    </el-container>
</template>

<script>
    import {mapState} from 'vuex'

    export default {
        name: "PasswordSetting",
        computed: {
            ...mapState({
                user: 'user'
            })
        },
        data() {
            const _this = this
            const newPwdValidator = (rule, value, callback) => {
                if (_this.modifyPwdForm.oldPassword === _this.modifyPwdForm.newPassword) {
                    callback(new Error('新密码不能和旧密码相同'));
                    return
                }
                callback()
            }
            const ensurePwdValidator = (rule, value, callback) => {
                if (_this.modifyPwdForm.ensurePassword !== _this.modifyPwdForm.newPassword) {
                    callback(new Error('确认密码不一致'));
                    return;
                }
                callback()
            }
            return {
                modifyPwdFormRules: {
                    oldPassword: [{required: true, message: '请输入原密码', trigger: 'blur'}],
                    newPassword: [
                        {required: true, message: '请输入新密码', trigger: 'blur'},
                        {validator: newPwdValidator, trigger: 'blur'},
                        {min: 6, max: 18, message: '长度在 6 到 18 个字符', trigger: 'blur'}
                    ],
                    ensurePassword: [
                        {required: true, message: '请确认新密码', trigger: 'blur'},
                        {validator: ensurePwdValidator, trigger: 'blur'},
                        {min: 6, max: 18, message: '长度在 6 到 18 个字符', trigger: 'blur'}
                    ]
                },
                modifyPwdForm: {
                    oldPassword: '',
                    newPassword: '',
                    ensurePassword: '',
                }
            }
        },
        methods: {
            onSure() {
                this.$refs['modifyPwdForm']
                    .validate(valid => {
                        if (!valid) {
                            return
                        }

                        this.$request.modifyPassword(this.user.suId, this.modifyPwdForm.oldPassword, this.modifyPwdForm.newPassword)
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message.error(res.data.message)
                                    return;
                                }

                                this.$message.success('修改成功, 请重新登录')
                                this.$store.dispatch('logout').then(() => {
                                    this.$router.push('/')
                                })
                            })
                            .catch(err => {
                                if (!err.response || !err.response.data)
                                    return
                                if (!err.response.data.message) {
                                    this.$message.error(err.response.data)
                                    return
                                }
                                this.$message.error(err.response.data.message)
                            })
                    })
            }
        }
    }
</script>

<style scoped>
    .password-setting-container {
        height: 100%;
        border-left: 1px solid #d2d2d2;
        border-right: 1px solid #d2d2d2;
        margin: 0 10%;
        padding: 5% 5%;
    }

    .password-setting-main {
        border-radius: 10px;
        background-color: #c0c0c0;
    }

    .password-setting-title {
        font-size: 3rem;
        font-weight: normal;
        color: #515151;
        padding: 0 47px;
        margin-bottom: 30px;
    }

    .password-setting-form {
        max-width: 400px;
        padding: 0 50px;
    }

    .password-setting-button {
        width: 80px;
        margin-top: 8px;
    }
</style>