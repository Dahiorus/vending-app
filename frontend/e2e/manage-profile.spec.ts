import { expect, type Page, type Route, test } from '@playwright/test';

/** Unsigned JWT: the frontend only reads the payload, the backend is mocked here. */
function fakeAccessToken(): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode({
    sub: 'user@vending.me',
    roles: ['ROLE_USER'],
    exp: 4102444800,
    token_type: 'access',
  })}.signature`;
}

const user = {
  id: 'u-1',
  email: 'user@vending.me',
  firstname: 'Ada',
  lastname: 'Lovelace',
  _links: {
    self: { href: '/api/v1/me' },
    'me:picture': { href: '/api/v1/me/picture' },
    'me:password': { href: '/api/v1/me/password' },
  },
};

async function fulfillJson(route: Route, body: unknown, status = 200) {
  await route.fulfill({
    status,
    contentType: 'application/hal+json',
    body: JSON.stringify(body),
  });
}

async function signIn(page: Page) {
  await page.goto('/login');
  await page.getByLabel('Email').fill('user@vending.me');
  await page.getByLabel('Password').fill('S3cret!Passw0rd');
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page).toHaveURL(/\/machines$/);
  await expect(page.getByText('user@vending.me')).toBeVisible();
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/v1', async (route) => {
    await fulfillJson(route, { _links: { vendingMachines: { href: '/api/v1/vending-machines' } } });
  });

  await page.route('**/api/v1/authenticate', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ accessToken: fakeAccessToken() }),
    });
  });

  await page.route('**/api/v1/authenticate/refresh', async (route) => {
    await route.fulfill({ status: 401 });
  });

  await page.route('**/api/v1/vending-machines**', async (route) => {
    await fulfillJson(route, {
      _embedded: { elements: [] },
      page: { size: 10, totalElements: 0, totalPages: 0, number: 0 },
    });
  });
});

test('a signed-in user can manage profile information, picture, and password', async ({ page }) => {
  let firstname = user.firstname;
  let lastname = user.lastname;
  let updateBody: unknown = null;
  let pictureUploadCount = 0;
  let passwordChanged = false;

  await page.route('**/api/v1/me', async (route) => {
    const request = route.request();

    if (request.method() === 'GET') {
      await fulfillJson(route, { ...user, firstname, lastname });
      return;
    }

    if (request.method() === 'PUT') {
      updateBody = request.postDataJSON();
      const body = updateBody as { firstname: string; lastname: string };
      firstname = body.firstname;
      lastname = body.lastname;
      await fulfillJson(route, { ...user, firstname, lastname });
      return;
    }

    throw new Error(`Unexpected /me request: ${request.method()}`);
  });

  await page.route('**/api/v1/me/picture**', async (route) => {
    const request = route.request();

    if (request.method() === 'GET') {
      await route.fulfill({ status: 404 });
      return;
    }

    if (request.method() === 'POST') {
      pictureUploadCount += 1;
      await fulfillJson(route, { ...user, firstname, lastname });
      return;
    }

    throw new Error(`Unexpected /me/picture request: ${request.method()}`);
  });

  await page.route('**/api/v1/me/password', async (route) => {
    const request = route.request();
    expect(request.method()).toBe('POST');
    expect(request.postDataJSON()).toEqual({
      oldPassword: 'OldPassw0rd!',
      newPassword: 'NewPassw0rd!',
    });
    passwordChanged = true;
    await route.fulfill({ status: 204 });
  });

  await page.route('**/api/v1/authenticate/logout', async (route) => {
    await route.fulfill({ status: 204 });
  });

  await signIn(page);
  await page.getByRole('link', { name: 'My profile' }).click();
  await expect(page).toHaveURL(/\/profile$/);
  await expect(page.getByRole('heading', { name: 'My profile' })).toBeVisible();

  await page.getByLabel('First name').fill('Grace');
  await page.getByLabel('Last name').fill('Hopper');
  await page.getByRole('button', { name: 'Save profile' }).click();
  await expect(page.getByText('Profile updated.')).toBeVisible();
  expect(updateBody).toEqual({ firstname: 'Grace', lastname: 'Hopper' });

  await page.getByTestId('image-upload-input').setInputFiles({
    name: 'avatar.png',
    mimeType: 'image/png',
    buffer: Buffer.from('avatar'),
  });
  await expect(page.getByText('Profile picture updated.')).toBeVisible();
  expect(pictureUploadCount).toBe(1);

  await page.getByLabel('Current password').fill('OldPassw0rd!');
  await page.getByLabel('New password', { exact: true }).fill('NewPassw0rd!');
  await page.getByLabel('Confirm new password').fill('NewPassw0rd!');
  await page.getByRole('button', { name: 'Change password' }).click();
  await expect(page).toHaveURL(/\/login$/);
  expect(passwordChanged).toBe(true);
});
