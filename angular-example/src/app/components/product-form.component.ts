import { Component } from '@angular/core';
import { Product, ProductService } from './product.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html'
})
export class ProductFormComponent {
  product: Product = { name: '', description: '', price: 0, quantity: 0 };
  images: File[] = [];
  // Set this when editing an existing product (e.g. from a product list).
  productId?: number;

  constructor(private productService: ProductService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.images = Array.from(input.files);
    }
  }

  save(): void {
    if (this.productId) {
      this.productService.updateProduct(this.productId, this.product, this.images).subscribe({
        next: (res) => console.log('Updated:', res.data),
        error: (err) => console.error('Update failed:', err)
      });
    } else {
      this.productService.createProduct(this.product, this.images).subscribe({
        next: (res) => {
          console.log('Created:', res.data);
          this.productId = res.data.id;
        },
        error: (err) => console.error('Create failed:', err)
      });
    }
  }
}
