<template>
    <el-container>
        <el-header height="auto">
            <el-form class="coz-manage-form" :inline="true" size="mini" :model="selectForm" @submit.native.prevent>
                <el-form-item label="年级">
                    <el-select @keyup.enter.native="onSearch" placeholder="年级" v-model="selectForm.scGrade" :disabled="disabled">
                        <el-option v-for="val in lockGrade" :key="`cim_${val}`" :label="val" :value="val.toString()"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="学院">
                    <el-select @keyup.enter.native="onSearch" placeholder="学院" v-model="selectForm.sdId"
                               :filterable="true" :remote="true"
                               @focus="remoteMethod(selectForm.sdId)"
                               :remote-method="remoteMethod" :loading="selectForm.sdLoading">
                        <el-option label="不指定" value=""></el-option>
                        <el-option v-for="val in selectForm.departmentList"
                                   :key="val.sdId"
                                   :label="val.sdName"
                                   :value="val.sdId">
                        </el-option>
                    </el-select>
                </el-form-item>
                <br v-if="screenWidth < 1200">
                <el-form-item label="课程序号">
                    <el-input @keyup.enter.native="onSearch" v-model="selectForm.scId"></el-input>
                </el-form-item>
                <el-form-item label="课程名字">
                    <el-input @keyup.enter.native="onSearch" v-model="selectForm.scName"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="onSearch" :loading="loading || disabled" :disabled="loading || disabled">
                        搜索
                    </el-button>
                </el-form-item>
                <el-form-item>
                    <el-button @click="showAddCourseDialog" :loading="loading" :disabled="loading">
                        新增课程
                    </el-button>
                </el-form-item>
            </el-form>
        </el-header>
        <el-main>
            <el-table v-loading="loading || disabled"
                      :data="courseList"
                      @selection-change="handleSelectionChange">
                <el-table-column type="selection"
                                 width="55">
                </el-table-column>
                <el-table-column prop="scId" label="课程序号" width="110px"/>
                <el-table-column prop="scName" label="课程名字" width="130px"/>
                <el-table-column label="任课老师" width="90px">
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
                <el-table-column label="教室容量" prop="scMaxSize"></el-table-column>
                <el-table-column label="课程人数" prop="scActSize"></el-table-column>
                <el-table-column label="操作" width="300px">
                    <template slot-scope="scope">
                        <el-button type="primary" size="mini" @click="showModifyJoinCourseDialog(scope.row.scId)">
                            修改学生
                        </el-button>
                        <el-button type="warning" size="mini" @click="showModifyCourseDialog(scope.row)">
                            查看/修改信息
                        </el-button>
                        <el-button type="danger" size="mini" @click="delCourse(scope.row.scId)">
                            删除
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
        <ModifyCourseDialog :dialogVisible="modifyCourseDialog.dialogVisible"
                            :course="modifyCourseDialog.course"
                            @closeDialog="closeModifyCourseDialog"/>
        <AddCourseDialog :dialogVisible="addCourseDialog.dialogVisible"
                         @closeDialog="closeAddCourseDialog"/>
        <ModifyJoinCourseDialog :dialogVisible="modifyJoinCourseDialog.dialogVisible"
                                :scId="modifyJoinCourseDialog.scId"
                                @closeDialog="closeModifyJoinCourseDialog"/>
    </el-container>
</template>

<script>
    import courseUtils from '@/util/courseUtils'
    import ModifyCourseDialog from "../dialog/ModifyCourseDialog";
    import ModifyJoinCourseDialog from "../dialog/ModifyJoinCourseDialog";
    import AddCourseDialog from "../dialog/AddCourseDialog";

    export default {
        name: "CozInfoManage",
        components: {AddCourseDialog, ModifyJoinCourseDialog, ModifyCourseDialog},
        mounted() {
            const _this = this
            window.onresize = () => {
                return (() => {
                    _this.screenWidth = document.documentElement.clientWidth
                })()
            }
        },
        data() {
            return {
                disabled: true,
                lockGrade: [2014, 2015, 2016, 2017, 2018],
                screenWidth: document.documentElement.clientWidth,
                loading: false,
                selectForm: {
                    scGrade: '',
                    sdLoading: false,
                    sdId: '',
                    scId: '',
                    scName: '',
                    departmentList: []
                },
                pagination: {
                    currentPage: 1,
                    total: 0,
                    size: 10
                },
                hasMonitor: null,
                needMonitor: null,
                courseList: [],
                selectionList: [],
                modifyCourseDialog: {
                    dialogVisible: false,
                    course: null
                },
                modifyJoinCourseDialog: {
                    dialogVisible: false,
                    scId: null
                },
                addCourseDialog: {
                    dialogVisible: false
                }
            }
        },
        created() {
            this.disabled = true
            this.$request.administrator.getLockGrade()
                .then(res => {
                    const list = []
                    if (res.data) {
                        list.push(parseInt(res.data))
                        this.lockGrade = list
                        this.selectForm.scGrade = res.data.toString()
                    }
                    this.disabled = false
                    this.handleCurrentChange(1)
                })
                .catch(() => {
                    this.$message.error("获取权限错误, 请刷新或联系管理员");
                })
        },
        methods: {
            remoteMethod(val) {
                this.selectForm.sdLoading = true
                this.$request.administrator
                    .getDepartments(val)
                    .then(res => {
                        this.selectForm.departmentList = res.data
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
                        this.selectForm.sdLoading = false
                    })
            },
            onSearch() {
                this.handleCurrentChange(1)
            },
            handleCurrentChange(page) {
                this.loading = true
                this.$request.administrator
                    .getCourse(
                        page,
                        this.pagination.size,
                        this.hasMonitor,
                        this.needMonitor,
                        '' === this.selectForm.sdId ? null : this.selectForm.sdId,
                        '' === this.selectForm.scGrade ? null : this.selectForm.scGrade,
                        '' === this.selectForm.scId ? null : this.selectForm.scId,
                        '' === this.selectForm.scName ? null : this.selectForm.scName
                    )
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
            },
            handleSizeChange(size) {
                this.pagination.size = size
                this.onSearch()
            },
            handleSelectionChange(selectionList) {
                this.selectionList = selectionList
            },
            showAddCourseDialog() {
                this.addCourseDialog.dialogVisible = true
            },
            closeAddCourseDialog() {
                this.addCourseDialog.dialogVisible = false
            },
            showModifyCourseDialog(course) {
                this.modifyCourseDialog.course = course
                this.modifyCourseDialog.dialogVisible = true
            },
            closeModifyCourseDialog() {
                this.handleCurrentChange(this.pagination.currentPage)
                this.modifyCourseDialog.course = null
                this.modifyCourseDialog.dialogVisible = false
            },
            showModifyJoinCourseDialog(scId) {
                this.modifyJoinCourseDialog.scId = scId
                this.modifyJoinCourseDialog.dialogVisible = true
            },
            closeModifyJoinCourseDialog() {
                this.handleCurrentChange(this.pagination.currentPage)
                this.modifyJoinCourseDialog.scId = null
                this.modifyJoinCourseDialog.dialogVisible = false
            },
            delCourse(scId) {
                this.$confirm(`将会永久删除该课程, 且会影响已存在的督导和签到, 请慎重考虑`, '删除课程')
                    .then(() => {
                        this.$request.administrator.deleteCourse(scId)
                            .then((res => {
                                if (!res.dara.success) {
                                    this.$message.error(res.data.message)
                                    return
                                }
                                this.$message.success('操作成功')
                            }))
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
                                this.handleCurrentChange(this.pagination.currentPage)
                            })
                    })
                    .catch(() => {
                    })
            }
        }
    }
</script>

<style>
    .coz-manage-pagination {
        float: right;
    }

    .coz-manage-form .el-form-item__content {
        width: 100px;
    }

    .coz-manage-form-remember .el-form-item__content {
        width: 20px;
    }
</style>
