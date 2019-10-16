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
                <el-table-column label="状态" width="120px">
                    <template slot-scope="scope">
                        <el-button size="mini" type="info" v-if="scope.row.sisSupervision">
                            查看历史
                        </el-button>
                        <el-button size="mini" type="info" v-else disabled="">
                            暂未督导
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
        name: "MonitorTrans",
        data() {
            return {
                loading: false,
                transList: []
            }
        },
        created() {
            this.loading = true
            this.$request.monitor.getSchedules('agree')
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
        methods: {
            getTeacherList(joinCourseList) {
                return joinCourseList
                    .filter(joinCourse => joinCourse.joinCourseType === 1)
                    .map(joinCourse => joinCourse.sisUser)
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            }
        }
    }
</script>

<style scoped>

</style>