package com.widetns.batch.core;

public interface BatchJob {
    /**
     * 배치 실행 메서드
     * 
     * @return 실행 결과 (성공: true, 실패: false)
     * @throws Exception 실행 중 발생한 예외
     */
    boolean execute() throws Exception;

    /**
     * 배치 ID 반환
     * 
     * @return 배치 고유 ID
     */
    String getBatchId();

    /**
     * 배치명 반환
     * 
     * @return 배치 이름
     */
    String getBatchName();

    /**
     * 배치 설명 반환
     * 
     * @return 배치 설명
     */
    String getBatchDescription();
}
