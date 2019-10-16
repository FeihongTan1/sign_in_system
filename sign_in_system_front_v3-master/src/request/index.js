import axios from './axiosConfig'
import student from './student'
import administrator from './administrator'
import teacher from './teacher'
import monitor from './monitor'

function setAuthorizationValue(token) {
    axios.defaults.headers.Authorization = `Bearer ${token}`
}

async function getToken(loginData) {
    const formData = new FormData()
    formData.append('suId', loginData.suId)
    formData.append('suPassword', loginData.suPassword)
    return await axios.post("/tokens", formData)
}

async function getWeek() {
    return await axios.get("/week")
}

async function modifyPassword(suId, oldPassword, newPassword) {
    const formData = new FormData()
    formData.append('oldPassword', oldPassword)
    formData.append('newPassword', newPassword)
    return await axios.post(`/users/${suId}`, formData)
}

async function getLocation(slId) {
    return await axios.get(`/locations/${slId}`)
}

async function getLocations(page, pageSize, slId, slName) {
    return await axios.get('/locations', {
        params: {
            page,
            pageSize,
            slId,
            slName
        }
    })
}

async function contact(contact) {
    return await axios.post('/contacts', contact)
}

export default {
    axios: axios,
    setAuthorizationValue,
    getToken,
    getWeek,
    modifyPassword,
    getLocation,
    getLocations,
    contact,
    student,
    administrator,
    teacher,
    monitor
}