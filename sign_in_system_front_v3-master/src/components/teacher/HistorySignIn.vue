<template>
    <el-container>
        <el-header>
            <el-form :inline="true" size="mini" :model="selectForm">
                <el-form-item label="课程">
                    <el-select v-model="selectForm.scId" @change="clearSsId">
                        <el-option label="请先选择" value=""></el-option>
                        <el-option v-for="val in courseList"
                                   :key="val.scId"
                                   :label="`${val.scName} ${val.scId}`"
                                   :value="val.scId">

                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="上课时间">
                    <el-select v-model="selectForm.ssId">
                        <el-option label="请先选择" value=""></el-option>
                        <el-option
                                v-for="val in getScheduleList(selectForm.scId)"
                                :key="val.ssId"
                                :label="getScheduleTimeString(val)"
                                :value="val.ssId">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="周">
                    <el-select v-model="selectForm.week" :loading="loading">
                        <el-option label="请先选择" value=""></el-option>
                        <el-option
                                v-for="val in getWeekList(selectForm.ssId)"
                                :key="val.value"
                                :label="val.label"
                                :value="val.value.toString()">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="签到状态">
                    <el-select v-model="selectForm.siStatus">
                        <el-option label="全部" :value="null"></el-option>
                        <el-option label="已签到" :value="true"></el-option>
                        <el-option label="缺勤" :value="false"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="onSure" :loading="loading">
                        确定
                    </el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="info" :plain="true" @click="exportData()">
                        导出表格
                    </el-button>
                </el-form-item>
                <br>
                <el-form-item label="应到人数">
                    <span v-text="getScActSize()"></span>
                </el-form-item>
                <el-form-item label="实到人数">
                    <span v-text="getActSize()"></span>
                </el-form-item>
                <el-form-item label="本次到勤率">
                    <span v-text="getAttRate()"></span>
                </el-form-item>
                <el-form-item>
                    <el-button size="mini" @click="getRefer">本次参照物</el-button>
                </el-form-item>
            </el-form>
        </el-header>
        <el-main>
            <el-table :data="filterTableData()">
                <el-table-column label="学号" prop="suId"></el-table-column>
                <el-table-column label="姓名" prop="sisUser.suName"></el-table-column>
                <el-table-column label="签到状态">
                    <template slot-scope="scope">
                        <div v-text="scope.row.ssidStatus ? '已签到' : '缺勤'">
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="签到凭证" prop="ssidPicture">
                    <template slot-scope="scope">
                        <div v-if="null == scope.row.ssidPicture">无</div>
                        <el-button v-else type="info" size="mini"
                                   @click="showSignInPictureDialog(scope.row.ssidPicture)">
                            查看凭证
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
        <SignInPictureDialog :dialog-visible="signInPictureDialog.dialogVisible" :pic-src="signInPictureDialog.picSrc"
                             @closeDialog="closeSignInPictureDialog"/>
    </el-container>
</template>

<script>
    import courseUtils from '@/util/courseUtils'
    import {mapState} from 'vuex'
    import SignInPictureDialog from "@/components/dialog/SignInPictureDialog";

    export default {
        name: "HistorySignIn",
        components: {SignInPictureDialog},
        computed: {
            ...mapState({
                token: 'token',
            })
        },
        data() {
            return {
                courseList: [],
                selectForm: {
                    scId: '',
                    ssId: '',
                    week: '',
                    siStatus: null
                },
                loading: false,
                scheduleWithSignInList: [],
                signIn: null,
                signInPictureDialog: {
                    dialogVisible: false,
                    picSrc: null
                }
            }
        },
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
            getScheduleList(scId) {
                const course = this.courseList.find(course => course.scId === scId)
                if (!course)
                    return []
                return course.sisScheduleList
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            clearSsId() {
                this.selectForm.ssId = ''
                this.loading = true
                this.$request.teacher.getSignIns(this.selectForm.scId)
                    .then(res => {
                        const scheduleWithSignInList = res.data.sisScheduleList
                        this.scheduleWithSignInList = null == scheduleWithSignInList ? null : scheduleWithSignInList
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
            getWeekList(ssId) {
                if ('' === ssId)
                    return []
                const course = this.courseList.find(course => course.scId === this.selectForm.scId)
                if (!course)
                    return []
                const schedule = course.sisScheduleList.find(schedule => schedule.ssId === ssId)
                if (!schedule)
                    return []
                const weekList = ((start, end) => Array(end - start + 1).fill(0).map((v, i) => i + start))(schedule.ssStartWeek, schedule.ssEndWeek)
                if (weekList.length <= 0)
                    return []
                if (this.loading)
                    return []
                if (null == this.scheduleWithSignInList)
                    return []
                const scheduleWithSignIn = this.scheduleWithSignInList.find(schedule => schedule.ssId === ssId)
                if (!scheduleWithSignIn)
                    return []

                return weekList
                    .map(week => {
                        week = parseInt(week)
                        const processing = scheduleWithSignIn.sisProcessingList.find(e => e.week === week)
                        if (processing) {
                            return {
                                value: week,
                                label: `${week} 进行中`
                            }
                        }

                        const signIn = scheduleWithSignIn.sisSignInList.find(e => e.ssiWeek === week)
                        if (signIn)
                            return {
                                value: week,
                                label: `${week} 已签到`
                            }

                        return null
                    })
                    .filter(s => null != s)
            },
            onSure() {
                this.loading = true
                const ssId = this.selectForm.ssId;
                const week = this.selectForm.week;
                this.signIn = this.scheduleWithSignInList.find(schedule => schedule.ssId === ssId).sisSignInList.find(e => e.ssiWeek === parseInt(week))
                const _this = this
                setTimeout(() => {
                    _this.loading = false
                }, 300)
            },
            getScActSize() {
                if ('' === this.selectForm.scId) return ''
                const course = this.courseList.find(c => c.scId === this.selectForm.scId)
                if (null == course) return ''
                return course.scActSize + ' 人'
            },
            getActSize() {
                if (null == this.signIn) return ''
                return this.signIn.sisSignInDetailList.filter(s => s.ssidStatus === true).length + ' 人'
            },
            getAttRate() {
                if (null == this.signIn) return ''
                if ('' === this.selectForm.scId) return ''
                const course = this.courseList.find(c => c.scId === this.selectForm.scId)
                if (null == course) return ''
                const a = course.scActSize
                const b = this.signIn.sisSignInDetailList.filter(s => s.ssidStatus === true).length
                return `${(Math.round((b / a) * 10000) / 100).toFixed(2) + '%'}`
            },
            getRefer() {
                if (null == this.signIn) return ''

                this.signInPictureDialog.dialogVisible = true
                this.signInPictureDialog.picSrc = this.signIn.ssiPicture
            },
            filterTableData() {
                const ssidList = null == this.signIn ? null : this.signIn.sisSignInDetailList
                if (null == ssidList) return []
                if (null == this.selectForm.siStatus)
                    return ssidList
                if (this.selectForm.siStatus) return ssidList.filter(s => s.ssidStatus === true)
                return ssidList.filter(s => s.ssidStatus === false)
            },
            exportData() {
                if (null == this.selectForm.scId || '' === this.selectForm.scId) {
                    this.message.error('请先选择课程')
                    return
                }
                const course = this.courseList.find(c => c.scId === this.selectForm.scId)
                if (null == course) {
                    this.message.error('未知错误, 请刷新')
                    return
                }
                const frame = document.createElement("iframe")
                frame.src = `https://api.xsix103.cn/sign_in_system/v3/courses/${course.scId}/signIns/export?accessToken=${this.token}`
                frame.style.display = 'none'
                document.body.appendChild(frame);
            },
            showSignInPictureDialog(picSrc) {
                this.signInPictureDialog = {
                    dialogVisible: true,
                    picSrc
                }
            },
            closeSignInPictureDialog() {
                this.signInPictureDialog = {
                    dialogVisible: false,
                    picSrc: null
                }
            }
        }
    }
</script>

<style scoped>

</style>