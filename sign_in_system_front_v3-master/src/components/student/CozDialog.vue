<template>
    <el-dialog
            width="500px"
            title="查看课程"
            v-if="null != this.course"
            :visible="dialogVisible"
            :before-close="closeDialog">
        <el-tabs v-model="active" type="card" :stretch="true">
            <el-tab-pane label="课程信息" name="showCozDtl">
                <el-form class="course-dtl-form" size="mini" label-position="left" label-width="80px">
                    <el-form-item label="课程序号">
                        <span v-text="course.scId"></span>
                    </el-form-item>
                    <el-form-item label="课程名称">
                        <span v-text="course.scName"></span>
                    </el-form-item>
                    <el-form-item label="任课老师">
                        <div v-for="val in teacherList"
                             :key="val.suId">
                            <span v-text="`${val.suName}`"></span>
                            <br>
                        </div>
                    </el-form-item>
                    <el-form-item label="上课时间">
                        <div v-for="schedule in course.sisScheduleList"
                             :key="schedule.ssId">
                            <span v-text="getScheduleTimeString(schedule)">
                            </span>
                            <br>
                        </div>
                    </el-form-item>
                </el-form>
            </el-tab-pane>
            <el-tab-pane label="历史签到" name="hisSignIn">
                <el-container>
                    <el-header class="header">
                        <el-form inline size="mini">
                            <el-form-item label="上课时间">
                                <el-select
                                        placeholder="上课时间"
                                        value=""
                                        v-model="selectSchIdx">
                                    <el-option label="未选择" value=""></el-option>
                                    <el-option
                                            v-for="(val, idx) in hisScheduleList"
                                            :key="val.ssId"
                                            :label="getScheduleTimeString(val)"
                                            :value="idx.toString()">
                                    </el-option>
                                </el-select>
                            </el-form-item>
                        </el-form>
                    </el-header>
                    <el-main>
                        <el-table :data="historyList">
                            <el-table-column label="周" prop="ssiWeek"></el-table-column>
                            <el-table-column label="状态" prop="sisSignInDetail.ssidStatus">
                                <template slot-scope="scope">
                                    <span v-text="codeMap.get(scope.row.sisSignInDetail.ssidStatus)"></span>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-main>
                </el-container>
            </el-tab-pane>
        </el-tabs>
    </el-dialog>
</template>

<script>
    import {mapState} from 'vuex'
    import courseUtils from '@/util/courseUtils.js'

    export default {
        name: 'CozDialog',
        props: {
            course: Object,
            dialogVisible: Boolean,
        },
        computed: {
            ...mapState({
                user: 'user'
            }),
            teacherList() {
                if (null == this.course)
                    return []
                return this.course
                    .sisJoinCourseList
                    .filter(sisJoinCourse => sisJoinCourse.joinCourseType === 1)
                    .map(sisJoinCourse => sisJoinCourse.sisUser)
            },
            historyList() {
                const selectSchIdx = parseInt(this.selectSchIdx)
                if (null == this.hisScheduleList[selectSchIdx])
                    return []
                const sisSignInList = JSON.parse(JSON.stringify(
                    this.hisScheduleList[selectSchIdx].sisSignInList
                ))

                sisSignInList.forEach(sisSignIn => {
                    const sisSignInDetailList = sisSignIn.sisSignInDetailList
                    sisSignIn.sisSignInDetail = sisSignInDetailList
                        .find(sisSignInDetail => sisSignInDetail.suId === this.user.suId)
                    delete sisSignIn.sisSignInDetailList
                })
                return sisSignInList
            }
        },
        data() {
            return {
                codeMap: new Map([[false, '缺勤'], [true, '到勤']]),
                selectSchIdx: '',
                active: 'showCozDtl',
                hisScheduleList: []
            }
        },
        watch: {
            course() {
                if (this.course === null) {
                    return
                }
                this.$request.student.getHisSignIns(this.course.scId)
                    .then(res => {
                        this.hisScheduleList = res.data.sisScheduleList
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
        methods: {
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            closeDialog() {
                this.active = 'showCozDtl'
                this.$emit('closeDialog')
            }
        }
    }
</script>

<style scoped>
    .course-dtl-form {
        margin: 20px;
    }

    .header {
        border-bottom: #c3d5e2 1px solid;
    }
</style>
