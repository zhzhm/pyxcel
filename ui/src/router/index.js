import Vue from 'vue'
import VueRouter from 'vue-router'
import PyExcel from '../views/PyExcel.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/session/:id',
    name: 'Home',
    component: PyExcel
  },
  {
    path: '/about',
    name: 'About',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ '../views/About.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

export default router
