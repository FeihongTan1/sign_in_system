<template>
    <el-container>
        <el-main>
            <el-table :data="courseList" v-loading="loading">
                <el-table-column label="课程序号" prop="scId" width="110px"></el-table-column>
                <el-table-column label="课程名字" prop="scName" width="130px"></el-table-column>
                <el-table-column label="任课老师" width="120px">
                    <template slot-scope="scope">
                        <div v-for="val in getTeacherList(scope.row.sisJoinCourseList)"
                             :key="val.suId">
                            <span v-text="`${val.suName}`">
                                <br>
                            </span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="上课时间">
                    <template slot-scope="scope">
                        <div v-for="val in scope.row.sisScheduleList"
                             :key="val.ssId">
                            <span v-text="getScheduleTimeString(val)">
                                <br>
                            </span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="教室容量" prop="scMaxSize" width="80px"></el-table-column>
                <el-table-column label="课程人数" prop="scActSize" width="80px"></el-table-column>
                <el-table-column label="总到勤率" prop="scAttRate" width="70">
                    <template slot-scope="scope">
                        <div v-text="null == scope.row.scAttRate ? '无' : `${(Math.round(scope.row.scAttRate * 10000)/100).toFixed(2) + '%'}`"></div>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200px">
                    <template slot-scope="scope">
                        <el-button type="info" size="mini" @click="showSupervision(scope.row)">
                            督导历史
                        </el-button>
                        <el-button size="mini" @click="showTransDialog(scope.row)">
                            发起转接
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
        <SupervisionsDialog :dialogVisible="supervisionsDialog.dialogVisible"
                            :course="supervisionsDialog.course"
                            @closeDialog="closeSupervision"/>
        <TransDialog :dialogVisible="transDialog.dialogVisible"
                     :course="transDialog.course"
                     @closeDialog="closeTransDialog"/>
    </el-container>
</template>

<script>
    import courseUtils from '@/util/courseUtils'
    import SupervisionsDialog from "../dialog/SupervisionsDialog";
    import TransDialog from "../dialog/TransDialog";

    export default {
        name: "CourseTable",
        components: {TransDialog, SupervisionsDialog},
        data() {
            return {
                loading: false,
                courseList: [],
                supervisionsDialog: {
                    dialogVisible: false,
                    scId: ''
                },
                transDialog: {
                    dialogVisible: false,
                    course: null
                }
            }
        },
        created() {
            this.loading = true
            this.$request.monitor.getCourseTable()
                .then(res => {
                    this.courseList = res.data.list
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
        methods: {
            showSupervision(course) {
                this.supervisionsDialog.dialogVisible = true
                this.supervisionsDialog.course = course
            },
            getTeacherList(joinCourseList) {
                return joinCourseList
                    .filter(joinCourse => joinCourse.joinCourseType === 1)
                    .map(joinCourse => joinCourse.sisUser)
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            closeSupervision() {
                this.supervisionsDialog.dialogVisible = false
                this.supervisionsDialog.course = null
            },
            showTransDialog(course) {
                this.transDialog = {
                    dialogVisible: true,
                    course
                }
            },
            closeTransDialog() {
                this.transDialog = {
                    dialogVisible: false,
                    course: null
                }
            }
        }
    }
</script>

<style scoped>

</style>