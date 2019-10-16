<template>
    <div id="app">
        <transition name="el-fade-in" mode="out-in">
            <router-view>
            </router-view>
        </transition>
    </div>
</template>

<script>
    export default {
        name: 'app',
        created() {
            const _this = this
            this.$request.axios.interceptors.response.use(function (response) {
                if (!response)
                    return Promise.reject()
                return response
            }, function (error) {
                if (null == error.response) {
                    _this.$message.error('服务器无反应, 请稍后再试')
                    return Promise.reject(error)
                }

                if (401 === error.response.status) {
                    _this.$message.error('登录信息过期, 请重新登录')
                    _this.$router.push("/")
                    error.response = null
                    return Promise.reject(error)
                }
                return Promise.reject(error)
            })
        }
    }
</script>

<style>
    html {
        min-width: 1024px;
        background-color: #fcfcfc;
        font-family: Roboto, -apple-system, BlinkMacSystemFont,
        "Segoe UI", Helvetica, Arial, sans-serif,
        "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
    }
</style>
