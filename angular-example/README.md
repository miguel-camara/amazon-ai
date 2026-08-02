# Angular Client Example (Product multipart upload)

Minimal example showing how to consume the product **create** and **update** endpoints with `multipart/form-data` using `HttpClient`.

## Files

- `src/app/services/product.service.ts` — `ProductService` with `createProduct()` and `updateProduct()`
- `src/app/components/product-form.component.ts` — component that wires the form to the service
- `src/app/components/product-form.component.html` — form template

## Setup

1. `app.module.ts` must import `HttpClientModule` and `FormsModule`:

```typescript
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ProductFormComponent } from './components/product-form.component';

@NgModule({
  imports: [BrowserModule, HttpClientModule, FormsModule],
  declarations: [ProductFormComponent],
  bootstrap: [ProductFormComponent]
})
export class AppModule {}
```

2. Store the JWT in `localStorage` under the key `token` after login
   (used by `ProductService.authOptions()`).

3. Optionally configure a proxy to avoid CORS issues in dev:

`proxy.conf.json`:
```json
{
  "/api": { "target": "http://localhost:8080", "changeOrigin": true }
}
```
Run with `ng serve --proxy-config proxy.conf.json` and change `apiUrl` to `/api/products`.

## Key points

- Do **not** set `Content-Type` manually — Angular adds the `multipart/form-data` boundary automatically.
- The `product` part must be a `Blob` with `application/json` type so Spring's `@RequestPart` deserializes it.
- Each file is appended under the part name `images`, matching the API's `@RequestPart("images")`.
- Send the token in the `Authorization` header (required, since these endpoints are ADMIN-only).
