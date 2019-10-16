<template>
    <el-dialog title="增加学院"
               width="400px"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-form :model="departForm" :rules="rules" ref="departForm">
            <el-form-item label="学院名字" prop="sdName">
                <el-input v-model="departForm.sdName"></el-input>
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
        name: "AddDepartDialog",
        props: {
            dialogVisible: Boolean
        },
        data() {
            return {
                rules: {
                    sdName: [
                        {required: true, message: '请输入学院名', trigger: 'blur'},
                        {min: 5, max: 25, message: '长度在 5 到 25 个字符', trigger: 'blur'}]
                },
                departForm: {
                    sdName: ''
                }
            }
        },
        methods: {
            closeDialog() {
                this.$emit('closeDialog')
            },
            onSure() {
                this.$request.administrator.addDepartment(this.departForm.sdName).then(res => {
                    if (!res.data.success) {
                        this.$message.error('增加失败')
                        return
                    }
                    this.$message.success('增加成功')
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
        }
    }
</script>

<style scoped>

</style>