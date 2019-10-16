<template>
    <el-dialog title="修改地点"
               width="500px"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-form :model="locationForm" v-loading="loading">
            <el-form-item label="地点名">
                <el-input v-model="locationForm.slName"></el-input>
            </el-form-item>
            <el-form-item label="经度">
                <el-input v-model="locationForm.slLong"></el-input>
            </el-form-item>
            <el-form-item label="纬度">
                <el-input v-model="locationForm.slLat"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="doModify">
                    确定
                </el-button>
                <el-button @click="showMapDialog">
                    选择地图
                </el-button>
            </el-form-item>
        </el-form>
        <MapDialog :dialogVisible="mapDialog.dialogVisible" @closeDialog="closeMapDialog"/>
    </el-dialog>
</template>

<script>
    import MapDialog from "./MapDialog";

    export default {
        name: "ModifyLocationDialog",
        components: {MapDialog},
        props: {
            dialogVisible: Boolean,
            slId: String
        },
        watch: {
            slId() {
                if (null == this.slId || '' === this.slId)
                    return
                this.loading = true
                this.$request.getLocation(this.slId)
                    .then(res => {
                        this.locationForm.slName = res.data.slName
                        this.locationForm.slLong = res.data.slLong
                        this.locationForm.slLat = res.data.slLat
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
        },
        data() {
            return {
                loading: false,
                mapDialog: {
                    dialogVisible: false
                },
                locationForm: {
                    slName: '',
                    slLong: '',
                    slLat: ''
                }
            }
        },
        methods: {
            showMapDialog() {
                this.mapDialog.dialogVisible = true
            },
            closeMapDialog(latLng) {
                if (latLng) {
                    this.locationForm.slLat = latLng.lat
                    this.locationForm.slLong = latLng.long
                }
                this.mapDialog.dialogVisible = false
            },
            closeDialog() {
                this.loading = false
                this.locationForm.slName = ''
                this.locationForm.slLong = ''
                this.locationForm.slLat = ''
                this.$emit('closeDialog')
            },
            doModify() {
                this.loading = true
                this.$request.administrator.modifyLocation(this.slId, this.locationForm)
                    .then(res => {
                        if (!res.data.success) {
                            this.$message.error("提交失败")
                            return
                        }

                        this.$message.success("操作成功")
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
                    .finally(() => {
                        this.loading = false
                    })
            }
        }
    }
</script>

<style scoped>

</style>
