<template>
    <el-container>
        <el-main>
            <el-table v-loading="loading" :data="transList">
                <el-table-column label="督导周" prop="smtWeek" width="70px"></el-table-column>
                <el-table-column label="课程序号" prop="sisSchedule.sisCourse.scId" width="110px"></el-table-column>
                <el-table-column label="课程名字" prop="sisSchedule.sisCourse.scName" width="130px"></el-table-column>
                <el-table-column label="任课老师" width="120px">
                    <template slot-scope="scope">
                        <div v-for="val in getTeacherList(scope.row.sisSchedule.sisCourse.sisJoinCourseList)"
                             :key="val.suId">
                            <span v-text="`${val.suName}`">
                                <br>
                            </span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="上课时间">
                    <template slot-scope="scope">
                        <span v-text="getScheduleTimeString(scope.row.sisSchedule)"></span>
                    </template>
                </el-table-column>
                <el-table-column label="教室容量" prop="sisSchedule.sisCourse.scMaxSize" width="90px"></el-table-column>
                <el-table-column label="课程人数" prop="sisSchedule.sisCourse.scActSize" width="90px"></el-table-column>
                <el-table-column label="到勤率" prop="sisSchedule.sisCourse.scAttRate" width="70px">
                    <template slot-scope="scope">
                        <div v-text="null == scope.row.sisSchedule.sisCourse.scAttRate ? '无' : scope.row.sisSchedule.sisCourse.scAttRate"></div>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="150px">
                    <template slot-scope="scope">
                        <el-button type="success" size="mini" @click="onAccept(scope.row)">
                            接受
                        </el-button>
                        <el-button type="danger" size="mini" @click="onRefuse(scope.row)">
                            拒绝
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
    </el-container>
</template>

<script>
    import courseUtils from "@/util/courseUtils";

    export default {
        name: "MyUntreatedTrans",
        data() {
            return {
                loading: false,
                transList: []
            }
        },
        created() {
            this.onSearch()
        },
        methods: {
            onSearch() {
                this.loading = true
                this.$request.monitor.getSchedules('untreated')
                    .then(res => {
                        this.transList = res.data
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
            },
            getTeacherList(joinCourseList) {
                return joinCourseList
                    .filter(joinCourse => joinCourse.joinCourseType === 1)
                    .map(joinCourse => joinCourse.sisUser)
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            onAccept(monitorTrans) {
                this.$confirm('确定接受？', '接受转接')
                    .then(() => {
                        monitorTrans.smtStatus = 1
                        delete monitorTrans.sisSchedule
                        this.$request.monitor
                            .modifyTransfer(monitorTrans.ssId, monitorTrans)
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message.error("提交失败")
                                    return
                                }

                                this.$message.success("操作成功")
                                this.loading = false
                                this.onSearch()
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
                    .catch(() => {
                    })
            },
            onRefuse(monitorTrans) {
                this.$confirm('确定拒绝？', '接受转接')
                    .then(() => {
                        monitorTrans.smtStatus = 2
                        delete monitorTrans.sisSchedule
                        this.$request.monitor
                            .modifyTransfer(monitorTrans.ssId, monitorTrans)
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message.error("提交失败")
                                    return
                                }

                                this.$message.success("操作成功")
                                this.loading = false
                                this.onSearch()
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
                    .catch(() => {
                    })
            }
        }
    }
</script>

<style scoped>

</style>