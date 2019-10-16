<template>
    <el-dialog title="发起转接"
               width="500px"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-container>
            <el-main v-if="course">
                <el-form :model="form" size="small" label-position="right" label-width="120px">
                    <el-form-item label="上课时间">
                        <el-select v-model="form.ssId" @change="handleSelect">
                            <el-option label="未选择" value=""></el-option>
                            <el-option v-for="val in course.sisScheduleList"
                                       :key="`td_${val.ssId}`"
                                       :label="`${getScheduleTimeString(val)}`"
                                       :value="val.ssId.toString()">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="转接周">
                        <el-select v-model="form.smtWeek">
                            <el-option label="未选择" value=""></el-option>
                            <el-option v-for="val in getWeekList(selectSchedule)"
                                       :key="`td_${val}`"
                                       :label="val"
                                       :value="val">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="学号">
                        <el-autocomplete v-model="form.suId"
                                         placeholder="请输入学号"
                                         :fetch-suggestions="queryBySuId"
                                         @select="handleSuIdSelect">
                        </el-autocomplete>
                    </el-form-item>
                    <el-form-item label="姓名(二者填一)">
                        <el-autocomplete v-model="form.suName"
                                         placeholder="请输入姓名"
                                         :fetch-suggestions="queryBySuName"
                                         @select="handleSuNameSelect">
                        </el-autocomplete>
                    </el-form-item>
                    <el-form-item>
                        <el-button @click="closeDialog">返回</el-button>
                        <el-button type="primary" @click="onSure">确定</el-button>
                    </el-form-item>
                </el-form>
            </el-main>
        </el-container>
    </el-dialog>
</template>

<script>
    import {mapState} from 'vuex'
    import courseUtils from "@/util/courseUtils";

    export default {
        name: "TransDialog",
        props: {
            dialogVisible: Boolean,
            course: Object
        },
        computed: {
            ...mapState({
                week: 'week'
            })
        },
        data() {
            return {
                form: {
                    ssId: '',
                    smtWeek: '',
                    suId: ''
                },
                selectSchedule: null
            }
        },
        methods: {
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            closeDialog() {
                this.$emit('closeDialog')
                this.form = {
                    ssId: '',
                    smtWeek: '',
                    suId: ''
                }
                this.selectSchedule = null
            },
            handleSelect(ssId) {
                this.selectSchedule = this.course.sisScheduleList.find(s => s.ssId === parseInt(ssId))
            },
            getWeekList(schedule) {
                if (null == schedule) return []
                const startWeek = schedule.ssStartWeek > this.week ? schedule.ssStartWeek : this.week
                return ((start, end) => Array(end - start + 1).fill(0).map((v, i) => i + start))(startWeek, schedule.ssEndWeek).map(n => n.toString())
            },
            queryBySuId(val, callback) {
                if ('' === val) {
                    callback([])
                    return
                }
                this.$request.administrator.getStudents(1, 10, val, null)
                    .then(res => {
                        const list = res.data.list
                        list.forEach(user => user.value = `${user.suId} ${user.suName}`)
                        callback(list)
                    })
                    .catch(() => {
                        callback([])
                    })
            },
            handleSuIdSelect(user) {
                this.form.suId = user.suId
                this.form.suName = user.suName
            },
            queryBySuName(val, callback) {
                if ('' === val) {
                    callback([])
                    return
                }
                this.$request.administrator.getStudents(1, 10, null, val)
                    .then(res => {
                        const list = res.data.list
                        list.forEach(user => user.value = `${user.suId} ${user.suName}`)
                        callback(list)
                    })
                    .catch(() => {
                        callback([])
                    })
            },
            handleSuNameSelect(user) {
                this.form.suId = user.suId
                this.form.suName = user.suName
            },
            onSure() {
                this.$request.monitor.applyTrans(this.form.ssId, this.form)
                    .then(res => {
                        if (!res.data.success) {
                            this.$message.error("提交失败")
                            return
                        }

                        this.$message.success("操作成功")
                        this.loading = false
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
            }
        }
    }
</script>

<style scoped>

</style>