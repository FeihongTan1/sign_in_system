<template>
    <el-dialog title="课程信息"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-container>
            <el-header>
                <el-form size="mini" :inline="true" :model="form">
                    <el-form-item label="督导课">
                        <el-select v-model="form.scId" @change="getSuvSchList">
                            <el-option label="请先选择" value=""></el-option>
                            <el-option v-for="val in courseList"
                                       :key="`mid_${val.scId}`"
                                       :label="`${val.scName} ${val.scId}`"
                                       :value="val.scId"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="上课时间">
                        <el-select v-model="form.ssId" :loading="loading" @change="getSupervisionList">
                            <el-option label="请先选择" value=""></el-option>
                            <el-option v-for="val in getSchedule()"
                                       :key="`mid_${val.ssId}`"
                                       :label="getScheduleTimeString(val)"
                                       :value="val.ssId.toString()"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="应到人数">
                        <span v-text="getActSize()"></span>
                    </el-form-item>
                </el-form>
            </el-header>
            <el-main>
                <el-table :data="sisSupervisionList" v-loading="loading">
                    <el-table-column label="督导周" prop="ssvWeek"></el-table-column>
                    <el-table-column label="本次到勤率">
                        <template slot-scope="scope">
                            <span v-text="getAttRate(scope.row.ssvActualNum)"></span>
                        </template>
                    </el-table-column>
                    <el-table-column label="实勤人数" prop="ssvActualNum"></el-table-column>
                    <el-table-column label="玩手机人数" prop="ssvMobileNum"></el-table-column>
                    <el-table-column label="睡觉人数" prop="ssvSleepNum"></el-table-column>
                    <el-table-column label="督导备注" prop="ssvRecInfo"></el-table-column>
                </el-table>
            </el-main>
        </el-container>
    </el-dialog>
</template>

<script>
    import courseUtils from "@/util/courseUtils";

    export default {
        name: "MonitorInfoDialog",
        props: {
            dialogVisible: Boolean,
            suId: String
        },
        watch: {
            suId(suId) {
                if (null == suId || '' === suId) {
                    this.courseList = []
                    return
                }
                this.$request.administrator.getMonitorCourse(suId)
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
            }
        },
        data() {
            return {
                loading: false,
                courseList: [],
                scheduleList: [],
                sisSupervisionList: [],
                form: {
                    scId: '',
                    ssId: ''
                }
            }
        },
        methods: {
            closeDialog() {
                this.$emit('closeDialog')
                this.loading = false
                this.courseList = []
                this.scheduleList = []
                this.sisSupervisionList = []
                this.form = {
                    scId: '',
                    ssId: ''
                }
            },
            getSchedule() {
                const course = this.courseList.find(c => c.scId === this.form.scId)
                if (null == course)
                    return []
                return course.sisScheduleList
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            getSuvSchList(scId) {
                if ('' === scId) return

                this.loading = true
                this.$request.administrator
                    .getSupervision(scId)
                    .then(res => {
                        this.scheduleList = res.data
                        this.scheduleList.forEach(s => s.ssId = s.ssId.toString())

                        if (this.scheduleList.length > 0) {
                            this.ssId = this.scheduleList[0].ssId
                        }
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
            getSupervisionList(ssId) {
                if ('' === ssId) {
                    this.sisSupervisionList = null
                    return
                }

                this.sisSupervisionList = this.scheduleList
                    .find(schedule => schedule.ssId === ssId)
                    .sisSupervisionList
            },
            getActSize() {
                if ('' === this.form.scId) return ''
                const course = this.courseList.find(c => c.scId === this.form.scId)
                if (null == course)
                    return ''
                return `${course.scActSize} 人`
            },
            getAttRate(ssvActualNum) {
                const course = this.courseList.find(c => c.scId === this.form.scId)
                if (null == course)
                    return '无'
                return `${(Math.round((ssvActualNum / course.scActSize) * 10000) / 100).toFixed(2) + '%'}`
            }
        }
    }
</script>

<style scoped>

</style>