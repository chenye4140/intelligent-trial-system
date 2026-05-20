import { test, expect } from './fixtures'
import { login, generateTestCode } from './utils'

test.describe('TC-WF Workflow Approval Module (P1)', () => {

  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto('/workflow')
    await page.waitForTimeout(2000)
  })

  test('TC-WF-01: Workflow page renders', async ({ page }) => {
    // Verify page loads
    await expect(page).toHaveURL(/\/workflow/)
    // Check for main sections - may have tabs for different views
    await expect(page.locator('.el-tabs, .el-table, .el-card').first()).toBeVisible()
  })

  test('TC-WF-02: Process definition list loads', async ({ page }) => {
    // Navigate to process definitions section
    // Look for tab navigation if available
    const defTab = page.locator('.el-tabs__item').filter({ hasText: '流程定义' })
    if (await defTab.isVisible().catch(() => false)) {
      await defTab.click()
      await page.waitForTimeout(1000)
    }
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-WF-03: Task list shows pending tasks', async ({ page }) => {
    // Look for task tab
    const taskTab = page.locator('.el-tabs__item').filter({ hasText: '待办任务' })
    if (await taskTab.isVisible().catch(() => false)) {
      await taskTab.click()
      await page.waitForTimeout(1000)
    }
    // Table should be visible (possibly empty)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-WF-04: Process instance list loads', async ({ page }) => {
    // Look for instance tab
    const instanceTab = page.locator('.el-tabs__item').filter({ hasText: '流程实例' })
    if (await instanceTab.isVisible().catch(() => false)) {
      await instanceTab.click()
      await page.waitForTimeout(1000)
    }
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-WF-05: Complete task dialog', async ({ page }) => {
    // If there are tasks in the list, try to complete one
    const firstRow = page.locator('.el-table__body tr').first()
    if (await firstRow.isVisible()) {
      const completeBtn = firstRow.locator('button').filter({ hasText: '完成' })
      const isVisible = await completeBtn.isVisible().catch(() => false)
      if (isVisible) {
        await completeBtn.click()
        await page.waitForTimeout(500)
        // A dialog should appear for task completion
        await expect(page.locator('.el-dialog')).toBeVisible()
      }
    }
  })

  test('TC-WF-06: Process history view', async ({ page }) => {
    // If there are process instances, check history
    const firstRow = page.locator('.el-table__body tr').first()
    if (await firstRow.isVisible()) {
      const historyBtn = firstRow.locator('button').filter({ hasText: '历史' }).or(
        firstRow.locator('button').filter({ hasText: '详情' })
      )
      const isVisible = await historyBtn.isVisible().catch(() => false)
      if (isVisible) {
        await historyBtn.click()
        await page.waitForTimeout(1000)
        await expect(page.locator('.el-dialog')).toBeVisible()
      }
    }
  })
})
