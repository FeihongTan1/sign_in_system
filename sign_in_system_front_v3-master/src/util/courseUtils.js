const dayMap = new Map([[1, '一'], [2, '二'], [3, '三'], [4, '四'], [5, '五'], [6, '六'], [7, '日']])
const fortMap = new Map([[0,'全'],[1,'单'],[2,'双']])

function getScheduleTimeString(schedule) {
    return `${fortMap.get(schedule.ssFortnight)} 星期${dayMap.get(schedule.ssDayOfWeek)} 第 ${schedule.ssStartTime < 10 ? `0${schedule.ssStartTime}` : schedule.ssStartTime} ~ ${schedule.ssEndTime < 10 ? `0${schedule.ssEndTime}` : schedule.ssEndTime} 节 ${schedule.ssRoom}`
}

function getCourseMonitorStatus(sisCourse) {
    if (!sisCourse.scNeedMonitor)
        return '未督导'
    if (null == sisCourse.suId)
        return '未领取'
    return `督导中 ${sisCourse.monitor ? `${sisCourse.monitor.suName} ${sisCourse.monitor.suId}` : ''}`
}

export default {
    getCourseMonitorStatus,
    getScheduleTimeString
}