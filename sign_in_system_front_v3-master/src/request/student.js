import axios from "./axiosConfig"

async function getCourseTable() {
    return await axios.get("/courses", {
        params: {
            getType: 'student'
        }
    })
}

async function getHisSignIns(scId) {
    return await axios.get(`/courses/${scId}/signIns`, {
        params: {
            queryType: 'student'
        }
    })
}

export default {
    getCourseTable,
    getHisSignIns
}