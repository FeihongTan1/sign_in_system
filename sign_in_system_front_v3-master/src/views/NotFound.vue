<template>
    <el-container>
        <el-header>
            <el-menu class="nf-menu" mode="horizontal" background-color="#545c64" text-color="#fff"
                     active-text-color="#ffd04b">
                <li class="nf-menu-brand">&emsp;Happy&nbsp;Sign&nbsp;In&emsp;</li>
            </el-menu>
        </el-header>
        <el-container class="nf-second-container">
            <el-main class="nf-main">
                <el-card class="not-found-card">
                    <div slot="header" class="clearfix">
                        <span>NOT FOUND</span>
                        <el-button @click="goIndex" style="float: right; padding: 0" type="text"><i class="el-icon-d-arrow-left"></i>返回主页</el-button>
                    </div>
                    <el-row>
                        <el-col>
                            <p>很抱歉，你访问的页面不存在</p>
                            <p>
                                <br>/(ㄒoㄒ)/~~
                                <br>/(ㄒoㄒ)/~~
                                <br>/(ㄒoㄒ)/~~
                            </p>
                        </el-col>
                    </el-row>
                </el-card>
            </el-main>
        </el-container>
    </el-container>
</template>

<script>
    export default {
        name: 'notFound',
        methods: {
            goIndex() {
                this.$router.push("/index")
            }
        }
    }
</script>

<style>
    .nf-second-container {
        margin-top: 8px;
    }

    .nf-el-aside {
        margin-left: 20px;
        background-color: #4b4b4b;
        border-bottom-left-radius: 5px;
        box-shadow: 0 4px 15px #888888;
    }

    .nf-second-menu {
        height: calc(100vh - 24px - 60px);
    }

    .nf-main {
        display: flex;
        height: calc(100vh - 24px - 60px);
        margin-left: 20px;
        margin-right: 20px;
        padding: 10px 10%;
        background-color: transparent;
        border-bottom-right-radius: 5px;
        align-items: center;
        justify-content: center;
    }

    .nf-menu {
        border-top-left-radius: 5px;
        border-top-right-radius: 5px;
        box-shadow: 3px 1px 15px #888888;
    }

    .nf-menu-brand {
        float: left;
        height: 60px;
        width: 240px;
        border-top-left-radius: 5px;
        text-align: center;
        font-size: 1.5rem;
        line-height: 60px;
        padding: 0 20px;
        margin: 0;
        color: rgb(255, 255, 255);
        background-color: rgb(67, 74, 80);
        border-bottom-color: transparent;
    }

    .nf-menu-brand:focus, .nf-menu-datetime:focus, .nf-menu-greeting:focus {
        outline: unset;
    }

    .not-found-card {
        width: 20vw;
        height: 30vh;
        background-color: #ffffff;
        border-radius: 5px;
        box-shadow: 3px 4px 15px #888888;

    }

    @media screen and (max-width: 1440px) {
        .nf-main {
            color: #4b4b4b;
            height: calc(100vh - 24px - 60px);
            margin-right: 20px;
            padding: 10px 0;
            background-color: transparent;
            border-bottom-right-radius: 5px;
        }
    }
</style>