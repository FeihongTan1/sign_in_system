<template>
    <el-dialog title="联系我们"
               :visible="visible"
               width="600px"
               :before-close="closeDialog">
        <el-container>
            <el-main>
                <el-form ref="contactForm" :model="form" label-width="80px" label-position="right" :rules="rules">
                    <el-form-item label="称呼" prop="sctName">
                        <el-input v-model="form.sctName"></el-input>
                    </el-form-item>
                    <el-form-item label="联系方式" prop="sctContact">
                        <el-input v-model="form.sctContact"></el-input>
                    </el-form-item>
                    <el-form-item label="内容" prop="sctContent">
                        <el-input type="textarea" v-model="form.sctContent"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button @click="closeDialog">
                            返回
                        </el-button>
                        <el-button type="primary" @click="submit">
                            提交
                        </el-button>
                    </el-form-item>
                </el-form>
            </el-main>
        </el-container>
    </el-dialog>
</template>

<script>
    export default {
        name: "ContactDialog",
        props: {
            visible: Boolean
        },
        data() {
            return {
                rules: {
                    sctName: [
                        {required: true, message: '请输入您的称呼', trigger: 'blur'},
                        {min: 3, max: 30, message: '长度在 3 到 30 个字符', trigger: 'blur'}],
                    sctContact: [
                        {required: true, message: '请输入联系方式', trigger: 'blur'},
                        {min: 5, max: 50, message: '长度在 5 到 50 个字符', trigger: 'blur'}],
                    sctContent: [
                        {required: true, message: '请输入内容', trigger: 'blur'},
                        {min: 15, max: 200, message: '长度在 15 到 200 个字符', trigger: 'blur'}]
                },
                form: {
                    sctName: '',
                    sctContact: '',
                    sctContent: ''
                }
            }
        },
        methods: {
            closeDialog() {
                this.$emit('closeDialog')
            },
            submit() {
                this.$refs.contactForm.validate(v => {
                    if (!v)
                        return
                    this.$request
                        .contact(this.form)
                        .then(res => {
                            if (!res.data.success) {
                                this.$message.error("提交失败")
                                return
                            }

                            this.$message.success("提交成功")
                            this.closeDialog()
                            this.form = {
                                sctName: '',
                                sctContact: '',
                                sctContent: ''
                            }
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

</style>