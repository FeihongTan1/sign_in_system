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
                <el-form-item label="按缺勤排序">
                    <el-checkbox v-model="selectForm.orderByCozLackNum"></el-checkbox>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="onSearch" :loading="loading" :disabled="loading">
                        搜索
                    </el-button>
                </el-form-item>
            </el-form>
        </el-header>
        <el-main>
            <el-table v-loading="loading" :data="studentList">
                <el-table-column label="学号" prop="suId"></el-table-column>
                <el-table-column label="姓名" prop="suName"></el-table-column>
                <el-table-column label="缺勤数" prop="suiCozLackNum"></el-table-column>
                <el-table-column label="查看">
                    <template slot-scope="scope">
                        <el-button type="info" size="mini" @click="showCozLackDetail(scope.row.suId)">
                            查看详情
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
        <el-footer>
            <el-pagination
                    class="coz-manage-pagination"
                    @size-change="handleSizeChange"
                    @current-change="getPage"
                    :current-page.sync="pagination.currentPage"
                    :page-size="pagination.size"
                    :page-sizes="[10, 30, 50, 100, 200]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="pagination.total">
            </el-pagination>
        </el-footer>
        <CozLackDetailDialog :dialogVisible="cozLackDetailDialog.dialogVisible"
                             :suId="cozLackDetailDialog.suId"
                             @closeDialog="closeCozLackDetail"/>
    </el-container>
</template>

<script>
    import CozLackDetailDialog from "../dialog/CozLackDetailDialog";

    export default {
        name: "StudentManage",
        components: {CozLackDetailDialog},
        data() {
            return {
                loading: false,
                selectForm: {
                    suId: '',
                    suName: '',
                    orderByCozLackNum: null
                },
                pagination: {
                    currentPage: 1,
                    total: 0,
                    size: 10
                },
                studentList: [],
                cozLackDetailDialog: {
                    dialogVisible: false,
                    suId: ''
                }
            }
        },
        created() {
            this.onSearch()
        },
        methods: {
            showCozLackDetail(suId) {
                this.cozLackDetailDialog.dialogVisible = true
                this.cozLackDetailDialog.suId = suId
            },
            closeCozLackDetail() {
                this.cozLackDetailDialog.dialogVisible = false
                this.cozLackDetailDialog.suId = ''
            },
            handleSizeChange(size) {
                this.pagination.size = size
                this.onSearch()
            },
            onSearch() {
                this.pagination.currentPage = 1
                this.getPage()
            },
            getPage() {
                this.loading = true
                this.$request.administrator
                    .getStudents(this.pagination.currentPage, this.pagination.size, this.selectForm.suId, this.selectForm.suName, this.selectForm.orderByCozLackNum)
                    .then(res => {
                        const pageIntro = res.data
                        this.pagination.currentPage = pageIntro.pageNum
                        this.pagination.total = pageIntro.total
                        this.studentList = pageIntro.list
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
            }
        }
    }
</script>

<style>
    .coz-manage-pagination {
        float: right;
    }
</style>
