<template>
    <el-dialog title="查询地点"
               width="400px"
               :append-to-body="true"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-form size="mini" :model="modifyForm">
            <el-form-item label="地点ID">
                <el-autocomplete v-model="modifyForm.slId"
                                 placeholder="通过请输入ID查询"
                                 :fetch-suggestions="queryBySlId"
                                 @select="handleSuIdSelect">
                </el-autocomplete>
            </el-form-item>
            <el-form-item label="地点名">
                <el-autocomplete v-model="modifyForm.slName"
                                 placeholder="通过请输入地点名查询"
                                 :fetch-suggestions="queryBySlName"
                                 @select="handleSuNameSelect">
                </el-autocomplete>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="doModify" :loading="loading">
                    确定
                </el-button>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>

<script>
    export default {
        name: "ModifyScheduleLocationDialog",
        props: {
            dialogVisible: Boolean,
            schedule: Object
        },
        data() {
            return {
                modifyForm: {
                    slId: '',
                    slName: ''
                },
                locationSuggestList: [],
                loading: false
            }
        },
        methods: {
            closeDialog(slId) {
                this.modifyForm.slId = ''
                this.modifyForm.slName = ''
                this.suggestList = []
                this.loading = false
                this.$emit('closeDialog', slId)
            },
            queryBySlId(val, callback) {
                if ('' === val) {
                    callback([])
                    return
                }
                val = parseInt(val)
                if (isNaN(val))
                    return
                this.$request.getLocations(1, 10, val, null)
                    .then(res => {
                        if (!res.data.success) {
                            callback([])
                            return
                        }

                        this.suggestList = res.data.data.list
                        this.locationSuggestList.forEach(user => user.value = `${user.slId} ${user.slName}`)
                        callback(this.locationSuggestList)
                    })
                    .catch(() => {
                        callback([])
                    })
            },
            handleSuIdSelect(location) {
                this.modifyForm.suId = location.slId
                this.modifyForm.suName = location.slName
            },
            queryBySlName(val, callback) {
                if ('' === val) {
                    callback([])
                    return
                }
                this.$request.getLocations(1, 10, null, val)
                    .then(res => {
                        if (!res.data.success) {
                            callback([])
                            return
                        }

                        this.suggestList = res.data.data.list
                        this.locationSuggestList.forEach(user => user.value = `${user.slId} ${user.slName}`)
                        callback(this.locationSuggestList)
                    })
                    .catch(() => {
                        callback([])
                    })
            },
            handleSuNameSelect(location) {
                this.modifyForm.slId = location.slId.toString()
                this.modifyForm.slName = location.slName
            },
            doModify() {
                if ('' === this.modifyForm.slId)
                    return
                this.loading = true
                this.$request.administrator.modifyScheduleLocation(this.schedule.ssId, this.modifyForm.slId)
                    .then(res => {
                        if (!res.data.success) {
                            this.$message.error("提交失败")
                            return
                        }

                        this.$message.success("操作成功, 开始发起")
                        this.closeDialog(this.modifyForm.slId)
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
            }
        }
    }
</script>

<style scoped>

</style>