<template>
    <el-dialog :title="getTitle()"
               width="900px"
               :close-on-click-modal="!disabled"
               :close-on-press-escape="!disabled"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-container>
            <el-header height="auto">
                <el-form :inline="true" size="mini">
                    <el-form-item label="上课时间">
                        <el-select placeholder="上课时间" v-model="ssId">
                            <el-option label="请先选择" value=""></el-option>
                            <el-option v-for="val in scheduleList"
                                       :key="`hsid_${val.ssId}`"
                                       :label="getScheduleTimeString(val)"
                                       :value="val.ssId">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="周">
                        <el-select placeholder="请选择周" v-model="week" @change="handleWeekSelect">
                            <el-option v-for="val in getWeekList(ssId)"
                                       :disabled="val.disable"
                                       :key="`hsid_${val.value}`"
                                       :label="val.label"
                                       :value="val.value.toString()">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="签到状态">
                        <el-select v-model="siStatus">
                            <el-option label="全部" :value="null"></el-option>
                            <el-option label="已签到" :value="true"></el-option>
                            <el-option label="缺勤" :value="false"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="info" :plain="true" @click="exportData()">
                            导出表格
                        </el-button>
                        <el-button v-if="disabled" type="danger" @click="disabled = false">解锁</el-button>
                        <el-button v-if="!disabled" type="primary" @click="reset">重置</el-button>
                        <el-button v-if="!disabled" type="danger" @click="update">保存</el-button>
                    </el-form-item>
                    <el-form-item label="应到人数">
                        <span v-text="null == course ? '' : course.scActSize"></span>
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
                <el-table :data="filterTableData()" v-loading="loading">
                    <el-table-column label="学号" prop="suId"></el-table-column>
                    <el-table-column label="姓名" prop="sisUser.suName"></el-table-column>
                    <el-table-column label="签到状态" prop="ssidStatus">
                        <template slot-scope="scope">
                            <div v-show="disabled" v-text="scope.row.ssidStatus ? '已签到' : '缺勤'">
                            </div>
                            <el-switch
                                    v-show="!disabled"
                                    v-model="scope.row.ssidStatus"
                                    active-text="已签到"
                                    inactive-text="缺勤">
                            </el-switch>
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
        </el-container>
        <SignInPictureDialog :dialog-visible="signInPictureDialog.dialogVisible" :pic-src="signInPictureDialog.picSrc"
                             @closeDialog="closeSignInPictureDialog"/>
    </el-dialog>
</template>

<script>
    import courseUtils from "@/util/courseUtils";
    import {mapState} from 'vuex'
    import SignInPictureDialog from "./SignInPictureDialog";

    export default {
        name: "HistorySignInDialog",
        components: {SignInPictureDialog},
        props: {
            course: Object,
            dialogVisible: Boolean
        },
        computed: {
            ...mapState({
                token: 'token',
            })
        },
        data() {
            return {
                loading: false,
                disabled: true,
                ssId: '',
                week: '',
                siStatus: null,
                scheduleList: [],
                tableData: [],
                signInPictureDialog: {
                    dialogVisible: false,
                    picSrc: null
                }
            }
        },
        watch: {
            course: function (course) {
                if (null == course) return

                this.loading = true
                this.$request.administrator
                    .getSignIns(course.scId)
                    .then(res => {
                        this.scheduleList = res.data.sisScheduleList
                        this.scheduleList.forEach(s => s.ssId = s.ssId.toString())
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
                this.ssId = ''
                this.week = ''
                this.scheduleList = []
                this.tableData = []
                this.disabled = true
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            getWeekList(ssId) {
                if ('' === ssId)
                    return []
                const schedule = this.scheduleList.find(schedule => schedule.ssId === ssId)
                if (null == schedule)
                    return []
                const weekList = ((start, end) => Array(end - start + 1).fill(0).map((v, i) => i + start))(schedule.ssStartWeek, schedule.ssEndWeek)
                return weekList
                    .map(week => {
                        const processing = schedule
                            .sisProcessingList
                            .find(e => e.week === week)
                        if (processing) {
                            return {
                                value: week,
                                label: `${week} 进行中`,
                                disable: true
                            }
                        }

                        const signIn = schedule
                            .sisSignInList
                            .find(e => e.ssiWeek === week)
                        if (signIn)
                            return {
                                value: week,
                                label: `${week} 已签到`,
                                disable: false
                            }

                        return null
                    })
                    .filter(s => null != s)
            },
            handleWeekSelect(week) {
                if ('' === week)
                    return
                const schedule = this.scheduleList.find(schedule => schedule.ssId === this.ssId)
                if (null == schedule)
                    return

                const sisSignIn = schedule.sisSignInList.find(e => e.ssiWeek === parseInt(week))
                if (null === sisSignIn)
                    return
                this.tableData = JSON.parse(JSON.stringify(sisSignIn.sisSignInDetailList))
            },
            getTitle() {
                if (null == this.course) return '历史签到'
                let tchStr = ''
                this.course.sisJoinCourseList
                    .map(joinCourse => joinCourse.sisUser)
                    .forEach(t => tchStr += t.suName + ' ')

                return `${this.course.scName} ${this.course.scId} ${tchStr}`
            },
            getActSize() {
                if (null == this.tableData || this.tableData.length <= 0) return ''
                return this.tableData.filter(s => s.ssidStatus === true).length + ' 人'
            },
            getAttRate() {
                if (null == this.tableData || this.tableData.length <= 0) return ''
                const a = this.tableData.filter(s => s.ssidStatus === true).length
                const b = this.course.scActSize
                return `${(Math.round((a / b) * 10000) / 100).toFixed(2) + '%'}`
            },
            getRefer() {
                if ('' === this.week)
                    return
                const schedule = this.scheduleList.find(schedule => schedule.ssId === this.ssId)
                if (null == schedule)
                    return

                const sisSignIn = schedule.sisSignInList.find(e => e.ssiWeek === parseInt(this.week))
                if (null === sisSignIn)
                    return

                this.signInPictureDialog.dialogVisible = true
                this.signInPictureDialog.picSrc = sisSignIn.ssiPicture
            },
            filterTableData() {
                if (null == this.siStatus)
                    return this.tableData
                if (this.siStatus) return this.tableData.filter(s => s.ssidStatus === true)
                return this.tableData.filter(s => s.ssidStatus === false)
            },
            exportData() {
                const frame = document.createElement("iframe")
                frame.src = `https://api.xsix103.cn/sign_in_system/v3/courses/${this.course.scId}/signIns/export?accessToken=${encodeURI(this.token)}`
                frame.style.display = 'none'
                document.body.appendChild(frame);
            },
            reset() {
                this.handleWeekSelect(this.week)
            },
            update() {
                this.$confirm('确认保存更改', '变更到勤')
                    .then(() => {
                        this.tableData
                            .forEach(d => delete d.sisUser)
                        const schedule = this.scheduleList.find(schedule => schedule.ssId === this.ssId)
                        const sisSignIn = schedule.sisSignInList.find(e => e.ssiWeek === parseInt(this.week))
                        this.$request.administrator
                            .modifySignIns(sisSignIn.ssiId, this.tableData)
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message.error("提交失败")
                                    return
                                }

                                this.$message.success("操作成功")
                                this.closeDialog()
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