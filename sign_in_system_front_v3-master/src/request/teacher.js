import axios from "./axiosConfig"

async function getCourseTable() {
    return await axios.get("/courses", {
        params: {
            getType: 'teacher'
        }
    })
}

async function getSignIns(scId) {
    return await axios.get(`/courses/${scId}/signIns`, {
        params: {
            queryType: 'teacher'
        }
    })
}

export default {
    getCourseTable,
    getSignIns
}