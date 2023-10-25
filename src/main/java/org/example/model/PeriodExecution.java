package org.example.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PeriodExecution {
    DEFAULT(0),
    TWO(2),
    SIX(6),
    TWELVE(12);
    private final int time;
}
