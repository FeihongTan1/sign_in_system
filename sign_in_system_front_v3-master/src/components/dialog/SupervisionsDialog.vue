<template>
    <el-dialog :title="getTitle()"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-container>
            <el-header>
                <el-form :inline="true" size="mini">
                    <el-form-item label="上课时间">
                        <el-select placeholder="上课时间" v-model="ssId">
                            <el-option label="未选择" value="">
                            </el-option>
                            <el-option v-for="val in scheduleList"
                                       :key="`sd_${val.ssId}`"
                                       :label="getScheduleTimeString(val)"
                                       :value="val.ssId">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="应到人数">
                        <span v-text="null == this.course ? '' : `${this.course.scActSize} 人`"></span>
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
                    <el-table-column label="实到人数" prop="ssvActualNum"></el-table-column>
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
        name: "SupervisionsDialog",
        props: {
            course: Object,
            dialogVisible: Boolean
        },
        data() {
            return {
                loading: false,
                ssId: '',
                sisSupervisionList: null,
                scheduleList: []
            }
        },
        watch: {
            ssId() {
                if ('' === this.ssId) {
                    const list = []
                    this.scheduleList.forEach(s => {
                        list.push(...s.sisSupervisionList)
                    })
                    this.sisSupervisionList = list
                    return
                }

                this.sisSupervisionList = this.scheduleList
                    .find(schedule => schedule.ssId === this.ssId)
                    .sisSupervisionList
            },
            course() {
                if (null == this.course) return

                this.loading = true
                this.$request.administrator
                    .getSupervision(this.course.scId)
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
            }
        },
        methods: {
            closeDialog() {
                this.$emit('closeDialog')
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            getAttRate(ssvActualNum) {
                if (null == this.course) return ''
                return `${(Math.round((ssvActualNum / this.course.scActSize) * 10000) / 100).toFixed(2) + '%'}`
            },
            getTitle() {
                if (null == this.course) return '课程信息'
                let tchStr = ''
                this.course.sisJoinCourseList
                    .map(joinCourse => joinCourse.sisUser)
                    .forEach(t => tchStr += t.suName + ' ')

                return `${this.course.scName} ${this.course.scId} ${tchStr}`
            }
        }
    }
</script>

<style scoped>

</style>