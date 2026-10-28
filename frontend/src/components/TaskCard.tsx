import { assetUrl } from '@/lib/api';
import { STAGE_LABEL, Task } from '@/lib/types';
import StatusBadge from './StatusBadge';

export default function TaskCard({ task, title }: { task: Task; title: string }) {
  return (
    <div className="task-card" data-testid={`task-${task.id}`}>
      <div className="task-header">
        <div>
          <h2 className="task-title">{title}</h2>
          <p className="muted">创建时间：{new Date(task.createdAt).toLocaleString()}</p>
        </div>
        <StatusBadge status={task.status} />
      </div>

      {task.status === 'PROCESSING' && task.currentStage && (
        <p className="stage-line">当前阶段：{STAGE_LABEL[task.currentStage]}</p>
      )}

      {task.status === 'FAILED' && (
        <p className="fail-reason">
          失败原因（{task.failStage ? STAGE_LABEL[task.failStage] : '未知阶段'}）：{task.failReason ?? '未知原因'}
        </p>
      )}

      {task.status === 'SUCCESS' && task.resultUrl && (
        <img className="task-result" src={assetUrl(task.resultUrl) ?? ''} alt="制作结果" />
      )}

      <p className="task-id muted">任务 ID：{task.id}</p>
    </div>
  );
}