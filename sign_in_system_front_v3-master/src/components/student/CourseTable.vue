<template>
    <el-container>
        <el-header>
            <el-form :inline="true" size="mini" :model="selectForm">
                <el-form-item label="学年度学期">
                    <el-select v-model="selectForm.yearEtTerm">
                        <el-option v-for="val in getYearEtTerm()"
                                   :key="val"
                                   :label="val"
                                   :value="val"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="周">
                    <el-select v-model="selectForm.week">
                        <el-option v-for="val in getWeek()"
                                   :key="val"
                                   :label="val"
                                   :value="val"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="reDraw" :loading="loading">
                        确定
                    </el-button>
                </el-form-item>
            </el-form>
        </el-header>
        <el-main>
            <el-table ref="courseTable"
                      class="course-table"
                      v-loading="loading"
                      :border="true"
                      :span-method="spanMethod"
                      :data="tableData"
                      :cell-style="cellStyle"
                      @cell-click="showCozDtl">
                <el-table-column prop="period" label="#">
                    <template slot-scope="scope">
                        <span class="course-title" v-text="scope.row.period"></span>
                    </template>
                </el-table-column>
                <el-table-column prop="mon" label="星期一">
                    <template slot-scope="scope">
                        <span class="course-title" v-text="scope.row.mon.scName"></span>
                    </template>
                </el-table-column>
                <el-table-column prop="tue" label="星期二">
                    <template slot-scope="scope">
                        <span class="course-title" v-text="scope.row.tue.scName"></span>
                    </template>
                </el-table-column>
                <el-table-column prop="wed" label="星期三">
                    <template slot-scope="scope">
                        <span class="course-title" v-text="scope.row.wed.scName"></span>
                    </template>
                </el-table-column>
                <el-table-column prop="thu" label="星期四">
                    <template slot-scope="scope">
                        <span class="course-title" v-text="scope.row.thu.scName"></span>
                    </template>
                </el-table-column>
                <el-table-column prop="fri" label="星期五">
                    <template slot-scope="scope">
                        <span class="course-title" v-text="scope.row.fri.scName"></span>
                    </template>
                </el-table-column>
                <el-table-column prop="sat" label="星期六">
                    <template slot-scope="scope">
                        <span class="course-title" v-text="scope.row.sat.scName"></span>
                    </template>
                </el-table-column>
                <el-table-column prop="sun" label="星期日">
                    <template slot-scope="scope">
                        <span class="course-title" v-text="scope.row.sun.scName"></span>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
        <coz-dialog :course="dialogCourse" :dialogVisible="dialogVisible" @closeDialog="closeDialog"/>
    </el-container>
</template>

<script>
    import {mapState} from 'vuex'

    export default {
        name: "CourseTable",
        components: {
            CozDialog: () => import('./CozDialog')
        },
        computed: {
            ...mapState({
                week: 'week',
                user: 'user'
            }),
            fortnight() {
                return this.selectForm.week % 2 === 1 ? 1 : 2
            }
        },
        data() {
            return {
                dayMap: new Map([[1, 'mon'], [2, 'tue'], [3, 'wed'], [4, 'thu'], [5, 'fri'], [6, 'sat'], [7, 'sun']]),
                courseList: [],
                selectForm: {
                    yearEtTerm: '2017-2018-2',
                    week: '1'
                },
                loading: false,
                tableData: [],
                colorMap: new Map(),
                rowSpanMap: new Map(),
                nextColor: 0,
                dialogCourse: null,
                dialogVisible: false
            }
        },
        created() {
            const date = new Date()
            const year = date.getFullYear()
            const term = date.getMonth() >= 8 && date.getMonth() <= 11 || date.getMonth() >= 0 && date.getMonth() <= 1 ? '1' : '2'
            this.selectForm.yearEtTerm = year + '-' + (year + 1) + '-' + term
            this.$request.getWeek()
                .then(res => {
                    if (!res.data.week)
                        return
                    this.selectForm.week = res.data.week.toString()
                })
                .catch(() => {
                })
            this.loading = true
            this.$request.student.getCourseTable()
                .then(res => {
                    this.courseList = res.data.list;
                    this.reDraw()
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
        mounted() {
            this.$refs.courseTable.$el.getElementsByClassName('el-table__body')[0].setAttribute('cellspacing', '5')
            this.$refs.courseTable.$el.getElementsByClassName('el-table__header')[0].setAttribute('cellspacing', '5')
        },
        methods: {
            reDraw() {
                this.loading = true
                const tempTableData = []
                for (let i = 0; i < 12; i++) {
                    tempTableData[i] = {period: i + 1, mon: '', tue: '', wed: '', thu: '', fri: '', sat: '', sun: ''}
                }
                const courseList = this.courseList
                for (let i = 0; i < courseList.length; i++) {
                    const course = courseList[i];
                    const scheduleList = course.sisScheduleList
                    for (let j = 0; j < scheduleList.length; j++) {
                        const schedule = scheduleList[j];

                        if (this.selectForm.yearEtTerm !== schedule.ssYearEtTerm ||
                            this.selectForm.week < schedule.ssStartWeek ||
                            this.selectForm.week > schedule.ssEndWeek ||
                            (schedule.ssFortnight !== 0 && schedule.ssFortnight !== this.fortnight)) {
                            continue;
                        }

                        for (let k = schedule.ssStartTime; k <= schedule.ssEndTime; k++) {
                            // 跨行数
                            if (k === schedule.ssStartTime) {
                                this.rowSpanMap.set(`${k - 1}_${schedule.ssDayOfWeek}`,
                                    [schedule.ssEndTime - schedule.ssStartTime + 1, 1])
                            } else {
                                this.rowSpanMap.set(`${k - 1}_${schedule.ssDayOfWeek}`,
                                    [0, 0])
                            }
                            // 课程插入tempTable
                            tempTableData[k - 1][this.dayMap.get(schedule.ssDayOfWeek)] = course
                        }
                    }
                }
                this.tableData = tempTableData
                this.loading = false
            },
            spanMethod({rowIndex, columnIndex}) {
                let rowSpan = this.rowSpanMap.get(`${rowIndex}_${columnIndex}`)
                if (rowSpan) {
                    return rowSpan
                }
            },
            cellStyle({row, column}) {
                const colorList = ['#7EA9FB', '#67C23A', '#FEBC50', '#F56C6C', '#A388FC', '#ff3df4', '#00ff9f']
                if (column.property === 'period') {
                    return
                }
                const name = row[column.property]
                if (name == null || name === '') {
                    return
                }
                let color = this.colorMap.get(name)
                if (color == null) {
                    color = colorList[++this.nextColor % 7]
                    this.colorMap.set(name, color)
                    return {
                        backgroundColor: color,
                        cursor: 'pointer',
                        'border-radius': '10px',
                        'box-shadow': `1px 1px 7px ${color}`,
                        'border-bottom': 'none',
                        'z-index': '1'
                    }
                }
                return {
                    backgroundColor: color,
                    cursor: 'pointer',
                    'border-radius': '10px',
                    'box-shadow': `1px 1px 7px ${color}`,
                    'border-bottom': 'none',
                    'z-index': '1'
                }
            },
            showCozDtl(row, column) {
                if (column.property === 'period' || row[column.property] === '' || row[column.property] == null) {
                    return
                }
                this.dialogCourse = row[column.property]
                this.dialogVisible = true
            },
            closeDialog() {
                this.dialogCourse = null
                this.dialogVisible = false
            },
            getYearEtTerm() {
                const list = []
                for (let i = 2014; i < 2020; i++) {
                    list.push(i + '-' + (i + 1) + '-' + 1)
                    list.push(i + '-' + (i + 1) + '-' + 2)
                }
                return list
            },
            getWeek() {
                const list = []
                for (let i = 1; i <= 22; i++) {
                    list.push(i.toString())
                }
                return list
            }
        }
    }
</script>

<style scoped>

</style>