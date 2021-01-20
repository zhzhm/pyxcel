import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import VueCodeMirror from 'vue-codemirror'
import 'codemirror/lib/codemirror.css'
import 'element-ui/lib/theme-chalk/index.css'

import VueNativeSock from 'vue-native-websocket'
Vue.use(VueNativeSock, '//' + location.host + '/console', {
  reconnection: true, // (Boolean) whether to reconnect automatically (false)
  reconnectionAttempts: 5, // (Number) number of reconnection attempts before giving up (Infinity),
  reconnectionDelay: 3000, // (Number) how long to initially wait before attempting a new (1000)
  connectManually: true
})
Vue.config.productionTip = false
Vue.use(VueCodeMirror)
Vue.use(ElementUI, { locale })

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
