import type { WebMediaObjectMessageContainer } from '../../types/webmedia'

const TRANSACTION_TIMEOUT = 30 * 1000

type Transaction = {
  resolve: (value?: WebMediaObjectMessageContainer) => void
  reject: (error: Error) => void
}

export class WebMediaClient {
  private nextMessageId = 0
  private transactionMap: Record<string, Transaction> = {}
  private messageCallback: (container: WebMediaObjectMessageContainer) => void

  roomId: string | null = null
  connected = false
  private client: WebSocket | null = null

  constructor(messageCallback: (container: WebMediaObjectMessageContainer) => void) {
    this.messageCallback = messageCallback
  }

  connect(websocketUrl: string, roomId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      this.addTransaction(
        'connect',
        () => resolve(),
        reject,
      )

      this.roomId = roomId
      this.client = new WebSocket(websocketUrl)
      this.client.onopen = this.onOpen
      this.client.onclose = this.onClose
      this.client.onerror = this.onError
      this.client.onmessage = this.onMessage
    })
  }

  sendMessage(
    message: Record<string, unknown>,
    type: string,
    isTransaction: boolean,
  ): Promise<WebMediaObjectMessageContainer | void> {
    return new Promise((resolve, reject) => {
      if (!this.connected || !this.client || !this.roomId) {
        reject(new Error('접속 중이 아닙니다'))
        return
      }

      const messageId = String(this.nextMessageId++)
      const request = {
        roomId: this.roomId,
        from: 'client',
        to: 'webmedia-ws',
        type,
        messageId,
        message: JSON.stringify(message),
      }

      if (isTransaction) {
        this.addTransaction(messageId, (value) => {
          resolve(value as WebMediaObjectMessageContainer)
        }, reject)
      }

      this.client.send(JSON.stringify(request))
      if (!isTransaction) {
        resolve(undefined)
      }
    })
  }

  close(): void {
    this.connected = false
    if (this.client) {
      this.client.close()
      this.client = null
    }
  }

  private onOpen = (): void => {
    const transaction = this.getTransaction('connect')
    if (transaction) {
      this.connected = true
      transaction.resolve()
    }
  }

  private onClose = (): void => {
    this.connected = false
  }

  private onError = (): void => {
    this.connected = false
  }

  private onMessage = (event: MessageEvent<string>): void => {
    if (!event.data) {
      return
    }

    const container = JSON.parse(event.data) as WebMediaObjectMessageContainer
    const transaction = this.getTransaction(container.messageId)
    if (transaction) {
      transaction.resolve(container)
    } else {
      this.messageCallback(container)
    }
  }

  private addTransaction(
    key: string,
    resolve: (value?: WebMediaObjectMessageContainer) => void,
    reject: (error: Error) => void,
  ): void {
    this.transactionMap[key] = { resolve, reject }
    setTimeout(() => {
      const transaction = this.transactionMap[key]
      if (transaction) {
        delete this.transactionMap[key]
        transaction.reject(new Error('Transaction 시간 초과'))
      }
    }, TRANSACTION_TIMEOUT)
  }

  private getTransaction(key: string): Transaction | undefined {
    const transaction = this.transactionMap[key]
    if (transaction) {
      delete this.transactionMap[key]
    }
    return transaction
  }
}
