export interface Counts {
  users: number;
  projects: number;
  submissions: number;
  reviews: number;
}

export interface IssueSummary {
  issueType: string;
  issueLabel: string;
  severity: string;
  count: number;
}

export interface MigrationRun {
  id: number;
  mode: string;
  modeLabel: string;
  status: string;
  statusLabel: string;
  description: string | null;
  createdAt: string;
  startedAt: string | null;
  finishedAt: string | null;
  attemptCount: number;
  errorMessage: string | null;
  errorCount: number;
  infoCount: number;
  inserted: Counts;
  staged: Record<string, number>;
  issueSummary: IssueSummary[];
}

export interface MigrationIssue {
  id: number;
  tableName: string;
  tableLabel: string;
  rowNo: number;
  sourceKey: string | null;
  issueType: string;
  issueLabel: string;
  severity: string;
  message: string;
}

export interface TableVerification {
  tableName: string;
  tableLabel: string;
  staged: number;
  excluded: number;
  expected: number;
  loaded: number;
  missing: number;
}

export interface Finding {
  tableLabel: string;
  sourceKey: string;
  reason: string;
}

export interface VerificationReport {
  tables: TableVerification[];
  missing: Finding[];
  duplicates: Finding[];
  wrongLinks: Finding[];
  passed: boolean;
}

export interface SubmissionSummary {
  subNo: number;
  instCode: string;
  regnoold: string;
  title: string;
  status: string;
  statusLabel: string;
  projectNo: string;
  applicantName: string | null;
  source: string;
  deleted: boolean;
  updatedAt: string | null;
  reviewCount: number;
  attachmentCount: number;
}

export interface SubmissionDetail {
  subNo: number;
  instCode: string;
  regnoold: string;
  title: string;
  statusLabel: string;
  projectNo: string;
  projectTitle: string | null;
  virtualProject: boolean;
  applicantUserId: string;
  applicantName: string | null;
  submittedAt: string | null;
  updatedAt: string | null;
  source: string;
  migrationRunId: number | null;
  deleted: boolean;
  version: number;
}

export interface Review {
  reviewNo: number;
  legacyReviewId: string;
  reviewerName: string | null;
  resultLabel: string;
  opinion: string | null;
  reviewedAt: string | null;
  source: string;
}

export interface Attachment {
  id: number;
  requestId: string;
  originInstCode: string | null;
  originFileId: string | null;
  originSeq: number | null;
  fileName: string;
  contentType: string | null;
  byteSize: number;
  sha256: string;
  bucket: string;
  objectKey: string;
  status: string;
  statusLabel: string;
  lastError: string | null;
  updatedAt: string | null;
}

export interface AttachmentVerification {
  attachmentId: number;
  metadataSize: number;
  databaseSize: number | null;
  databaseSha256: string | null;
  storageSize: number | null;
  storageSha256: string | null;
  matches: boolean;
  message: string;
}

export interface FileTransfer {
  transferId: number;
  source: string;
  instCode: string;
  legacyFileId: string;
  legacyRegno: string | null;
  seq: number | null;
  fileName: string | null;
  sourceBucket: string;
  sourceObjectKey: string;
  status: string;
  statusLabel: string;
  attachmentId: number | null;
  attempts: number;
  lastError: string | null;
}

export interface TransferBatch {
  batchId: number;
  total: number;
  done: number;
  unmapped: number;
  failed: number;
  items: FileTransfer[];
}

export interface SnapshotTable {
  tableName: string;
  label: string;
  expectedRows: number | null;
  scanCompleted: boolean;
  stagedRows: number;
}

export interface CdcRun {
  id: number;
  migrationRunId: number;
  migrationStatus: string | null;
  topicPrefix: string;
  connectorName: string;
  connectorState: string | null;
  status: string;
  statusLabel: string;
  snapshotCompleted: boolean;
  snapshotCompletedAt: string | null;
  initialLoadAt: string | null;
  lastError: string | null;
  createdAt: string;
  snapshotTables: SnapshotTable[];
  initialLoadBlockers: string[];
  eventCounts: Record<string, number>;
  duplicateEvents: number;
}

export interface CdcEvent {
  eventId: number;
  tableName: string;
  op: string;
  sourceKey: string | null;
  status: string;
  attempts: number;
  duplicateCount: number;
  message: string | null;
  kafkaPartition: number;
  kafkaOffset: number;
  sourceLsn: number | null;
  receivedAt: string | null;
  appliedAt: string | null;
}

export interface CdcEventPage {
  items: CdcEvent[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface DemoFaults {
  failNextMigration: boolean;
  failNextContentSave: boolean;
}
