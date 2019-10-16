<template>
    <el-container>
        <el-main>
            <el-table v-loading="loading"
                      :data="courseList"
                      @selection-change="handleSelectionChange">
                <!--                <el-table-column type="selection"
                                                 width="55">
                                </el-table-column>-->
                <el-table-column prop="scId" label="课程序号" width="110px"/>
                <el-table-column prop="scName" label="课程名字" width="130px"/>
                <el-table-column label="任课老师" width="120px">
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
                <el-table-column label="督导状态" width="80px">
                    <template slot-scope="scope">
                        <span v-text="getCourseMonitorStatus(scope.row)"></span>
                    </template>
                </el-table-column>
                <el-table-column label="教室容量" prop="scMaxSize" width="90px"></el-table-column>
                <el-table-column label="课程人数" prop="scActSize" width="90px"></el-table-column>
                <el-table-column label="到勤率" prop="scAttRate" width="70px">
                </el-table-column>
                <el-table-column label="操作" width="80px">
                    <template slot-scope="scope">
                        <el-button size="mini" type="primary" @click="receive(scope.row.scId)">
                            领取
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
        <el-footer>
            <el-pagination
                    class="coz-manage-pagination"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page.sync="pagination.currentPage"
                    :page-size="pagination.size"
                    :page-sizes="[10, 30, 50, 100, 200]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="pagination.total">
            </el-pagination>
        </el-footer>
    </el-container>
</template>

<script>
    import courseUtils from "@/util/courseUtils";

    export default {
        name: "MonitorPool",
        data() {
            return {
                loading: false,
                courseList: [],
                selectionList: [],
                pagination: {
                    currentPage: 1,
                    total: 0,
                    size: 10
                }
            }
        },
        created() {
            this.onSearch()
        },
        methods: {
            onSearch() {
                this.pagination.currentPage = 1
                this.handleCurrentChange(1)
            },
            getTeacherList(joinCourseList) {
                return joinCourseList
                    .filter(joinCourse => joinCourse.joinCourseType === 1)
                    .map(joinCourse => joinCourse.sisUser)
            },
            getScheduleTimeString(schedule) {
                return courseUtils.getScheduleTimeString(schedule)
            },
            getCourseMonitorStatus(course) {
                return courseUtils.getCourseMonitorStatus(course)
            },
            handleCurrentChange(page) {
                this.loading = true
                this.$request.monitor
                    .getMonitorPool(page, this.pagination.size)
                    .then(res => {
                        const pageIntro = res.data
                        this.pagination.currentPage = pageIntro.pageNum
                        this.pagination.total = pageIntro.total
                        this.courseList = pageIntro.list
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
            handleSizeChange(size) {
                this.pagination.size = size
                this.onSearch()
            },
            handleSelectionChange(selectionList) {
                this.selectionList = selectionList
            },
            receive(scId) {
                this.$confirm('确定领取该课程?', '领取督导')
                    .then(() => {
                        this.$request.monitor
                            .receiveCourse(scId)
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message.error(res.data.message)
                                    return
                                }

                                this.$message.success('操作成功')
                                this.handleCurrentChange(this.pagination.currentPage)
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
            }
        }
    }
</script>

<style scoped>

</style>