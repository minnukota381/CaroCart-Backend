# Order Service API

This module exposes endpoints for order management. Key approval endpoints include:

- `PUT /orders/vendor/items/{itemId}/approve` – vendor approves an item.
- `PUT /orders/vendor/items/{itemId}/reject` – vendor rejects an item.
- `PUT /orders/admin/items/{itemId}/approve` – admin approves an item.
- `PUT /orders/admin/items/{itemId}/reject` – admin rejects an item.

These routes return simple confirmation messages.
