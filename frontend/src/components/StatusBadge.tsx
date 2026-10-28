import { STATUS_LABEL, TaskStatus } from '@/lib/types';

const CLASS_BY_STATUS: Record<TaskStatus, string> = {
  WAITING: 'status-waiting',
  PROCESSING: 'status-processing',
  SUCCESS: 'status-success',
  FAILED: 'status-failed',
};

export default function StatusBadge({ status }: { status: TaskStatus }) {
  return <span className={`status-badge ${CLASS_BY_STATUS[status]}`}>{STATUS_LABEL[status]}</span>;
}