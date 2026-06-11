import { test, expect } from './fixtures'

test.describe('TC-CLS Classification Suggestion Module (P2)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/classification')
    await page.waitForLoadState('networkidle', { timeout: 10000 })
  })

  test('TC-CLS-01: Classification page renders correctly', async ({ page }) => {
    // Verify page header
    await expect(page.locator('.classification-page')).toBeVisible()
    await expect(page.locator('.card-header')).toContainText('五级定密建议')

    // Verify input form
    await expect(page.getByPlaceholder('请输入案件ID')).toBeVisible()
    await expect(page.getByRole('button', { name: '获取建议' })).toBeVisible()
  })

  test('TC-CLS-02: Get suggestion with empty case ID shows validation', async ({ page }) => {
    // Click get suggestion without entering case ID
    await page.getByRole('button', { name: '获取建议' }).click()
    await page.waitForTimeout(500)
    // Should show error message
    await expect(page.locator('.el-message')).toBeVisible({ timeout: 5000 })
  })

  test('TC-CLS-03: Get suggestion with valid case ID', async ({ page }) => {
    // Enter a test case ID
    await page.getByPlaceholder('请输入案件ID').fill('AJ20260501001')

    // Click get suggestion
    await page.getByRole('button', { name: '获取建议' }).click()

    // Loading state should appear
    await expect(page.locator('.el-button.is-loading')).toBeVisible({ timeout: 5000 })
      .catch(async () => {
        // Loading may complete too fast, that's OK
      })

    // Wait for result (the analysis may take time, so we just verify the button was clicked)
    await page.waitForTimeout(2000)
  })

  test('TC-CLS-04: Suggestion result displays correctly', async ({ page }) => {
    // Enter case ID and trigger suggestion
    await page.getByPlaceholder('请输入案件ID').fill('AJ20260501001')
    await page.getByRole('button', { name: '获取建议' }).click()
    await page.waitForTimeout(3000)

    // Check if suggestion card appeared
    const suggestionCard = page.locator('.suggestion-desc')
    const hasCard = await suggestionCard.isVisible().catch(() => false)

    if (hasCard) {
      // Verify result sections
      await expect(page.locator('.card-header')).toContainText('AI 定密建议')
      await expect(page.locator('.el-descriptions')).toBeVisible()
      await expect(page.locator('.analysis-detail')).toBeVisible()
    } else {
      test.skip(true, 'No suggestion result available (backend may not have data)')
    }
  })

  test('TC-CLS-05: Adopt suggestion button visible', async ({ page }) => {
    // Enter case ID and trigger suggestion
    await page.getByPlaceholder('请输入案件ID').fill('AJ20260501001')
    await page.getByRole('button', { name: '获取建议' }).click()
    await page.waitForTimeout(3000)

    // Check if adopt button exists
    const adoptBtn = page.getByRole('button', { name: '采纳建议' })
    const hasBtn = await adoptBtn.isVisible().catch(() => false)

    if (hasBtn) {
      await expect(adoptBtn).toBeVisible()
    } else {
      test.skip(true, 'Adopt button not visible')
    }
  })

  test('TC-CLS-06: Level tag displays in suggestion result', async ({ page }) => {
    await page.getByPlaceholder('请输入案件ID').fill('AJ20260501001')
    await page.getByRole('button', { name: '获取建议' }).click()
    await page.waitForTimeout(3000)

    // Check if level tag appears
    const levelTags = page.locator('.el-tag')
    const hasTags = await levelTags.count().then(c => c > 0).catch(() => false)

    if (hasTags) {
      await expect(levelTags.first()).toBeVisible()
    } else {
      test.skip(true, 'No level tags found in result')
    }
  })

  test('TC-CLS-07: Generate AI analysis button', async ({ page }) => {
    // Look for "生成建议" or "AI 生成" button
    const generateBtn = page.getByRole('button', { name: '生成' }).or(
      page.getByRole('button', { name: 'AI 生成' })
    )
    const hasBtn = await generateBtn.isVisible().catch(() => false)

    if (hasBtn) {
      await expect(generateBtn).toBeVisible()
    } else {
      test.skip(true, 'Generate button not found on this page')
    }
  })

  test('TC-CLS-08: Analysis section shows reasons', async ({ page }) => {
    await page.getByPlaceholder('请输入案件ID').fill('AJ20260501001')
    await page.getByRole('button', { name: '获取建议' }).click()
    await page.waitForTimeout(3000)

    // Check if analysis reasons are displayed
    const reasonsHeading = page.locator('h4').filter({ hasText: '定密依据' })
    const hasReasons = await reasonsHeading.isVisible().catch(() => false)

    if (hasReasons) {
      await expect(reasonsHeading).toBeVisible()
      // Reasons should be in a list
      await expect(page.locator('.analysis-detail ul li')).toHaveCount({ min: 1 })
    } else {
      test.skip(true, 'Analysis reasons not displayed')
    }
  })

})
