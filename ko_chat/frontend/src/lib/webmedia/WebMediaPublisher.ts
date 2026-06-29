export function createWebMediaPublisher(apiUrl: string, streamUrl: string) {
  const pc = new RTCPeerConnection()
  pc.onconnectionstatechange = () => {
    console.log('RTCPeerConnection state changed', pc.connectionState)
  }

  const publish = async (stream: MediaStream, appId: string, feedId: string) => {
    pc.addTransceiver('audio', { direction: 'sendonly' })
    pc.addTransceiver('video', { direction: 'sendonly' })

    const audioTrack = stream.getAudioTracks()[0]
    if (audioTrack) {
      pc.addTrack(audioTrack)
    }
    const videoTrack = stream.getVideoTracks()[0]
    if (videoTrack) {
      pc.addTrack(videoTrack)
    }

    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)

    const fullApiUrl = `${apiUrl}/rtc/v1/publish/`
    const fullStreamUrl = `${streamUrl}/${appId}/${feedId}`
    const transactionId = Number.parseInt(String(Date.now() * Math.random() * 100), 10)
      .toString(16)
      .slice(0, 7)

    const response = await fetch(fullApiUrl, {
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

    if (!response.ok) {
      throw new Error('SRS publish 요청 실패')
    }

    const session = await response.json()
    if (session.code) {
      throw new Error('SRS publish 응답 오류')
    }

    await pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp: session.sdp }))
    return session
  }

  const findSender = (kind: 'audio' | 'video') => {
    const byTrack = pc.getSenders().find((sender) => sender.track?.kind === kind)
    if (byTrack) {
      return byTrack
    }
    const transceivers = pc.getTransceivers()
    const index = kind === 'audio' ? 0 : 1
    return transceivers[index]?.sender
  }

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
