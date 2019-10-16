<template>
    <el-dialog
            :append-to-body="true"
            :visible="dialogVisible"
            :before-close="closeDialog">
        <img v-if="null != picSrc" :src="picSrc">
    </el-dialog>
</template>

<script>
    export default {
        name: "SignInPictureDialog",
        props: {
            dialogVisible: Boolean,
            picSrc: String
        },
        methods: {
            closeDialog() {
                this.$emit('closeDialog')
            }
        }
    }
</script>

<style scoped>

</style>