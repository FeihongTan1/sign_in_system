<template>
    <el-container>
        <el-main>
            <el-table :data="courseList">
                <el-table-column prop="scId" label="课程序号" width="110px"/>
                <el-table-column prop="scName" label="课程名字" width="110px"/>
                <el-table-column label="任课老师" width="150px">
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
                <el-table-column label="督导状态" width="100px">
                    <template slot-scope="scope">
                        <span v-text="getCourseMonitorStatus(scope.row)"></span>
                    </template>
                </el-table-column>
                <el-table-column label="教室容量" prop="scMaxSize" width="80px"></el-table-column>
                <el-table-column label="课程人数" prop="scActSize" width="80px"></el-table-column>
                <el-table-column label="总到勤率" prop="scAttRate" width="90px">
                    <template slot-scope="scope">
                        <div v-text="null == scope.row.scAttRate ? '无' : `${(Math.round(scope.row.scAttRate * 10000)/100).toFixed(2) + '%'}`"></div>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120px">
                    <template slot-scope="scope">
                        <el-dropdown>
                            <el-button type="primary" size="mini">
                                课程管理<i class="el-icon-arrow-down el-icon--right"></i>
                            </el-button>
                            <el-dropdown-menu slot="dropdown">
                                <el-dropdown-item @click.native="showCourse(scope.row)">
                                    课程信息
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="showStudents(scope.row.scId)">
                                    学生信息
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="showSupervision(scope.row)">
                                    历史督导
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="showCreateSignIn(scope.row)" :divided="true">
                                    发起签到
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </el-dropdown>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
        <CourseDialog :dialogVisible="courseDialog.dialogVisible"
                      :course="courseDialog.course"
                      @closeDialog="closeCourse"/>
        <StudentDialog :dialogVisible="studentsDialog.dialogVisible"
                       :scId="studentsDialog.scId"
                       @closeDialog="closeStudents"/>
        <SupervisionsDialog :dialogVisible="supervisionsDialog.dialogVisible"
                            :course="supervisionsDialog.course"
                            @closeDialog="closeSupervision"/>
        <CreateSignInDialog :dialogVisible="createSignInDialog.dialogVisible"
                            :course="createSignInDialog.course"
                            @closeDialog="closeCreateSignInDialog"/>
    </el-container>
</template>

<script>
    import courseUtils from '@/util/courseUtils'
    import StudentDialog from "@/components/dialog/StudentDialog";
    import CourseDialog from "@/components/dialog/CourseDialog";
    import SupervisionsDialog from "@/components/dialog/SupervisionsDialog";
    import CreateSignInDialog from "@/components/dialog/CreateSignInDialog";

    export default {
        name: "CourseTable",
        data() {
            return {
                courseList: [],
                courseDialog: {
                    dialogVisible: false,
                    course: null
                },
                studentsDialog: {
                    dialogVisible: false,
                    scId: ''
                },
                supervisionsDialog: {
                    dialogVisible: false,
                    course: null
                },
                createSignInDialog: {
                    dialogVisible: false,
                    course: null
                },
            }
        },
        components: {CreateSignInDialog, SupervisionsDialog, StudentDialog, CourseDialog},
        created() {
            this.$request.teacher.getCourseTable()
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
        },
        methods: {
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            getCourseMonitorStatus(course) {
                return courseUtils.getCourseMonitorStatus(course)
            },
            getTeacherList(joinCourseList) {
                return joinCourseList
                    .filter(joinCourse => joinCourse.joinCourseType === 1)
                    .map(joinCourse => joinCourse.sisUser)
            },
            showCreateSignIn(course) {
                this.createSignInDialog.course = course
                this.createSignInDialog.dialogVisible = true
            },
            closeCreateSignInDialog() {
                this.createSignInDialog.course = null
                this.createSignInDialog.dialogVisible = false
                this.$request.teacher.getCourseTable()
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
            },
            showCourse(course) {
                this.courseDialog.course = course
                this.courseDialog.dialogVisible = true
            },
            closeCourse() {
                this.courseDialog.course = null
                this.courseDialog.dialogVisible = false
            },
            showStudents(scId) {
                this.studentsDialog.scId = scId
                this.studentsDialog.dialogVisible = true
            },
            closeStudents() {
                this.studentsDialog.scId = ''
                this.studentsDialog.dialogVisible = false
            },
            showSupervision(course) {
                this.supervisionsDialog.course = course
                this.supervisionsDialog.dialogVisible = true
            },
            closeSupervision() {
                this.supervisionsDialog.course = null
                this.supervisionsDialog.dialogVisible = false
            },
        }
    }
</script>

<style scoped>

</style>