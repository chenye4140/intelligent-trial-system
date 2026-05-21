import { test, expect } from './fixtures'
import { generateTestCode } from './utils'

test.describe('TC-RN Reading Note Module (P2)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/readingnote')
    await page.waitForSelector('.el-table', { timeout: 10000 })
  })

  test('TC-RN-01: Reading note page renders with search and table', async ({ page }) => {
    // Verify search form
    await expect(page.getByPlaceholder('请输入案件ID')).toBeVisible()
    await expect(page.locator('.el-select').filter({ hasText: '笔记类型' })).toBeVisible()

    // Verify toolbar
    await expect(page.getByRole('button', { name: '新增笔记' })).toBeVisible()
    await expect(page.getByRole('button', { name: '查看共享笔记' })).toBeVisible()

    // Verify table
    await expect(page.locator('.el-table')).toBeVisible()
    await expect(page.locator('.el-table')).toContainText('案件ID')
    await expect(page.locator('.el-table')).toContainText('标题')
    await expect(page.locator('.el-table')).toContainText('类型')
    await expect(page.locator('.el-table')).toContainText('共享')

    // Verify pagination
    await expect(page.locator('.el-pagination')).toBeVisible()
  })

  test('TC-RN-02: Create new reading note', async ({ page }) => {
    await page.getByRole('button', { name: '新增笔记' }).click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('新增笔记')

    const testTitle = `E2E笔记 ${Date.now()}`

    // Fill form
    await page.getByPlaceholder('请输入案件ID').first().fill(generateTestCode('RN'))
    await page.getByPlaceholder('请输入标题').first().fill(testTitle)

    // Select note type
    await page.locator('.el-select').filter({ hasText: '笔记类型' }).first().click()
    await page.getByRole('option', { name: '阅卷摘要' }).click()

    // Fill content
    await page.getByPlaceholder('请输入笔记内容').first().fill('E2E自动化测试笔记内容')
    await page.getByPlaceholder('请输入标签，用逗号分隔').first().fill('测试,自动化')

    // Submit
    await page.getByRole('button', { name: '保存' }).click()

    // Verify success
    await expect(page.locator('.el-message--success')).toContainText('新增成功', { timeout: 5000 })
  })

  test('TC-RN-03: Required field validation', async ({ page }) => {
    await page.getByRole('button', { name: '新增笔记' }).click()
    await expect(page.locator('.el-dialog')).toBeVisible()

    await page.getByRole('button', { name: '保存' }).click()
    await page.waitForTimeout(500)

    const errors = page.locator('.el-form-item__error')
    await expect(errors.first()).toBeVisible()
  })

  test('TC-RN-04: Search by case ID', async ({ page }) => {
    // Create a note first
    const searchCode = generateTestCode('RNS')
    await page.getByRole('button', { name: '新增笔记' }).click()
    await page.getByPlaceholder('请输入案件ID').first().fill(searchCode)
    await page.getByPlaceholder('请输入标题').first().fill(`搜索笔记 ${Date.now()}`)
    await page.locator('.el-select').filter({ hasText: '笔记类型' }).first().click()
    await page.getByRole('option', { name: '证据分析' }).click()
    await page.getByPlaceholder('请输入笔记内容').first().fill('搜索测试')
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page.locator('.el-message--success')).toContainText('新增成功', { timeout: 5000 })

    // Search
    await page.getByPlaceholder('请输入案件ID').fill(searchCode)
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForTimeout(1000)

    await expect(page.locator('.el-table')).toContainText(searchCode)
  })

  test('TC-RN-05: Filter by note type', async ({ page }) => {
    await page.locator('.el-select').filter({ hasText: '笔记类型' }).click()
    await page.getByRole('option', { name: '审理意见' }).click()
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForTimeout(1000)

    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-RN-06: Search reset', async ({ page }) => {
    await page.getByPlaceholder('请输入案件ID').fill('some-value')
    await page.getByRole('button', { name: '重置' }).click()
    await page.waitForTimeout(500)

    const inputVal = await page.getByPlaceholder('请输入案件ID').inputValue()
    expect(inputVal).toBe('')
  })

  test('TC-RN-07: View note detail', async ({ page }) => {
    const detailBtn = page.locator('.el-table .el-button:has-text("详情")').first()
    const isVisible = await detailBtn.isVisible().catch(() => false)
    if (!isVisible) {
      test.skip()
      return
    }
    await detailBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()
  })

  test('TC-RN-08: Edit note dialog opens', async ({ page }) => {
    const editBtn = page.locator('.el-table .el-button:has-text("编辑")').first()
    const isVisible = await editBtn.isVisible().catch(() => false)
    if (!isVisible) {
      test.skip()
      return
    }
    await editBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('编辑笔记')
  })

  test('TC-RN-09: Toggle share', async ({ page }) => {
    const shareBtn = page.locator('.el-table .el-button:has-text("共享"), .el-table .el-button:has-text("取消共享")').first()
    const isVisible = await shareBtn.isVisible().catch(() => false)
    if (!isVisible) {
      test.skip()
      return
    }
    await shareBtn.click()
    await page.waitForTimeout(1000)

    // Verify success message
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
  })

  test('TC-RN-10: Shared notes dialog', async ({ page }) => {
    await page.getByRole('button', { name: '查看共享笔记' }).click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog__title')).toContainText('共享笔记')
  })

  test('TC-RN-11: Delete note confirmation', async ({ page }) => {
    // Create a note to delete
    const deleteCode = generateTestCode('RNDEL')
    await page.getByRole('button', { name: '新增笔记' }).click()
    await page.getByPlaceholder('请输入案件ID').first().fill(deleteCode)
    await page.getByPlaceholder('请输入标题').first().fill(`删除测试 ${Date.now()}`)
    await page.locator('.el-select').filter({ hasText: '笔记类型' }).first().click()
    await page.getByRole('option', { name: '其他' }).click()
    await page.getByPlaceholder('请输入笔记内容').first().fill('待删除')
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page.locator('.el-message--success')).toContainText('新增成功', { timeout: 5000 })

    // Find and click delete
    const deleteBtn = page.locator(`tr:has-text("${deleteCode}") .el-button--danger`).first()
    await deleteBtn.click()

    // Confirm deletion
    await page.getByRole('button', { name: '确认' }).click()
    await page.waitForTimeout(1000)

    // Verify success
    await expect(page.locator('.el-message--success')).toContainText('删除成功', { timeout: 5000 })
  })

  test('TC-RN-12: Pagination changes', async ({ page }) => {
    // Verify pagination is visible
    await expect(page.locator('.el-pagination')).toBeVisible()

    // Try changing page size
    const pageSizeSelect = page.locator('.el-pagination .el-select')
    const isVisible = await pageSizeSelect.isVisible().catch(() => false)
    if (!isVisible) {
      test.skip()
      return
    }
    await pageSizeSelect.click()
    await page.waitForTimeout(500)
    await page.getByRole('option', { name: '20' }).click()
    await page.waitForTimeout(1000)

    // Table should still render
    await expect(page.locator('.el-table')).toBeVisible()
  })
})
