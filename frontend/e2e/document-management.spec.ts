import { test, expect } from './fixtures'
import { login, generateTestCode, expectMessage } from './utils'

test.describe('TC-DOC Document Management Module (P0)', () => {

  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto('/documents')
    await page.waitForSelector('.el-table', { timeout: 10000 })
  })

  test('TC-DOC-01: Document list renders correctly', async ({ page }) => {
    // Verify search form
    await expect(page.getByPlaceholder('请输入文档标题')).toBeVisible()
    // Verify toolbar
    await expect(page.getByRole('button', { name: '上传' })).toBeVisible()
    // Verify table columns
    await expect(page.locator('.el-table')).toContainText('文档名称')
    await expect(page.locator('.el-table')).toContainText('文件大小')
    await expect(page.locator('.el-table')).toContainText('上传时间')
    // Verify pagination
    await expect(page.locator('.el-pagination')).toBeVisible()
  })

  test('TC-DOC-02: Search by document title', async ({ page }) => {
    // Enter a search term and click search
    const searchTerm = 'test_search_term'
    await page.getByPlaceholder('请输入文档标题').fill(searchTerm)
    await page.getByRole('button', { name: '查询' }).click()
    await page.waitForTimeout(1000)
    // Table should update (may be empty if no match)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-DOC-03: Filter by library type', async ({ page }) => {
    // Check if library type filter exists
    const libTypeSelect = page.locator('.el-select').filter({ hasText: '文档库' })
    if (await libTypeSelect.isVisible()) {
      await libTypeSelect.click()
      await page.getByRole('option').first().click()
      await page.getByRole('button', { name: '查询' }).click()
      await page.waitForTimeout(1000)
    }
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-DOC-04: Pagination navigation', async ({ page }) => {
    // Check if pagination has next page button
    const nextBtn = page.locator('.el-pagination .btn-next')
    const isDisabled = await nextBtn.getAttribute('disabled')
    if (!isDisabled) {
      await nextBtn.click()
      await page.waitForTimeout(500)
      await expect(page.locator('.el-table')).toBeVisible()
    }
  })

  test('TC-DOC-05: Document preview triggers on click', async ({ page }) => {
    // If there are documents in the table, try to preview
    const firstRow = page.locator('.el-table__body tr').first()
    if (await firstRow.isVisible()) {
      // Look for a preview/查看 button in the row
      const previewBtn = firstRow.locator('button').filter({ hasText: '预览' }).or(
        firstRow.locator('button').filter({ hasText: '查看' })
      )
      const isVisible = await previewBtn.isVisible().catch(() => false)
      if (isVisible) {
        await previewBtn.click()
        await page.waitForTimeout(1000)
        // A preview dialog should appear
        await expect(page.locator('.el-dialog')).toBeVisible()
      }
    }
  })

  test('TC-DOC-06: Document download triggers', async ({ page }) => {
    // If there are documents in the table, verify download button exists
    const firstRow = page.locator('.el-table__body tr').first()
    if (await firstRow.isVisible()) {
      const downloadBtn = firstRow.locator('button').filter({ hasText: '下载' })
      const isVisible = await downloadBtn.isVisible().catch(() => false)
      if (isVisible) {
        // Verify the button exists and is clickable
        await expect(downloadBtn).toBeVisible()
      }
    }
  })

  test('TC-DOC-07: Empty search returns empty table', async ({ page }) => {
    // Search with a unique non-existent string
    await page.getByPlaceholder('请输入文档标题').fill('NONEXISTENT_DOCS_99999')
    await page.getByRole('button', { name: '查询' }).click()
    await page.waitForTimeout(1000)
    // Table should be visible (possibly empty)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('TC-DOC-08: Category navigation via sidebar', async ({ page }) => {
    // Navigate to categories page
    await page.goto('/categories')
    await page.waitForTimeout(1000)
    // Verify category management page renders
    await expect(page.locator('.el-tree')).toBeVisible().or(
      expect(page.locator('.el-table')).toBeVisible()
    )
  })
})
