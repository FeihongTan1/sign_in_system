<template>
    <el-dialog title="课程信息"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-form size="mini" v-if="null != course" label-width="80px" label-position="right">
            <el-form-item label="年级">
                <span v-text="course.scGrade"></span>
            </el-form-item>
            <el-form-item label="课程序号">
                <span v-text="course.scId"></span>
            </el-form-item>
            <el-form-item label="课程名字">
                <span v-text="course.scName"></span>
            </el-form-item>
            <el-form-item label="任课老师">
                <span v-for="val in getTeacherList(course.sisJoinCourseList)"
                      :key="`cd_${val.suId}`" v-text="`${val.suName}`">
                </span>
            </el-form-item>
            <el-form-item label="上课时间">
                <span v-for="val in course.sisScheduleList"
                      :key="`cd_${val.ssId}`" v-text="`${getScheduleTimeString(val)}; `">
                </span>
            </el-form-item>
            <el-form-item label="督导状态">
                <span v-text="getCourseMonitorStatus(course)"></span>
            </el-form-item>
            <el-form-item label="督导人" v-if="null != course.suId">
                <span v-text="`${course.monitor ? `${course.monitor.suName} ${course.monitor.suId}` : ''}`"></span>
            </el-form-item>
            <el-form-item label="课程人数">
                <span v-text="course.scActSize"></span>
            </el-form-item>
            <el-form-item label="教室容量">
                <span v-text="course.scMaxSize"></span>
            </el-form-item>
            <el-form-item label="总到勤率">
                <span v-text="`${(Math.round(course.scAttRate * 10000)/100).toFixed(2) + '%'}`"></span>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>

<script>
    import courseUtils from "@/util/courseUtils";

    export default {
        name: "ShowCourseDialog",
        props: {
            dialogVisible: Boolean,
            course: Object
        },
        data() {
            return {}
        },
        methods: {
            closeDialog() {
                this.$emit('closeDialog')
            },
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
            }
        }
    }
</script>

<style scoped>

</style>