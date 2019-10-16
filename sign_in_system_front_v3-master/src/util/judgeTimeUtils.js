const splitMonth = 8 //Sept
const classDuration = 45
const startDate = new Date(2018, 8, 3)
const firstClass = new Date(1970, 0, 1, 8, 30, 0)
const secondClass = new Date(1970, 0, 1, 9, 20, 0)
const thirdClass = new Date(1970, 0, 1, 10, 20, 0)
const fourthClass = new Date(1970, 0, 1, 11, 10, 0)
const fifthClass = new Date(1970, 0, 1, 14, 30, 0)
const sixthClass = new Date(1970, 0, 1, 15, 20, 0)
const seventhClass = new Date(1970, 0, 1, 16, 20, 0)
const eighthClass = new Date(1970, 0, 1, 17, 10, 0)
const ninthClass = new Date(1970, 0, 1, 19, 30, 0)
const tenthClass = new Date(1970, 0, 1, 20, 20, 0)
const eleventhClass = new Date(1970, 0, 1, 21, 10, 0)
const twelfthClass = new Date(1970, 0, 1, 22, 0, 0)
const java2jsDayOfWeekMap = new Map([[0, 7], [1, 1], [2, 2], [3, 3], [4, 4], [5, 5], [6, 6]]);

function isCourseTime(schedule, actionWeek, nowDate) {
    const res1 = judgeYearEtTerm(nowDate, schedule.ssYearEtTerm)
    const res2 = judgeWeekEtFortnight(nowDate, actionWeek, schedule.ssStartWeek, schedule.ssEndWeek, schedule.ssFortnight)
    const res3 = judgeDayOfWeek(nowDate, schedule.ssDayOfWeek)
    const res4 = judgeTime(nowDate, schedule.ssStartTime, schedule.ssEndTime)
    return res1 && res2 && res3 && res4
}

function judgeYearEtTerm(date, ssYearEtTerm) {
    const termYear = new Date().getMonth() >= splitMonth ?
        new Date().getFullYear() : new Date().getFullYear() - 1
    const term = new Date().getMonth() >= splitMonth ? 1 : 2

    const yearTerms = ssYearEtTerm.split("-")
    const stdStartYear = parseInt(yearTerms[0])
    const stdTerm = parseInt(yearTerms[2])

    return stdStartYear === termYear && stdTerm === term
}

function judgeWeekEtFortnight(date, actionWeek, stdSsStartWeek, stdSsEndWeek, stdSsFortnight) {
    const nowWeek = getWeek(date)
    if (nowWeek !== actionWeek || nowWeek < stdSsStartWeek || nowWeek > stdSsEndWeek)
        return false

    const fortnight = (nowWeek % 2) === 0 ? 1 : 2;

    return stdSsFortnight === 0 || stdSsFortnight === fortnight
}

function judgeDayOfWeek(date, javaDayOfWeek) {
    const stdDayOfWeek = java2jsDayOfWeekMap.get(javaDayOfWeek)

    return date.getDay() === stdDayOfWeek
}

function judgeTime(nowDate, ssStartTime, ssEndTime) {
    const date = new Date(nowDate)
    date.setFullYear(1970)
    date.setMonth(0)
    date.setDate(1)

    const stdStartTime = getClassTime(ssStartTime)
    let stdEndTime = getClassTime(ssEndTime)
    stdEndTime = new Date(stdEndTime.getTime() + classDuration * 60 * 1000)

    return (date - stdStartTime) > 0 && (stdEndTime - date) > 0
}


function getWeek(date) {
    const days = parseInt((date - startDate) / (1000 * 60 * 60 * 24))
    return parseInt(days / 7) + 1
}

function getClassTime(classTime) {
    switch (classTime) {
        case 1:
            return firstClass
        case 2:
            return secondClass
        case 3:
            return thirdClass
        case 4:
            return fourthClass
        case 5:
            return fifthClass
        case 6:
            return sixthClass
        case 7:
            return seventhClass
        case 8:
            return eighthClass
        case 9:
            return ninthClass
        case 10:
            return tenthClass
        case 11:
            return eleventhClass
        case 12:
            return twelfthClass
        default:
            return null
    }
}

export default {
    isCourseTime
}
