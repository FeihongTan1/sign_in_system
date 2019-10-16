<template>
    <el-dialog title="学生信息"
               top="20px"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-container v-if="joinCourseList">
            <el-header height="40px">
                <span>查看课堂缺勤数请在历史签到查看</span>
            </el-header>
            <el-main>
                <el-table :data="joinCourseList" height="600px" v-loading="loading">
                    <el-table-column label="账号/学号" prop="sisUser.suId"></el-table-column>
                    <el-table-column label="姓名" prop="sisUser.suName"></el-table-column>
                    <el-table-column label="总缺勤数(按该学生课表计算)" prop="sisUser.suiCozLackNum"></el-table-column>
                </el-table>
            </el-main>
        </el-container>
    </el-dialog>
</template>

<script>
    export default {
        name: "ShowStudentDialog",
        props: {
            dialogVisible: Boolean,
            scId: String
        },
        watch: {
            scId(scId) {
                if (null == scId || '' === scId) {
                    this.joinCourseList = null
                    return
                }

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
                    .finally(() => this.loading = false)
            }
        },
        data() {
            return {
                loading: false,
                joinCourseList: null
            }
        },
        methods: {
            closeDialog() {
                this.$emit('closeDialog')
                this.joinCourseList = null
            }
        }
    }
</script>

<style scoped>

</style>