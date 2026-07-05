export function createWebMediaSubscriber(apiUrl: string, streamUrl: string) {
  const stream = new MediaStream()
  const pc = new RTCPeerConnection()

  pc.onconnectionstatechange = () => {
    console.log('RTCPeerConnection state changed', pc.connectionState)
  }

  pc.ontrack = (event) => {
    if (event.track) {
      stream.addTrack(event.track)
    }
  }

  const waitForRemoteTrack = (timeoutMs = 8000) =>
    new Promise<void>((resolve, reject) => {
      if (stream.getVideoTracks().length > 0 || stream.getAudioTracks().length > 0) {
        resolve()
        return
      }

      const timer = window.setTimeout(() => {
        pc.removeEventListener('track', onTrack)
        reject(new Error('원격 미디어 트랙 수신 시간 초과'))
      }, timeoutMs)

      const onTrack = () => {
        if (stream.getVideoTracks().length > 0 || stream.getAudioTracks().length > 0) {
          window.clearTimeout(timer)
          pc.removeEventListener('track', onTrack)
          resolve()
        }
      }

      pc.addEventListener('track', onTrack)
    })

  const subscribe = async (appId: string, feedId: string) => {
    pc.addTransceiver('audio', { direction: 'recvonly' })
    pc.addTransceiver('video', { direction: 'recvonly' })

    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)

    const fullApiUrl = `${apiUrl}/rtc/v1/play/`
    const fullStreamUrl = `${streamUrl}/${appId}/${feedId}`
    const transactionId = Number.parseInt(String(Date.now() * Math.random() * 100), 10)
      .toString(16)
      .slice(0, 7)

    let response: Response
    try {
      response = await fetch(fullApiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          api: fullApiUrl,
          streamurl: fullStreamUrl,
          tid: transactionId,
          clientip: null,
          sdp: offer.sdp,
        }),
      })
    } catch {
      throw new Error('미디어 서버에 연결할 수 없습니다. SRS 서버가 실행 중인지 확인하세요.')
    }

    if (!response.ok) {
      throw new Error('미디어 서버 연결 실패 (SRS 서버가 실행 중인지 확인하세요)')
    }

    let session: { code?: number; sdp?: string }
    try {
      session = await response.json()
    } catch {
      throw new Error('미디어 서버 응답을 처리할 수 없습니다. SRS 서버가 실행 중인지 확인하세요.')
    }

    if (session.code) {
      throw new Error('미디어 서버 응답 오류')
    }

    await pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp: session.sdp }))
    await waitForRemoteTrack()
    return session
  }

  const close = () => {
    pc.close()
  }

  return { subscribe, close, stream, pc }
}
