let audioBlob = null;
let mediaRecorder = null;
let stream = null;
let recordingTimeout = null;

// 최대 녹음 시간 (5분)
const MAX_RECORDING_TIME = 5 * 60 * 1000;

async function startRecording() {
    try {
        // 이전 스트림이 있으면 정리
        if (stream) {
            stream.getTracks().forEach(track => track.stop());
        }

        stream = await navigator.mediaDevices.getUserMedia({
            audio: {
                echoCancellation: true, // 에코 제거
                noiseSuppression: true, // 노이즈 억제
                autoGainControl: true,  // 자동 게인 조절
                sampleRate: 44100       // 샘플 레이트 (일반적으로 호환성 좋음)
            }
        });

        let mimeType = 'audio/webm;codecs=opus';
        if (!MediaRecorder.isTypeSupported(mimeType)) {
            mimeType = 'audio/webm';
            if (!MediaRecorder.isTypeSupported(mimeType)) {

                console.warn('Opus 및 WebM 코덱이 지원되지 않습니다. 다른 기본값으로 시도합니다.');

                mimeType = undefined; // MediaRecorder가 자체적으로 지원하는 가장 좋은 타입을 선택
            }
        }

        const options = mimeType ? { mimeType: mimeType, audioBitsPerSecond: 128000 } : { audioBitsPerSecond: 128000 };
        mediaRecorder = new MediaRecorder(stream, options);
        const chunks = [];

        // 이벤트 핸들러 설정
        mediaRecorder.ondataavailable = event => {
            if (event.data.size > 0) {
                chunks.push(event.data);
                console.log('Data chunk received:', event.data.size, 'bytes');
            }
        };

        mediaRecorder.onstop = async () => {
            console.log('Recording stopped, processing chunks...');

            if (chunks.length === 0) {
                console.error('No audio data recorded');
                alert('녹음된 데이터가 없습니다. 다시 시도해주세요.');
                updateRecordingUI(false); // UI 상태 복원
                return;
            }

            const recordedMimeType = mediaRecorder.mimeType;
            audioBlob = new Blob(chunks, { type: recordedMimeType });

            console.log('Audio blob created:', {
                size: audioBlob.size,
                type: recordedMimeType
            });

            const fileName = `recording_${Date.now()}.wav`; // 확장자만 WAV로 설정
            const file = new File([audioBlob], fileName, { type: audioBlob.type }); // Blob의 실제 타입을 File에 전달

            const dataTransfer = new DataTransfer();
            dataTransfer.items.add(file);
            document.getElementById('audioInput').files = dataTransfer.files;

            console.log('Audio file ready:', {
                name: fileName,
                size: file.size,
                type: file.type // 실제 Blob 타입이 여기에 들어감
            });

            // UI 업데이트
            updateRecordingUI(false);

            // 녹음 완료 알림
            showRecordingComplete(file.size);
        };

        mediaRecorder.onerror = event => {
            console.error('MediaRecorder error:', event.error);
            alert('녹음 중 오류가 발생했습니다: ' + event.error.name + ' - ' + event.error.message);
            updateRecordingUI(false);
        };

        // 녹음 시작
        mediaRecorder.start(1000); // 1초마다 데이터 수집
        console.log('Recording started with MIME type:', mediaRecorder.mimeType);

        // UI 업데이트
        updateRecordingUI(true);

        // 최대 녹음 시간 타이머 설정
        recordingTimeout = setTimeout(() => {
            if (mediaRecorder && mediaRecorder.state === 'recording') {
                console.log('Maximum recording time reached, stopping...');
                stopRecording();
                alert('최대 녹음 시간(5분)에 도달하여 자동으로 중지되었습니다.');
            }
        }, MAX_RECORDING_TIME);

    } catch (err) {
        console.error('Recording start error:', err);

        let errorMessage = '녹음 시작 실패: ';
        if (err.name === 'NotAllowedError') {
            errorMessage += '마이크 사용 권한이 거부되었습니다. 브라우저 설정에서 마이크 권한을 허용해주세요.';
        } else if (err.name === 'NotFoundError') {
            errorMessage += '마이크를 찾을 수 없습니다. 마이크가 연결되어 있는지 확인해주세요.';
        } else if (err.name === 'NotSupportedError') {
            errorMessage += '이 브라우저에서는 녹음 기능을 지원하지 않습니다.';
        } else if (err.name === 'SecurityError') {
            errorMessage += '보안상의 이유로 마이크 접근이 거부되었습니다. 웹사이트가 HTTPS로 제공되는지 확인해주세요.';
        } else if (err.name === 'AbortError') {
            errorMessage += '기기 문제로 마이크 접근이 중단되었습니다.';
        } else {
            errorMessage += err.message;
        }

        alert(errorMessage);
        updateRecordingUI(false);
    }
}

function stopRecording() {
    try {
        // 타이머 정리
        if (recordingTimeout) {
            clearTimeout(recordingTimeout);
            recordingTimeout = null;
        }

        // MediaRecorder 중지
        if (mediaRecorder && mediaRecorder.state !== 'inactive') {
            mediaRecorder.stop();
            console.log('Recording stopped');
        } else {
            console.warn('No active media recorder to stop');
        }

        // 스트림 정리
        if (stream) {
            stream.getTracks().forEach(track => {
                track.stop();
                console.log('Track stopped:', track.kind);
            });
            stream = null;
        }

    } catch (err) {
        console.error('Stop recording error:', err);
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

    // 사용자 친화적인 알림
    if (confirm(message + '\n\n지금 바로 변환하시겠습니까?')) {
        const form = document.getElementById('transcribeForm');
        if (validateTranscribeForm()) {
            form.submit();
        }
    }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // MediaDevices 및 MediaRecorder API 지원 여부 확인
    const isMediaDevicesSupported = !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia);
    const isMediaRecorderSupported = !!window.MediaRecorder;

    if (!isMediaDevicesSupported) {
        alert('이 브라우저는 마이크 접근을 지원하지 않습니다. 최신 브라우저를 사용해주세요 (예: Chrome, Firefox, Safari).');
        document.getElementById('startRecord').disabled = true;
    } else if (!isMediaRecorderSupported) {
        alert('이 브라우저는 음성 녹음 기능을 지원하지 않습니다. 최신 브라우저를 사용해주세요.');
        document.getElementById('startRecord').disabled = true;
    } else {
        console.log('Audio recording initialized successfully. MediaDevices and MediaRecorder are supported.');
        // HTTPS 여부 확인 (개발 환경에서는 localhost가 HTTP여도 허용될 수 있음)
        if (location.protocol !== 'https:' && location.hostname !== 'localhost' && location.hostname !== '127.0.0.1') {
            alert('마이크 접근은 보안 연결(HTTPS)이 필요합니다. 현재 연결은 안전하지 않습니다.');
            document.getElementById('startRecord').disabled = true;
        }
    }
});

// 페이지 언로드 시 정리
window.addEventListener('beforeunload', function() {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
        stopRecording();
    }
});

// 브라우저 탭 변경 시 처리
document.addEventListener('visibilitychange', function() {
    if (document.hidden && mediaRecorder && mediaRecorder.state === 'recording') {
        console.log('Tab hidden, continuing recording...');
    }
});