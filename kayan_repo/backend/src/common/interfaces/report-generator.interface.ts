export interface ReportFilter {
  tenantId: string;
  from: Date;
  to: Date;
}

export interface IReportGenerator {
  generateDaily(filter: ReportFilter): Promise<Buffer>;
  generateMonthly(filter: ReportFilter): Promise<Buffer>;
}
