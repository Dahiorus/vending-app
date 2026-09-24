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

const machineId = '11111111-1111-1111-1111-111111111111';
const itemId = '22222222-2222-2222-2222-222222222222';
const machinePath = `/api/v1/vending-machines/${machineId}`;
const stockPath = `${machinePath}/stock`;
const orderPath = `${machinePath}/items/${itemId}/order`;
const itemName = 'Sparkling Water';

const machinesPage = {
  _embedded: {
    elements: [
      {
        id: machineId,
        serialNumber: 'SN-0001',
        address: { city: 'Lyon', streetName: 'Rue de la Paix', postalCode: '69001' },
        itemType: 'SNACK',
        powerStatus: 'POWER_ON',
        workingStatus: 'WORKING',
        _links: { self: { href: machinePath } },
      },
    ],
  },
  page: { size: 10, totalElements: 1, totalPages: 1, number: 0 },
};

const machineDetail = {
  id: machineId,
  serialNumber: 'SN-0001',
  address: { streetNumber: 1, streetName: 'Rue de la Paix', postalCode: '69001', city: 'Lyon' },
  itemType: 'SNACK',
  powerStatus: 'POWER_ON',
  workingStatus: 'WORKING',
  temperature: 4,
  lastIntervention: null,
  rfidStatus: 'WORKING',
  smartCardStatus: 'WORKING',
  changeMoneyStatus: 'WORKING',
  _links: { stock: { href: stockPath } },
};

function stockResponse(quantity: number) {
  return {
    itemQuantities: [{ itemId, itemName, quantity }],
    _links: {
      item: [{ href: `/api/v1/items/${itemId}` }],
      order: quantity > 0 ? [{ href: orderPath }] : [],
    },
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

async function signIn(page: Page) {
  await page.goto('/login');

  await page.getByLabel('Email').fill('user@vending.me');
  await page.getByLabel('Password').fill('S3cret!Passw0rd');
  await page.getByRole('button', { name: 'Sign in' }).click();

  await expect(page).toHaveURL(/\/machines$/);
  await expect(page.getByText('user@vending.me')).toBeVisible();
  await expect(page.getByText('SN-0001')).toBeVisible();
}

async function goToMachineDetail(page: Page) {
  await page.getByRole('link', { name: 'View details' }).click();
  await expect(page).toHaveURL(new RegExp(`/machines/${machineId}$`));
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/v1', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/hal+json',
      body: JSON.stringify({
        _links: {
          vendingMachines: { href: '/api/v1/vending-machines' },
        },
      }),
    });
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

  await page.route('**/api/v1/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/hal+json',
      body: JSON.stringify({
        id: 'u-1',
        email: 'user@vending.me',
        firstname: 'Ada',
        lastname: 'Lovelace',
      }),
    });
  });

  await page.route('**/api/v1/authenticate/logout', async (route) => {
    await route.fulfill({ status: 204 });
  });
});

test('a signed-in user can confirm an order and sees the stock reload with the updated quantity', async ({
  page,
}) => {
  let stockRequestCount = 0;
  let orderPostCount = 0;

  await page.route('**/api/v1/vending-machines**', async (route) => {
    const request = route.request();
    const pathname = new URL(request.url()).pathname;

    if (request.method() === 'GET' && pathname === '/api/v1/vending-machines') {
      await fulfillJson(route, machinesPage);
      return;
    }

    if (request.method() === 'GET' && pathname === machinePath) {
      await fulfillJson(route, machineDetail);
      return;
    }

    if (request.method() === 'GET' && pathname === stockPath) {
      stockRequestCount += 1;
      await fulfillJson(route, stockResponse(stockRequestCount === 1 ? 3 : 2));
      return;
    }

    if (request.method() === 'POST' && pathname === orderPath) {
      orderPostCount += 1;
      await fulfillJson(
        route,
        { amount: 1.5, createdAt: '2024-01-01T10:00:00' },
        'application/json',
      );
      return;
    }

    throw new Error(`Unexpected vending machines request: ${request.method()} ${pathname}`);
  });

  await signIn(page);
  await goToMachineDetail(page);

  await expect(page.getByRole('heading', { name: 'Vending machine details' })).toBeVisible();

  const itemRow = page.locator('tbody tr').filter({ hasText: itemName });
  await expect(itemRow).toContainText('3');

  await itemRow.getByRole('button', { name: 'Order' }).click();

  await expect(page.getByText(`Order 1x ${itemName}? Remaining stock: 3.`)).toBeVisible();
  await page.getByTestId('order-confirm-dialog-confirm').click();

  await expect(page.getByText(/Ordered Sparkling Water for 1\.5 €/)).toBeVisible();
  await expect(itemRow).toContainText('2');
  expect(orderPostCount).toBe(1);
  expect(stockRequestCount).toBe(2);
});

test('a signed-in user can cancel an order without sending the POST request', async ({ page }) => {
  let orderPostCount = 0;

  await page.route('**/api/v1/vending-machines**', async (route) => {
    const request = route.request();
    const pathname = new URL(request.url()).pathname;

    if (request.method() === 'GET' && pathname === '/api/v1/vending-machines') {
      await fulfillJson(route, machinesPage);
      return;
    }

    if (request.method() === 'GET' && pathname === machinePath) {
      await fulfillJson(route, machineDetail);
      return;
    }

    if (request.method() === 'GET' && pathname === stockPath) {
      await fulfillJson(route, stockResponse(3));
      return;
    }

    if (request.method() === 'POST' && pathname === orderPath) {
      orderPostCount += 1;
      await fulfillJson(
        route,
        { amount: 1.5, createdAt: '2024-01-01T10:00:00' },
        'application/json',
      );
      return;
    }

    throw new Error(`Unexpected vending machines request: ${request.method()} ${pathname}`);
  });

  await signIn(page);
  await goToMachineDetail(page);

  const itemRow = page.locator('tbody tr').filter({ hasText: itemName });
  await expect(itemRow).toContainText('3');

  await itemRow.getByRole('button', { name: 'Order' }).click();

  const dialogText = page.getByText(`Order 1x ${itemName}? Remaining stock: 3.`);
  await expect(dialogText).toBeVisible();

  await page.getByTestId('order-confirm-dialog-cancel').click();

  await expect(dialogText).toBeHidden();
  await page.waitForLoadState('networkidle');

  expect(orderPostCount).toBe(0);
  await expect(itemRow).toContainText('3');
});
