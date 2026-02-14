export function statusLabel(s: string): string {
  if (s === "PENDING") return "승인대기"
  if (s === "ACTIVE") return "활성"
  if (s === "SUSPENDED") return "정지"
  if (s === "WITHDRAWN") return "탈퇴"
  return s
}
