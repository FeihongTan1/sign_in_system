<template>
    <el-dialog title="修改学生"
               top="10px"
               width="800px"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-main>
            <el-table v-loading="loading" :data="joinCourseList" height="calc(100vh - 300px)">
                <el-table-column label="学号" prop="suId">
                    <template slot-scope="scope">
                        <el-autocomplete size="mini"
                                         v-model="scope.row.suId"
                                         placeholder="请输入学号"
                                         :fetch-suggestions="queryBySuId"
                                         @select="handleSuIdSelect(scope.row, $event)"
                                         :disabled="null != scope.row.sjcId && '' !== scope.row.sjcId">
                        </el-autocomplete>
                    </template>
                </el-table-column>
                <el-table-column label="姓名" prop="sisUser.suName">
                    <template slot-scope="scope">
                        <el-autocomplete size="mini" v-model="scope.row.sisUser.suName"
                                         placeholder="请输入姓名"
                                         :fetch-suggestions="queryBySuName"
                                         @select="handleSuNameSelect(scope.row, $event)"
                                         :disabled="null != scope.row.sjcId && '' !== scope.row.sjcId">
                        </el-autocomplete>
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template slot-scope="scope">
                        <el-button size="mini"
                                   :type="null != scope.row.sjcId && '' !== scope.row.sjcId ? 'danger' : 'warning'"
                                   @click="delJoinStudent(scope.$index, null != scope.row.sjcId && '' !== scope.row.sjcId)">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
        <el-footer>
            <el-button size="mini" @click="closeDialog">
                返回
            </el-button>
            <el-button size="mini"
                       type="primary" @click="addNewJoin">
                新增
            </el-button>
            <el-button size="mini"
                       type="danger" @click="onSure">
                确定修改
            </el-button>
            <el-button size="mini" type="success" @click="reset">
                重置
            </el-button>
        </el-footer>
    </el-dialog>
</template>

<script>
    export default {
        name: "ModifyJoinCourseDialog",
        props: {
            dialogVisible: Boolean,
            scId: String
        },
        watch: {
            scId(scId) {
                this.loading = true
                this.$request.administrator.getJoinCourses(scId)
                    .then(res => {
                        this.joinCourseList = res.data
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
        },
        data() {
            return {
                loading: false,
                joinCourseList: [],
                suggestList: []
            }
        },
        methods: {
            reset() {
                this.loading = true
                this.$request.administrator.getJoinCourses(this.scId)
                    .then(res => {
                        this.joinCourseList = res.data
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
                this.$emit('closeDialog')
            },
            addNewJoin() {
                this.joinCourseList.unshift({
                    scId: this.scId,
                    suId: '',
                    sisUser: {
                        suName: ''
                    }
                })
            },
            delJoinStudent(index, confirm) {
                if (confirm) {
                    this.$confirm('删除已存在的上课学生, 将影响已存在的相关督导和签到, 请慎重考虑吧', '删除学生')
                        .then(() => this.joinCourseList.splice(index, 1))
                        .catch(() => {
                        })
                    return
                }
                this.joinCourseList.splice(index, 1)
            },
            queryBySuId(val, callback) {
                if ('' === val) {
                    callback([])
                    return
                }
                this.$request.administrator.getStudents(1, 10, val, null)
                    .then(res => {
                        this.suggestList = res.data.list
                        this.suggestList.forEach(user => user.value = `${user.suId} ${user.suName}`)
                        callback(this.suggestList)
                    })
                    .catch(() => {
                    })
            },
            handleSuIdSelect(val, $event) {
                val.suId = $event.suId
                val.sisUser.suName = $event.suName
            },
            queryBySuName(val, callback) {
                if ('' === val)
                    return
                this.$request.administrator.getStudents(1, 10, null, val)
                    .then(res => {
                        if (!res.data.success)
                            return

                        this.suggestList = res.data.data.list
                        this.suggestList.forEach(user => user.value = `${user.suId} ${user.suName}`)
                        callback(this.suggestList)
                    })
                    .catch(() => {
                    })
            },
            handleSuNameSelect(val, $event) {
                val.suId = $event.suId
                val.sisUser.suName = $event.suName
            },
            onSure() {
                this.$confirm('请再次确认', '修改信息')
                    .then(() => {

                        this.loading = true
                        this.$request.administrator.modifyJoinCourse(this.scId, this.joinCourseList)
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message.error("提交失败")
                                    return
                                }

                                this.$message.success("操作成功")
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
                    })
                    .catch(() => {
                    })
            }
        }
    }
</script>

<style scoped>

</style>