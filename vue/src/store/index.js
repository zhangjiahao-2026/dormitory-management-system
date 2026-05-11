import {createStore} from 'vuex'

export default createStore({
    state: {
        identity: ''
    },
    mutations: {
        setIdentity(state, identity) {
            state.identity = identity
        }
    },
    actions: {},
    modules: {}
})
