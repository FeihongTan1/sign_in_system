<template>
    <el-container>
        <el-header>
            <el-form :inline="true" size="mini" @submit.native.prevent>
                <el-form-item label="学院名">
                    <el-input v-model="sdName"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="onSearch">
                        搜索
                    </el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="info" @click="showAddDepartDialog">
                        增加学院信息
                    </el-button>
                </el-form-item>
            </el-form>
        </el-header>
        <el-main>
            <el-table :data="tableList" v-loading="loading">
                <el-table-column label="学院编号" prop="sdId"></el-table-column>
                <el-table-column label="学院名字" prop="sdName"></el-table-column>
                <el-table-column label="操作">
                    <template slot-scope="scope">
                        <el-button type="info" size="mini" @click="showModifyDepartDialog(scope.row)">
                            修改信息
                        </el-button>
                        <el-button type="danger" size="mini" @click="delDepart(scope.row)">
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
        <ModifyDepartDialog :dialogVisible="modifyDepartDialog.dialogVisible"
                            :department="modifyDepartDialog.department"
                            @closeDialog="closeModifyDepartDialog"/>
        <AddDepartDialog :dialogVisible="addDepartDialog.dialogVisible"
                         @closeDialog="closeAddDepartDialog"/>
    </el-container>
</template>

<script>
    import ModifyDepartDialog from "../dialog/ModifyDepartDialog";
    import AddDepartDialog from "../dialog/AddDepartDialog";

    export default {
        name: "DepartManage",
        components: {AddDepartDialog, ModifyDepartDialog},
        data() {
            return {
                loading: false,
                sdName: '',
                departList: [],
                tableList: [],
                pagination: {
                    currentPage: 1,
                    total: 0,
                    size: 10
                },
                modifyDepartDialog: {
                    dialogVisible: false,
                    department: null
                },
                addDepartDialog: {
                    dialogVisible: false
                }
            }
        },
        created() {
            this.getTotalDepart()
        },
        methods: {
            getTotalDepart() {
                this.loading = true
                this.$request.administrator.getDepartments('')
                    .then(res => {
                        this.departList = res.data
                        this.pagination.total = this.departList.length
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
                    .finally(() => this.loading = false)
            },
            handleSizeChange(size) {
                this.pagination.size = size
                this.handleCurrentChange(this.pagination.currentPage)
            },
            handleCurrentChange(page) {
                this.loading = true
                const start = (page - 1) * this.pagination.size
                const end = start + this.pagination.size
                let reg;
                if ('' === this.sdName.trim()) {
                    reg = new RegExp(/(\s\S)*/)
                } else {
                    let r = '[\\s\\S]*'
                    this.sdName.replace(/\s/, '').split('').forEach(s => {
                        r += `${s}+[\\s\\S]*`
                    })
                    reg = r
                }
                const resList = this.departList.filter(d => null != d.sdName.match(reg) && d.sdName.match(reg).length > 0)
                this.pagination.total = resList.length
                this.tableList = resList.slice(start, end)
                this.loading = false
            },
            onSearch() {
                this.pagination.currentPage = 1
                this.handleCurrentChange(1)
            },
            showAddDepartDialog() {
                this.addDepartDialog.dialogVisible = true
            },
            closeAddDepartDialog() {
                this.addDepartDialog.dialogVisible = false
                this.getTotalDepart()
            },
            showModifyDepartDialog(depart) {
                this.modifyDepartDialog.department = depart
                this.modifyDepartDialog.dialogVisible = true
            },
            closeModifyDepartDialog() {
                this.modifyDepartDialog.dialogVisible = false
                this.modifyDepartDialog.department = null
                this.getTotalDepart()
            },
            delDepart(depart) {
                this.$confirm(`将会删除学院 ${depart.sdName}, 请确认`, '删除学院')
                    .then(() => {
                        this.loading = true
                        this.$request.administrator.deleteDepartment(depart.sdId)
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message.error('出现未知错误')
                                    return
                                }

                                this.$message.success('删除成功')
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
                                this.getTotalDepart()
                            })
                    })
                    .catch(() => {
                    })
            }
        }
    }
</script>

<style scoped>
    .coz-manage-pagination {
        float: right;
    }
</style>