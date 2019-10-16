<template>
    <el-container>
        <el-header>
            <el-form :inline="true" size="mini" :model="selectForm">
                <el-form-item label="学号">
                    <el-input v-model="selectForm.suId"></el-input>
                </el-form-item>
                <el-form-item label="姓名">
                    <el-input v-model="selectForm.suName"></el-input>
                </el-form-item>
                <el-form-item label="按督导分排序">
                    <el-checkbox v-model="selectForm.ordByLackNum"></el-checkbox>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="onSearch" :loading="loading" :disabled="loading">
                        搜索
                    </el-button>
                </el-form-item>
                <el-form-item>
                    <el-button @click="showGrantMonitorDialog">
                        新增督导员
                    </el-button>
                </el-form-item>
                <el-form-item>
                    <el-button @click="batchRevokeMonitor">
                        撤回督导员（选中项）
                    </el-button>
                </el-form-item>
            </el-form>
        </el-header>
        <el-main>
            <el-table :data="monitorList"
                      v-loading="loading"
                      @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column prop="suId" label="学号"></el-table-column>
                <el-table-column prop="suName" label="姓名"></el-table-column>
                <el-table-column prop="suiLackNum" label="缺勤数"></el-table-column>
                <el-table-column label="操作">
                    <template slot-scope="scope">
                        <el-dropdown>
                            <el-button type="info" size="mini">
                                更多信息<i class="el-icon-arrow-down el-icon--right"></i>
                            </el-button>
                            <el-dropdown-menu slot="dropdown">
                                <el-dropdown-item @click.native="showMonitorInfoDialog(scope.row.suId)">督导列表</el-dropdown-item>
                            </el-dropdown-menu>
                        </el-dropdown>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
        <el-footer>
            <el-pagination
                    class="monitor-manage-pagination"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page.sync="pagination.currentPage"
                    :page-size="pagination.size"
                    :page-sizes="[10, 30, 50, 100, 200]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="pagination.total">
            </el-pagination>
        </el-footer>
        <GrantMonitorDialog :dialogVisible="grantMonitorDialog.dialogVisible"
                            @closeDialog="closeGrantMonitorDialog"/>
        <MonitorInfoDialog :dialogVisible="monitorInfoDialog.dialogVisible" :suId="monitorInfoDialog.suId"
                           @closeDialog="closeMonitorInfoDialog"/>
    </el-container>
</template>

<script>
    import GrantMonitorDialog from '@/components/dialog/GrantMonitorDialog'
    import MonitorInfoDialog from "../dialog/MonitorInfoDialog";

    export default {
        name: "MonitorManage",
        components: {
            MonitorInfoDialog,
            GrantMonitorDialog
        },
        data() {
            return {
                loading: false,
                selectForm: {
                    suId: '',
                    suName: '',
                    ordByLackNum: false
                },
                selectionList: [],
                monitorList: [],
                pagination: {
                    currentPage: 1,
                    total: 0,
                    size: 10
                },
                grantMonitorDialog: {
                    dialogVisible: false
                },
                monitorInfoDialog: {
                    dialogVisible: false,
                    suId: ''
                }
            }
        },
        created() {
            const rememberForm = JSON.parse(localStorage.getItem('monitorManagerForm'))
            if (rememberForm)
                this.selectForm = rememberForm
            this.handleCurrentChange(1)
        },
        methods: {
            handleSelectionChange(selectionList) {
                this.selectionList = selectionList
            },
            handleSizeChange(size) {
                this.pagination.size = size
                this.onSearch()
            },
            handleCurrentChange(page) {
                this.loading = true
                this.$request.administrator
                    .getMonitors(
                        page,
                        this.pagination.size,
                        this.selectForm.suId,
                        this.selectForm.suName,
                        this.selectForm.ordByLackNum
                    )
                    .then(res => {
                        const pageIntro = res.data
                        this.pagination.currentPage = pageIntro.pageNum
                        this.pagination.total = pageIntro.total
                        this.monitorList = pageIntro.list
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
            onSearch() {
                if (this.selectForm.remember)
                    localStorage.setItem('monitorManagerForm', JSON.stringify(this.selectForm))
                this.handleCurrentChange(1)
            },
            showGrantMonitorDialog() {
                this.grantMonitorDialog.dialogVisible = true
            },
            closeGrantMonitorDialog() {
                this.handleCurrentChange(this.pagination.currentPage)
                this.grantMonitorDialog.dialogVisible = false
            },
            showMonitorInfoDialog(suId) {
                this.monitorInfoDialog = {
                    dialogVisible: true,
                    suId
                }
            },
            closeMonitorInfoDialog() {
                this.monitorInfoDialog = {
                    dialogVisible: false,
                    suId: ''
                }
            },
            batchRevokeMonitor() {
                const suIdList = this.selectionList.map(user => user.suId)
                if (suIdList.length <= 0) {
                    this.$message.warning('没有选中任何项')
                    return
                }
                this.$confirm(`将会对选中结果撤回督导员权限，总${suIdList.length}条，请认真检查`, '撤回督导权')
                    .then(() => {
                        Promise
                            .all(
                                suIdList.map(suId => {
                                    return this.$request.administrator.revokeMonitor(suId)
                                }))
                            .then((ress) => {
                                const errList = []
                                ress.forEach(res => {
                                    if (!res.data.success)
                                        errList.push(res)
                                })

                                if (errList.length > 0) {
                                    console.log(errList)
                                    this.$message.error("部分操作失败")
                                    return
                                }
                                this.$message.success('操作成功')
                            })
                            .catch((errs) => {
                                console.error(errs)
                                this.$message.error("操作失败")
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
    .monitor-manage-pagination {
        float: right;
    }

    .monitor-manage-form .el-form-item__content {
        width: 100px;
    }

    .monitor-manage-form-remember .el-form-item__content {
        width: 20px;
    }
</style>