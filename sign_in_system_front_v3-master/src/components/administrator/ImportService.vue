<template>
    <el-container>
        <el-main>
            <el-progress v-show="cozProgressVisible" :percentage="cozProgress" :status="cozStatus"></el-progress>
            <el-progress v-show="stuProgressVisible" :percentage="stuProgress" :status="stuStatus"></el-progress>
            <el-form label-position="right">
                <el-form-item label="上传课程信息（教学任务列表）">
                    <el-button size="small" type="success" @click="submitCozUpload">
                        上传到服务器
                    </el-button>
                </el-form-item>
                <el-form-item>
                    <el-upload
                            action=""
                            ref="cozUpload"
                            :drag="true"
                            :file-list="cozFileList"
                            :http-request="uploadCoz"
                            :on-change="handleCozChange"
                            :multiple="false"
                            :auto-upload="false">
                        <i class="el-icon-upload"></i>
                        <div class="el-upload__text">将课程文件拖到此处，或<em>点击上传</em></div>
                        <div slot="tip" class="el-upload__tip">只能上传xls/xlsx文件, 10MB以内</div>
                    </el-upload>
                </el-form-item>
            </el-form>
            <hr>
            <el-form label-position="right">
                <el-form-item label="上传学生信息（上课名单列表）">
                    <el-button size="small" type="success" @click="submitStuUpload">
                        上传到服务器
                    </el-button>
                </el-form-item>
                <el-form-item>
                    <el-upload
                            action=""
                            ref="stuUpload"
                            :drag="true"
                            :file-list="stuFileList"
                            :http-request="uploadStu"
                            :on-change="handleStuChange"
                            :multiple="false"
                            :auto-upload="false">
                        <i class="el-icon-upload"></i>
                        <div class="el-upload__text">将课程文件拖到此处，或<em>点击上传</em></div>
                        <div slot="tip" class="el-upload__tip">只能上传xls/xlsx文件, 10MB以内</div>
                    </el-upload>
                </el-form-item>
            </el-form>
        </el-main>
    </el-container>
</template>

<script>
    export default {
        name: "ImportService",
        data() {
            return {
                cozFileList: [],
                cozKey: null,
                cozProgress: 0,
                cozProgressVisible: false,
                cozUploadTimer: null,
                cozStatus: '',

                stuFileList: [],
                stuKey: null,
                stuProgress: 0,
                stuProgressVisible: false,
                stuUploadTimer: null,
                stuStatus: ''
            }
        },
        methods: {
            submitCozUpload() {
                this.$refs.cozUpload.submit()
            },
            submitStuUpload() {
                this.$refs.stuUpload.submit()
            },
            uploadCoz(data) {
                const file = data.file
                const formData = new FormData();
                formData.append('cozInfo', file)
                this.cozProgressVisible = true
                this.$request.administrator.importCozInfo(formData)
                    .then(res => {
                        if (!res.data.success) {
                            this.$message.error(res.data.message)
                            return
                        }

                        this.$message.success('上传成功，正在导入')
                        this.cozKey = res.data.data
                        const _this = this
                        this.cozUploadTimer = setInterval(() => {
                            _this.$request.administrator.getProgress(this.cozKey)
                                .then(res => {
                                    if (!res.data.success) {
                                        _this.$message.error(res.data.message)
                                        _this.cozStatus = 'exception'
                                        clearInterval(_this.cozUploadTimer)
                                        return
                                    }

                                    if (res.data.data === 100) {
                                        clearInterval(_this.cozUploadTimer)
                                        _this.cozStatus = 'success'
                                        _this.$message.success('导入成功')
                                    }
                                    _this.cozProgress = res.data.data
                                })
                                .catch(err => {
                                    _this.cozStatus = 'exception'
                                    if (!err.response || !err.response.data)
                                        return
                                    if (!err.response.data.message) {
                                        _this.$message.error(err.response.data)
                                        return
                                    }
                                    _this.$message.error(err.response.data.message)
                                    _this.clearInterval(this.cozUploadTimer)
                                })
                        }, 500)
                    })
                    .catch(err => {
                        this.cozStatus = 'exception'
                        if (!err.response || !err.response.data)
                            return
                        if (!err.response.data.message) {
                            this.$message.error(err.response.data)
                            return
                        }
                        this.$message.error(err.response.data.message)
                    })
            },
            uploadStu(data) {
                const file = data.file
                const formData = new FormData();
                formData.append('stuInfo', file)
                this.stuProgressVisible = true
                this.$request.administrator.importStuInfo(formData)
                    .then(res => {
                        if (!res.data.success) {
                            this.$message.error(res.data.message)
                            return
                        }

                        this.$message.success('上传成功，正在导入')
                        this.stuKey = res.data.data
                        const _this = this
                        this.stuUploadTimer = setInterval(() => {
                            _this.$request.administrator.getProgress(this.stuKey)
                                .then(res => {
                                    if (!res.data.success) {
                                        _this.$message.error(res.data.message)
                                        _this.stuStatus = 'exception'
                                        clearInterval(_this.stuUploadTimer)
                                        return
                                    }

                                    if (res.data.data === 100) {
                                        clearInterval(_this.stuUploadTimer)
                                        _this.stuStatus = 'success'
                                        _this.$message.success('导入成功')
                                    }
                                    _this.stuProgress = res.data.data
                                })
                                .catch(err => {
                                    _this.stuStatus = 'exception'
                                    if (!err.response || !err.response.data)
                                        return
                                    if (!err.response.data.message) {
                                        _this.$message.error(err.response.data)
                                        return
                                    }
                                    _this.$message.error(err.response.data.message)
                                    _this.clearInterval(this.stuUploadTimer)
                                })
                        }, 500)
                    })
                    .catch(err => {
                        this.stuStatus = 'exception'
                        if (!err.response || !err.response.data)
                            return
                        if (!err.response.data.message) {
                            this.$message.error(err.response.data)
                            return
                        }
                        this.$message.error(err.response.data.message)
                    })
            },
            handleCozChange(file) {
                this.cozFileList = []
                this.cozFileList.push(file)
            },
            handleStuChange(file) {
                this.stuFileList = []
                this.stuFileList.push(file)
            }
        }
    }
</script>

<style scoped>

</style>