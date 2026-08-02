import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Product {
  id?: number;
  name: string;
  description?: string;
  price: number;
  quantity: number;
  imageUrls?: string[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class ProductService {
  private apiUrl = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

  // POST /api/products  (multipart/form-data)
  createProduct(product: Product, images: File[]): Observable<ApiResponse<Product>> {
    const formData = this.buildFormData(product, images);
    return this.http.post<ApiResponse<Product>>(this.apiUrl, formData, this.authOptions());
  }

  // PUT /api/products/{id}  (multipart/form-data)
  updateProduct(id: number, product: Product, images: File[]): Observable<ApiResponse<Product>> {
    const formData = this.buildFormData(product, images);
    return this.http.put<ApiResponse<Product>>(`${this.apiUrl}/${id}`, formData, this.authOptions());
  }

  private buildFormData(product: Product, images: File[]): FormData {
    const formData = new FormData();

    // Part "product" must be JSON; wrap it in a Blob so the boundary part is typed application/json.
    formData.append(
      'product',
      new Blob([JSON.stringify(product)], { type: 'application/json' })
    );

    // Part "images" = one part per file, each named "images".
    for (const image of images) {
      formData.append('images', image, image.name);
    }

    return formData;
  }

  private authOptions() {
    const token = localStorage.getItem('token') || '';
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
        // IMPORTANT: do NOT set Content-Type here. Angular sets it automatically,
        // including the required multipart boundary.
      })
    };
  }
}
