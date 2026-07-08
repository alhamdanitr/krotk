export interface SmsNotificationPayload {
  toPhone: string;
  message: string;
  tenantId: string;
}

export interface INotificationProvider {
  sendSms(payload: SmsNotificationPayload): Promise<void>;
}
