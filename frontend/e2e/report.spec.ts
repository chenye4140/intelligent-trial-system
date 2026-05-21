import { test, expect } from './fixtures'
import { generateTestCode } from './utils'

test.describe('TC-RPT Report Generation Module (P2)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/report')
    await page.waitForSelector('.el-card', { timeout: 10000 })
  })

  test('TC-RPT-01: Report page renders with form and records', async ({ page }) => {
    // Verify generate form
    await expect(page.getByPlaceholder('请输入案件ID').first()).toBeVisible()
    await expect(page.locator('.el-select').filter({ hasText: '文书模板' })).toBeVisible()
    await expect(page.getByRole('button', { name: '生成文书' })).toBeVisible()

    // Verify records section
    await expect(page.getByText('生成记录')).toBeVisible()
    await expect(page.getByRole('button', { name: '刷新' })).toBeVisible()

    // Verify search form in records
    await expect(page.locator('.search-form')).toBeVisible()
    await expect(page.getByPlaceholder('请输入案件ID').nth(1)).toBeVisible()

    // Verify table
    await expect(page.locator('.el-table')).toBeVisible()
    await expect(page.locator('.el-table')).toContainText('记录ID')
    await expect(page.locator('.el-table')).toContainText('案件ID')
    await expect(page.locator('.el-table')).toContainText('模板名称')
    await expect(page.locator('.el-table')).toContainText('状态')
  })

  test('TC-RPT-02: Template dropdown loads', async ({ page }) => {
    await page.locator('.el-select').filter({ hasText: '文书模板' }).click()
    await page.waitForTimeout(500)

    // Verify template options appear
    const options = page.locator('.el-select-dropdown .el-select-dropdown__item')
    const count = await options.count()
    expect(count).toBeGreaterThan(0)
  })

  test('TC-RPT-03: Generate report with valid input', async ({ page }) => {
    // Enter case ID
    await page.getByPlaceholder('请输入案件ID').first().fill('AJ20260501001')

    // Select template
    await page.locator('.el-select').filter({ hasText: '文书模板' }).click()
    await page.waitForTimeout(500)
    await page.locator('.el-select-dropdown .el-select-dropdown__item').first().click()

    // Click generate
    await page.getByRole('button', { name: '生成文书' }).click()

    // Verify loading state or success message
    // The generate may take time (async), so check for either
    await page.waitForTimeout(2000)
    const hasSuccess = await page.locator('.el-message--success').isVisible().catch(() => false)
    const hasLoading = await page.getByRole('button', { name: '生成文书' }).getAttribute('disabled').catch(() => null)
    // At least one of these should indicate the action was triggered
    expect(hasSuccess || hasLoading !== null).toBe(true)
  })

  test('TC-RPT-04: Generate report without case ID shows validation', async ({ page }) => {
    // Select template first (so caseId is the only missing field)
    await page.locator('.el-select').filter({ hasText: '文书模板' }).click()
    await page.waitForTimeout(500)
    await page.locator('.el-select-dropdown .el-select-dropdown__item').first().click()

    // Try to generate without case ID
    await page.getByRole('button', { name: '生成文书' }).click()
    await page.waitForTimeout(1000)

    // Verify error or validation message
    const hasError = await page.locator('.el-message--error, .el-message--warning').isVisible().catch(() => false)
    const hasFormError = await page.locator('.el-form-item__error').isVisible().catch(() => false)
    expect(hasError || hasFormError).toBe(true)
  })

  test('TC-RPT-05: Search records by case ID', async ({ page }) => {
    // Use second case ID input (in the records search section)
    const searchInput = page.locator('.search-form input[placeholder="请输入案件ID"]')
    await searchInput.fill('AJ20260501001')
    await page.getByRole('button', { name: '查询' }).click()
    await page.waitForTimeout(1000)

    // Table should still render
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-RPT-06: Filter records by status', async ({ page }) => {
    await page.locator('.search-form .el-select').filter({ hasText: '状态' }).click()
    await page.waitForTimeout(500)
    await page.getByRole('option', { name: '已完成' }).click()
    await page.getByRole('button', { name: '查询' }).click()
    await page.waitForTimeout(1000)

    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-RPT-07: Search reset', async ({ page }) => {
    const searchInput = page.locator('.search-form input[placeholder="请输入案件ID"]')
    await searchInput.fill('some-value')
    await page.getByRole('button', { name: '重置' }).click()
    await page.waitForTimeout(500)

    const inputVal = await searchInput.inputValue()
    expect(inputVal).toBe('')
  })

  test('TC-RPT-08: View record detail', async ({ page }) => {
    // Click first "详情" button if available
    const detailBtn = page.locator('.el-table .el-button:has-text("详情")').first()
    const isVisible = await detailBtn.isVisible().catch(() => false)
    if (!isVisible) {
      test.skip()
      return
    }
    await detailBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()
  })

  test('TC-RPT-09: Refresh records', async ({ page }) => {
    await page.getByRole('button', { name: '刷新' }).click()
    await page.waitForTimeout(1000)

    // Table should still be visible
    await expect(page.locator('.el-table')).toBeVisible()
  })
})
