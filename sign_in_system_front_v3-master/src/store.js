import Vue from 'vue'
import Vuex from 'vuex'
import request from './request'

Vue.use(Vuex)

export default new Vuex.Store({
    state: {
        token: '',
        backSetting: '',
        user: null,
        role: '',
        week: 0
    },
    getters: {
        isLogin(state) {
            return state.token != null && state.token !== ''
        }
    },
    mutations: {
        setToken(state, token) {
            request.setAuthorizationValue(token)
            state.token = token
        },
        setUser(state, user) {
            state.user = user
        },
        setRole(state, role) {
            state.role = role
        },
        setWeek(state, week) {
            state.week = week
        },
        backSetting(state, backSetting) {
            state.backSetting = backSetting
        }
    },
    actions: {
        login({commit, state}, loginData) {
            commit('setToken', loginData.accessToken)
            commit('setUser', loginData.sisUser)
            localStorage.setItem('state', JSON.stringify(state))
        },
        getLocalStorageState({commit, state}) {
            if (state.token !== '' && state.user)
                return true
            const hisState = JSON.parse(localStorage.getItem('state'));
            if (!hisState)
                return false
            const role = localStorage.getItem('role')
            if (role)
                commit('setRole', role)
            commit('setToken', hisState.token)
            commit('setUser', hisState.user)
            commit('setWeek', hisState.week)
            return true
        },
        logout({state}) {
            state.token = ''
            state.user = null
            state.defaultRole = ''
            state.week = 0
            state.role = ''
            localStorage.removeItem('state')
        }
    }
})
