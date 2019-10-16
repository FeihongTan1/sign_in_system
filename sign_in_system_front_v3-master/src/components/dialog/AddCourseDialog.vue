<template>
    <el-dialog title="增加课程"
               width="800px"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-form ref="cozInfoForm" size="small" :model="courseForm" label-position="left" label-width="90px"
                 :rules="formRules">
            <el-form-item v-for="(depart,idx) in courseForm.departmentList"
                          :key="`acd_depart_${idx}`"
                          :label="`开课学院${idx + 1}`">
                <el-form size="mini">
                    <el-row type="flex" :gutter="15">
                        <el-col>
                            <el-form-item label="开课学院名" label-width="100px">
                                <el-autocomplete v-model="depart.sdName"
                                                 placeholder="通过输入学院名查询"
                                                 :fetch-suggestions="queryBySdName"
                                                 @select="handleSdNameSelect(idx,$event)">
                                </el-autocomplete>
                            </el-form-item>
                        </el-col>
                        <el-col>
                            <el-form-item label="开课学院编号" label-width="100px">
                                <el-input :disabled="true" v-model="depart.sdId"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="5">
                            <el-form-item>
                                <el-button
                                        type="warning"
                                        size="mini" @click="delDepart(idx)">
                                    删除
                                </el-button>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-form>
            </el-form-item>
            <el-form-item label="开课年级">
                <el-select v-model="courseForm.scGrade" placeholder="请选择开课年级">
                    <el-option label="不设定" value=""></el-option>
                    <el-option label="2014" value="2014"></el-option>
                    <el-option label="2015" value="2015"></el-option>
                    <el-option label="2016" value="2016"></el-option>
                    <el-option label="2017" value="2017"></el-option>
                    <el-option label="2018" value="2018"></el-option>
                    <el-option label="2019" value="2019"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="课程序号" prop="scId">
                <el-input v-model="courseForm.scId" placeholder="请输入课程序号"></el-input>
            </el-form-item>
            <el-form-item label="课程名字" prop="scName">
                <el-input v-model="courseForm.scName" placeholder="请输入课程名字"></el-input>
            </el-form-item>
            <el-form-item label="教室容量" prop="scMaxSize">
                <el-input v-model="courseForm.scMaxSize" placeholder="请输入教室容量"></el-input>
            </el-form-item>
            <el-form-item label="课程人数" prop="scActSize">
                <el-input v-model="courseForm.scActSize" placeholder="请输入课程人数"></el-input>
            </el-form-item>
            <el-form-item label="需要督导">
                <el-switch v-model="courseForm.scNeedMonitor"></el-switch>
            </el-form-item>
            <el-form-item v-for="(sch,idx) in courseForm.scheduleList"
                          :key="`acd_sch_${idx}`"
                          :label="`上课时间${idx + 1}`">
                <el-form size="mini" :model="courseForm.scheduleList[idx]" :rules="scheduleRules" :ref="`sch_${idx}`">
                    <el-row type="flex" :gutter="15">
                        <el-col>
                            <el-form-item label="起始周" label-width="55px" prop="ssStartWeek">
                                <el-input v-model="sch.ssStartWeek" placeholder="请输入开始周"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col>
                            <el-form-item label="结束周" label-width="55px" prop="ssEndWeek">
                                <el-input v-model="sch.ssEndWeek" placeholder="请输入结束周"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col>
                            <el-form-item label="单双周" label-width="55px" prop="ssFortnight">
                                <el-select v-model="sch.ssFortnight">
                                    <el-option label="未选择" value=""></el-option>
                                    <el-option label="全周" value="0"></el-option>
                                    <el-option label="单周" value="1"></el-option>
                                    <el-option label="双周" value="3"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row type="flex" :gutter="15">
                        <el-col>
                            <el-form-item label="星期" label-width="55px" prop="ssDayOfWeek">
                                <el-select v-model="sch.ssDayOfWeek">
                                    <el-option label="未选择" value=""></el-option>
                                    <el-option label="星期一" value="1"></el-option>
                                    <el-option label="星期二" value="2"></el-option>
                                    <el-option label="星期三" value="3"></el-option>
                                    <el-option label="星期四" value="4"></el-option>
                                    <el-option label="星期五" value="5"></el-option>
                                    <el-option label="星期六" value="6"></el-option>
                                    <el-option label="星期日" value="7"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col>
                            <el-form-item label="起始节" label-width="55px" prop="ssStartTime">
                                <el-input v-model="sch.ssStartTime" placeholder="请输入开始节"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col>
                            <el-form-item label="结束节" label-width="55px" prop="ssEndTime">
                                <el-input v-model="sch.ssEndTime" placeholder="请输入结束节"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row type="flex" :gutter="10">
                        <el-col>
                            <el-form-item label="学年度学期" label-width="85px" prop="ssYearEtTerm">
                                <el-select v-model="sch.ssYearEtTerm">
                                    <el-option label="未选择" value=""></el-option>
                                    <el-option v-for="val in getYearEtTerm()"
                                               :key="`acd_${val}`"
                                               :label="val"
                                               :value="val"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col>
                            <el-form-item label="上课地点名字" label-width="100px">
                                <el-autocomplete v-model="sch.slName"
                                                 placeholder="通过输入地点名查询"
                                                 :fetch-suggestions="queryBySlName"
                                                 @select="handleSlNameSelect(idx,$event)">
                                </el-autocomplete>
                            </el-form-item>
                        </el-col>
                        <el-col>
                            <el-form-item label="上课地点编号" label-width="100px">
                                <el-input :disabled="true" v-model="sch.slId"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="5">
                            <el-form-item>
                                <el-button :type="null != sch.ssId && '' !== sch.ssId.trim() ? 'danger' : 'warning'"
                                           size="mini" @click="delSchedule(idx)">
                                    删除
                                </el-button>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-form>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="onSure">确定</el-button>
                <el-button @click="addCozTime">新增上课时间</el-button>
                <el-button @click="addDepartment">新增开课学院</el-button>
                <el-button @click="resetForm">重置</el-button>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>

<script>
    export default {
        name: "AddCourseDialog",
        props: {
            dialogVisible: Boolean
        },
        data() {
            const numberValidator = (rule, value, callback) => {
                if (value.trim() === '') {
                    callback();
                    return
                }
                if (isNaN(parseInt(value))) {
                    callback(new Error('必须为数字'));
                    return
                }
                callback();
            }
            const notBlankValidator = (rule, value, callback) => {
                if (value.trim() === '') {
                    callback(new Error('不能为空'));
                    return
                }
                callback();
            }
            return {
                scheduleRules: {
                    ssStartWeek: [
                        {
                            validator: notBlankValidator,
                            trigger: 'blur'
                        },
                        {
                            validator: numberValidator,
                            trigger: 'blur'
                        }
                    ],
                    ssEndWeek: [
                        {
                            validator: notBlankValidator,
                            trigger: 'blur'
                        },
                        {
                            validator: numberValidator,
                            trigger: 'blur'
                        }
                    ],
                    ssStartTime: [
                        {
                            validator: notBlankValidator,
                            trigger: 'blur'
                        },
                        {
                            validator: numberValidator,
                            trigger: 'blur'
                        }
                    ],
                    ssEndTime: [
                        {
                            validator: notBlankValidator,
                            trigger: 'blur'
                        },
                        {
                            validator: numberValidator,
                            trigger: 'blur'
                        }
                    ],
                    ssFortnight: [
                        {
                            validator: notBlankValidator,
                            trigger: 'change'
                        }
                    ],
                    ssDayOfWeek: [
                        {
                            validator: notBlankValidator,
                            trigger: 'change'
                        }
                    ],
                    ssYearEtTerm: [
                        {
                            validator: notBlankValidator,
                            trigger: 'change'
                        }
                    ]
                },
                formRules: {
                    scId: [
                        {
                            validator: notBlankValidator,
                            trigger: 'blur'
                        }
                    ],
                    scName: [
                        {
                            validator: notBlankValidator,
                            trigger: 'blur'
                        }
                    ],
                    scMaxSize: [
                        {
                            validator: numberValidator,
                            trigger: 'blur'
                        }
                    ],
                    scActSize: [
                        {
                            validator: numberValidator,
                            trigger: 'blur'
                        }
                    ]
                },
                departmentSuggestList: [],
                locationSuggestList: [],
                courseForm: {
                    departmentList: [{
                        sdId: '',
                        sdName: ''
                    }],
                    scId: '',
                    scName: '',
                    scGrade: '',
                    scMaxSize: '',
                    scActSize: '',
                    scNeedMonitor: false,
                    scheduleList: [{
                        ssId: '',
                        ssStartWeek: '',
                        ssEndWeek: '',
                        ssFortnight: '',
                        ssStartTime: '',
                        ssEndTime: '',
                        ssDayOfWeek: '',
                        slId: '',
                        slName: ''
                    }]
                }
            }
        },
        methods: {
            closeDialog() {
                this.$emit('closeDialog')
            },
            addDepartment() {
                this.courseForm.departmentList.push({
                    sdId: '',
                    sdName: ''
                })
            },
            addCozTime() {
                this.courseForm.scheduleList.push({
                    ssId: '',
                    ssStartWeek: '',
                    ssEndWeek: '',
                    ssFortnight: '',
                    ssStartTime: '',
                    ssEndTime: '',
                    ssDayOfWeek: '',
                    ssYearEtTerm: '',
                    slId: '',
                    slName: ''
                })
            },
            resetForm() {
                this.courseForm = {
                    departmentList: [{
                        sdId: '',
                        sdName: ''
                    }],
                    scId: '',
                    scName: '',
                    scGrade: '',
                    scMaxSize: '',
                    scActSize: '',
                    scNeedMonitor: false,
                    scheduleList: [{
                        ssId: '',
                        ssStartWeek: '',
                        ssEndWeek: '',
                        ssFortnight: '',
                        ssStartTime: '',
                        ssEndTime: '',
                        ssDayOfWeek: '',
                        slId: '',
                        slName: ''
                    }]
                }
            },
            queryBySlName(val, callback) {
                if (null == val || '' === val) {
                    callback([])
                    return
                }
                this.$request.getLocations(1, 10, null, val)
                    .then(res => {
                        this.locationSuggestList = res.data.list
                        this.locationSuggestList.forEach(l => l.value = `${l.slId} ${l.slName}`)
                        callback(this.locationSuggestList)
                    })
                    .catch(() => {
                        callback([])
                    })
            },
            handleSlNameSelect(idx, location) {
                this.courseForm.scheduleList[idx].slId = location.slId.toString()
                this.courseForm.scheduleList[idx].slName = location.slName
            },
            queryBySdName(val, callback) {
                if ('' === val) {
                    callback([])
                    return
                }
                this.$request.administrator.getDepartments(val)
                    .then(res => {
                        this.departmentSuggestList = res.data
                        this.departmentSuggestList.forEach(d => d.value = `${d.sdId} ${d.sdName}`)
                        callback(this.departmentSuggestList)
                    })
                    .catch(() => {
                        callback([])
                    })
            },
            handleSdNameSelect(idx, department) {
                this.courseForm.departmentList[idx].sdId = department.sdId.toString()
                this.courseForm.departmentList[idx].sdName = department.sdName
            },
            getYearEtTerm() {
                const list = []
                for (let i = 2014; i < 2020; i++) {
                    list.push(i + '-' + (i + 1) + '-' + 1)
                    list.push(i + '-' + (i + 1) + '-' + 2)
                }
                return list
            },

            delSchedule(idx) {
                this.courseForm.scheduleList.splice(idx, 1)
            },
            delDepart(idx) {
                this.courseForm.departmentList.splice(idx, 1)
            },
            onSure() {
                const formList = [this.$refs.cozInfoForm]
                let idx = 0
                while (null != this.$refs[`sch_${idx}`] && this.$refs[`sch_${idx}`].length > 0) {
                    formList.push(this.$refs[`sch_${idx++}`][0])
                }


                const resList = []
                formList.forEach(f => f.validate((validated) => {
                    if (validated)
                        resList.push(true)
                    else
                        resList.push(false)
                }))

                while (resList.length !== formList.length) {
                    if (resList.length === formList.length)
                        break;
                }

                if (resList.find(s => false === s)) {
                    return
                }

                const course = this.courseForm
                const departList = course.departmentList.filter(d => null != d.sdId && '' !== d.sdId.trim())
                this.$request.administrator
                    .addCourse(this.courseForm.scId, course, course.scheduleList, departList)
                    .then(res => {
                        if (!res.data.success) {
                            this.$message.error(res.data.message)
                            return
                        }

                        this.$message.success('修改成功')
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
                        this.closeDialog()
                    })


            },
        }
    }
</script>

<style scoped>

</style>