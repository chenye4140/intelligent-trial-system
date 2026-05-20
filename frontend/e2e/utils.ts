import { Page, expect } from '@playwright/test'

const DEFAULT_USER = 'admin'
const DEFAULT_PASSWORD = 'admin123'

/**
 * Perform login on the login page
 */
export async function login(page: Page, username = DEFAULT_USER, password = DEFAULT_PASSWORD) {
  await page.goto('/login')
  await page.getByPlaceholder('请输入用户名').fill(username)
  await page.getByPlaceholder('请输入密码').fill(password)
  await page.getByRole('button', { name: '登 录' }).click()
  // Wait for successful navigation to dashboard
  await expect(page).toHaveURL(/\/(dashboard)?$/, { timeout: 10000 })
}

/**
 * Wait for an API response and return the response body
 */
export async function waitForApiResponse(
  page: Page,
  urlPattern: string | RegExp,
  action: () => Promise<void>
) {
  const [response] = await Promise.all([
    page.waitForResponse((res) => {
      const url = res.url()
      if (typeof urlPattern === 'string') return url.includes(urlPattern)
      return urlPattern.test(url)
    }),
    action(),
  ])
  return response
}

/**
 * Generate a unique test case identifier to avoid conflicts
 */
export function generateTestCode(prefix = 'E2E'): string {
  return `${prefix}-${Date.now()}`
}

/**
 * Clear localStorage to simulate unauthenticated state
 */
export async function clearAuth(page: Page) {
  await page.evaluate(() => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  })
}

/**
 * Set an expired token in localStorage to test expiry handling
 */
export async function setExpiredToken(page: Page) {
  await page.evaluate(() => {
    localStorage.setItem('accessToken', 'eyJhbGciOiJIUzI1NiJ9.expired.token')
    localStorage.setItem('refreshToken', 'expired-refresh-token')
  })
}

/**
 * Wait for ElMessage to appear and verify its content
 */
export async function expectMessage(page: Page, text: string, type: 'success' | 'warning' | 'error' = 'error') {
  await expect(page.locator(`.el-message--${type} .el-message__content`)).toContainText(text, { timeout: 5000 })
}
