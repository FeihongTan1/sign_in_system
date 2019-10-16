<template>
    <el-container>
        <el-header>
            <el-form :inline="true" size="mini" :model="selectForm">
                <el-form-item label="地点ID">
                    <el-input v-model="selectForm.slId"></el-input>
                </el-form-item>
                <el-form-item label="地点名">
                    <el-input v-model="selectForm.slName"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="onSearch" :loading="loading" :disabled="loading">
                        搜索
                    </el-button>
                </el-form-item>
            </el-form>
        </el-header>
        <el-main>
            <el-table v-loading="loading" :data="locationList">
                <el-table-column label="地点ID" prop="slId"></el-table-column>
                <el-table-column label="地点名" prop="slName"></el-table-column>
                <el-table-column label="经度" prop="slLong"></el-table-column>
                <el-table-column label="纬度" prop="slLat"></el-table-column>
                <el-table-column label="查看">
                    <template slot-scope="scope">
                        <el-button type="primary" size="mini" @click="showModifyLocationDialog(scope.row.slId)">
                            修改
                        </el-button>
                        <el-button type="danger" size="mini" @click="onDelete(scope.row)" :loading="loading"
                                   :disabled="loading">
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
                    @current-change="getPage"
                    :current-page.sync="pagination.currentPage"
                    :page-size="pagination.size"
                    :page-sizes="[10, 30, 50, 100, 200]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="pagination.total">
            </el-pagination>
        </el-footer>
        <ModifyLocationDialog :dialogVisible="modifyLocationDialog.dialogVisible" :slId="modifyLocationDialog.slId"
                              @closeDialog="closeModifyLocationDialog"/>
    </el-container>
</template>

<script>
    import ModifyLocationDialog from "../dialog/ModifyLocationDialog";

    export default {
        name: "LocationManage",
        components: {ModifyLocationDialog},
        data() {
            return {
                loading: false,
                selectForm: {
                    slId: '',
                    slName: ''
                },
                pagination: {
                    currentPage: 1,
                    total: 0,
                    size: 10
                },
                locationList: [],
                modifyLocationDialog: {
                    dialogVisible: false,
                    slId: ''
                }
            }
        },
        created() {
            this.onSearch()
        },
        methods: {
            onSearch() {
                this.pagination.currentPage = 1
                this.getPage()
            },
            showModifyLocationDialog(slId) {
                this.modifyLocationDialog.dialogVisible = true
                this.modifyLocationDialog.slId = slId.toString()
            },
            closeModifyLocationDialog() {
                this.modifyLocationDialog.dialogVisible = false
                this.modifyLocationDialog.slId = ''
                this.getPage()
            },
            handleSizeChange(size) {
                this.pagination.size = size
                this.onSearch()
            },
            getPage() {
                this.loading = true
                this.$request
                    .getLocations(this.pagination.currentPage, this.pagination.size, this.selectForm.slId, this.selectForm.slName)
                    .then(res => {
                        const pageIntro = res.data
                        this.pagination.currentPage = pageIntro.pageNum
                        this.pagination.total = pageIntro.total
                        this.locationList = pageIntro.list
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
            onDelete(location) {
                this.$confirm(`将会删除地点${location.slName} ${location.slId}`, '删除地点')
                    .then(() => {
                        this.$request.administrator.deleteLocation(location.slId)
                            .then(res => {
                                if (!res.data.success) {
                                    this.$message.error("提交失败")
                                    return
                                }

                                this.$message.success("操作成功")
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
                                this.getPage()
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
</style>
