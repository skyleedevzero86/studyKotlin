import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import ItemListView from './views/ItemListView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: ItemListView },
  ],
})

const app = createApp(App)
app.use(router)
app.mount('#app')
