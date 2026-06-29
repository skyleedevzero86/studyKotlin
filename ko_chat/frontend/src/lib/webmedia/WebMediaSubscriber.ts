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
      throw new Error('SRS play 요청 실패')
    }

    const session = await response.json()
    if (session.code) {
      throw new Error('SRS play 응답 오류')
    }

    await pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp: session.sdp }))
    return session
  }

  const close = () => {
    pc.close()
  }

  return { subscribe, close, stream, pc }
}
