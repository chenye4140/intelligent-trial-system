import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTabsStore = defineStore('tabs', () => {
  const visitedViews = ref([
    { path: '/dashboard', title: '首页', name: 'Dashboard', affix: true }
  ])
  const cachedViews = ref(['Dashboard'])

  function addView(view) {
    addVisitedView(view)
    addCachedView(view)
  }

  function addVisitedView(view) {
    if (visitedViews.value.some(v => v.path === view.path)) return
    visitedViews.value.push({ ...view })
  }

  function addCachedView(view) {
    if (!view.name) return
    if (cachedViews.value.includes(view.name)) return
    if (view.affix) return
    cachedViews.value.push(view.name)
  }

  function delView(view) {
    return new Promise((resolve) => {
      delVisitedView(view)
      delCachedView(view)
      resolve({ visitedViews: [...visitedViews.value], cachedViews: [...cachedViews.value] })
    })
  }

  function delVisitedView(view) {
    return new Promise((resolve) => {
      for (const [i, v] of visitedViews.value.entries()) {
        if (v.path === view.path) {
          visitedViews.value.splice(i, 1)
          break
        }
      }
      resolve([...visitedViews.value])
    })
  }

  function delCachedView(view) {
    return new Promise((resolve) => {
      const index = cachedViews.value.indexOf(view.name)
      index > -1 && cachedViews.value.splice(index, 1)
      resolve([...cachedViews.value])
    })
  }

  function delOthersViews(view) {
    return new Promise((resolve) => {
      visitedViews.value = visitedViews.value.filter(v => v.affix || v.path === view.path)
      cachedViews.value = cachedViews.value.filter(name => name === view.name)
      resolve({ visitedViews: [...visitedViews.value], cachedViews: [...cachedViews.value] })
    })
  }

  function delAllViews() {
    return new Promise((resolve) => {
      visitedViews.value = visitedViews.value.filter(v => v.affix)
      cachedViews.value = []
      resolve({ visitedViews: [...visitedViews.value], cachedViews: [...cachedViews.value] })
    })
  }

  return {
    visitedViews,
    cachedViews,
    addView,
    delView,
    delVisitedView,
    delCachedView,
    delOthersViews,
    delAllViews
  }
})
