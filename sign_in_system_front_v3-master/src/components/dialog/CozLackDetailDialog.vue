<template>
    <el-dialog title="历史签到"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-table :data="signInDetailList" v-loading="loading">
            <el-table-column label="课程">
                <template slot-scope="scope">
                    <span v-text="`${scope.row.sisSignIn.sisSchedule.sisCourse.scName} ${scope.row.sisSignIn.sisSchedule.sisCourse.scId}`"></span>
                </template>
            </el-table-column>
            <el-table-column label="上课时间">
                <template slot-scope="scope">
                    <span v-text="getScheduleTimeString(scope.row.sisSignIn.sisSchedule)"></span>
                </template>
            </el-table-column>
            <el-table-column label="签到周" prop="sisSignIn.ssiWeek"></el-table-column>
            <el-table-column label="签到状态">
                <template slot-scope="scope">
                    <span v-text="ssidStatus ? '到勤' : '缺勤'"></span>
                </template>
            </el-table-column>
        </el-table>
    </el-dialog>
</template>

<script>
    import courseUtils from "@/util/courseUtils";

    export default {
        name: "CozLackDetailDialog",
        props: {
            dialogVisible: Boolean,
            suId: String
        },
        data() {
            return {
                loading: false,
                signInDetailList: []
            }
        },
        watch: {
            suId() {
                this.getDetail()
            }
        },
        created() {
            this.getDetail()
        },
        methods: {
            getDetail() {
                if ('' === this.suId || null == this.suId) {
                    this.signInDetailList = []
                    return
                }
                this.loading = true
                this.$request.administrator.getStuSignIns(this.suId)
                    .then(res => {
                        this.signInDetailList = res.data
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
            closeDialog() {
                this.signInDetailList = []
                this.$emit('closeDialog')
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            }

        }
    }
</script>

<style scoped>

</style>