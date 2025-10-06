// Legal ID Type Enum
export enum LegalIdType {
  DRIVER_LICENSE = 'DRIVER_LICENSE',
  PASSPORT = 'PASSPORT',
  NATIONAL_ID = 'NATIONAL_ID',
  SSN = 'SSN',
  TAX_ID = 'TAX_ID',
  VOTER_ID = 'VOTER_ID',
  OTHER = 'OTHER'
}

// Legal ID Type Display Names
export const LegalIdTypeLabels: Record<LegalIdType, string> = {
  [LegalIdType.DRIVER_LICENSE]: "Driver's License",
  [LegalIdType.PASSPORT]: 'Passport',
  [LegalIdType.NATIONAL_ID]: 'National ID / DNI',
  [LegalIdType.SSN]: 'Social Security Number',
  [LegalIdType.TAX_ID]: 'Tax ID',
  [LegalIdType.VOTER_ID]: 'Voter ID',
  [LegalIdType.OTHER]: 'Other'
};

// Billing Address Interface
export interface BillingAddress {
  id?: number;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  user?: number;
}

// User Registration Request Interface
export interface UserRegistrationRequest {
  email: string;
  password: string;
  username?: string;
  legalIdType: LegalIdType;
  legalIdNumber: string;
  dob?: Date | string;
  billingAddress: BillingAddress;

  // Deprecated - mantener para compatibilidad
  driverLicense?: string;
}

// User Registration Response Interface
export interface UserRegistrationResponse {
  token: string;
  username: string;
  userID: string;
  role: string;
  secretKey?: string;
  message?: string;
}

// User Profile Interface
export interface User {
  user_id: number;
  username?: string;
  email: string;
  roles: string;
  legalIdType?: LegalIdType;
  legalIdNumber?: string;
  dob?: Date;
  validationImagePath?: string;
  validationImageName?: string;
  accountValidated: boolean;
  billingAddress?: BillingAddress;
  secretKey?: string;
}

// Pending User Validation Interface (for admin)
export interface PendingUserValidation {
  userId: number;
  email: string;
  legalIdType?: LegalIdType;
  legalIdNumber?: string;
  validationImageName?: string;
  validationImageBase64?: string;
  billingAddress?: BillingAddress;
  registrationDate?: string;
}

// Upload Validation Image Request
export interface UploadValidationImageRequest {
  userId: number;
  file: File;
}

// Upload Validation Image Response
export interface UploadValidationImageResponse {
  message: string;
  imageName: string;
  accountValidated: boolean;
}

// Account Validation Request (admin)
export interface AccountValidationRequest {
  userId: number;
  validated: boolean;
}

// Account Validation Response
export interface AccountValidationResponse {
  message: string;
  userId: number;
  accountValidated: boolean;
}

// Error Response Interface
export interface ApiErrorResponse {
  error: string;
  message?: string;
  status?: number;
}

// Auth Response Interface (for login)
export interface AuthResponse {
  token: string;
  username: string;
  userID: string;
  role: string;
  secretKey?: string;
  message?: string;
}

// Login Request Interface
export interface LoginRequest {
  email: string;
  password: string;
  twoFactorCode?: number;
}

