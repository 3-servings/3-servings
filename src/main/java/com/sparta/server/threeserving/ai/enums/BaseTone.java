package com.sparta.server.threeserving.ai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BaseTone {

    SENSORY_STIMULATION("음식의 맛, 냄새, 식감을 생생하게 묘사하여 읽는 즉시 침샘을 자극하는 먹음직스러운 말투"),

    WITTY_AND_TRENDY("친근하고 텐션이 높으며, 젊은 타겟층에 맞게 가볍고 재치 발랄한 말투"),

    NEUTRAL("화려한 수식어나 과장을 모두 자제하고, 식재료와 메뉴명만 객관적이고 담백하게 나열하는 건조한 말투"),

    EMOTIONAL_STORYTELLING("차분하고 따뜻한 어조로, 음식에 담긴 정성이나 고객의 하루를 위로하는 감성적이고 고급스러운 말투"),

    TRUST_AND_PROFESSIONAL("마치 셰프가 직접 설명하듯, 음식의 우수한 퀄리티와 위생적인 조리 과정을 강조하는 전문적이고 신뢰감 있는 말투"),

    URGENT_PROMOTION("가성비와 푸짐함을 강조하고, 오늘 꼭 먹어야 할 것 같은 느낌을 주어 즉각적인 주문을 유도하는 적극적인 말투"),

    CUSTOM("기존의 말투 규칙을 무시하고, '추가 요청사항(additionalRequest)'에 작성된 지시를 기준으로 삼아 작성하는 맞춤형 말투");

    private final String description;

}
