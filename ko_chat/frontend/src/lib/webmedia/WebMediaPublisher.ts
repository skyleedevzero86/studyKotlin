export function createWebMediaPublisher(apiUrl: string, streamUrl: string) {
  const pc = new RTCPeerConnection()
  pc.onconnectionstatechange = () => {
    console.log('RTCPeerConnection state changed', pc.connectionState)
  }

  const publish = async (stream: MediaStream, appId: string, feedId: string) => {
    const audioTrack = stream.getAudioTracks()[0]
    const videoTrack = stream.getVideoTracks()[0]

    if (audioTrack) {
      pc.addTrack(audioTrack, stream)
    } else {
      pc.addTransceiver('audio', { direction: 'sendonly' })
    }

    if (videoTrack) {
      pc.addTrack(videoTrack, stream)
    } else {
      pc.addTransceiver('video', { direction: 'sendonly' })
    }

    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)

    const fullApiUrl = `${apiUrl}/rtc/v1/publish/`
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
    return session
  }

  const findSender = (kind: 'audio' | 'video') =>
    pc.getSenders().find((sender) => sender.track?.kind === kind)

  const replaceTrack = async (kind: 'audio' | 'video', track: MediaStreamTrack | null) => {
    const sender = findSender(kind)
    if (!sender) {
      if (track) {
        pc.addTrack(track)
      }
      return
    }
    await sender.replaceTrack(track)
  }

  const close = () => {
    pc.close()
  }

  return { publish, replaceTrack, close, pc }
}
