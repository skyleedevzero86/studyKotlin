export interface HealthPort {
  check(): Promise<HealthResult>
}

export interface HealthResult {
  status: string
  service: string
  timestamp: string
}

export async function checkHealth(port: HealthPort): Promise<HealthResult> {
  return port.check()
}
