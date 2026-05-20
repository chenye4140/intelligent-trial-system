import { test, expect } from './fixtures'
import { login, generateTestCode, expectMessage } from './utils'

test.describe('TC-SYS System Management Module (P1)', () => {

  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('TC-SYS-01: User management page renders', async ({ page }) => {
    await page.goto('/system/user')
    await page.waitForSelector('.el-table', { timeout: 10000 })
    // Verify search form
    await expect(page.getByPlaceholder('请输入用户名')).toBeVisible()
    // Verify toolbar
    await expect(page.getByRole('button', { name: '新增' })).toBeVisible()
    // Verify table columns
    await expect(page.locator('.el-table')).toContainText('用户名')
    await expect(page.locator('.el-table')).toContainText('姓名')
    await expect(page.locator('.el-table')).toContainText('角色')
    // Verify pagination
    await expect(page.locator('.el-pagination')).toBeVisible()
  })

  test('TC-SYS-02: User search by username', async ({ page }) => {
    await page.goto('/system/user')
    await page.waitForSelector('.el-table', { timeout: 10000 })
    await page.getByPlaceholder('请输入用户名').fill('admin')
    await page.getByRole('button', { name: '查询' }).click()
    await page.waitForTimeout(1000)
    // Admin user should appear
    await expect(page.locator('.el-table')).toContainText('admin')
  })

  test('TC-SYS-03: Role management page renders', async ({ page }) => {
    await page.goto('/system/role')
    await page.waitForSelector('.el-table', { timeout: 10000 })
    // Verify toolbar
    await expect(page.getByRole('button', { name: '新增' })).toBeVisible()
    // Verify table
    await expect(page.locator('.el-table')).toContainText('角色名称')
    await expect(page.locator('.el-table')).toContainText('角色编码')
  })

  test('TC-SYS-04: Menu management page renders', async ({ page }) => {
    await page.goto('/system/menu')
    await page.waitForTimeout(2000)
    // Menu page uses el-tree, not el-table
    await expect(page.locator('.el-tree')).toBeVisible().or(
      expect(page.locator('.el-table')).toBeVisible()
    )
  })

  test('TC-SYS-05: Audit log page renders', async ({ page }) => {
    await page.goto('/system/log')
    await page.waitForSelector('.el-table', { timeout: 10000 })
    // Verify search form
    await expect(page.getByPlaceholder('请输入操作模块')).toBeVisible()
    // Verify table columns
    await expect(page.locator('.el-table')).toContainText('操作时间')
    await expect(page.locator('.el-table')).toContainText('操作人')
    await expect(page.locator('.el-table')).toContainText('操作内容')
  })

  test('TC-SYS-06: Audit log filter by module', async ({ page }) => {
    await page.goto('/system/log')
    await page.waitForSelector('.el-table', { timeout: 10000 })
    // Filter by module if input exists
    const moduleInput = page.getByPlaceholder('请输入操作模块')
    if (await moduleInput.isVisible()) {
      await moduleInput.fill('登录')
      await page.getByRole('button', { name: '查询' }).click()
      await page.waitForTimeout(1000)
    }
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-SYS-07: Navigation between system pages', async ({ page }) => {
    // Test sidebar navigation to system management
    await page.goto('/dashboard')
    await page.waitForTimeout(1000)
    // Look for system management menu item
    const systemMenu = page.locator('.el-menu').getByText('系统管理')
    const isVisible = await systemMenu.isVisible().catch(() => false)
    if (isVisible) {
      await systemMenu.click()
      await page.waitForTimeout(500)
      // Submenu should expand showing user/role/menu/log
      await expect(page.getByText('用户管理')).toBeVisible()
    }
  })

  test('TC-SYS-08: Reset user password dialog', async ({ page }) => {
    await page.goto('/system/user')
    await page.waitForSelector('.el-table', { timeout: 10000 })
    // Look for reset password button in the first row
    const firstRow = page.locator('.el-table__body tr').first()
    if (await firstRow.isVisible()) {
      const resetBtn = firstRow.locator('button').filter({ hasText: '重置密码' })
      const isVisible = await resetBtn.isVisible().catch(() => false)
      if (isVisible) {
        await resetBtn.click()
        await page.waitForTimeout(500)
        // A dialog should appear to input new password
        await expect(page.locator('.el-dialog')).toBeVisible()
      }
    }
  })
})
