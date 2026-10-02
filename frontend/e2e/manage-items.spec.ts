import { expect, type Page, type Route, test } from '@playwright/test';

/** Unsigned JWT: the frontend only reads the payload, the backend is mocked here. */
function fakeAdminAccessToken(): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode({
    sub: 'admin@vending.me',
    roles: ['ROLE_ADMIN'],
    exp: 4102444800,
    token_type: 'access',
  })}.signature`;
}

const itemId = '22222222-2222-2222-2222-222222222222';
const itemsPath = '/api/v1/items';
const itemPath = `${itemsPath}/${itemId}`;
const imagePath = `${itemPath}/image`;

function itemResource(price = 1.5) {
  return {
    id: itemId,
    name: 'Cola',
    type: 'COLD_BEVERAGE',
    price,
    _links: {
      self: { href: itemPath },
      'item:image': { href: imagePath },
    },
  };
}

function itemsPage(price = 1.5) {
  return {
    _embedded: { elements: [itemResource(price)] },
    _links: { self: { href: itemsPath } },
    _templates: { default: { method: 'post' } },
    page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
  };
}

async function fulfillJson(
  route: Route,
  body: unknown,
  contentType = 'application/hal+json',
  status = 200,
) {
  await route.fulfill({
    status,
    contentType,
    body: JSON.stringify(body),
  });
}

async function signInAsAdmin(page: Page) {
  await page.goto('/login');

  await page.getByLabel('Email').fill('admin@vending.me');
  await page.getByLabel('Password').fill('S3cret!Passw0rd');
  await page.getByRole('button', { name: 'Sign in' }).click();

  await expect(page).toHaveURL(/\/machines$/);
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/v1', async (route) => {
    await fulfillJson(route, {
      _links: {
        vendingMachines: { href: '/api/v1/vending-machines' },
        items: { href: itemsPath },
      },
    });
  });

  await page.route('**/api/v1/authenticate', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ accessToken: fakeAdminAccessToken() }),
    });
  });

  await page.route('**/api/v1/authenticate/refresh', async (route) => {
    await route.fulfill({ status: 401 });
  });

  await page.route('**/api/v1/authenticate/logout', async (route) => {
    await route.fulfill({ status: 204 });
  });

  await page.route('**/api/v1/vending-machines**', async (route) => {
    await fulfillJson(route, {
      page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
    });
  });
});

test('an admin can create, edit image, and delete an item', async ({ page }) => {
  let created = false;
  let price = 1.5;
  let uploadCount = 0;
  let deleteCount = 0;
  const imageRequests: string[] = [];

  await page.route('**/api/v1/items**', async (route) => {
    const request = route.request();
    const pathname = new URL(request.url()).pathname;

    if (request.method() === 'GET' && pathname === itemsPath) {
      await fulfillJson(
        route,
        created
          ? itemsPage(price)
          : {
              _links: { self: { href: itemsPath } },
              _templates: { default: { method: 'post' } },
              page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
            },
      );
      return;
    }

    if (request.method() === 'POST' && pathname === itemsPath) {
      created = true;
      await fulfillJson(route, itemResource(price), 'application/hal+json', 201);
      return;
    }

    if (request.method() === 'GET' && pathname === itemPath) {
      await fulfillJson(route, itemResource(price));
      return;
    }

    if (request.method() === 'PUT' && pathname === itemPath) {
      const body = JSON.parse(request.postData() ?? '{}') as { price: number };
      price = body.price;
      await fulfillJson(route, itemResource(price));
      return;
    }

    if (request.method() === 'POST' && pathname === imagePath) {
      uploadCount += 1;
      await fulfillJson(route, itemResource(price));
      return;
    }

    if (request.method() === 'GET' && pathname === imagePath) {
      imageRequests.push(request.url());
      await route.fulfill({ status: 404 });
      return;
    }

    if (request.method() === 'DELETE' && pathname === itemPath) {
      deleteCount += 1;
      created = false;
      await route.fulfill({ status: 204 });
      return;
    }

    throw new Error(`Unexpected items request: ${request.method()} ${pathname}`);
  });

  await signInAsAdmin(page);
  // In-app navigation, not `page.goto`: a full reload would lose the in-memory token
  // (refresh is mocked as 401) and `adminGuard` would redirect to /machines.
  await page.getByRole('link', { name: 'Items' }).click();
  await expect(page).toHaveURL(/\/items$/);

  await page.getByRole('link', { name: /New item/ }).click();
  await expect(page).toHaveURL(/\/items\/new$/);

  await page.getByLabel('Name').fill('Cola');
  await page.getByLabel('Type').click();
  await page.getByRole('option', { name: 'COLD_BEVERAGE' }).click();
  await page.getByLabel('Price').fill('1.5');
  await page.getByRole('button', { name: 'Create item' }).click();

  await expect(page).toHaveURL(/\/items$/);
  await expect(page.getByText('Cola')).toBeVisible();

  await page.getByRole('link', { name: 'View details' }).click();
  await expect(page).toHaveURL(new RegExp(`/items/${itemId}$`));
  await expect(page.getByRole('heading', { name: 'Item details' })).toBeVisible();

  await page.getByRole('link', { name: 'Edit' }).click();
  await expect(page).toHaveURL(new RegExp(`/items/${itemId}/edit$`));
  await page.getByLabel('Price').fill('2');
  await page.getByTestId('image-upload-input').setInputFiles({
    name: 'cola.png',
    mimeType: 'image/png',
    buffer: Buffer.from('image'),
  });
  await page.getByRole('button', { name: 'Save item' }).click();

  await expect(page).toHaveURL(new RegExp(`/items/${itemId}$`));
  await expect(page.getByRole('definition').filter({ hasText: /^2$/ })).toBeVisible();
  expect(uploadCount).toBe(1);
  // After an upload the detail page must bust the image cache with `?v=`.
  await expect.poll(() => imageRequests.some((url) => /[?&]v=\d+/.test(url))).toBe(true);

  await page.getByRole('button', { name: 'Delete' }).click();
  await expect(page.getByText('Delete Cola?')).toBeVisible();
  await page.getByTestId('item-delete-dialog-confirm').click();

  await expect(page).toHaveURL(/\/items$/);
  await expect(page.getByText('No item found.')).toBeVisible();
  expect(deleteCount).toBe(1);
});
