let audioBlob = null;
let mediaRecorder = null;
let stream = null;
let recordingTimeout = null;
let retryCount = 0;
const MAX_RETRY_ATTEMPTS = 3;

const MAX_RECORDING_TIME = 5 * 60 * 1000;

function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function getAudioDeviceInfo() {
    try {
        const devices = await navigator.mediaDevices.enumerateDevices();
        const audioInputs = devices.filter(device => device.kind === 'audioinput');
        return audioInputs;
    } catch (error) {
        return [];
    }
}

async function forceCleanupAllStreams() {
    try {
        if (mediaRecorder) {
            if (mediaRecorder.state === 'recording') {
                mediaRecorder.stop();
            }
            mediaRecorder = null;
        }

        if (stream) {
            stream.getTracks().forEach(track => {
                track.stop();
            });
            stream = null;
        }

        if (window.activeStreams && Array.isArray(window.activeStreams)) {
            window.activeStreams.forEach((s, index) => {
                s.getTracks().forEach(track => track.stop());
            });
            window.activeStreams = [];
        }

        if (window.globalStream) {
            window.globalStream.getTracks().forEach(track => track.stop());
            window.globalStream = null;
        }

        if (window.chrome) {
        } else if (window.safari) {
        }

        await delay(1000);

    } catch (error) {
    }
}

async function checkMicrophoneAvailability() {
    try {
        const audioDevices = await getAudioDeviceInfo();
        if (audioDevices.length === 0) {
            throw new Error('No audio input devices found');
        }

        for (let i = 0; i < Math.min(audioDevices.length, 3); i++) {
            const device = audioDevices[i];
            try {
                const testConstraints = {
                    audio: {
                        deviceId: device.deviceId ? { exact: device.deviceId } : undefined,
                        echoCancellation: false,
                        noiseSuppression: false,
                        autoGainControl: false,
                        channelCount: 1
                    }
                };

                const testStream = await navigator.mediaDevices.getUserMedia(testConstraints);

                const tracks = testStream.getAudioTracks();
                if (tracks.length > 0 && tracks[0].readyState === 'live') {
                    testStream.getTracks().forEach(track => track.stop());
                    await delay(300);
                    return true;
                }

                testStream.getTracks().forEach(track => track.stop());
            } catch (deviceError) {
                continue;
            }
        }

        return false;
    } catch (error) {
        return false;
    }
}

async function diagnoseMicrophoneIssue() {
    const diagnosis = {
        permissions: 'unknown',
        devices: [],
        browser: navigator.userAgent,
        protocol: location.protocol,
        recommendations: []
    };

    try {
        if (navigator.permissions) {
            const permission = await navigator.permissions.query({ name: 'microphone' });
            diagnosis.permissions = permission.state;
        }

        diagnosis.devices = await getAudioDeviceInfo();

        if (diagnosis.permissions === 'denied') {
            diagnosis.recommendations.push('브라우저에서 마이크 권한을 허용해주세요.');
        }

        if (diagnosis.devices.length === 0) {
            diagnosis.recommendations.push('마이크 디바이스를 확인하고 시스템에서 인식되는지 확인해주세요.');
        }

        if (location.protocol !== 'https:' && location.hostname !== 'localhost') {
            diagnosis.recommendations.push('HTTPS 연결을 사용해주세요.');
        }

        diagnosis.recommendations.push(
            '다른 애플리케이션(Zoom, Skype, Discord 등)에서 마이크를 사용 중인지 확인해주세요.',
            '브라우저를 완전히 종료한 후 다시 시작해보세요.',
            '시스템을 재부팅해보세요.'
        );

    } catch (error) {
        diagnosis.recommendations.push('시스템 재부팅 후 다시 시도해주세요.');
    }

    return diagnosis;
}

async function startRecording() {
    try {
        await forceCleanupAllStreams();

        const diagnosis = await diagnoseMicrophoneIssue();

        if (diagnosis.permissions === 'denied') {
            throw new Error('마이크 권한이 거부되었습니다. 브라우저 설정에서 권한을 허용해주세요.');
        }

        const isAvailable = await checkMicrophoneAvailability();
        if (!isAvailable) {
            if (retryCount < MAX_RETRY_ATTEMPTS - 1) {
                retryCount++;
                await delay(2000);
                return startRecording();
            }

            const errorMsg = [
                '마이크에 접근할 수 없습니다.',
                '',
                '가능한 해결 방법:',
                ...diagnosis.recommendations
            ].join('\n');
            throw new Error(errorMsg);
        }

        const constraintOptions = [
            {
                audio: {
                    echoCancellation: false,
                    noiseSuppression: false,
                    autoGainControl: false,
                    channelCount: 1,
                    sampleRate: 44100,
                    sampleSize: 16
                }
            },
            {
                audio: {
                    echoCancellation: false,
                    noiseSuppression: false,
                    autoGainControl: false,
                    channelCount: 1
                }
            },
            {
                audio: {
                    channelCount: 1
                }
            },
            {
                audio: true
            }
        ];

        let lastError = null;
        for (let i = 0; i < constraintOptions.length; i++) {
            try {
                stream = await navigator.mediaDevices.getUserMedia(constraintOptions[i]);
                break;
            } catch (error) {
                lastError = error;
                if (error.name === 'NotReadableError') {
                    await delay(1500);
                } else {
                    await delay(500);
                }
            }
        }

        if (!stream) {
            throw lastError || new Error('모든 제약 조건으로 마이크 접근 실패');
        }

        const tracks = stream.getAudioTracks();
        if (tracks.length === 0 || tracks[0].readyState !== 'live') {
            throw new Error('스트림을 획득했지만 트랙이 활성 상태가 아닙니다.');
        }

        let mimeType = '';
        const supportedTypes = [
            'audio/webm;codecs=opus',
            'audio/webm',
            'audio/mp4',
            'audio/ogg;codecs=opus',
            'audio/wav'
        ];

        for (const type of supportedTypes) {
            if (MediaRecorder.isTypeSupported(type)) {
                mimeType = type;
                break;
            }
        }

        const options = {
            audioBitsPerSecond: 128000
        };

        if (mimeType) {
            options.mimeType = mimeType;
        }

        mediaRecorder = new MediaRecorder(stream, options);
        const chunks = [];

        mediaRecorder.ondataavailable = event => {
            if (event.data.size > 0) {
                chunks.push(event.data);
            }
        };

        mediaRecorder.onstop = async () => {
            if (chunks.length === 0) {
                alert('녹음된 데이터가 없습니다. 다시 시도해주세요.');
                updateRecordingUI(false);
                return;
            }

            const recordedMimeType = mediaRecorder.mimeType;
            audioBlob = new Blob(chunks, { type: recordedMimeType });

            let extension = 'wav';
            if (recordedMimeType.includes('webm')) {
                extension = 'webm';
            } else if (recordedMimeType.includes('mp4')) {
                extension = 'm4a';
            } else if (recordedMimeType.includes('ogg')) {
                extension = 'ogg';
            }

            const fileName = `recording_${Date.now()}.${extension}`;
            const file = new File([audioBlob], fileName, { type: audioBlob.type });

            const dataTransfer = new DataTransfer();
            dataTransfer.items.add(file);
            document.getElementById('audioInput').files = dataTransfer.files;

            updateRecordingUI(false);
            showRecordingComplete(file.size);
        };

        mediaRecorder.onerror = event => {
            alert('녹음 중 오류가 발생했습니다: ' + event.error.name + ' - ' + event.error.message);
            updateRecordingUI(false);
        };

        tracks.forEach(track => {
            track.addEventListener('ended', () => {
                if (mediaRecorder && mediaRecorder.state === 'recording') {
                    stopRecording();
                    alert('마이크 연결이 끊어졌습니다. 녹음이 중지되었습니다.');
                }
            });

            track.addEventListener('mute', () => {
            });

            track.addEventListener('unmute', () => {
            });
        });

        mediaRecorder.start(1000);
        retryCount = 0;
        updateRecordingUI(true);

        recordingTimeout = setTimeout(() => {
            if (mediaRecorder && mediaRecorder.state === 'recording') {
                stopRecording();
                alert('최대 녹음 시간(5분)에 도달하여 자동으로 중지되었습니다.');
            }
        }, MAX_RECORDING_TIME);

    } catch (err) {
        let errorMessage = '녹음 시작 실패:\n\n';

        switch (err.name) {
            case 'NotAllowedError':
                errorMessage += '마이크 사용 권한이 거부되었습니다.\n브라우저 설정에서 마이크 권한을 허용해주세요.';
                break;
            case 'NotFoundError':
                errorMessage += '마이크를 찾을 수 없습니다.\n마이크가 연결되어 있는지 확인해주세요.';
                break;
            case 'NotReadableError':
                errorMessage += '마이크에 접근할 수 없습니다.\n\n가능한 해결 방법:\n';
                errorMessage += '• 다른 애플리케이션(Zoom, Skype, Discord 등)을 종료해주세요\n';
                errorMessage += '• 브라우저를 완전히 종료한 후 다시 시작해주세요\n';
                errorMessage += '• 시스템을 재부팅해주세요\n';
                errorMessage += '• 마이크 드라이버를 업데이트해주세요';
                break;
            case 'NotSupportedError':
                errorMessage += '이 브라우저에서는 녹음 기능을 지원하지 않습니다.';
                break;
            case 'SecurityError':
                errorMessage += '보안상의 이유로 마이크 접근이 거부되었습니다.\nHTTPS 연결을 사용하는지 확인해주세요.';
                break;
            case 'AbortError':
                errorMessage += '기기 문제로 마이크 접근이 중단되었습니다.';
                break;
            case 'OverconstrainedError':
                errorMessage += '요청된 오디오 설정을 지원하지 않습니다.';
                break;
            default:
                errorMessage += err.message || '알 수 없는 오류가 발생했습니다.';
        }

        alert(errorMessage);
        updateRecordingUI(false);

        if (stream) {
            stream.getTracks().forEach(track => track.stop());
            stream = null;
        }
    }
}

async function stopRecording() {
    try {
        if (recordingTimeout) {
            clearTimeout(recordingTimeout);
            recordingTimeout = null;
        }

        if (mediaRecorder && mediaRecorder.state !== 'inactive') {
            mediaRecorder.stop();
        }

        if (stream) {
            await delay(100);
            stream.getTracks().forEach(track => {
                track.stop();
            });
            stream = null;
        }

    } catch (err) {
        updateRecordingUI(false);
    }
}

function updateRecordingUI(isRecording) {
    const startButton = document.getElementById('startRecord');
    const stopButton = document.getElementById('stopRecord');
    const indicator = document.getElementById('recordingIndicator');

    if (isRecording) {
        startButton.disabled = true;
        startButton.textContent = '🎙️ 녹음 중...';
        startButton.classList.add('recording');

        stopButton.disabled = false;
        stopButton.textContent = '⏹️ 녹음 중지';

        indicator.classList.add('active');
    } else {
        startButton.disabled = false;
        startButton.textContent = '🔴 녹음 시작';
        startButton.classList.remove('recording');

        stopButton.disabled = true;
        stopButton.textContent = '⏹️ 녹음 중지';

        indicator.classList.remove('active');
    }
}

function showRecordingComplete(fileSize) {
    const fileSizeMB = (fileSize / (1024 * 1024)).toFixed(2);
    const message = `녹음이 완료되었습니다!\n파일 크기: ${fileSizeMB}MB\n\n'텍스트로 변환' 버튼을 클릭하여 음성을 텍스트로 변환하세요.`;

    if (confirm(message + '\n\n지금 바로 변환하시겠습니까?')) {
        const form = document.getElementById('transcribeForm');
        if (validateTranscribeForm()) {
            form.submit();
        }
    }
}

document.addEventListener('DOMContentLoaded', async function() {
    const isMediaDevicesSupported = !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia);
    const isMediaRecorderSupported = !!window.MediaRecorder;

    if (!isMediaDevicesSupported || !isMediaRecorderSupported) {
        alert('이 브라우저는 음성 녹음 기능을 지원하지 않습니다. 최신 브라우저를 사용해주세요.');
        document.getElementById('startRecord').disabled = true;
        return;
    }

    if (location.protocol !== 'https:' && location.hostname !== 'localhost' && location.hostname !== '127.0.0.1') {
        alert('마이크 접근은 보안 연결(HTTPS)이 필요합니다.');
        document.getElementById('startRecord').disabled = true;
        return;
    }

    await forceCleanupAllStreams();

    if (!window.activeStreams) {
        window.activeStreams = [];
    }

    const diagnosis = await diagnoseMicrophoneIssue();

    try {
        const permission = await navigator.permissions.query({ name: 'microphone' });
        permission.addEventListener('change', () => {
            if (permission.state === 'denied') {
                alert('마이크 권한이 거부되었습니다. 브라우저 설정에서 권한을 허용해주세요.');
            }
        });
    } catch (error) {
    }
});

window.addEventListener('beforeunload', function() {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
        stopRecording();
    }
});

document.addEventListener('visibilitychange', function() {
    if (document.hidden && mediaRecorder && mediaRecorder.state === 'recording') {
    }
});

async function checkMicrophonePermission() {
    try {
        const permission = await navigator.permissions.query({ name: 'microphone' });
        permission.addEventListener('change', () => {
            if (permission.state === 'denied') {
                alert('마이크 권한이 거부되었습니다. 브라우저 설정에서 권한을 허용해주세요.');
            }
        });
        return permission.state;
    } catch (error) {
        return 'unknown';
    }
}

window.runMicrophoneDiagnosis = async function() {
    const diagnosis = await diagnoseMicrophoneIssue();
    return diagnosis;
};