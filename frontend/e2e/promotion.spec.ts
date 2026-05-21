import { test, expect } from './fixtures'
import { generateTestCode } from './utils'

test.describe('TC-PROM Case Reform Analysis Module (P2)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/promotion')
    await page.waitForSelector('.el-card', { timeout: 10000 })
  })

  test('TC-PROM-01: Promotion page renders with form and history', async ({ page }) => {
    // Verify generate form
    await expect(page.getByPlaceholder('请输入案件ID').first()).toBeVisible()
    await expect(page.locator('.el-select').filter({ hasText: '分析类型' })).toBeVisible()
    await expect(page.getByRole('button', { name: '生成分析' })).toBeVisible()

    // Verify history section
    await expect(page.getByText('历史分析记录')).toBeVisible()

    // Verify search form
    await expect(page.locator('.search-form')).toBeVisible()

    // Verify table
    await expect(page.locator('.el-table')).toBeVisible()
    await expect(page.locator('.el-table')).toContainText('案件ID')
    await expect(page.locator('.el-table')).toContainText('分析类型')
    await expect(page.locator('.el-table')).toContainText('状态')
  })

  test('TC-PROM-02: Analysis type dropdown has all options', async ({ page }) => {
    await page.locator('.el-select').filter({ hasText: '分析类型' }).click()
    await page.waitForTimeout(500)

    // Verify all 5 analysis type options
    await expect(page.getByRole('option', { name: '制度漏洞分析' })).toBeVisible()
    await expect(page.getByRole('option', { name: '廉政风险分析' })).toBeVisible()
    await expect(page.getByRole('option', { name: '警示教育方案' })).toBeVisible()
    await expect(page.getByRole('option', { name: '整改建议报告' })).toBeVisible()
    await expect(page.getByRole('option', { name: '综合分析报告' })).toBeVisible()
  })

  test('TC-PROM-03: Generate analysis with valid input', async ({ page }) => {
    await page.getByPlaceholder('请输入案件ID').first().fill('AJ20260501001')

    await page.locator('.el-select').filter({ hasText: '分析类型' }).click()
    await page.waitForTimeout(500)
    await page.getByRole('option', { name: '综合分析报告' }).click()

    await page.getByRole('button', { name: '生成分析' }).click()

    // Verify action triggered (loading or success)
    await page.waitForTimeout(2000)
    const hasMessage = await page.locator('.el-message').first().isVisible().catch(() => false)
    expect(hasMessage).toBe(true)
  })

  test('TC-PROM-04: Generate without case ID shows validation', async ({ page }) => {
    await page.locator('.el-select').filter({ hasText: '分析类型' }).click()
    await page.waitForTimeout(500)
    await page.getByRole('option', { name: '廉政风险分析' }).click()

    await page.getByRole('button', { name: '生成分析' }).click()
    await page.waitForTimeout(1000)

    const hasError = await page.locator('.el-message--error, .el-message--warning').isVisible().catch(() => false)
    const hasFormError = await page.locator('.el-form-item__error').isVisible().catch(() => false)
    expect(hasError || hasFormError).toBe(true)
  })

  test('TC-PROM-05: Search history by case ID', async ({ page }) => {
    const searchInput = page.locator('.search-form input[placeholder="请输入案件ID"]')
    await searchInput.fill('AJ20260501001')
    await page.getByRole('button', { name: '查询' }).click()
    await page.waitForTimeout(1000)

    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-PROM-06: Filter by analysis type', async ({ page }) => {
    await page.locator('.search-form .el-select').filter({ hasText: '分析类型' }).click()
    await page.waitForTimeout(500)
    await page.getByRole('option', { name: '制度漏洞分析' }).click()
    await page.getByRole('button', { name: '查询' }).click()
    await page.waitForTimeout(1000)

    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-PROM-07: Filter by status', async ({ page }) => {
    await page.locator('.search-form .el-select').filter({ hasText: '状态' }).click()
    await page.waitForTimeout(500)
    await page.getByRole('option', { name: '已完成' }).click()
    await page.getByRole('button', { name: '查询' }).click()
    await page.waitForTimeout(1000)

    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-PROM-08: Search reset', async ({ page }) => {
    const searchInput = page.locator('.search-form input[placeholder="请输入案件ID"]')
    await searchInput.fill('some-value')
    await page.getByRole('button', { name: '重置' }).click()
    await page.waitForTimeout(500)

    const inputVal = await searchInput.inputValue()
    expect(inputVal).toBe('')
  })

  test('TC-PROM-09: View analysis detail', async ({ page }) => {
    const detailBtn = page.locator('.el-table .el-button:has-text("详情")').first()
    const isVisible = await detailBtn.isVisible().catch(() => false)
    if (!isVisible) {
      test.skip()
      return
    }
    await detailBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()
  })

  test('TC-PROM-10: Refresh history', async ({ page }) => {
    await page.getByRole('button', { name: '刷新' }).click()
    await page.waitForTimeout(1000)
    await expect(page.locator('.el-table')).toBeVisible()
  })
})
