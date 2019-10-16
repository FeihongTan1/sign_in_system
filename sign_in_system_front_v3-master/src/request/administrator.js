import axios from "./axiosConfig"

async function getCourse(page, pageSize, hasMonitor, needMonitor, sdId, scGrade, scId, scName, orderCol, order) {
    return axios.get('/courses', {
        params: {
            getType: 'administrator',
            hasMonitor,
            needMonitor,
            page,
            pageSize,
            sdId: '' === sdId ? null : sdId,
            scGrade: '' === scGrade ? null : scGrade,
            scId: '' === scId ? null : scId,
            scName: '' === scName ? null : scName,
            orderCol,
            order
        }
    })
}

async function batchSupervisions(monitorStatus, hasMonitor, needMonitor,
                                 sdId, scGrade, scId, scName) {
    return axios.put('/courses/sc-need-monitor', null, {
        params: {
            monitorStatus,
            hasMonitor,
            needMonitor,
            sdId: '' === sdId ? null : sdId,
            scGrade: '' === scGrade ? null : scGrade,
            scId: '' === scId ? null : scId,
            scName: '' === scName ? null : scName
        }
    })
}

async function batchSelectionSupervisions(monitorStatus, scIdList) {
    return axios.put('/courses/sc-need-monitor', scIdList, {
        params: {
            monitorStatus
        }
    })
}

async function getSupervision(scId) {
    return await axios.get(`/courses/${scId}/supervisions`)
}

async function getDepartments(sdName) {
    return await axios.get('/departments', {
        params: {
            sdName: '' === sdName.trim() ? '' : sdName
        }
    })
}

async function modifyScNeedMonitor(scId, scNeedMonitor) {
    return await axios.put(`/courses/${scId}/sc-need-monitor`, {
        scNeedMonitor
    })
}

async function getMonitors(page, pageSize, suId, suName, ordByLackNum) {
    return await axios.get('/monitors', {
        params: {
            page,
            pageSize,
            suId: '' === suId ? null : suId,
            suName: '' === suName ? null : suName,
            ordByLackNum
        }
    })
}

async function grantMonitor(suId) {
    return await axios.post(`/monitors/${suId}`)
}

async function revokeMonitor(suId) {
    return await axios.delete(`/monitors/${suId}`)
}

async function getTeachers(page, pageSize, suId, suName) {
    return await axios.get('/teachers', {
        params: {
            page,
            pageSize,
            suId: '' === suId ? null : suId,
            suName: '' === suName ? null : suName,
        }
    })
}

async function getStudents(page, pageSize, suId, suName, orderByCozLackNum) {
    return await axios.get('/students', {
        params: {
            page,
            pageSize,
            suId: '' === suId ? null : suId,
            suName: '' === suName ? null : suName,
            orderByCozLackNum
        }
    })
}

async function createSignIn(ssId, file) {
    const formData = new FormData();
    formData.append("picture", file)
    return await axios.post(`/schedules/${ssId}/signIns`, formData)
}

async function getSignIns(scId) {
    return await axios.get(`/courses/${scId}/signIns`, {
        params: {
            queryType: 'administrator'
        }
    })
}

async function modifyScheduleLocation(ssId, slId) {
    return await axios.put(`/schedules/${ssId}/locations/${slId}`)
}

async function getProgress(key) {
    return await axios.get(`/progress/${key}`)
}

async function importCozInfo(formData) {
    return await axios.post('/courses/import', formData)
}

async function importStuInfo(formData) {
    return await axios.post('/students/import', formData)
}

async function getStuSignIns(suId) {
    return await axios.get(`/users/${suId}/signIns`)
}

async function modifyLocation(slId, sisLocation) {
    return await axios.put(`/locations/${slId}`, sisLocation)
}

async function deleteLocation(slId) {
    return await axios.delete(`/locations/${slId}`)
}

async function getCourseDepartments(scId) {
    return await axios.get(`/courses/${scId}/departments`)
}

async function modifyCourse(scId, course, mScheduleList, nScheduleList, departList) {
    if (null != mScheduleList && (departList instanceof Array))
        mScheduleList = mScheduleList.filter(s => null != s.ssId && '' !== s.ssId.trim())
    else
        mScheduleList = []

    if (null == nScheduleList || !(nScheduleList instanceof Array))
        nScheduleList = []

    if (null != departList && (departList instanceof Array))
        departList = departList.filter(d => null != d.sdId && '' !== d.sdId.trim())
    else
        departList = []

    const formData = new FormData();
    formData.append('course', JSON.stringify(course))
    formData.append('mScheduleList', JSON.stringify(mScheduleList))
    formData.append('nScheduleList', JSON.stringify(nScheduleList))
    formData.append('departList', JSON.stringify(departList))
    return await axios.put(`/courses/${scId}`, formData)
}

async function deleteCourse(scId) {
    return await axios.delete(`/courses/${scId}`)
}

async function modifyDepartment(sdId, sdName) {
    return await axios.put(`/departments/${sdId}`, null, {
        params: {
            sdName
        }
    })
}

async function deleteDepartment(sdId) {
    return await axios.delete(`/departments/${sdId}`)
}

async function addDepartment(sdName) {
    return await axios.post('/departments', null, {
        params: {
            sdName
        }
    })
}

async function getJoinCourses(scId) {
    return await axios.get(`/courses/${scId}/joinCourses`)
}

async function modifyJoinCourse(scId, joinCourseList) {
    joinCourseList.forEach(sjc => {
        delete sjc.sisUser
        if (null != sjc.sjcId && isNaN(parseInt(sjc.sjcId))) {
            sjc.sjcId = null
        }
    })
    return await axios.put(`/courses/${scId}/joinCourses`, joinCourseList)
}

async function addCourse(scId, course, scheduleList, departList) {
    if (null == scheduleList || !(scheduleList instanceof Array))
        scheduleList = []

    if (null != departList && (departList instanceof Array))
        departList = departList.filter(d => null != d.sdId && '' !== d.sdId.trim())
    else
        departList = []

    const formData = new FormData();
    formData.append('course', JSON.stringify(course))
    formData.append('scheduleList', JSON.stringify(scheduleList))
    formData.append('departList', JSON.stringify(departList))
    return await axios.post(`/courses/${scId}`, formData)
}

async function getMonitorCourse(suId) {
    return await axios.get('/courses', {
        params: {
            getType: 'monitor',
            suId
        }
    })
}

async function getLockGrade() {
    return await axios.get('/lock_grade')
}

async function getTchCozTable(tchSuId) {
    return await axios.get('/courses', {
        params: {
            getType: 'teacher',
            tchSuId
        }
    })
}

async function modifySignIns(ssiId, signInDeltailList) {
    return await axios.put(`/signIns/${ssiId}`, signInDeltailList)
}

export default {
    getCourse,
    getStudents,
    batchSupervisions,
    getSupervision,
    getDepartments,
    modifyScNeedMonitor,
    batchSelectionSupervisions,
    getMonitors,
    grantMonitor,
    revokeMonitor,
    createSignIn,
    getSignIns,
    modifyScheduleLocation,
    getProgress,
    importCozInfo,
    importStuInfo,
    getStuSignIns,
    modifyLocation,
    deleteLocation,
    getCourseDepartments,
    modifyCourse,
    deleteCourse,
    modifyDepartment,
    deleteDepartment,
    addDepartment,
    getJoinCourses,
    modifyJoinCourse,
    addCourse,
    getMonitorCourse,
    getLockGrade,
    getTeachers,
    getTchCozTable,
    modifySignIns
}