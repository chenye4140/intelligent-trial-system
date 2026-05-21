import { test, expect } from './fixtures'
import { generateTestCode } from './utils'

test.describe('TC-PUN Punishment Execution Module (P2)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/punishment')
    await page.waitForSelector('.el-table', { timeout: 10000 })
  })

  test('TC-PUN-01: Punishment page renders with stats and search', async ({ page }) => {
    // Verify stat cards
    await expect(page.locator('.stats-row')).toBeVisible()
    await expect(page.locator('.stat-value')).toHaveCount(4)

    // Verify search form
    await expect(page.getByPlaceholder('请输入案件ID')).toBeVisible()
    await expect(page.locator('.el-select').filter({ hasText: '处分类型' })).toBeVisible()
    await expect(page.locator('.el-select').filter({ hasText: '状态' })).toBeVisible()

    // Verify toolbar
    await expect(page.getByRole('button', { name: '新增处分执行' })).toBeVisible()
    await expect(page.getByRole('button', { name: '查看逾期' })).toBeVisible()

    // Verify table
    await expect(page.locator('.el-table')).toBeVisible()
    await expect(page.locator('.el-table')).toContainText('案件ID')
    await expect(page.locator('.el-table')).toContainText('处分类型')
    await expect(page.locator('.el-table')).toContainText('状态')

    // Verify pagination
    await expect(page.locator('.el-pagination')).toBeVisible()
  })

  test('TC-PUN-02: Create punishment execution record', async ({ page }) => {
    await page.getByRole('button', { name: '新增处分执行' }).click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('新增处分执行')

    // Fill form
    const testCode = generateTestCode('PUN')
    await page.getByPlaceholder('请输入案件ID').first().fill(testCode)

    // Select punishment type
    await page.locator('.el-select').filter({ hasText: '处分类型' }).first().click()
    await page.getByRole('option', { name: '警告' }).click()

    // Fill decision content
    await page.getByPlaceholder('请输入处分决定内容').first().fill('E2E自动化测试处分决定')

    // Set decision date
    await page.locator('input[placeholder="请选择决定日期"]').first().click()
    await page.locator('.el-date-picker__header-label').click()
    await page.locator('.el-date-table td').filter({ hasText: '15' }).first().click()

    // Submit
    await page.getByRole('button', { name: '保存' }).click()

    // Verify success
    await expect(page.locator('.el-message--success')).toContainText('新增成功', { timeout: 5000 })
    await expect(page.locator('.el-table')).toContainText(testCode)
  })

  test('TC-PUN-03: Required field validation', async ({ page }) => {
    await page.getByRole('button', { name: '新增处分执行' }).click()
    await expect(page.locator('.el-dialog')).toBeVisible()

    // Try to save without filling required fields
    await page.getByRole('button', { name: '保存' }).click()
    await page.waitForTimeout(500)

    // Verify validation errors
    const errors = page.locator('.el-form-item__error')
    await expect(errors.first()).toBeVisible()
  })

  test('TC-PUN-04: Search by case ID', async ({ page }) => {
    // First create a record to search
    const searchCode = generateTestCode('PUNS')
    await page.getByRole('button', { name: '新增处分执行' }).click()
    await page.getByPlaceholder('请输入案件ID').first().fill(searchCode)
    await page.locator('.el-select').filter({ hasText: '处分类型' }).first().click()
    await page.getByRole('option', { name: '记过' }).click()
    await page.getByPlaceholder('请输入处分决定内容').first().fill('搜索测试')
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page.locator('.el-message--success')).toContainText('新增成功', { timeout: 5000 })

    // Search by case ID
    await page.getByPlaceholder('请输入案件ID').fill(searchCode)
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForTimeout(1000)

    await expect(page.locator('.el-table')).toContainText(searchCode)
  })

  test('TC-PUN-05: Filter by punishment type', async ({ page }) => {
    await page.locator('.el-select').filter({ hasText: '处分类型' }).click()
    await page.getByRole('option', { name: '开除' }).click()
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForTimeout(1000)

    // Table should still render (possibly empty)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-PUN-06: Filter by status', async ({ page }) => {
    await page.locator('.el-select').filter({ hasText: '状态' }).click()
    await page.getByRole('option', { name: '执行中' }).click()
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForTimeout(1000)

    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-PUN-07: Search reset', async ({ page }) => {
    // Enter search value
    await page.getByPlaceholder('请输入案件ID').fill('some-value')
    await page.getByRole('button', { name: '重置' }).click()
    await page.waitForTimeout(500)

    // Verify input is cleared
    const inputVal = await page.getByPlaceholder('请输入案件ID').inputValue()
    expect(inputVal).toBe('')
  })

  test('TC-PUN-08: Detail view opens', async ({ page }) => {
    // Click the first "详情" button if available, otherwise skip
    const detailBtn = page.locator('.el-table .el-button:has-text("详情")').first()
    const isVisible = await detailBtn.isVisible().catch(() => false)
    if (!isVisible) {
      test.skip()
      return
    }
    await detailBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('处分执行详情')
  })

  test('TC-PUN-09: Status change dialog', async ({ page }) => {
    // Click the first status change button if available
    const statusBtn = page.locator('.el-table .el-button:has-text("状态变更")').first()
    const isVisible = await statusBtn.isVisible().catch(() => false)
    if (!isVisible) {
      test.skip()
      return
    }
    await statusBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()
  })

  test('TC-PUN-10: Overdue records view', async ({ page }) => {
    await page.getByRole('button', { name: '查看逾期' }).click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('逾期记录')
  })
})
