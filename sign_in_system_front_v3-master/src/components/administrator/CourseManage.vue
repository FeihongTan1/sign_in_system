<template>
    <el-container>
        <el-header height="auto">
            <el-form class="coz-manage-form" :inline="true" size="mini" :model="selectForm" label-width="100px">
                <el-row type="flex" justify="start" :gutter="0">
                    <el-col :span="5">
                        <el-form-item label="年级">
                            <el-select @keyup.enter.native="onSearch" placeholder="年级" v-model="selectForm.scGrade"
                                       :disabled="disabled">
                                <el-option v-for="val in lockGrade" :key="`cm_${val}`" :label="val"
                                           :value="val.toString()"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="5">
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
                    </el-col>
                    <el-col :span="5">
                        <el-form-item label="课程序号">
                            <el-input @keyup.enter.native="onSearch" v-model="selectForm.scId"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="5">
                        <el-form-item label="课程名字">
                            <el-input @keyup.enter.native="onSearch" v-model="selectForm.scName"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row type="flex">
                    <el-col :span="5">
                        <el-form-item label="督导人学号">
                            <el-input @keyup.enter.native="onSearch" :disabled="true" placeholder="暂未开放"
                                      v-model="selectForm.sisUser.suId"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="5">
                        <el-form-item label="督导人名字">
                            <el-input @keyup.enter.native="onSearch" :disabled="true" placeholder="暂未开放"
                                      v-model="selectForm.sisUser.suName"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-form-item class="coz-manage-form-remember" label="记住">
                        <el-checkbox @keyup.enter.native="onSearch" v-model="selectForm.remember"></el-checkbox>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="onSearch" :loading="loading || disabled" :disabled="loading || disabled">
                            搜索
                        </el-button>
                    </el-form-item>
                    <el-form-item label="丨">
                        <el-dropdown>
                            <el-button type="info">
                                批量督导<i class="el-icon-arrow-down el-icon--right"></i>
                            </el-button>
                            <el-dropdown-menu slot="dropdown">
                                <el-dropdown-item @click.native="batchSelectionSupervisions(true)">
                                    发起督导（选中项）
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="batchSelectionSupervisions(false)">
                                    取消督导（选中项）
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="batchSupervisions(true)" :divided="true">
                                    发起督导（搜索条件）
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="batchSupervisions(false)">
                                    取消督导（搜索条件）
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </el-dropdown>
                    </el-form-item>
                </el-row>
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
                <el-table-column prop="scName" label="课程名字" width="110px"/>
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
                <el-table-column label="督导状态" width="90px">
                    <template slot-scope="scope">
                        <span v-text="getCourseMonitorStatus(scope.row)"></span>
                    </template>
                </el-table-column>
                <el-table-column label="教室容量" prop="scMaxSize" width="80px"></el-table-column>
                <el-table-column label="课程人数" prop="scActSize" width="80px"></el-table-column>
                <el-table-column label="操作" width="120px">
                    <template slot-scope="scope">
                        <el-dropdown>
                            <el-button type="primary" size="mini">
                                课程管理<i class="el-icon-arrow-down el-icon--right"></i>
                            </el-button>
                            <el-dropdown-menu slot="dropdown">
                                <!--todo 停课周 学生统计-->
                                <el-dropdown-item @click.native="showCourse(scope.row)">
                                    课程信息
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="showStudents(scope.row.scId)">
                                    学生信息
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="showHistorySignIn(scope.row)">
                                    历史签到
                                </el-dropdown-item>
                                <el-dropdown-item
                                        @click.native="modifyScNeedMonitor(scope.row.scId, !scope.row.scNeedMonitor)"
                                        :divided="true"
                                        v-text="scope.row.scNeedMonitor?'取消督导':'发起督导'"></el-dropdown-item>
                                <el-dropdown-item @click.native="showCreateSignIn(scope.row)">
                                    发起签到
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </el-dropdown>
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
        <CourseDialog :dialogVisible="courseDialog.dialogVisible"
                      :course="courseDialog.course"
                      @closeDialog="closeCourse"/>
        <StudentDialog :dialogVisible="studentsDialog.dialogVisible"
                       :scId="studentsDialog.scId"
                       @closeDialog="closeStudents"/>
        <HistorySignInDialog :dialogVisible="historySignInDialog.dialogVisible"
                             :course="historySignInDialog.course"
                             @closeDialog="closeHistorySignIn"/>
        <CreateSignInDialog :dialogVisible="createSignInDialog.dialogVisible"
                            :course="createSignInDialog.course"
                            @closeDialog="closeCreateSignInDialog"/>
    </el-container>
</template>

<script>
    import courseUtils from '@/util/courseUtils'
    import StudentDialog from "@/components/dialog/StudentDialog";
    import CourseDialog from "@/components/dialog/CourseDialog";
    import SupervisionsDialog from "@/components/dialog/SupervisionsDialog";
    import CreateSignInDialog from "@/components/dialog/CreateSignInDialog";
    import HistorySignInDialog from "../dialog/HistorySignInDialog";

    export default {
        name: "CourseManage",
        components: {HistorySignInDialog, CreateSignInDialog, SupervisionsDialog, StudentDialog, CourseDialog},
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
                    remember: false,
                    scGrade: '',
                    sdLoading: false,
                    sdId: '',
                    scId: '',
                    scName: '',
                    sisUser: {
                        suId: '',
                        suName: ''
                    },
                    needMonitor: false,
                    hasMonitor: false,
                    departmentList: []
                },
                pagination: {
                    currentPage: 1,
                    total: 0,
                    size: 10
                },
                courseList: [],
                courseDialog: {
                    dialogVisible: false,
                    course: null
                },
                studentsDialog: {
                    dialogVisible: false,
                    scId: ''
                },
                supervisionsDialog: {
                    dialogVisible: false,
                    scId: ''
                },
                createSignInDialog: {
                    dialogVisible: false,
                    course: null
                },
                historySignInDialog: {
                    dialogVisible: false,
                    course: null
                },
                selectionList: []
            }
        },
        created() {
            const rememberForm = JSON.parse(localStorage.getItem('cozManagerForm'))
            if (rememberForm)
                this.selectForm = rememberForm
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
                if (this.selectForm.remember)
                    localStorage.setItem('cozManagerForm', JSON.stringify(this.selectForm))
                this.selectForm.needMonitor = false
                this.selectForm.hasMonitor = null
                this.handleCurrentChange(1)
            },
            handleCurrentChange(page) {
                this.loading = true
                this.$request.administrator
                    .getCourse(
                        page,
                        this.pagination.size,
                        this.selectForm.hasMonitor,
                        this.selectForm.needMonitor,
                        '' === this.selectForm.sdId ? null : this.selectForm.sdId,
                        '' === this.selectForm.scGrade ? null : this.selectForm.scGrade,
                        '' === this.selectForm.scId ? null : this.selectForm.scId,
                        '' === this.selectForm.scName ? null : this.selectForm.scName
                    )
                    .then(res => {
                        const pageIntro = res.data
                        if (pageIntro.total === 0) this.$message.warning('暂无数据')
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
                return joinCourseList.map(joinCourse => joinCourse.sisUser)
            },
            showCourse(course) {
                this.courseDialog.course = course
                this.courseDialog.dialogVisible = true
            },
            closeCourse() {
                this.courseDialog.course = null
                this.courseDialog.dialogVisible = false
            },
            showStudents(scId) {
                this.studentsDialog.scId = scId;
                this.studentsDialog.dialogVisible = true
            },
            closeStudents() {
                this.studentsDialog.scId = null;
                this.studentsDialog.dialogVisible = false
            },
            showSupervision(scId) {
                this.supervisionsDialog.scId = scId
                this.supervisionsDialog.dialogVisible = true
            },
            closeSupervision() {
                this.supervisionsDialog.scId = ''
                this.supervisionsDialog.dialogVisible = false
            },
            showHistorySignIn(course) {
                this.historySignInDialog.course = course
                this.historySignInDialog.dialogVisible = true
            },
            closeHistorySignIn() {
                this.historySignInDialog.course = null
                this.historySignInDialog.dialogVisible = false
            },
            showCreateSignIn(course) {
                this.createSignInDialog.course = course
                this.createSignInDialog.dialogVisible = true
            },
            closeCreateSignInDialog() {
                this.createSignInDialog.course = null
                this.createSignInDialog.dialogVisible = false
            },
            modifyScNeedMonitor(scId, status) {
                this.$confirm(`将对课程 ${scId} ${status ? '发起督导' : '取消督导'}，请确认`, status ? '发起督导' : '取消督导',)
                    .then(() => {
                        this.$request.administrator.modifyScNeedMonitor(scId, status)
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
            },
            batchSupervisions(status) {
                this
                    .$confirm(`将会对搜索结果${status ? '发起督导' : '取消督导'}，总${this.pagination.total}条，请认真检查`, '发起督导')
                    .then(() => {
                        this.$request.administrator
                            .batchSupervisions(
                                status,
                                this.hasMonitor,
                                this.needMonitor,
                                '' === this.selectForm.sdId ? null : this.selectForm.sdId,
                                '' === this.selectForm.scGrade ? null : this.selectForm.scGrade,
                                '' === this.selectForm.scId ? null : this.selectForm.scId,
                                '' === this.selectForm.scName ? null : this.selectForm.scName
                            )
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message(res.data.message)
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
            },
            handleSelectionChange(selectionList) {
                this.selectionList = selectionList
            },
            batchSelectionSupervisions(status) {
                const scIdList = this.selectionList.map(course => course.scId)
                if (scIdList.length <= 0) {
                    this.$message.warning('没有选中任何项')
                    return
                }
                this.$confirm(`将会对选中结果${status ? '发起督导' : '取消督导'}，总${scIdList.length}条，请认真检查`, `${status ? '发起督导' : '取消督导'}`)
                    .then(() => {
                        this.$request.administrator.batchSelectionSupervisions(status, scIdList)
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message(res.data.message)
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
            },
            handleSizeChange(size) {
                this.pagination.size = size
                this.onSearch()
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