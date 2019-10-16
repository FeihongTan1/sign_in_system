import axios from "./axiosConfig"

async function getSchedules(smtStatus) {
    return await axios.get('/schedules/monitor-trans', {
        params: {
            smtStatus
        }
    })
}

async function getCourseTable() {
    return await axios.get('/courses', {
        params: {
            getType: 'monitor'
        }
    })
}

async function getMonitorPool(page, pageSize) {
    return await axios.get('/courses', {
        params: {
            getType: 'monitor',
            page,
            pageSize,
            needMonitor: true,
            hasMonitor: false
        }
    })
}

async function receiveCourse(scId) {
    return await axios.post(`/courses/${scId}/monitor`)
}

async function applyTrans(ssId, monitorTrans) {
    return await axios.post(`/schedules/${ssId}/monitor-trans`, monitorTrans)
}

async function modifyTransfer(ssId, monitorTrans) {
    return await axios.put(`/schedules/${ssId}/monitor-trans`, monitorTrans)
}

export default {
    getCourseTable,
    getMonitorPool,
    receiveCourse,
    getSchedules,
    applyTrans,
    modifyTransfer
}