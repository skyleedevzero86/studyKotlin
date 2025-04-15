CREATE TABLE `tb_user_question`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '기본 키',
    `user_id`       bigint      NOT NULL COMMENT '사용자 ID',
    `user_question` text        NOT NULL COMMENT '사용자가 입력한 질문',
    `question_time` datetime    NOT NULL COMMENT '질문한 시간',
    `status`        tinyint     NOT NULL COMMENT '상태: 0 삭제됨, 1 정상, 2 비활성화',
    `create_date`    datetime    NOT NULL COMMENT '생성 시각',
    `modify_date`  datetime    NOT NULL COMMENT '최근 수정 시각',
    PRIMARY KEY (`id`)
) COMMENT = 'AI 사용자의 질문 기록 테이블';
