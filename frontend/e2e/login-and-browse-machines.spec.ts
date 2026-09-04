import { expect, test } from '@playwright/test';

/** Unsigned JWT: the frontend only reads the payload, the backend is mocked here. */
function fakeAccessToken(): string {
  const encode = (value: unknown) =>
    Buffer.from(JSON.stringify(value))
      .toString('base64')
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode({
    sub: 'admin@vending.me',
    roles: ['ROLE_ADMIN'],
    exp: 4102444800,
    token_type: 'access',
  })}.signature`;
}

const machinesPage = {
  _embedded: {
    elements: [
      {
        id: '11111111-1111-1111-1111-111111111111',
        serialNumber: 'SN-0001',
        address: { city: 'Lyon', streetName: 'Rue de la Paix', postalCode: '69001' },
        itemType: 'SNACK',
        powerStatus: 'POWER_ON',
        workingStatus: 'WORKING',
      },
    ],
  },
  page: { size: 10, totalElements: 1, totalPages: 1, number: 0 },
};

test.beforeEach(async ({ page }) => {
  await page.route('**/api/v1/authenticate', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ accessToken: fakeAccessToken(), refreshToken: 'refresh-1' }),
    });
  });

  await page.route('**/api/v1/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/hal+json',
      body: JSON.stringify({
        id: 'u-1',
        email: 'admin@vending.me',
        firstname: 'Ada',
        lastname: 'Lovelace',
      }),
    });
  });

  await page.route('**/api/v1/vending-machines**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/hal+json',
      body: JSON.stringify(machinesPage),
    });
  });
});

test('an anonymous visitor can browse the vending machines', async ({ page }) => {
  await page.goto('/machines');

  await expect(page.getByRole('heading', { name: 'Vending machines' })).toBeVisible();
  await expect(page.getByText('SN-0001')).toBeVisible();
  await expect(page.getByRole('link', { name: 'Sign in' })).toBeVisible();
});

test('a user can sign in and sees their account in the toolbar', async ({ page }) => {
  await page.goto('/login');

  await page.getByLabel('Email').fill('admin@vending.me');
  await page.getByLabel('Password').fill('S3cret!Passw0rd');
  await page.getByRole('button', { name: 'Sign in' }).click();

  await expect(page).toHaveURL(/\/machines$/);
  await expect(page.getByText('admin@vending.me')).toBeVisible();
  await expect(page.getByText('SN-0001')).toBeVisible();

  await page.getByRole('button', { name: 'Sign out' }).click();
  await expect(page).toHaveURL(/\/login$/);
});
