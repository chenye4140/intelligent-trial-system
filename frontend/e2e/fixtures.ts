import { test as base, Page } from '@playwright/test'
import { login, clearAuth } from './utils'

/**
 * Extended test fixtures for E2E tests
 */
interface TestFixtures {
  /** Page with authenticated session (already logged in as admin) */
  authPage: Page
}

export const test = base.extend<TestFixtures>({
  authPage: async ({ page }, use) => {
    // Login before each test that uses authPage
    await login(page)
    await use(page)
  },
})

export { expect } from '@playwright/test'

/**
 * Create a test case and return its ID for cleanup
 * Tests should call this to create cases, then clean up in afterEach
 */
export async function createTestViaAPI(page: Page, caseData: {
  caseCode: string
  caseName: string
  caseType: number
  respondentName: string
  filingDate: string
  description?: string
  remark?: string
}): Promise<number | null> {
  const token = await page.evaluate(() => localStorage.getItem('accessToken'))
  if (!token) return null

  const response = await page.evaluate(async ({ data, token }) => {
    const res = await fetch('/api/case', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    })
    const json = await res.json()
    return json
  }, { data: caseData, token })

  // Return the created case ID if response contains it
  if (response && response.code === 200 && response.data) {
    return response.data.id || null
  }
  return null
}

/**
 * Delete a test case by ID via API
 */
export async function deleteTestViaAPI(page: Page, caseId: number): Promise<void> {
  const token = await page.evaluate(() => localStorage.getItem('accessToken'))
  if (!token) return

  await page.evaluate(async ({ id, token }) => {
    await fetch(`/api/case/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
  }, { id: caseId, token })
}
