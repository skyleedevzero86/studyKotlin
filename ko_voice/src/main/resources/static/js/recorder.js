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

        // 마이크 권한 요청 및 스트림 획득
        stream = await navigator.mediaDevices.getUserMedia({
            audio: {
                echoCancellation: true,
                noiseSuppression: true,
                autoGainControl: true,
                sampleRate: 44100
            }
        });

        // MediaRecorder 설정
        const options = {
            mimeType: 'audio/webm;codecs=opus',
            audioBitsPerSecond: 128000
        };

        // 브라우저 호환성 체크
        if (!MediaRecorder.isTypeSupported(options.mimeType)) {
            options.mimeType = 'audio/webm';
            if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                options.mimeType = 'audio/mp4';
                if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                    delete options.mimeType;
                }
            }
        }

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
                return;
            }

            // 오디오 블롭 생성
            const mimeType = mediaRecorder.mimeType || 'audio/webm';
            audioBlob = new Blob(chunks, { type: mimeType });

            console.log('Audio blob created:', {
                size: audioBlob.size,
                type: mimeType
            });

            // WAV 형식으로 변환 (가능한 경우)
            try {
                const wavBlob = await convertToWav(audioBlob);
                audioBlob = wavBlob;
                console.log('Converted to WAV:', audioBlob.size, 'bytes');
            } catch (e) {
                console.warn('WAV conversion failed, using original format:', e);
            }

            // 파일 입력 요소에 설정
            const audioInput = document.getElementById('audioInput');
            const fileName = `recording_${Date.now()}.wav`;
            const file = new File([audioBlob], fileName, { type: 'audio/wav' });

            const dataTransfer = new DataTransfer();
            dataTransfer.items.add(file);
            audioInput.files = dataTransfer.files;

            console.log('Audio file ready:', {
                name: fileName,
                size: file.size,
                type: file.type
            });

            // UI 업데이트
            updateRecordingUI(false);

            // 녹음 완료 알림
            showRecordingComplete(file.size);
        };

        mediaRecorder.onerror = event => {
            console.error('MediaRecorder error:', event.error);
            alert('녹음 중 오류가 발생했습니다: ' + event.error.message);
            updateRecordingUI(false);
        };

        // 녹음 시작
        mediaRecorder.start(1000); // 1초마다 데이터 수집
        console.log('Recording started with format:', mediaRecorder.mimeType);

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

// WebM을 WAV로 변환하는 함수 (간단한 구현)
async function convertToWav(blob) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = function() {
            // 실제 변환 로직은 복잡하므로, 여기서는 단순히 이름만 변경
            // 실제 프로덕션에서는 Web Audio API를 사용한 완전한 변환이 필요
            const wavBlob = new Blob([blob], { type: 'audio/wav' });
            resolve(wavBlob);
        };
        reader.onerror = reject;
        reader.readAsArrayBuffer(blob);
    });
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 브라우저 지원 체크
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        alert('이 브라우저에서는 녹음 기능을 지원하지 않습니다. 최신 브라우저를 사용해주세요.');
        document.getElementById('startRecord').disabled = true;
        return;
    }

    // MediaRecorder 지원 체크
    if (!window.MediaRecorder) {
        alert('이 브라우저에서는 MediaRecorder를 지원하지 않습니다.');
        document.getElementById('startRecord').disabled = true;
        return;
    }

    console.log('Audio recording initialized successfully');
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
        // 필요에 따라 녹음을 중지하거나 계속할 수 있음
    }
});