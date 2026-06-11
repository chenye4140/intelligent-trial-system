import { test, expect } from './fixtures'
import { generateTestCode } from './utils'

test.describe('TC-IDOC Incoming Document Registration Module (P1)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/incoming-doc')
    await page.waitForSelector('.el-table', { timeout: 10000 })
  })

  test('TC-IDOC-01: Incoming doc page renders with search and table', async ({ page }) => {
    // Verify search form
    await expect(page.getByPlaceholder('请输入标题')).toBeVisible()
    await expect(page.getByPlaceholder('请输入单位')).toBeVisible()
    await expect(page.locator('.el-select').filter({ hasText: '状态' })).toBeVisible()

    // Verify toolbar
    await expect(page.getByRole('button', { name: '来文登记' })).toBeVisible()

    // Verify table
    await expect(page.locator('.el-table')).toBeVisible()
    await expect(page.locator('.el-table')).toContainText('来文文号')
    await expect(page.locator('.el-table')).toContainText('来文标题')
    await expect(page.locator('.el-table')).toContainText('来文单位')
    await expect(page.locator('.el-table')).toContainText('状态')

    // Verify pagination
    await expect(page.locator('.el-pagination')).toBeVisible()
  })

  test('TC-IDOC-02: Create incoming document record', async ({ page }) => {
    await page.getByRole('button', { name: '来文登记' }).click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('来文登记')

    // Fill form
    const testCode = generateTestCode('IDOC')
    await page.getByPlaceholder('请输入来文文号').first().fill(testCode)
    await page.getByPlaceholder('请输入来文标题').first().fill('E2E自动化测试来文')
    await page.getByPlaceholder('请输入来文单位').first().fill('E2E测试单位')

    // Submit
    await page.getByRole('button', { name: '确 定', exact: true }).click()
    await page.waitForTimeout(1000)
  })

  test('TC-IDOC-03: Required field validation', async ({ page }) => {
    await page.getByRole('button', { name: '来文登记' }).click()
    await expect(page.locator('.el-dialog')).toBeVisible()

    // Submit empty form
    await page.getByRole('button', { name: '确 定', exact: true }).click()
    await page.waitForTimeout(500)

    // Should show validation error
    await expect(page.locator('.el-form-item__error')).toBeVisible()
  })

  test('TC-IDOC-04: Search by document title', async ({ page }) => {
    const searchInput = page.getByPlaceholder('请输入标题')
    await expect(searchInput).toBeVisible()
    await searchInput.fill('测试')
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForTimeout(500)
    // Table should still be visible after search
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-IDOC-05: Search by from unit', async ({ page }) => {
    const searchInput = page.getByPlaceholder('请输入单位')
    await expect(searchInput).toBeVisible()
    await searchInput.fill('纪委')
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForTimeout(500)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-IDOC-06: Filter by status', async ({ page }) => {
    const statusSelect = page.locator('.el-select').filter({ hasText: '状态' })
    await expect(statusSelect).toBeVisible()
    await statusSelect.click()
    await page.getByRole('option', { name: '待处理' }).click()
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForTimeout(500)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-IDOC-07: Search reset', async ({ page }) => {
    // Fill search
    await page.getByPlaceholder('请输入标题').fill('测试')
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForTimeout(500)

    // Reset
    await page.getByRole('button', { name: '重置' }).click()
    await page.waitForTimeout(500)
    // Search field should be cleared
    const titleValue = await page.getByPlaceholder('请输入标题').inputValue()
    expect(titleValue).toBe('')
  })

  test('TC-IDOC-08: Detail view opens', async ({ page }) => {
    // Click first detail button in table
    const detailBtn = page.locator('.el-table .el-button').filter({ hasText: '详情' }).first()
    await expect(detailBtn).toBeVisible()
    await detailBtn.click()
    await page.waitForTimeout(500)
    // Dialog should appear
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('来文详情')
  })

  test('TC-IDOC-09: Edit dialog opens', async ({ page }) => {
    const editBtn = page.locator('.el-table .el-button').filter({ hasText: '编辑' }).first()
    await expect(editBtn).toBeVisible()
    await editBtn.click()
    await page.waitForTimeout(500)
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('编辑来文')
  })

  test('TC-IDOC-10: Status change - process pending doc', async ({ page }) => {
    // Find "开始处理" button for a pending document
    const processBtn = page.locator('.el-table .el-button').filter({ hasText: '开始处理' }).first()
    const hasProcessBtn = await processBtn.isVisible().catch(() => false)

    if (hasProcessBtn) {
      await processBtn.click()
      await page.waitForTimeout(1000)
      // Should show success message
      await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    } else {
      test.skip(true, 'No pending documents found to process')
    }
  })

  test('TC-IDOC-11: Status change - complete processing doc', async ({ page }) => {
    // Find "办结" button for a processing document
    const completeBtn = page.locator('.el-table .el-button').filter({ hasText: '办结' }).first()
    const hasCompleteBtn = await completeBtn.isVisible().catch(() => false)

    if (hasCompleteBtn) {
      await completeBtn.click()
      await page.waitForTimeout(1000)
      await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    } else {
      test.skip(true, 'No processing documents found to complete')
    }
  })

  test('TC-IDOC-12: Status change - archive document', async ({ page }) => {
    // Find "归档" button
    const archiveBtn = page.locator('.el-table .el-button').filter({ hasText: '归档' }).first()
    const hasArchiveBtn = await archiveBtn.isVisible().catch(() => false)

    if (hasArchiveBtn) {
      await archiveBtn.click()
      await page.waitForTimeout(1000)
      await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    } else {
      test.skip(true, 'No archiveable documents found')
    }
  })

})
