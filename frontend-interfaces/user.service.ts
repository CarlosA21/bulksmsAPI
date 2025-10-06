import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  UserRegistrationRequest,
  UserRegistrationResponse,
  UploadValidationImageResponse,
  PendingUserValidation,
  AccountValidationResponse,
  ApiErrorResponse,
  AuthResponse,
  LoginRequest
} from '../interfaces/user.interfaces';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'https://localhost:8443/api/auth';

  constructor(private http: HttpClient) {}

  /**
   * Register a new user
   */
  register(userData: UserRegistrationRequest): Observable<UserRegistrationResponse> {
    return this.http.post<UserRegistrationResponse>(`${this.apiUrl}/register`, userData)
      .pipe(
        map(response => {
          // Save token to localStorage
          if (response.token) {
            localStorage.setItem('authToken', response.token);
            localStorage.setItem('userId', response.userID);
            localStorage.setItem('userRole', response.role);
          }
          return response;
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Login user
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        map(response => {
          // Save token to localStorage
          if (response.token) {
            localStorage.setItem('authToken', response.token);
            localStorage.setItem('userId', response.userID);
            localStorage.setItem('userRole', response.role);
          }
          return response;
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Upload validation image for a user
   */
  uploadValidationImage(userId: number, imageFile: File): Observable<UploadValidationImageResponse> {
    const formData = new FormData();
    formData.append('file', imageFile);

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.getToken()}`
    });

    return this.http.post<UploadValidationImageResponse>(
      `${this.apiUrl}/upload-validation-image/${userId}`,
      formData,
      { headers }
    ).pipe(catchError(this.handleError));
  }

  /**
   * Get all pending validation users (admin only)
   */
  getPendingValidations(): Observable<PendingUserValidation[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.getToken()}`
    });

    return this.http.get<PendingUserValidation[]>(
      `${this.apiUrl}/pending-validations`,
      { headers }
    ).pipe(catchError(this.handleError));
  }

  /**
   * Validate or reject a user account (admin only)
   */
  validateUserAccount(userId: number, validated: boolean): Observable<AccountValidationResponse> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.getToken()}`
    });

    return this.http.post<AccountValidationResponse>(
      `${this.apiUrl}/validate-account/${userId}?validated=${validated}`,
      {},
      { headers }
    ).pipe(catchError(this.handleError));
  }

  /**
   * Check if user account is validated
   */
  isAccountValidated(userId: number): Observable<{ userId: number; accountValidated: boolean }> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.getToken()}`
    });

    return this.http.get<{ userId: number; accountValidated: boolean }>(
      `${this.apiUrl}/is-validated/${userId}`,
      { headers }
    ).pipe(catchError(this.handleError));
  }

  /**
   * Get validation image for a user
   */
  getValidationImage(userId: number): Observable<Blob> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.getToken()}`
    });

    return this.http.get(
      `${this.apiUrl}/validation-image/${userId}`,
      { headers, responseType: 'blob' }
    ).pipe(catchError(this.handleError));
  }

  /**
   * Logout user
   */
  logout(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
  }

  /**
   * Get stored token
   */
  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  /**
   * Get stored user ID
   */
  getUserId(): string | null {
    return localStorage.getItem('userId');
  }

  /**
   * Get stored user role
   */
  getUserRole(): string | null {
    return localStorage.getItem('userRole');
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Check if user is admin
   */
  isAdmin(): boolean {
    const role = this.getUserRole();
    return role === 'ADMIN' || role === 'ROLE_ADMIN';
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      if (error.error && error.error.error) {
        errorMessage = error.error.error;
      } else if (error.message) {
        errorMessage = error.message;
      } else {
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.statusText}`;
      }
    }

    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}

