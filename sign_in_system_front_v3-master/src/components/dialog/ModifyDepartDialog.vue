<template>
    <el-dialog title="修改学院信息"
               width="400px"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-form :model="departForm" :rules="rules" ref="departForm">
            <el-form-item label="学院名字" prop="sdName">
                <el-input v-model="departForm.sdName"></el-input>
            </el-form-item>
            <el-form-item label="学院编号" prop="sdId">
                <el-input v-model="departForm.sdId" :disabled="true"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="onSure">
                    确定
                </el-button>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>

<script>
    export default {
        name: "ModifyDepartDialog",
        props: {
            dialogVisible: Boolean,
            department: Object
        },
        watch: {
            department() {
                if (null == this.department)
                    return
                this.departForm.sdId = this.department.sdId.toString()
                this.departForm.sdName = this.department.sdName.toString()
            }
        },
        data() {
            return {
                rules: {
                    sdId: [
                        {required: true, message: '未知错误, 请重新尝试', trigger: 'blur'},],
                    sdName: [
                        {required: true, message: '请输入学院名', trigger: 'blur'},
                        {min: 5, max: 25, message: '长度在 5 到 25 个字符', trigger: 'blur'}]
                },
                departForm: {
                    sdId: '',
                    sdName: ''
                }
            }
        },
        methods: {
            closeDialog() {
                this.departForm.sdId = ''
                this.departForm.sdName = ''
                this.$emit('closeDialog')
            },
            onSure() {
                this.$refs.departForm.validate((v) => {
                    if (v) {
                        this.$request.administrator.modifyDepartment(this.departForm.sdId, this.departForm.sdName.trim())
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message.error('修改失败')
                                    return
                                }
                                this.$message.success('修改成功')
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
                            .finally(() => this.closeDialog())
                    }
                })
            }
        }
    }
</script>

<style scoped>

</style>