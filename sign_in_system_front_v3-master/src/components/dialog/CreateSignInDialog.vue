<template>
    <el-dialog :title="getTitle()"
               width="500px"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-form v-if="null != course" label-width="80px" label-position="right">
            <el-form-item label="上课时间">
                <el-select v-model="ssId" @change="handleSelect">
                    <el-option label="未选择" value=""></el-option>
                    <el-option v-for="val in course.sisScheduleList"
                               :key="`csd_${val.ssId}`"
                               :label="`${getScheduleTimeString(val)}`"
                               :value="val.ssId.toString()">
                    </el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="地点">
                <span v-text="getLocation()"></span>
            </el-form-item>
            <el-form-item label="参照物">
                <el-upload
                        :limit="1"
                        drag
                        list-type="picture"
                        :auto-upload="false"
                        :on-change="onChangeFile"
                        action="#">
                </el-upload>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="createSignIn" :loading="loading">
                    确定
                </el-button>
            </el-form-item>
        </el-form>
        <ModifyScheduleLocationDialog :dialogVisible="modifyScheduleLocationDialog.dialogVisible"
                                      :schedule="modifyScheduleLocationDialog.schedule"
                                      @closeDialog="closeModifyScheduleLocationDialog"/>
    </el-dialog>
</template>

<script>
    import {mapState} from 'vuex'
    import courseUtils from '@/util/courseUtils';
    import judgeTimeUtils from '../../util/judgeTimeUtils';
    import ModifyScheduleLocationDialog from './ModifyScheduleLocationDialog'

    export default {
        name: "CreateSignInDialog",
        props: {
            dialogVisible: Boolean,
            course: Object
        },
        computed: {
            ...mapState({
                week: 'week'
            })
        },
        components: {ModifyScheduleLocationDialog},
        data() {
            return {
                ssId: '',
                loading: false,
                schedule: null,
                location: null,
                file: null,
                modifyScheduleLocationDialog: {
                    dialogVisible: false,
                    schedule: null
                }
            }
        },
        methods: {
            closeDialog() {
                if (this.loading)
                    return false
                this.$emit('closeDialog')
                this.ssId = ''
                this.loading = false
                this.schedule = null
                this.location = null
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            checkTime(schedule) {
                return judgeTimeUtils.isCourseTime(schedule, this.week, new Date())
            },
            createSignIn() {
                if ('' === this.ssId)
                    return
                if (null == this.schedule.slId) {
                    this.modifyScheduleLocationDialog.dialogVisible = true
                    this.modifyScheduleLocationDialog.schedule = this.schedule
                    return
                }
                if (null == this.location) {
                    this.$message.error("地点加载失败，请重新尝试")
                    return
                }
                if (!this.checkTime(this.schedule)) {
                    this.$confirm('现在不是课堂时间, 确定发起?', '发起签到')
                        .then(() => {
                            this.loading = true
                            this.$request.administrator.createSignIn(this.ssId, this.file)
                                .then(res => {
                                    if (!res.data.success) {
                                        this.$message.error(res.data.message)
                                        return
                                    }

                                    this.$message.success('发起成功')
                                    this.loading = false
                                    this.closeDialog()
                                })
                                .catch(err => {
                                    this.loading = false
                                    if (!err.response || !err.response.data)
                                        return
                                    if (!err.response.data.message) {
                                        this.$message.error(err.response.data)
                                        return
                                    }
                                    this.$message.error(err.response.data.message)
                                })
                                .finally(() => this.loading = false)
                        })
                        .catch(() => {
                        })
                    return
                }
                this.loading = true
                this.$request.administrator.createSignIn(this.ssId, this.file)
                    .then(res => {
                        if (!res.data.success) {
                            this.$message.error(res.data.message)
                            return
                        }

                        this.$message.success('发起成功')
                        this.loading = false
                        this.closeDialog()
                    })
                    .catch(err => {
                        this.loading = false
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
            handleSelect(ssId) {
                if ('' === ssId) {
                    return
                }
                const schedule = this.course.sisScheduleList.find(schedule => schedule.ssId === parseInt(ssId))
                if (null == schedule) {
                    return
                }
                this.schedule = schedule

                if (null == schedule.slId) {
                    return
                }
                this.$request.getLocation(schedule.slId)
                    .then(res => this.location = res.data)
                    .catch(() => {
                    })
            },
            getLocation() {
                if (null == this.schedule)
                    return ''
                if (null == this.schedule.slId)
                    return '该课程未指定地点'
                if (null == this.location)
                    return '未加载'
                return this.location.slName
            },
            closeModifyScheduleLocationDialog(slId) {
                if (null != slId) {
                    this.loading = true
                    this.$request.administrator.createSignIn(parseInt(slId), this.file)
                        .then(res => {
                            if (!res.data.success) {
                                this.$message.error(res.data.message)
                                return
                            }

                            this.$message.success('发起成功')
                            this.loading = false
                            this.closeDialog()
                        })
                        .catch(err => {
                            this.loading = false
                            if (!err.response || !err.response.data)
                                return
                            if (!err.response.data.message) {
                                this.$message.error(err.response.data)
                                return
                            }
                            this.$message.error(err.response.data.message)
                        })
                }
                this.modifyScheduleLocationDialog.dialogVisible = false
                this.modifyScheduleLocationDialog.schedule = null
                this.closeDialog()
            },
            getTitle() {
                if (null == this.course) return '发起签到'
                let tchStr = ''
                this.course.sisJoinCourseList
                    .map(joinCourse => joinCourse.sisUser)
                    .forEach(t => tchStr += t.suName + ' ')

                return `发起签到 ${this.course.scName} ${this.course.scId} ${tchStr}`
            },
            onChangeFile(file) {
                this.file = file.raw
            }
        }
    }
</script>

<style scoped>

</style>