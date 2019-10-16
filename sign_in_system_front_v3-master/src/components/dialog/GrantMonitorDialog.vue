<template>
    <el-dialog title="新增督导员"
               width="400px"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-form :model="newMonitorForm" size="mini">
            <el-form-item label="学号">
                <el-autocomplete v-model="newMonitorForm.suId"
                                 placeholder="请输入学号"
                                 :fetch-suggestions="queryBySuId"
                                 @select="handleSuIdSelect">
                </el-autocomplete>
            </el-form-item>
            <el-form-item label="姓名">
                <el-autocomplete v-model="newMonitorForm.suName"
                                 placeholder="请输入姓名"
                                 :fetch-suggestions="queryBySuName"
                                 @select="handleSuNameSelect">
                </el-autocomplete>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="doGrant" :loading="loading">
                    确定
                </el-button>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>

<script>
    export default {
        name: "GrantMonitorDialog",
        props: {
            dialogVisible: Boolean
        },
        data() {
            return {
                newMonitorForm: {
                    suId: '',
                    suName: ''
                },
                loading: false
            }
        },
        methods: {
            doGrant() {
                this.loading = true
                this.$request.administrator.grantMonitor(this.newMonitorForm.suId)
                    .then(res => {
                        if (!res.data.success) {
                            this.$message.error("提交失败")
                            return
                        }

                        this.$message.success("操作成功")
                        this.loading = false
                        this.closeDialog()
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
                    .finally(() => {
                        this.loading = false
                    })
            },
            closeDialog() {
                if (this.loading)
                    return false
                this.newMonitorForm.suId = ''
                this.newMonitorForm.suName = ''
                this.$emit('closeDialog')
            },
            queryBySuId(val, callback) {
                if ('' === val) {
                    callback([])
                    return
                }
                this.$request.administrator.getStudents(1, 10, val, null)
                    .then(res => {
                        const list = res.data.list
                        list.forEach(user => user.value = `${user.suId} ${user.suName}`)
                        callback(list)
                    })
                    .catch(() => {
                    })
            },
            handleSuIdSelect(user) {
                this.newMonitorForm.suId = user.suId
                this.newMonitorForm.suName = user.suName
            },
            queryBySuName(val, callback) {
                if ('' === val) {
                    callback([])
                    return
                }
                this.$request.administrator.getStudents(1, 10, null, val)
                    .then(res => {
                        const list = res.data.list
                        list.forEach(user => user.value = `${user.suId} ${user.suName}`)
                        callback(list)
                    })
                    .catch(() => {
                    })
            },
            handleSuNameSelect(user) {
                this.newMonitorForm.suId = user.suId
                this.newMonitorForm.suName = user.suName
            }
        }
    }
</script>

<style scoped>

</style>